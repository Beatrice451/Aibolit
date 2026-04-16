import axiosInstance, { initAuth } from './axiosInstance';

const authApi = {
  login: async (email, password) => {
    const response = await axiosInstance.post('/api/auth/login', { email, password });
    const { accessToken } = response.data;
    if (accessToken) {
      localStorage.setItem('accessToken', accessToken);
      axiosInstance.defaults.headers.common['Authorization'] = `Bearer ${accessToken}`;
    }
    return response.data;
  },

  register: async (userData) => {
    const response = await axiosInstance.post('/api/auth/register', userData);
    return response.data;
  },

  logout: async () => {
    try {
      await axiosInstance.post('/api/auth/logout', {}, {
        withCredentials: true
      });
    } catch (err) {
      console.log('Logout API error (ignoring):', err.message);
    } finally {
      localStorage.removeItem('accessToken');
      delete axiosInstance.defaults.headers.common['Authorization'];
    }
  },

  getCurrentUser: async () => {
    const response = await axiosInstance.get('/api/users/whoami');
    return response.data;
  },

  setAuthHeader: (token) => {
    if (token) {
      axiosInstance.defaults.headers.common['Authorization'] = `Bearer ${token}`;
    } else {
      delete axiosInstance.defaults.headers.common['Authorization'];
    }
  },

  initAuth: () => {
    initAuth();
  }
};

export default authApi;