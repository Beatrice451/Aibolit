import axiosInstance from './axiosInstance';

/**
 * Create a new order from the current cart
 * @param {Object} orderData - Order creation data
 * @param {number} orderData.pharmacyId - ID of the pharmacy for pickup
 * @param {string} [orderData.phone] - Phone number (required for guests)
 * @param {string} [orderData.email] - Email (required for guests)
 * @param {string} [orderData.firstName] - First name (required for guests)
 * @param {string} [orderData.lastName] - Last name (required for guests)
 * @returns {Promise} Order response with order details
 */
export const createOrder = async (orderData) => {
  const response = await axiosInstance.post('/api/orders', orderData);
  return response.data;
};

/**
 * Get all orders for the current user/guest
 * @returns {Promise} List of orders
 */
export const getOrders = async () => {
  const response = await axiosInstance.get('/api/orders');
  return response.data;
};

/**
 * Get a specific order by ID
 * @param {number} orderId - Order ID
 * @returns {Promise} Order details
 */
export const getOrderById = async (orderId) => {
  const response = await axiosInstance.get(`/api/orders/${orderId}`);
  return response.data;
};

export default {
  createOrder,
  getOrders,
  getOrderById,
};
