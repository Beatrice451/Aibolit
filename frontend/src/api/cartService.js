import axiosInstance from './axiosInstance';

const cartApi = {
  getCart: async () => {
    const response = await axiosInstance.get('/api/cart');
    return response.data;
  },

  addItem: async (productId, quantity = 1) => {
    const response = await axiosInstance.post('/api/cart', { productId, quantity });
    return response.data;
  },

  updateItem: async (productId, quantity) => {
    const response = await axiosInstance.put(`/api/cart/${productId}`, { quantity });
    return response.data;
  },

  removeItem: async (productId) => {
    const response = await axiosInstance.delete(`/api/cart/${productId}`);
    return response.data;
  },

  clearCart: async () => {
    const response = await axiosInstance.delete('/api/cart');
    return response.data;
  }
};

export default cartApi;