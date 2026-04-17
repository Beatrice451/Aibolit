import axiosInstance from './axiosInstance';

const flattenCategories = (tree) => {
  const result = [];
  const traverse = (nodes, depth = 0) => {
    nodes.forEach(node => {
      result.push({ id: node.id, name: node.name, depth });
      if (node.children?.length) traverse(node.children, depth + 1);
    });
  };
  traverse(tree);
  return result;
};

const adminApi = {
  // Products - using public endpoint
  getProducts: async (page = 0, size = 50, filters = {}) => {
    const params = new URLSearchParams();
    params.append('page', page);
    params.append('size', size);
    Object.entries(filters).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        params.append(key, value);
      }
    });
    const response = await axiosInstance.get('/api/products', { params });
    return response.data;
  },

  // Add product (admin only)
  addProduct: async (productData) => {
    const response = await axiosInstance.post('/api/admin/product', productData);
    return response.data;
  },

  // Update product (admin only)
  updateProduct: async (id, productData) => {
    const response = await axiosInstance.patch(`/api/admin/product/${id}`, productData);
    return response.data;
  },

  // Delete product (admin only)
  deleteProduct: async (id) => {
    const response = await axiosInstance.delete(`/api/admin/product/${id}`);
    return response.data;
  },

  // Categories - using public endpoint
  getCategories: async () => {
    const response = await axiosInstance.get('/api/categories');
    return response.data;
  },

  getCategoriesFlat: async () => {
    const response = await axiosInstance.get('/api/categories');
    return flattenCategories(response.data);
  },

  // Add category (admin only)
  addCategory: async (categoryData) => {
    const response = await axiosInstance.post('/api/admin/category', categoryData);
    return response.data;
  },

  // Upload file - expects form-data
  uploadFile: async (file) => {
    const token = localStorage.getItem('accessToken');
    const formData = new FormData();
    formData.append('file', file);
    
    const response = await axiosInstance.post('/api/files/upload', formData, {
      headers: { 
        'Content-Type': 'multipart/form-data',
        'Authorization': token ? `Bearer ${token}` : ''
      }
    });
    return response.data;
  },

  // Orders (admin only)
  getOrders: async (page = 0, size = 20) => {
    const params = new URLSearchParams();
    params.append('page', page);
    params.append('size', size);
    const paramsObj = { filter: {}, pageable: { page, size } };
    const response = await axiosInstance.get('/api/admin/order', { params: paramsObj });
    return response.data;
  },

  updateOrderStatus: async (id, status) => {
    const response = await axiosInstance.patch(`/api/admin/order/${id}`, { status });
    return response.data;
  }
};

export default adminApi;