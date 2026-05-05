import React, { useState } from 'react';
import cartApi from '../api/cartService';
import { FaPills } from 'react-icons/fa';

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

const Tabletblock = ({ title, price, id, imageUrl }) => {
  const [quantity, setQuantity] = useState(1);
  const [adding, setAdding] = useState(false);

  const incrementQuantity = () => {
    setQuantity(prev => prev + 1);
  };

  const decrementQuantity = () => {
    if (quantity > 1) {
      setQuantity(prev => prev - 1);
    }
  };

  const handleAddToCart = async () => {
    setAdding(true);
    try {
      await cartApi.addItem(id, quantity);
      showGlobalNotification(`Добавлено ${quantity} шт. "${title}" в корзину!`, 'success');
      setQuantity(1);
    } catch (err) {
      console.error('Error adding to cart:', err);
      showGlobalNotification('Не удалось добавить в корзину', 'error');
    } finally {
      setAdding(false);
    }
  };

  return (
    <div className="tablet-block">
      <div className="tablet-block__image">
        {imageUrl ? (
          <img 
            src={imageUrl} 
            alt={title} 
            style={{ width: '100%', height: '100%', objectFit: 'contain' }} 
          />
        ) : (
          <span><FaPills /></span>
        )}
      </div>
      <h4 className="tablet-block__title">{title}</h4>
      <div className="tablet-block__price">{price} ₽</div>
      
      <div className="tablet-block__quantity-selector">
        <button 
          className="quantity-btn quantity-btn--minus"
          onClick={decrementQuantity}
          disabled={quantity <= 1}
        >
          -
        </button>
        <span className="quantity-value">{quantity}</span>
        <button 
          className="quantity-btn quantity-btn--plus"
          onClick={incrementQuantity}
        >
          +
        </button>
      </div>
      
      <button 
        className="tablet-block__btn"
        onClick={handleAddToCart}
        disabled={adding}
      >
        {adding ? 'Добавление...' : 'В корзину'}
      </button>
    </div>
  );
};

export default Tabletblock;