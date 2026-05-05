import axiosInstance, { initAuth } from './axiosInstance';

const authApi = {
  login: async (email, password) => {
    const response = await axiosInstance.post('/api/auth/login', { email, password }, {
      withCredentials: true
    });
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

  isAdmin: async () => {
    try {
      const user = await axiosInstance.get('/api/users/whoami');
      return user.data.roles?.some(role => role.roleName === 'ADMIN');
    } catch {
      return false;
    }
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
  },

  verifyEmail: async (token) => {
    const response = await axiosInstance.get('/api/auth/verify-email', {
      params: { token }
    });
    return response.data;
  },

  resendVerification: async (email) => {
    const response = await axiosInstance.post('/api/auth/resend-verification', { email });
    return response.data;
  },

  updateUser: async (userData) => {
    const response = await axiosInstance.patch('/api/users/me', userData);
    return response.data;
  }
};

export default authApi;