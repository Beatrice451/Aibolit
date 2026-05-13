import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
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
  const [adding, setAdding] = useState(false);
  const navigate = useNavigate();

  const handleAddToCart = async (e) => {
    e.stopPropagation();
    setAdding(true);
    try {
      await cartApi.addItem(id, 1);
      showGlobalNotification(`"${title}" добавлен в корзину!`, 'success');
    } catch (err) {
      console.error('Error adding to cart:', err);
      showGlobalNotification('Не удалось добавить в корзину', 'error');
    } finally {
      setAdding(false);
    }
  };

  const handleCardClick = () => {
    navigate(`/product/${id}`);
  };

  return (
    <div className="tablet-block" onClick={handleCardClick}>
      <div className="tablet-block__image">
        {imageUrl ? (
          <img 
            src={imageUrl} 
            alt={title}
          />
        ) : (
          <div className="tablet-block__image-placeholder">
            <FaPills />
          </div>
        )}
      </div>
      <div className="tablet-block__content">
        <h4 className="tablet-block__title">{title}</h4>
        <div className="tablet-block__footer">
          <div className="tablet-block__price">{Number(price).toFixed(2)} ₽</div>
          <button 
            className="tablet-block__btn"
            onClick={handleAddToCart}
            disabled={adding}
          >
            {adding ? 'Добавление...' : 'В корзину'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default Tabletblock;