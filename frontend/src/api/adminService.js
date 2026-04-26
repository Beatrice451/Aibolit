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
  getOrders: async (page = 0, size = 20, filters = {}) => {
    const params = new URLSearchParams();
    params.append('page', page);
    params.append('size', size);

    if (filters.orderStatus && filters.orderStatus !== '') {
      params.append('orderStatus', filters.orderStatus);
    }
    if (filters.email && filters.email.trim() !== '') {
      params.append('email', filters.email.trim());
    }
    if (filters.phone && filters.phone.trim() !== '') {
      params.append('phone', filters.phone.trim());
    }
    if (filters.excludeCompleted) {
      params.append('excludeCompleted', 'true');
    }
    if (filters.excludeCancelled) {
      params.append('excludeCancelled', 'true');
    }

    console.log('[Admin] Fetching orders with params:', params.toString());
    console.log('[Admin] Token:', localStorage.getItem('accessToken'));
    console.log('[Admin] Auth header:', axiosInstance.defaults.headers.common['Authorization']);
    const response = await axiosInstance.get('/api/admin/order', { params });
    return response.data;
  },

  updateOrderStatus: async (id, status) => {
    const response = await axiosInstance.patch(`/api/admin/order/${id}`, { status });
    return response.data;
  },

  // Users (admin only)
  getUsers: async (page = 0, size = 20, filters = {}) => {
    const params = new URLSearchParams();
    params.append('page', page);
    params.append('size', size);

    if (filters.email) {
      params.append('email', filters.email);
    }
    if (filters.name) {
      params.append('name', filters.name);
    }
    if (filters.isDeleted !== undefined && filters.isDeleted !== '') {
      params.append('isDeleted', filters.isDeleted);
    }
    if (filters.role) {
      params.append('role', filters.role);
    }

    const response = await axiosInstance.get('/api/admin/user', { params });
    return response.data;
  },

  getRoles: async () => {
    const response = await axiosInstance.get('/api/admin/user/roles');
    return response.data;
  },

  assignRole: async (userId, roleId) => {
    const response = await axiosInstance.put(`/api/admin/user/${userId}/roles/${roleId}`);
    return response.data;
  },

  removeRole: async (userId, roleId) => {
    const response = await axiosInstance.delete(`/api/admin/user/${userId}/roles/${roleId}`);
    return response.data;
  },

  restoreUser: async (userId) => {
    const response = await axiosInstance.patch(`/api/admin/user/${userId}/restore`);
    return response.data;
  },

  deleteUser: async (userId) => {
    const response = await axiosInstance.delete(`/api/admin/user/${userId}`);
    return response.data;
  }
};

export default adminApi;