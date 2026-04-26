import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import authApi from '../api/authService';
import axiosInstance from '../api/axiosInstance';

const globalNotifications = [];
let globalNotificationId = 0;

const showGlobalNotification = (message, type = 'success') => {
  const id = ++globalNotificationId;
  const el = document.createElement('div');
  el.className = `notification notification--${type}`;
  el.textContent = message;
  el.style.position = 'fixed';
  el.style.top = '20px';
  el.style.right = '20px';
  document.body.appendChild(el);
  globalNotifications.push({ id, el });
  setTimeout(() => {
    const idx = globalNotifications.findIndex(n => n.id === id);
    if (idx >= 0) {
      globalNotifications.splice(idx, 1);
      el.remove();
    }
  }, 3000);
};

const Profile = () => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('info');
  const [ordersSubTab, setOrdersSubTab] = useState('current');
  const [orders, setOrders] = useState([]);
  const [ordersLoading, setOrdersLoading] = useState(false);
  const [deleteModal, setDeleteModal] = useState({ open: false, timer: 0 });
  const navigate = useNavigate();

  useEffect(() => {
    const fetchUser = async () => {
      const token = localStorage.getItem('accessToken');
      if (!token) {
        navigate('/login');
        return;
      }

      authApi.setAuthHeader(token);

      try {
        const userData = await authApi.getCurrentUser();
        setUser(userData);
      } catch (err) {
        console.error('Failed to fetch user:', err);
        localStorage.removeItem('accessToken');
        navigate('/login');
      } finally {
        setLoading(false);
      }
    };

    fetchUser();
  }, [navigate]);

  useEffect(() => {
    if (activeTab === 'orders' && orders.length === 0) {
      fetchOrders();
    }
  }, [activeTab]);

  useEffect(() => {
    if (deleteModal.open && deleteModal.timer > 0) {
      const interval = setInterval(() => {
        setDeleteModal(prev => {
          if (prev.timer <= 1) {
            clearInterval(interval);
            return { ...prev, timer: 0 };
          }
          return { ...prev, timer: prev.timer - 1 };
        });
      }, 1000);
      return () => clearInterval(interval);
    }
  }, [deleteModal.open, deleteModal.timer]);

  const fetchOrders = async () => {
    setOrdersLoading(true);
    try {
      const response = await axiosInstance.get('/api/orders');
      setOrders(response.data || []);
    } catch (err) {
      console.error('Failed to fetch orders:', err);
    } finally {
      setOrdersLoading(false);
    }
  };

  const handleLogout = async () => {
    await authApi.logout();
    navigate('/login');
  };

  const handleDeleteAccount = async () => {
    try {
      await axiosInstance.delete('/api/users/me');
      localStorage.removeItem('accessToken');
      showGlobalNotification('Аккаунт успешно удалён', 'success');
      setTimeout(() => navigate('/'), 1500);
    } catch (err) {
      console.error('Failed to delete account:', err);
      showGlobalNotification('Ошибка удаления аккаунта', 'error');
    }
  };

  const getStatusLabel = (status) => {
    const labels = {
      NEW: 'Новый',
      ASSEMBLING: 'Сборка',
      READY: 'Готов к выдаче',
      DELIVERY_PENDING: 'Ожидает доставки',
      DELIVERY_DELAYED: 'Задержка',
      COMPLETED: 'Выдан',
      CANCELLED_USER: 'Отменён',
      CANCELLED_SYSTEM: 'Отменён',
      EXPIRED: 'Истёк'
    };
    return labels[status] || status;
  };

  const getStatusClass = (status) => {
    return `order-status--${status?.toLowerCase()}`;
  };

  const currentOrders = orders.filter(o =>
    ['NEW', 'ASSEMBLING', 'READY', 'DELIVERY_PENDING', 'DELIVERY_DELAYED'].includes(o.orderStatus)
  );

  const orderHistory = orders.filter(o =>
    ['COMPLETED', 'CANCELLED_USER', 'CANCELLED_SYSTEM', 'EXPIRED'].includes(o.orderStatus)
  );

  const displayedOrders = ordersSubTab === 'current' ? currentOrders : orderHistory;

  if (loading) {
    return (
      <>
        <Header />
        <div className="loading">Загрузка...</div>
      </>
    );
  }

  if (!user) {
    return null;
  }

  return (
    <>
      <Header />
      <div className="profile-page">
        <div className="container">
          <div className="profile-layout">
            <div className="profile-sidebar">
              <div className="profile-sidebar__user">
                <div className="profile-sidebar__avatar">🧑‍⚕️</div>
                <div className="profile-sidebar__name">
                  {user.firstName && user.lastName
                    ? `${user.firstName} ${user.lastName}`
                    : user.firstName || user.lastName || user.email}
                </div>
              </div>

              <nav className="profile-sidebar__nav">
                <button
                  className={`profile-sidebar__item ${activeTab === 'info' ? 'active' : ''}`}
                  onClick={() => setActiveTab('info')}
                >
                  👤 Личная информация
                </button>
                <button
                  className={`profile-sidebar__item ${activeTab === 'orders' ? 'active' : ''}`}
                  onClick={() => setActiveTab('orders')}
                >
                  📦 Мои заказы
                </button>
                <button
                  className={`profile-sidebar__item ${activeTab === 'settings' ? 'active' : ''}`}
                  onClick={() => setActiveTab('settings')}
                >
                  ⚙️ Настройки
                </button>
              </nav>

              <button onClick={handleLogout} className="profile-sidebar__logout">
                🚪 Выйти из аккаунта
              </button>
            </div>

            <div className="profile-content">
              {activeTab === 'info' && (
                <div className="profile-card">
                  <h3 className="profile-card__title">Личная информация</h3>

                  <div className="profile-card__row">
                    <span className="profile-card__label">📧 Электронная почта</span>
                    <span className="profile-card__value">{user.email}</span>
                  </div>

                  <div className="profile-card__row">
                    <span className="profile-card__label">📞 Контактный телефон</span>
                    <span className="profile-card__value">{user.phone || 'Не указан'}</span>
                  </div>
                </div>
              )}

              {activeTab === 'orders' && (
                <div className="orders-history">
                  <div className="orders-subtabs">
                    <button
                      className={`orders-subtab ${ordersSubTab === 'current' ? 'active' : ''}`}
                      onClick={() => setOrdersSubTab('current')}
                    >
                      Текущие заказы ({currentOrders.length})
                    </button>
                    <button
                      className={`orders-subtab ${ordersSubTab === 'history' ? 'active' : ''}`}
                      onClick={() => setOrdersSubTab('history')}
                    >
                      История
                    </button>
                  </div>

                  {ordersLoading ? (
                    <div className="orders-history__loading">Загрузка заказов...</div>
                  ) : displayedOrders.length === 0 ? (
                    <div className="orders-history__empty">
                      {ordersSubTab === 'current' ? 'У вас нет активных заказов' : 'У вас нет завершённых заказов'}
                    </div>
                  ) : (
                    <div className="orders-history__list">
                      {displayedOrders.map(order => (
                        <div key={order.id} className="order-card">
                          <div className="order-card__header">
                            <span className="order-card__id">Заказ #{order.id}</span>
                            <span className={`order-card__status ${getStatusClass(order.orderStatus)}`}>
                              {getStatusLabel(order.orderStatus)}
                            </span>
                          </div>
                          <div className="order-card__info">
                            <span className="order-card__date">
                              {order.createdAt ? new Date(order.createdAt).toLocaleDateString('ru-RU') : ''}
                            </span>
                            <span className="order-card__amount">{order.amount?.total || 0} ₽</span>
                          </div>
                          <div className="order-card__items">
                            {order.items?.map((item, idx) => (
                              <span key={idx} className="order-card__item">
                                {item.name} × {item.quantity}
                              </span>
                            ))}
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              )}

              {activeTab === 'settings' && (
                <div className="profile-settings">
                  <h3 className="profile-card__title">Настройки аккаунта</h3>

                  <div className="profile-settings__danger">
                    <div className="profile-settings__danger-title">Опасная зона</div>
                    <p className="profile-settings__danger-text">
                      Удаление аккаунта — это необратимое действие. Вы потеряете доступ к аккаунту без возможности восстановления.
                    </p>
                    <button
                      className="profile-settings__delete-btn"
                      onClick={() => setDeleteModal({ open: true, timer: 10 })}
                    >
                      🗑️ Удалить аккаунт
                    </button>
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>

      {deleteModal.open && (
        <div className="delete-modal-overlay" onClick={() => setDeleteModal({ open: false, timer: 0 })}>
          <div className="delete-modal" onClick={e => e.stopPropagation()}>
            <h3>⚠️ Удаление аккаунта</h3>
            <p className="delete-modal__warning">
              Вы уверены, что хотите удалить аккаунт? Восстановить его будет невозможно!
            </p>
            <p className="delete-modal__note">
              После удаления вы больше не сможете использовать эту почту и номер телефона для регистрации новых аккаунтов.
            </p>
            <div className="delete-modal__actions">
              <button className="admin-btn" onClick={() => setDeleteModal({ open: false, timer: 0 })}>
                Отмена
              </button>
              <button
                className="admin-btn admin-btn--danger"
                disabled={deleteModal.timer > 0}
                onClick={handleDeleteAccount}
              >
                {deleteModal.timer > 0
                  ? `Подтвердить через ${deleteModal.timer} сек.`
                  : 'Да, я хочу удалить аккаунт'}
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
};

export default Profile;