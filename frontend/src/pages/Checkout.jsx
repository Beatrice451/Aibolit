import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import cartApi from '../api/cartService';
import pharmacyApi from '../api/pharmacyService';
import orderApi from '../api/orderService';

const getImageUrl = (imagePath) => {
  if (!imagePath) return null;
  if (imagePath.startsWith('http')) return imagePath;
  return imagePath.startsWith('/') ? imagePath : `/media/${imagePath}`;
};

const Checkout = () => {
  const navigate = useNavigate();
  const [cart, setCart] = useState(null);
  const [pharmacies, setPharmacies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  // Form state
  const [selectedPharmacy, setSelectedPharmacy] = useState('');
  const [phone, setPhone] = useState('');
  const [email, setEmail] = useState('');
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        
        // Check if user is authenticated
        const token = localStorage.getItem('accessToken');
        setIsAuthenticated(!!token);

        // Fetch cart
        const cartData = await cartApi.getCart();
        setCart(cartData);

        // Fetch pharmacies
        const pharmaciesData = await pharmacyApi.getPharmacies();
        setPharmacies(pharmaciesData);

        // Redirect if cart is empty
        if (!cartData.items || cartData.items.length === 0) {
          navigate('/cart');
        }
      } catch (err) {
        console.error('Error loading checkout:', err);
        setError('Не удалось загрузить данные для оформления заказа');
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [navigate]);

  const validatePhone = (phone) => {
    const phoneRegex = /^(\+7|8)\d{10}$/;
    return phoneRegex.test(phone);
  };

  const validateEmail = (email) => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);

    // Validation
    if (!selectedPharmacy) {
      setError('Выберите аптеку для получения заказа');
      return;
    }

    // For guests, validate contact info
    if (!isAuthenticated) {
      if (!phone || !validatePhone(phone)) {
        setError('Введите корректный номер телефона (формат: +7XXXXXXXXXX или 8XXXXXXXXXX)');
        return;
      }
      if (!email || !validateEmail(email)) {
        setError('Введите корректный email');
        return;
      }
      if (!firstName || firstName.trim().length === 0) {
        setError('Введите имя');
        return;
      }
      if (!lastName || lastName.trim().length === 0) {
        setError('Введите фамилию');
        return;
      }
    }

    try {
      setSubmitting(true);

      const orderData = {
        pharmacyId: parseInt(selectedPharmacy),
      };

      // Add contact info for guests
      if (!isAuthenticated) {
        orderData.phone = phone;
        orderData.email = email;
        orderData.firstName = firstName.trim();
        orderData.lastName = lastName.trim();
      }

      const order = await orderApi.createOrder(orderData);
      
      // Redirect to order confirmation
      navigate(`/order-confirmation/${order.id}`);
    } catch (err) {
      console.error('Error creating order:', err);
      setError(err.response?.data?.message || 'Не удалось оформить заказ. Попробуйте снова.');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <>
        <Header />
        <div className="checkout-page">
          <div className="container">
            <div className="loading">Загрузка...</div>
          </div>
        </div>
      </>
    );
  }

  if (!cart || !cart.items || cart.items.length === 0) {
    return null; // Will redirect in useEffect
  }

  return (
    <>
      <Header />
      <div className="checkout-page">
        <div className="container">
          <h2 className="checkout-page__title">Оформление заказа</h2>

          <div className="checkout-content">
            {/* Order Summary */}
            <div className="checkout-summary">
              <h3 className="checkout-summary__title">Ваш заказ</h3>
              <div className="checkout-items">
                {cart.items.map(item => (
                  <div key={item.productId} className="checkout-item">
                    <div className="checkout-item__image">
                      {item.productImage ? (
                        <img 
                          src={getImageUrl(item.productImage)} 
                          alt={item.productName}
                          onError={(e) => { e.target.style.display = 'none'; e.target.nextSibling.style.display = 'flex'; }}
                        />
                      ) : null}
                      <span className="checkout-item__placeholder" style={item.productImage ? { display: 'none' } : {}}>💊</span>
                    </div>
                    <div className="checkout-item__info">
                      <div className="checkout-item__name">{item.productName}</div>
                      <div className="checkout-item__quantity">× {item.quantity}</div>
                    </div>
                    <div className="checkout-item__price">{item.price * item.quantity} ₽</div>
                  </div>
                ))}
              </div>
              <div className="checkout-total">
                <span>Итого:</span>
                <span className="checkout-total__price">{cart.totalPrice} ₽</span>
              </div>
            </div>

            {/* Checkout Form */}
            <div className="checkout-form-wrapper">
              <form className="checkout-form" onSubmit={handleSubmit}>
                <h3 className="checkout-form__title">Данные для получения</h3>

                {error && (
                  <div className="checkout-error">
                    {error}
                  </div>
                )}

                {/* Pharmacy Selection */}
                <div className="form-group">
                  <label htmlFor="pharmacy">Аптека для получения *</label>
                  <select
                    id="pharmacy"
                    value={selectedPharmacy}
                    onChange={(e) => setSelectedPharmacy(e.target.value)}
                    required
                    disabled={submitting}
                  >
                    <option value="">Выберите аптеку</option>
                    {pharmacies.map(pharmacy => (
                      <option key={pharmacy.id} value={pharmacy.id}>
                        {pharmacy.name} - {pharmacy.address}
                      </option>
                    ))}
                  </select>
                </div>

                {/* Contact Info for Guests */}
                {!isAuthenticated && (
                  <>
                    <div className="form-group">
                      <label htmlFor="firstName">Имя *</label>
                      <input
                        type="text"
                        id="firstName"
                        value={firstName}
                        onChange={(e) => setFirstName(e.target.value)}
                        required
                        disabled={submitting}
                        placeholder="Иван"
                      />
                    </div>

                    <div className="form-group">
                      <label htmlFor="lastName">Фамилия *</label>
                      <input
                        type="text"
                        id="lastName"
                        value={lastName}
                        onChange={(e) => setLastName(e.target.value)}
                        required
                        disabled={submitting}
                        placeholder="Иванов"
                      />
                    </div>

                    <div className="form-group">
                      <label htmlFor="phone">Телефон *</label>
                      <input
                        type="tel"
                        id="phone"
                        value={phone}
                        onChange={(e) => setPhone(e.target.value)}
                        required
                        disabled={submitting}
                        placeholder="+79991234567 или 89991234567"
                      />
                      <small>Формат: +7XXXXXXXXXX или 8XXXXXXXXXX</small>
                    </div>

                    <div className="form-group">
                      <label htmlFor="email">Email *</label>
                      <input
                        type="email"
                        id="email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        required
                        disabled={submitting}
                        placeholder="example@mail.ru"
                      />
                    </div>
                  </>
                )}

                {isAuthenticated && (
                  <div className="checkout-info">
                    Контактные данные будут взяты из вашего профиля
                  </div>
                )}

                <div className="checkout-actions">
                  <button
                    type="button"
                    className="btn btn--secondary"
                    onClick={() => navigate('/cart')}
                    disabled={submitting}
                  >
                    Назад в корзину
                  </button>
                  <button
                    type="submit"
                    className="btn btn--primary"
                    disabled={submitting}
                  >
                    {submitting ? 'Оформление...' : 'Оформить заказ'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    </>
  );
};

export default Checkout;
