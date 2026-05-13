import axiosInstance from './axiosInstance';

const productApi = {
  getProducts: async (filters = {}, pageable = { page: 0, size: 20 }) => {
    const params = new URLSearchParams();

    Object.entries(filters).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        params.append(key, value);
      }
    });

    // 🔧 ИСПРАВЛЕНИЕ: передаём просто 'page' и 'size', без префикса 'pageable.'
    params.append('page', pageable.page?.toString() || '0');
    params.append('size', pageable.size?.toString() || '20');

    // Если нужна сортировка (опционально)
    if (pageable.sort && Array.isArray(pageable.sort)) {
      pageable.sort.forEach((s, index) => {
        params.append(`sort[${index}]`, s);
      });
    }

    const response = await axiosInstance.get('/api/products', { params });
    return response.data;
  },

  getCategoriesTree: async () => {
    const response = await axiosInstance.get('/api/categories'); // твой эндпоинт
    return response.data;
  },

  getProductById: async (id) => {
    const response = await axiosInstance.get(`/api/products/${id}`);
    return response.data;
  },

  getProductReviews: async (productId, pageable = { page: 0, size: 10 }) => {
    const params = new URLSearchParams();
    params.append('page', pageable.page?.toString() || '0');
    params.append('size', pageable.size?.toString() || '10');

    const response = await axiosInstance.get(`/api/products/${productId}/reviews`, { params });
    return response.data;
  },

  addReview: async (productId, comment, rating) => {
    const response = await axiosInstance.post(`/api/products/${productId}/reviews`, {
      comment,
      rating
    });
    return response.data;
  },

  deleteReview: async (reviewId) => {
    const response = await axiosInstance.delete(`/api/reviews/${reviewId}`);
    return response.data;
  }
};

export default productApi;