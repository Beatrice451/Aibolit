import React, { useEffect, useState } from 'react';
import { useOrders } from '../hooks';
import axiosInstance from '../../../api/axiosInstance';
import adminApi from '../../../api/adminService';
import Modal from '../../../components/Modal';
import PickupCodeModal from './PickupCodeModal';
import { showNotification } from '../../../components/NotificationSystem';
import { FaKey, FaPills } from 'react-icons/fa';

const STATUS_LABELS = {
  NEW: 'Новый',
  ASSEMBLING: 'Сборка',
  READY: 'Готов к выдаче',
  DELIVERY_PENDING: 'Ожидает доставки',
  DELIVERY_DELAYED: 'Доставка задерживается',
  COMPLETED: 'Выдан',
  CANCELLED_USER: 'Отменён пользователем',
  CANCELLED_SYSTEM: 'Отменён системой',
  EXPIRED: 'Истёк'
};

const OrdersTab = () => {
  const {
    orders,
    loading,
    page,
    totalPages,
    filters,
    setFilters,
    expandedOrders,
    loadOrders,
    toggleOrder,
    resetFilters
  } = useOrders();

  // TODO: исправить - получать imageUrl вместе с заказом, а не отдельным запросом
  const [productImages, setProductImages] = useState({});

  // Модальное окно подтверждения смены статуса
  const [confirmModal, setConfirmModal] = useState({ open: false, orderId: null, newStatus: null });

  // Модальное окно выдачи по коду
  const [pickupCodeModalOpen, setPickupCodeModalOpen] = useState(false);

  const handleStatusChange = (orderId, newStatus) => {
    setConfirmModal({ open: true, orderId, newStatus });
  };

  const confirmStatusChange = () => {
    const { orderId, newStatus } = confirmModal;
    adminApi.updateOrderStatus(orderId, newStatus)
      .then(() => loadOrders(page))
      .catch(err => {
        console.error('Error updating status:', err);
        alert('Ошибка изменения статуса');
      })
      .finally(() => setConfirmModal({ open: false, orderId: null, newStatus: null }));
  };

  const loadProductImage = async (productId) => {
    if (productImages[productId] !== undefined) return;
    try {
      const response = await axiosInstance.get(`/api/products/${productId}`);
      // TODO: исправить - получать imageUrl вместе с заказом, а не отдельным запросом
      const rawUrl = response.data.imageUrl;
      if (rawUrl) {
        const imgName = rawUrl.startsWith('/') ? rawUrl.slice(1) : rawUrl;
        setProductImages(prev => ({ ...prev, [productId]: `/media/${imgName}` }));
      } else {
        setProductImages(prev => ({ ...prev, [productId]: null }));
      }
    } catch {
      setProductImages(prev => ({ ...prev, [productId]: null }));
    }
  };

  useEffect(() => {
    loadOrders(0);
  }, [loadOrders]);

  useEffect(() => {
    orders.forEach(order => {
      if (expandedOrders[order.id] && order.items) {
        order.items.forEach(item => {
          loadProductImage(item.productId);
        });
      }
    });
  }, [expandedOrders, orders]);

  return (
    <div className="admin-content">
      <div className="orders-filters">
        <div className="orders-filters__row">
          <select
            value={filters.status}
            onChange={e => setFilters({ ...filters, status: e.target.value })}
          >
            <option value="">Все статусы</option>
            <option value="NEW">Новый</option>
            <option value="ASSEMBLING">Сборка</option>
            <option value="READY">Готов к выдаче</option>
            <option value="DELIVERY_PENDING">Ожидает доставки</option>
            <option value="DELIVERY_DELAYED">Доставка задерживается</option>
            <option value="COMPLETED">Выдан</option>
            <option value="CANCELLED_USER">Отменён пользователем</option>
            <option value="CANCELLED_SYSTEM">Отменён системой</option>
            <option value="EXPIRED">Истёк</option>
          </select>

          <input
            type="text"
            placeholder="Email"
            value={filters.email}
            onChange={e => setFilters({ ...filters, email: e.target.value })}
          />

          <input
            type="text"
            placeholder="Телефон"
            value={filters.phone}
            onChange={e => setFilters({ ...filters, phone: e.target.value })}
          />

          <button className="admin-btn" onClick={() => loadOrders(0)}>Найти</button>
          <button className="admin-btn" onClick={resetFilters}>Сбросить</button>
          <button 
            className="admin-btn admin-btn--success" 
            onClick={() => setPickupCodeModalOpen(true)}
            style={{ marginLeft: 'auto' }}
          >
            <FaKey /> Выдать по коду
          </button>
        </div>

        <div className="orders-filters__switches">
          <label className="orders-filters__switch">
            <input
              type="checkbox"
              checked={filters.showCompleted}
              onChange={e => setFilters({ ...filters, showCompleted: e.target.checked })}
            />
            <span>Показать завершённые</span>
          </label>
          <label className="orders-filters__switch">
            <input
              type="checkbox"
              checked={filters.showCancelled}
              onChange={e => setFilters({ ...filters, showCancelled: e.target.checked })}
            />
            <span>Показать отменённые</span>
          </label>
        </div>
      </div>

      {loading ? (
        <div className="admin-loading">Загрузка заказов...</div>
      ) : orders.length === 0 ? (
        <div className="admin-empty">Заказы не найдены</div>
      ) : (
        <>
          <div className="orders-table">
            <table>
              <thead>
                <tr>
                  <th></th>
                  <th>ID</th>
                  <th>Дата</th>
                  <th>Клиент</th>
                  <th>Email</th>
                  <th>Телефон</th>
                  <th>Сумма</th>
                  <th>Статус</th>
                </tr>
              </thead>
              <tbody>
                {orders.map(order => (
                  <React.Fragment key={order.id}>
                    <tr>
                      <td>
                        <span
                          className={`order-toggle-icon ${expandedOrders[order.id] ? 'order-toggle-icon--expanded' : ''}`}
                          onClick={() => toggleOrder(order.id)}
                        >
                          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                            <polyline points="9 18 15 12 9 6"></polyline>
                          </svg>
                        </span>
                      </td>
                      <td>#{order.id}</td>
                      <td>{new Date(order.createdAt).toLocaleString()}</td>
                      <td>{order.clientName}</td>
                      <td>{order.email}</td>
                      <td>{order.phone}</td>
                      <td>{order.amount?.total || 0} ₽</td>
                      <td>
                        <div 
                          onClick={(e) => {
                            if (['COMPLETED', 'CANCELLED_USER', 'CANCELLED_SYSTEM', 'EXPIRED'].includes(order.orderStatus)) {
                              e.preventDefault();
                              showNotification('Невозможно изменить статус заказа в терминальном состоянии', 'warning');
                            }
                          }}
                        >
                          <select
                            className={`order-status-select order-status-select--${(order.orderStatus || '').toLowerCase()}`}
                            value={order.orderStatus || ''}
                            onChange={(e) => {
                              const newStatus = e.target.value;
                              if (newStatus !== order.orderStatus) {
                                handleStatusChange(order.id, newStatus);
                              }
                            }}
                            disabled={['COMPLETED', 'CANCELLED_USER', 'CANCELLED_SYSTEM', 'EXPIRED'].includes(order.orderStatus)}
                          >
                            <option value="NEW">Новый</option>
                            <option value="ASSEMBLING">Сборка</option>
                            <option value="READY">Готов к выдаче</option>
                            <option value="DELIVERY_PENDING">Ожидает доставки</option>
                            <option value="DELIVERY_DELAYED">Доставка задерживается</option>
                            <option value="COMPLETED">Выдан</option>
                            <option value="CANCELLED_USER">Отменён пользователем</option>
                            <option value="CANCELLED_SYSTEM">Отменён системой</option>
                            <option value="EXPIRED">Истёк</option>
                          </select>
                        </div>
                      </td>
                    </tr>
                    {expandedOrders[order.id] && order.items && order.items.length > 0 && (
                      <tr className="order-items-row">
                        <td colSpan="8">
                          <div className="order-items-list">
                            {order.items.map((item, index) => {
                              const imageUrl = productImages[item.productId];
                              return (
                                <div key={index} className="order-item">
                                  <div className="order-item__image">
                                    {imageUrl ? (
                                      <img src={imageUrl} alt={item.name} />
                                    ) : (
                                      <span className="order-item__placeholder"><FaPills /></span>
                                    )}
                                  </div>
                                  <div className="order-item__info">
                                    <div className="order-item__name">{item.name}</div>
                                    <div className="order-item__qty">Количество: {item.quantity}</div>
                                  </div>
                                  <div className="order-item__price">
                                    {item.priceAtSale} ₽ × {item.quantity} = {Number(item.priceAtSale) * Number(item.quantity)} ₽
                                  </div>
                                </div>
                              );
                            })}
                          </div>
                        </td>
                      </tr>
                    )}
                  </React.Fragment>
                ))}
              </tbody>
            </table>
          </div>

          {totalPages > 1 && (
            <div className="admin-pagination">
              <button
                className="admin-btn"
                disabled={page === 0}
                onClick={() => loadOrders(page - 1)}
              >
                Предыдущая
              </button>
              <span className="admin-pagination__info">
                Страница {page + 1} из {totalPages}
              </span>
              <button
                className="admin-btn"
                disabled={page >= totalPages - 1}
                onClick={() => loadOrders(page + 1)}
              >
                Следующая
              </button>
            </div>
          )}
        </>
      )}

      <Modal
        isOpen={confirmModal.open}
        onClose={() => setConfirmModal({ open: false, orderId: null, newStatus: null })}
        title="Изменение статуса заказа"
      >
        <p>Вы уверены, что хотите изменить статус заказа #{confirmModal.orderId} на <strong>{STATUS_LABELS[confirmModal.newStatus]}</strong>?</p>
        <div style={{ display: 'flex', gap: '12px', justifyContent: 'center', marginTop: '20px' }}>
          <button className="admin-btn" onClick={() => setConfirmModal({ open: false, orderId: null, newStatus: null })}>Отмена</button>
          <button className="admin-btn admin-btn--primary" onClick={confirmStatusChange}>Подтвердить</button>
        </div>
      </Modal>

      <PickupCodeModal
        isOpen={pickupCodeModalOpen}
        onClose={() => setPickupCodeModalOpen(false)}
        onOrderCompleted={() => {
          loadOrders(page);
          alert('Заказ успешно выдан!');
        }}
      />
    </div>
  );
};

export default OrdersTab;