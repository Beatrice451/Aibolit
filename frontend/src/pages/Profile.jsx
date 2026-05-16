import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import Header from '../components/Header';
import { showNotification } from '../components/NotificationSystem';
import authApi from '../api/authService';
import pharmacyApi from '../api/pharmacyService';
import axiosInstance from '../api/axiosInstance';
import productApi from '../api/productService';
import StarRating from '../components/StarRating';
import { FaUserMd, FaUser, FaBox, FaCog, FaSignOutAlt, FaEnvelope, FaPhone, FaExclamationTriangle, FaTrash, FaEdit, FaSave, FaTimes, FaStar, FaCheck, FaChevronLeft, FaChevronRight, FaPills } from 'react-icons/fa';

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
  const [editingInfo, setEditingInfo] = useState(false);
  const [infoForm, setInfoForm] = useState({ firstName: '', lastName: '', phone: '' });
  const [savingInfo, setSavingInfo] = useState(false);
  const [pharmacies, setPharmacies] = useState([]);
  const [loadingPharmacies, setLoadingPharmacies] = useState(false);
  const [myReviews, setMyReviews] = useState([]);
  const [myReviewsPage, setMyReviewsPage] = useState(0);
  const [myReviewsTotalPages, setMyReviewsTotalPages] = useState(0);
  const [myReviewsLoading, setMyReviewsLoading] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();

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

  // Show verification notifications based on navigation state
  useEffect(() => {
    if (location.state?.verificationSuccess) {
      showNotification('Почта успешно подтверждена', 'success');
      // Clear the state to prevent showing notification on refresh
      navigate(location.pathname, { replace: true, state: {} });
    } else if (location.state?.verificationAlreadyDone) {
      showNotification('Почта была подтверждена ранее, повторное подтверждение не требуется', 'info');
      navigate(location.pathname, { replace: true, state: {} });
    } else if (location.state?.verificationError) {
      showNotification(location.state.errorMessage || 'Ошибка подтверждения email', 'error');
      navigate(location.pathname, { replace: true, state: {} });
    }
  }, [location.state, location.pathname, navigate]);

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

  useEffect(() => {
    if (activeTab === 'settings' && pharmacies.length === 0) {
      fetchPharmacies();
    }
  }, [activeTab]);

  const fetchPharmacies = async () => {
    setLoadingPharmacies(true);
    try {
      const data = await pharmacyApi.getPharmacies();
      setPharmacies(data || []);
    } catch (err) {
      console.error('Failed to fetch pharmacies:', err);
    } finally {
      setLoadingPharmacies(false);
    }
  };

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

  const fetchMyReviews = async (page = 0) => {
    setMyReviewsLoading(true);
    try {
      const data = await productApi.getMyReviews({ page, size: 10 });
      setMyReviews(data.content || []);
      setMyReviewsTotalPages(data.totalPages || 0);
      setMyReviewsPage(page);
    } catch (err) {
      console.error('Failed to fetch reviews:', err);
    } finally {
      setMyReviewsLoading(false);
    }
  };

  useEffect(() => {
    if (activeTab === 'reviews') {
      fetchMyReviews(0);
    }
  }, [activeTab]);

  const getImageUrl = (imagePath) => {
    if (!imagePath) return null;
    if (imagePath.startsWith('http')) return imagePath;
    return `/media/${imagePath}`;
  };

  const truncate = (text, max) => {
    if (!text) return '';
    return text.length > max ? text.slice(0, max) + '...' : text;
  };

  const handleResendVerification = async () => {
    if (!user?.email) return;
    try {
      await authApi.resendVerification(user.email);
      showNotification('Письмо отправлено. Проверьте почту.', 'success');
    } catch (err) {
      showNotification(err.response?.data?.message || 'Ошибка отправки', 'error');
    }
  };

  const handleStartEditInfo = () => {
    setInfoForm({
      firstName: user.firstName || '',
      lastName: user.lastName || '',
      phone: user.phone || ''
    });
    setEditingInfo(true);
  };

  const handleCancelEditInfo = () => {
    setEditingInfo(false);
    setInfoForm({ firstName: '', lastName: '', phone: '' });
  };

  const handleSaveInfo = async () => {
    setSavingInfo(true);
    try {
      const updatedUser = await authApi.updateUser({
        firstName: infoForm.firstName,
        lastName: infoForm.lastName,
        phone: infoForm.phone
      });
      setUser(updatedUser);
      setEditingInfo(false);
      showGlobalNotification('Данные сохранены', 'success');
    } catch (err) {
      console.error('Failed to update user:', err);
      showGlobalNotification('Ошибка сохранения', 'error');
    } finally {
      setSavingInfo(false);
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
          {!user.emailVerified && (
            <div className="email-verification-banner">
              <div className="email-verification-banner__content">
                <span className="email-verification-banner__icon"><FaExclamationTriangle /></span>
                <div className="email-verification-banner__text">
                  <strong>Аккаунт не подтверждён</strong>
                  <p>Не пришло письмо? Проверьте спам или запросите новый код.</p>
                </div>
                <button className="email-verification-banner__btn" onClick={handleResendVerification}>
                  Запросить повторный код
                </button>
              </div>
            </div>
          )}
          <div className="profile-layout">
            <div className="profile-sidebar">
              <div className="profile-sidebar__user">
                <div className="profile-sidebar__avatar"><FaUserMd /></div>
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
                  <FaUser /> Личная информация
                </button>
                <button
                  className={`profile-sidebar__item ${activeTab === 'orders' ? 'active' : ''}`}
                  onClick={() => setActiveTab('orders')}
                >
                  <FaBox /> Мои заказы
                </button>
                <button
                  className={`profile-sidebar__item ${activeTab === 'reviews' ? 'active' : ''}`}
                  onClick={() => setActiveTab('reviews')}
                >
                  <FaStar /> Мои отзывы
                </button>
                <button
                  className={`profile-sidebar__item ${activeTab === 'settings' ? 'active' : ''}`}
                  onClick={() => setActiveTab('settings')}
                >
                  <FaCog /> Настройки
                </button>
              </nav>

              <button onClick={handleLogout} className="profile-sidebar__logout">
                <FaSignOutAlt /> Выйти из аккаунта
              </button>
            </div>

            <div className="profile-content">
              {activeTab === 'info' && (
                <div className="profile-card">
                  <div className="profile-card__header">
                    <h3 className="profile-card__title">Личная информация</h3>
                    {!editingInfo && (
                      <button className="profile-card__edit-btn" onClick={handleStartEditInfo}>
                        <FaEdit /> Изменить
                      </button>
                    )}
                  </div>

                  {editingInfo ? (
                    <>
                      <div className="profile-card__row">
                        <span className="profile-card__label"><FaUser /> Имя</span>
                        <input
                          type="text"
                          className="profile-card__input"
                          value={infoForm.firstName}
                          onChange={(e) => setInfoForm({ ...infoForm, firstName: e.target.value })}
                          placeholder="Ваше имя"
                        />
                      </div>

                      <div className="profile-card__row">
                        <span className="profile-card__label"><FaUser /> Фамилия</span>
                        <input
                          type="text"
                          className="profile-card__input"
                          value={infoForm.lastName}
                          onChange={(e) => setInfoForm({ ...infoForm, lastName: e.target.value })}
                          placeholder="Ваша фамилия"
                        />
                      </div>

                      <div className="profile-card__row">
                        <span className="profile-card__label"><FaPhone /> Телефон</span>
                        <input
                          type="tel"
                          className="profile-card__input"
                          value={infoForm.phone}
                          onChange={(e) => setInfoForm({ ...infoForm, phone: e.target.value })}
                          placeholder="+79001234567"
                        />
                      </div>

                      <div className="profile-card__actions">
                        <button
                          className="admin-btn"
                          onClick={handleCancelEditInfo}
                          disabled={savingInfo}
                        >
                          <FaTimes /> Отмена
                        </button>
                        <button
                          className="admin-btn admin-btn--primary"
                          onClick={handleSaveInfo}
                          disabled={savingInfo}
                        >
                          <FaSave /> {savingInfo ? 'Сохранение...' : 'Сохранить'}
                        </button>
                      </div>
                    </>
                  ) : (
                    <>
                      <div className="profile-card__row">
                        <span className="profile-card__label"><FaUser /> Имя</span>
                        <span className="profile-card__value">{user.firstName || 'Не указано'}</span>
                      </div>

                      <div className="profile-card__row">
                        <span className="profile-card__label"><FaUser /> Фамилия</span>
                        <span className="profile-card__value">{user.lastName || 'Не указана'}</span>
                      </div>

                      <div className="profile-card__row">
                        <span className="profile-card__label"><FaEnvelope /> Электронная почта</span>
                        <span className="profile-card__value">{user.email}</span>
                      </div>

                      <div className="profile-card__row">
                        <span className="profile-card__label"><FaPhone /> Контактный телефон</span>
                        <span className="profile-card__value">{user.phone || 'Не указан'}</span>
                      </div>
                    </>
                  )}
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

                  <div className="profile-settings__section">
                    <h4 className="profile-settings__subtitle">Предпочитаемая аптека</h4>
                    <p className="profile-settings__desc">
                      Выберите аптеку, которая будет использоваться по умолчанию при оформлении заказов.
                    </p>
                    {loadingPharmacies ? (
                      <div className="loading">Загрузка аптек...</div>
                    ) : (
                      <select
                        className="profile-settings__select"
                        value={user.pharmacyId || ''}
                        onChange={async (e) => {
                          const pharmacyId = e.target.value ? Number(e.target.value) : null;
                          try {
                            await authApi.updateUser({
                              firstName: user.firstName,
                              lastName: user.lastName,
                              phone: user.phone,
                              preferredPharmacyId: pharmacyId
                            });
                            setUser({ ...user, pharmacyId: pharmacyId });
                            showGlobalNotification('Аптека сохранена', 'success');
                          } catch (err) {
                            console.error('Failed to update pharmacy:', err);
                            showGlobalNotification('Ошибка сохранения', 'error');
                          }
                        }}
                      >
                        <option value="">Не выбрана</option>
                        {pharmacies.map(pharmacy => (
                          <option key={pharmacy.id} value={pharmacy.id}>
                            {pharmacy.name} - {pharmacy.address}
                          </option>
                        ))}
                      </select>
                    )}
                  </div>

                  <div className="profile-settings__danger">
                    <div className="profile-settings__danger-title">Опасная зона</div>
                    <p className="profile-settings__danger-text">
                      Удаление аккаунта — это необратимое действие. Вы потеряете доступ к аккаунту без возможности восстановления.
                    </p>
                    <button
                      className="profile-settings__delete-btn"
                      onClick={() => setDeleteModal({ open: true, timer: 10 })}
                    >
                      <FaTrash /> Удалить аккаунт
                    </button>
                  </div>
                </div>
              )}

              {activeTab === 'reviews' && (
                <div className="profile-reviews">
                  <h3 className="profile-card__title">Мои отзывы</h3>

                  {myReviewsLoading ? (
                    <div className="profile-reviews__loading">Загрузка отзывов...</div>
                  ) : myReviews.length === 0 ? (
                    <div className="profile-reviews__empty">Вы ещё не оставляли отзывы</div>
                  ) : (
                    <>
                      <div className="profile-reviews__list">
                        {myReviews.map(review => (
                          <div key={review.reviewId} className="profile-reviews__item">
                            <div className="profile-reviews__item-actions">
                              <div className="profile-reviews__item-status">
                                <FaCheck /> Опубликован
                              </div>
                              <div className="profile-reviews__item-actions-group">
                                <button className="profile-reviews__item-delete" title="Удалить">
                                  <FaTrash />
                                </button>
                                <button className="profile-reviews__item-edit">Редактировать</button>
                              </div>
                            </div>

                            <div className="profile-reviews__item-body">
                              <Link to={`/product/${review.productId}`} className="profile-reviews__item-image-link">
                                <div className="profile-reviews__item-image">
                                  {review.productImageUrl ? (
                                    <img src={getImageUrl(review.productImageUrl)} alt={review.productName} />
                                  ) : (
                                    <FaPills />
                                  )}
                                </div>
                              </Link>
                              <div className="profile-reviews__item-info">
                                <div className="profile-reviews__item-rating-row">
                                  <StarRating rating={review.rating} size={18} />
                                  <span className="profile-reviews__item-date">
                                    {new Date(review.createdAt).toLocaleDateString('ru-RU', {
                                      day: 'numeric',
                                      month: 'long',
                                      year: 'numeric'
                                    })}
                                  </span>
                                </div>
                                <Link to={`/product/${review.productId}`} className="profile-reviews__item-product-link">
                                  <span className="profile-reviews__item-product">
                                    {truncate(review.productName, 64)}
                                  </span>
                                </Link>
                              </div>
                            </div>

                            <p className="profile-reviews__item-comment">{review.comment}</p>
                          </div>
                        ))}
                      </div>

                      {myReviewsTotalPages > 1 && (
                        <div className="profile-reviews__pagination">
                          <button
                            className="admin-btn admin-btn--small"
                            disabled={myReviewsPage === 0}
                            onClick={() => fetchMyReviews(myReviewsPage - 1)}
                          >
                            <FaChevronLeft />
                          </button>
                          <span className="profile-reviews__page-info">
                            {myReviewsPage + 1} из {myReviewsTotalPages}
                          </span>
                          <button
                            className="admin-btn admin-btn--small"
                            disabled={myReviewsPage >= myReviewsTotalPages - 1}
                            onClick={() => fetchMyReviews(myReviewsPage + 1)}
                          >
                            <FaChevronRight />
                          </button>
                        </div>
                      )}
                    </>
                  )}
                </div>
              )}
            </div>
          </div>
        </div>
      </div>

      {deleteModal.open && (
        <div className="delete-modal-overlay" onClick={() => setDeleteModal({ open: false, timer: 0 })}>
          <div className="delete-modal" onClick={e => e.stopPropagation()}>
            <h3><FaExclamationTriangle /> Удаление аккаунта</h3>
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