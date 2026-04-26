import axiosInstance from './axiosInstance';

const ORDER_STATUSES = [
  { value: 'NEW', label: 'Новый' },
  { value: 'ASSEMBLING', label: 'Сборка' },
  { value: 'READY', label: 'Готов к выдаче' },
  { value: 'DELIVERY_PENDING', label: 'Ожидает доставки' },
  { value: 'DELIVERY_DELAYED', label: 'Доставка задерживается' },
  { value: 'COMPLETED', label: 'Завершён' },
  { value: 'CANCELLED_USER', label: 'Отменён пользователем' },
  { value: 'CANCELLED_SYSTEM', label: 'Отменён системой' },
  { value: 'EXPIRED', label: 'Истёк' },
];

const ACTIVE_STATUSES = ['NEW', 'ASSEMBLING', 'READY', 'DELIVERY_PENDING', 'DELIVERY_DELAYED'];
const COMPLETED_STATUSES = ['COMPLETED', 'CANCELLED_USER', 'CANCELLED_SYSTEM', 'EXPIRED'];

const adminOrdersApi = {
  getOrders: async (filters = {}, page = 0, size = 20) => {
    const params = new URLSearchParams();
    params.append('page', page);
    params.append('size', size);
    params.append('sort', 'createdAt,desc');

    if (filters.orderStatus && filters.orderStatus !== 'ALL') {
      params.append('orderStatus', filters.orderStatus);
    }

    if (filters.search) {
      params.append('id', filters.search);
    }

    if (filters.email) {
      params.append('email', filters.email);
    }

    if (filters.phone) {
      params.append('phone', filters.phone);
    }

    const response = await axiosInstance.get('/api/admin/order', { params });
    return response.data;
  },

  updateOrderStatus: async (orderId, newStatus) => {
    const response = await axiosInstance.patch(`/api/admin/order/${orderId}`, {
      orderStatus: newStatus
    });
    return response.data;
  },

  getStatusLabel: (status) => {
    const found = ORDER_STATUSES.find(s => s.value === status);
    return found ? found.label : status;
  },

  ORDER_STATUSES,
  ACTIVE_STATUSES,
  COMPLETED_STATUSES,
};

export default adminOrdersApi;