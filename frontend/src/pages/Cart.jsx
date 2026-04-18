import React, { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import Header from '../components/Header';
import cartApi from '../api/cartService';

const getImageUrl = (imagePath) => {
  if (!imagePath) return null;
  if (imagePath.startsWith('http')) return imagePath;
  // Add /media/ prefix
  return imagePath.startsWith('/') ? imagePath : `/media/${imagePath}`;
};

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

const Cart = () => {
  const [cart, setCart] = useState(null);
  const [loading, setLoading] = useState(true);

  const fetchCart = useCallback(async () => {
    try {
      setLoading(true);
      const data = await cartApi.getCart();
      setCart(data);
    } catch (err) {
      console.error('Error fetching cart:', err);
      showGlobalNotification('Не удалось загрузить корзину', 'error');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchCart();
  }, [fetchCart]);

  const updateQuantity = async (productId, newQuantity) => {
    const oldCart = { ...cart };
    
    if (!cart?.items) return;
    
    setCart(prev => {
      if (!prev?.items) return prev;
      const newItems = prev.items.map(item => 
        item.productId === productId 
          ? { ...item, quantity: newQuantity < 1 ? 0 : newQuantity }
          : item
      ).filter(item => item.quantity > 0);
      
      const totalPrice = newItems.reduce((sum, i) => sum + i.price * i.quantity, 0);
      const totalItems = newItems.reduce((sum, i) => sum + i.quantity, 0);
      
      return { ...prev, items: newItems, totalPrice, totalItems };
    });
    
    try {
      if (newQuantity < 1) {
        await cartApi.removeItem(productId);
      } else {
        await cartApi.updateItem(productId, newQuantity);
      }
    } catch (err) {
      console.error('Error updating quantity:', err);
      setCart(oldCart);
    }
  };

  const removeItem = async (productId) => {
    const oldCart = { ...cart };
    if (!cart?.items) return;
    
    setCart(prev => {
      if (!prev?.items) return prev;
      const newItems = prev.items.filter(item => item.productId !== productId);
      const totalPrice = newItems.reduce((sum, i) => sum + i.price * i.quantity, 0);
      const totalItems = newItems.reduce((sum, i) => sum + i.quantity, 0);
      return { ...prev, items: newItems, totalPrice, totalItems };
    });
    
    try {
      await cartApi.removeItem(productId);
      showGlobalNotification('Товар удалён из корзины');
    } catch (err) {
      console.error('Error removing item:', err);
      setCart(oldCart);
    }
  };

  const clearCart = async () => {
    const oldCart = { ...cart };
    setCart(prev => prev ? { ...prev, items: [], totalPrice: 0, totalItems: 0 } : prev);
    
    try {
      await cartApi.clearCart();
      showGlobalNotification('Корзина очищена');
    } catch (err) {
      console.error('Error clearing cart:', err);
      setCart(oldCart);
    }
  };

  if (loading) {
    return (
      <>
        <Header />
        <div className="cart-page">
          <div className="container">Загрузка...</div>
        </div>
      </>
    );
  }

  if (!cart || !cart.items || cart.items.length === 0) {
    return (
      <>
        <Header />
        <div className="cart-page">
          <div className="container">
            <div className="cart-empty">
              <div className="cart-empty__icon">🛒</div>
              <h2 className="cart-empty__title">Корзина пуста</h2>
              <p className="cart-empty__text">Добавьте товары в корзину, чтобы оформить заказ</p>
              <Link to="/" className="cart-empty__btn">
                Перейти в каталог
              </Link>
            </div>
          </div>
        </div>
      </>
    );
  }

  return (
    <>
      <Header />
      <div className="cart-page">
        <div className="container">
          <h2 className="cart-page__title">Корзина</h2>
          
          <div className="cart-content">
            <div className="cart-items">
              {cart.items.map(item => (
                <div key={item.productId} className="cart-item">
                  <div className="cart-item__image">
                    {item.productImage ? (
                      <img 
                        src={getImageUrl(item.productImage)} 
                        alt={item.productName}
                        onError={(e) => { e.target.style.display = 'none'; e.target.nextSibling.style.display = 'flex'; }}
                      />
                    ) : null}
                    <span className="cart-item__placeholder" style={item.productImage ? { display: 'none' } : {}}>💊</span>
                  </div>
                  <div className="cart-item__info">
                    <h3 className="cart-item__title">{item.productName}</h3>
                    <div className="cart-item__price">{item.price} ₽</div>
                  </div>
                  <div className="cart-item__controls">
                    <button 
                      className="cart-item__btn cart-item__btn--decr"
                      onClick={() => updateQuantity(item.productId, item.quantity - 1)}
                    >
                      -
                    </button>
                    <span className="cart-item__quantity">{item.quantity}</span>
                    <button 
                      className="cart-item__btn cart-item__btn--incr"
                      onClick={() => updateQuantity(item.productId, item.quantity + 1)}
                    >
                      +
                    </button>
                    <button 
                      className="cart-item__remove"
                      onClick={() => removeItem(item.productId)}
                    >
                      🗑️
                    </button>
                  </div>
                </div>
              ))}
            </div>
            
            <div className="cart-summary">
              <div className="cart-summary__title">Итого</div>
              <div className="cart-summary__total">
                <span>Сумма:</span>
                <span className="cart-summary__price">{cart.totalPrice} ₽</span>
              </div>
              <button className="cart-summary__checkout">
                Оформить заказ
              </button>
              <button className="cart-summary__clear" onClick={clearCart}>
                Очистить корзину
              </button>
            </div>
          </div>
        </div>
      </div>
    </>
  );
};

export default Cart;