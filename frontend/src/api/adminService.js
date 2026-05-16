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

    const response = await axiosInstance.get('/api/admin/order', { params });
    return response.data;
  },

  getOrderById: async (id) => {
    const response = await axiosInstance.get(`/api/orders/${id}`);
    return response.data;
  },

  updateOrderStatus: async (id, status) => {
    const response = await axiosInstance.patch(`/api/admin/order/${id}`, { status });
    return response.data;
  },

  verifyPickupCode: async (pickupCode) => {
    const response = await axiosInstance.post('/api/admin/order/verify-pickup-code', { pickupCode });
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
  },

  // Pharmacies
  getPharmacies: async (includeInactive = false) => {
    const response = await axiosInstance.get('/api/pharmacies', {
      params: { includeInactive }
    });
    return response.data;
  },

  getPharmacy: async (id) => {
    const response = await axiosInstance.get(`/api/pharmacies/${id}`);
    return response.data;
  },

  createPharmacy: async (data) => {
    const response = await axiosInstance.post('/api/pharmacies', data);
    return response.data;
  },

  updatePharmacy: async (id, data) => {
    const response = await axiosInstance.patch(`/api/pharmacies/${id}`, data);
    return response.data;
  },

  deletePharmacy: async (id) => {
    const response = await axiosInstance.delete(`/api/pharmacies/${id}`);
    return response.data;
  },

  // Warehouses
  getWarehouses: async () => {
    const response = await axiosInstance.get('/api/warehouses');
    return response.data;
  },

  getWarehouse: async (id) => {
    const response = await axiosInstance.get(`/api/warehouses/${id}`);
    return response.data;
  },

  createWarehouse: async (data) => {
    const response = await axiosInstance.post('/api/warehouses', data);
    return response.data;
  },

  updateWarehouse: async (id, data) => {
    const response = await axiosInstance.patch(`/api/warehouses/${id}`, data);
    return response.data;
  },

  deleteWarehouse: async (id) => {
    const response = await axiosInstance.delete(`/api/warehouses/${id}`);
    return response.data;
  },

  // Stocks
  getStocks: async () => {
    const response = await axiosInstance.get('/api/stocks');
    return response.data;
  },

  getStocksByProduct: async (productId) => {
    const response = await axiosInstance.get(`/api/stocks/product/${productId}`);
    return response.data;
  },

  createStock: async (data) => {
    const response = await axiosInstance.post('/api/stocks', data);
    return response.data;
  },

  updateStock: async (productId, warehouseId, data) => {
    const response = await axiosInstance.patch(`/api/stocks/${productId}/${warehouseId}`, data);
    return response.data;
  },

  deleteStock: async (productId, warehouseId) => {
    const response = await axiosInstance.delete(`/api/stocks/${productId}/${warehouseId}`);
    return response.data;
  }
};

export default adminApi;