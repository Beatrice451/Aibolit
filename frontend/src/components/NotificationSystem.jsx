import React, { useState, useEffect, useCallback } from 'react';
import { FaExclamationTriangle, FaCheckCircle, FaInfoCircle } from 'react-icons/fa';

let notificationId = 0;
let addNotificationCallback = null;

export const showNotification = (message, type = 'info') => {
  if (addNotificationCallback) {
    addNotificationCallback(message, type);
  }
};

const NotificationSystem = () => {
  const [notifications, setNotifications] = useState([]);

  const addNotification = useCallback((message, type) => {
    const id = ++notificationId;
    const notification = { id, message, type, removing: false };
    
    setNotifications(prev => {
      // Keep only last 3 notifications
      const updated = [...prev, notification];
      return updated.slice(-3);
    });

    // Start removal animation after 4.7 seconds
    setTimeout(() => {
      setNotifications(prev => 
        prev.map(n => n.id === id ? { ...n, removing: true } : n)
      );
    }, 4700);

    // Actually remove after animation completes (300ms animation + 300ms for flexbox transition)
    setTimeout(() => {
      setNotifications(prev => prev.filter(n => n.id !== id));
    }, 5300);
  }, []);

  useEffect(() => {
    addNotificationCallback = addNotification;
    return () => {
      addNotificationCallback = null;
    };
  }, [addNotification]);

  return (
    <div className="notification-container">
      {notifications.map((notification) => (
        <div
          key={notification.id}
          className={`notification notification--${notification.type} ${notification.removing ? 'notification--removing' : ''}`}
        >
          <div className="notification__icon">
            {notification.type === 'error' && <FaExclamationTriangle />}
            {notification.type === 'success' && <FaCheckCircle />}
            {notification.type === 'info' && <FaInfoCircle />}
            {notification.type === 'warning' && <FaExclamationTriangle />}
          </div>
          <div className="notification__message">{notification.message}</div>
        </div>
      ))}
    </div>
  );
};

export default NotificationSystem;
