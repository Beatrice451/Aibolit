import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import Header from '../components/Header';
import orderApi from '../api/orderService';
import { FaCheckCircle } from 'react-icons/fa';

const OrderConfirmation = () => {
  const { orderId } = useParams();
  const navigate = useNavigate();
  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchOrder = async () => {
      try {
        setLoading(true);
        const orderData = await orderApi.getOrderById(orderId);
        setOrder(orderData);
      } catch (err) {
        console.error('Error fetching order:', err);
        setError('Не удалось загрузить информацию о заказе');
      } finally {
        setLoading(false);
      }
    };

    if (orderId) {
      fetchOrder();
    }
  }, [orderId]);

  if (loading) {
    return (
      <>
        <Header />
        <div className="order-confirmation">
          <div className="container">
            <div className="loading">Загрузка...</div>
          </div>
        </div>
      </>
    );
  }

  if (error || !order) {
    return (
      <>
        <Header />
        <div className="order-confirmation">
          <div className="container">
            <div className="error-message">
              {error || 'Заказ не найден'}
            </div>
            <Link to="/" className="btn btn--primary">
              На главную
            </Link>
          </div>
        </div>
      </>
    );
  }

  const getStatusText = (status) => {
    const statusMap = {
      'NEW': 'Новый',
      'ASSEMBLING': 'Собирается',
      'READY': 'Готов к выдаче',
      'DELIVERY_PENDING': 'Ожидает доставки',
      'DELIVERY_DELAYED': 'Доставка задерживается',
      'COMPLETED': 'Завершен',
      'CANCELLED_USER': 'Отменен пользователем',
      'CANCELLED_SYSTEM': 'Отменен системой',
      'EXPIRED': 'Истек'
    };
    return statusMap[status] || status;
  };

  return (
    <>
      <Header />
      <div className="order-confirmation">
        <div className="container">
          <div className="order-confirmation__success">
            <div className="success-icon"><FaCheckCircle /></div>
            <h2 className="order-confirmation__title">Заказ успешно оформлен!</h2>
            <p className="order-confirmation__subtitle">
              Номер заказа: <strong>#{order.id}</strong>
            </p>
          </div>

          <div className="order-details">
            <div className="order-details__section">
              <h3 className="order-details__heading">Статус заказа</h3>
              <div className="order-status">
                <span className={`order-status__badge order-status__badge--${order.orderStatus.toLowerCase()}`}>
                  {getStatusText(order.orderStatus)}
                </span>
              </div>
            </div>

            <div className="order-details__section">
              <h3 className="order-details__heading">Место получения</h3>
              <div className="order-pharmacy">
                <div className="order-pharmacy__name">{order.pharmacy.name}</div>
                <div className="order-pharmacy__address">{order.pharmacy.address}</div>
              </div>
            </div>

            {order.pickupCode && (
              <div className="order-details__section order-details__section--highlight">
                <h3 className="order-details__heading">Код получения</h3>
                <div className="pickup-code">
                  <div className="pickup-code__value">{order.pickupCode}</div>
                  <div className="pickup-code__hint">
                    Назовите этот код сотруднику аптеки при получении заказа
                  </div>
                </div>
              </div>
            )}

            <div className="order-details__section">
              <h3 className="order-details__heading">Состав заказа</h3>
              <div className="order-items">
                {order.items.map((item, index) => (
                  <div key={index} className="order-item">
                    <div className="order-item__name">{item.productName}</div>
                    <div className="order-item__quantity">× {item.quantity}</div>
                    <div className="order-item__price">{item.priceAtSale * item.quantity} ₽</div>
                  </div>
                ))}
              </div>
            </div>

            <div className="order-details__section">
              <h3 className="order-details__heading">Итого</h3>
              <div className="order-total">
                <div className="order-total__row">
                  <span>Сумма:</span>
                  <span>{order.amount.total} ₽</span>
                </div>
                {order.amount.discount > 0 && (
                  <div className="order-total__row order-total__row--discount">
                    <span>Скидка:</span>
                    <span>-{order.amount.discount} ₽</span>
                  </div>
                )}
                <div className="order-total__row order-total__row--final">
                  <span>К оплате:</span>
                  <span className="order-total__final-price">{order.amount.finalAmount} ₽</span>
                </div>
              </div>
            </div>

            <div className="order-details__section">
              <h3 className="order-details__heading">Контактная информация</h3>
              <div className="order-contact">
                <div className="order-contact__row">
                  <span>Имя:</span>
                  <span>{order.clientName}</span>
                </div>
                <div className="order-contact__row">
                  <span>Телефон:</span>
                  <span>{order.phone}</span>
                </div>
                <div className="order-contact__row">
                  <span>Email:</span>
                  <span>{order.email}</span>
                </div>
              </div>
            </div>
          </div>

          <div className="order-confirmation__actions">
            <Link to="/" className="btn btn--secondary">
              На главную
            </Link>
            <Link to="/profile" className="btn btn--primary">
              Мои заказы
            </Link>
          </div>

          <div className="order-confirmation__info">
            <p>
              Информация о готовности заказа будет отправлена на указанный email.
              Вы также можете отслеживать статус заказа в личном кабинете.
            </p>
          </div>
        </div>
      </div>
    </>
  );
};

export default OrderConfirmation;
