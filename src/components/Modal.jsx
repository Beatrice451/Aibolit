import React, { useState } from 'react';

const Modal = ({ isOpen, onClose, children, title }) => {
  if (!isOpen) return null;
  
  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={e => e.stopPropagation()}>
        {title && <div className="modal-header">
          <h3>{title}</h3>
          <button className="modal-close" onClick={onClose}>&times;</button>
        </div>}
        <div className="modal-body">
          {children}
        </div>
      </div>
    </div>
  );
};

export const useNotification = () => {
  const [notification, setNotification] = useState(null);

  const showNotification = (message, type = 'success') => {
    setNotification({ message, type });
    setTimeout(() => setNotification(null), 3000);
  };

  const NotificationComponent = () => {
    if (!notification) return null;
    
    return (
      <div className={`notification notification--${notification.type}`}>
        {notification.message}
      </div>
    );
  };

  return { showNotification, NotificationComponent };
};

export default Modal;