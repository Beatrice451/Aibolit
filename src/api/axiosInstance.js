import axios from 'axios';

const axiosInstance = axios.create({
  baseURL: 'http://localhost:1488',
});

const initAuth = () => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    axiosInstance.defaults.headers.common['Authorization'] = `Bearer ${token}`;
  }
};

initAuth();

let isRefreshing = false;
let refreshSubscribers = [];

const subscribeTokenRefresh = (callback) => {
  refreshSubscribers.push(callback);
};

const onTokenRefreshed = (newToken) => {
  refreshSubscribers.forEach(callback => callback(newToken));
  refreshSubscribers = [];
};

axiosInstance.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    const status = error.response?.status;
    
    // Handle 401 (unauthorized) or 418 (as some backends use this for invalid tokens)
    if ((status === 401 || status === 418) && !originalRequest._retry) {
      console.log(`[Auth] Caught ${status}, attempting token refresh...`);
      if (isRefreshing) {
        return new Promise(resolve => {
          subscribeTokenRefresh((newToken) => {
            originalRequest.headers['Authorization'] = `Bearer ${newToken}`;
            resolve(axiosInstance(originalRequest));
          });
        });
      }
      
      originalRequest._retry = true;
      isRefreshing = true;
      
      try {
        console.log('[Auth] Token expired, attempting refresh...');
        // Backend expects refresh token in cookies, not in Authorization header
        // Cookie will be sent automatically with withCredentials: true
        const response = await axiosInstance.post('/api/auth/refresh', {}, {
          withCredentials: true
        });
        
        const newToken = response.data.accessToken;
        console.log('[Auth] Refresh successful, new token:', newToken ? 'received' : 'MISSING');
        
        if (newToken) {
          localStorage.setItem('accessToken', newToken);
          axiosInstance.defaults.headers.common['Authorization'] = `Bearer ${newToken}`;
          
          onTokenRefreshed(newToken);
          
          originalRequest.headers['Authorization'] = `Bearer ${newToken}`;
          isRefreshing = false;
          return axiosInstance(originalRequest);
        } else {
          throw new Error('No token in refresh response');
        }
      } catch (refreshError) {
        isRefreshing = false;
        console.error('[Auth] Token refresh failed:', refreshError.message, refreshError.response?.data);
        
        // On any error during refresh, redirect to login
        localStorage.removeItem('accessToken');
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }
    
    console.error('API Error:', error.response?.data || error.message);
    return Promise.reject(error);
  }
);

export default axiosInstance;
export { initAuth };