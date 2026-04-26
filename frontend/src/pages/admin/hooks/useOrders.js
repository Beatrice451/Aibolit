import { useState, useCallback, useEffect } from 'react';
import adminApi from '../../../api/adminService';
import axiosInstance from '../../../api/axiosInstance';

export const useOrders = () => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [filters, setFilters] = useState({
    status: '',
    email: '',
    phone: '',
    showCompleted: false,
    showCancelled: false
  });
  const [expandedOrders, setExpandedOrders] = useState({});
  const [orderItemsImages, setOrderItemsImages] = useState({});
  const [statusModal, setStatusModal] = useState({ open: false, orderId: null, newStatus: null });

  const loadOrders = useCallback(async (pageNum = 0) => {
    setLoading(true);
    try {
      const activeFilters = {
        orderStatus: filters.status || null,
        email: filters.email || null,
        phone: filters.phone || null,
      };

      if (!filters.showCompleted) {
        activeFilters.excludeCompleted = true;
      }
      if (!filters.showCancelled) {
        activeFilters.excludeCancelled = true;
      }

      const data = await adminApi.getOrders(pageNum, 20, activeFilters);
      setOrders(data.content || []);
      setTotalPages(data.totalPages || 0);
      setPage(pageNum);
    } catch (err) {
      console.error('[Admin Orders] Error:', err);
    } finally {
      setLoading(false);
    }
  }, [filters]);

  const updateStatus = async (orderId, newStatus) => {
    try {
      await adminApi.updateOrderStatus(orderId, newStatus);
      await loadOrders(page);
    } catch (err) {
      console.error('Error updating order status:', err);
      alert('Ошибка изменения статуса заказа');
    }
  };

  const openStatusModal = (orderId, newStatus) => {
    setStatusModal({ open: true, orderId, newStatus });
  };

  const closeStatusModal = () => {
    setStatusModal({ open: false, orderId: null, newStatus: null });
  };

  const confirmStatusChange = async () => {
    const { orderId, newStatus } = statusModal;
    await updateStatus(orderId, newStatus);
    closeStatusModal();
  };

  const toggleOrder = (orderId) => {
    setExpandedOrders(prev => ({ ...prev, [orderId]: !prev[orderId] }));
  };

  useEffect(() => {
    const loadImages = async () => {
      const newImages = {};
      for (const order of orders) {
        if (expandedOrders[order.id] && order.items) {
          for (const item of order.items) {
            if (!orderItemsImages[item.productId]) {
              try {
                const response = await axiosInstance.get(`/api/products/${item.productId}`);
                newImages[item.productId] = response.data.imageUrl;
              } catch {
                newImages[item.productId] = null;
              }
            }
          }
        }
      }
      if (Object.keys(newImages).length > 0) {
        setOrderItemsImages(prev => ({ ...prev, ...newImages }));
      }
    };
    loadImages();
  }, [expandedOrders, orders]);

  const getProductImage = async (productId) => {
    if (orderItemsImages[productId]) return orderItemsImages[productId];
    try {
      const response = await axiosInstance.get(`/api/products/${productId}`);
      const imageUrl = response.data.imageUrl;
      setOrderItemsImages(prev => ({ ...prev, [productId]: imageUrl }));
      return imageUrl;
    } catch {
      return null;
    }
  };

  const resetFilters = () => {
    setFilters({ status: '', email: '', phone: '', showCompleted: false, showCancelled: false });
  };

  return {
    orders,
    loading,
    page,
    totalPages,
    filters,
    setFilters,
    expandedOrders,
    orderItemsImages,
    statusModal,
    loadOrders,
    updateStatus,
    openStatusModal,
    closeStatusModal,
    confirmStatusChange,
    toggleOrder,
    getProductImage,
    resetFilters
  };
};