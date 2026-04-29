import React, { useState } from 'react';
import Modal from '../../../components/Modal';
import adminApi from '../../../api/adminService';

const PickupCodeModal = ({ isOpen, onClose, onOrderCompleted }) => {
  const [pickupCode, setPickupCode] = useState('');
  const [verifying, setVerifying] = useState(false);
  const [verificationResult, setVerificationResult] = useState(null);
  const [orderDetails, setOrderDetails] = useState(null);
  const [error, setError] = useState(null);
  const [completing, setCompleting] = useState(false);

  const handleVerify = async () => {
    if (!pickupCode || pickupCode.trim().length === 0) {
      setError('Введите код получения');
      return;
    }

    setVerifying(true);
    setError(null);
    setVerificationResult(null);
    setOrderDetails(null);

    try {
      const result = await adminApi.verifyPickupCode(pickupCode.trim());
      console.log('[PickupCodeModal] Verification result:', result);
      setVerificationResult(result);
      
      if (!result.isValid) {
        setError(result.message || 'Код не найден');
      } else {
        // Fetch full order details to show items
        try {
          console.log('[PickupCodeModal] Fetching order details for ID:', result.orderId);
          const order = await adminApi.getOrderById(result.orderId);
          console.log('[PickupCodeModal] Order details:', order);
          setOrderDetails(order);
        } catch (err) {
          console.error('Error fetching order details:', err);
          // Continue even if order details fail - we have basic info
        }
      }
    } catch (err) {
      console.error('Error verifying pickup code:', err);
      setError('Ошибка проверки кода');
    } finally {
      setVerifying(false);
    }
  };

  const handleComplete = async () => {
    if (!verificationResult || !verificationResult.orderId) return;

    setCompleting(true);
    try {
      await adminApi.updateOrderStatus(verificationResult.orderId, 'COMPLETED');
      onOrderCompleted();
      handleClose();
    } catch (err) {
      console.error('Error completing order:', err);
      setError('Ошибка при выдаче заказа');
    } finally {
      setCompleting(false);
    }
  };

  const handleClose = () => {
    setPickupCode('');
    setVerificationResult(null);
    setOrderDetails(null);
    setError(null);
    onClose();
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter' && !verifying && !verificationResult) {
      handleVerify();
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={handleClose} title="Выдача заказа по коду">
      <div className="pickup-code-modal">
        {!verificationResult ? (
          <>
            <div className="pickup-code-input-group">
              <label htmlFor="pickupCode">Код получения</label>
              <input
                id="pickupCode"
                type="text"
                value={pickupCode}
                onChange={(e) => setPickupCode(e.target.value)}
                onKeyPress={handleKeyPress}
                placeholder="Введите 6-значный код"
                maxLength={6}
                disabled={verifying}
                autoFocus
                className="pickup-code-input"
              />
            </div>

            {error && (
              <div className="pickup-code-error">
                {error}
              </div>
            )}

            <div className="pickup-code-actions">
              <button 
                className="admin-btn" 
                onClick={handleClose}
                disabled={verifying}
              >
                Отмена
              </button>
              <button 
                className="admin-btn admin-btn--primary" 
                onClick={handleVerify}
                disabled={verifying || !pickupCode}
              >
                {verifying ? 'Проверка...' : 'Проверить'}
              </button>
            </div>
          </>
        ) : (
          <>
            {verificationResult.isValid ? (
              <div className="pickup-code-result pickup-code-result--success">
                <div className="pickup-code-result__icon">✓</div>
                <h3>Код действителен</h3>
                
                <div className="pickup-code-order-info">
                  <div className="pickup-code-order-row">
                    <span>Заказ:</span>
                    <strong>#{verificationResult.orderId}</strong>
                  </div>
                  <div className="pickup-code-order-row">
                    <span>Клиент:</span>
                    <strong>{verificationResult.customerName}</strong>
                  </div>
                  <div className="pickup-code-order-row">
                    <span>Аптека:</span>
                    <strong>{verificationResult.pharmacyName}</strong>
                  </div>
                  <div className="pickup-code-order-row">
                    <span>Статус:</span>
                    <strong className="status-ready">Готов к выдаче</strong>
                  </div>
                </div>

                {orderDetails && orderDetails.items && orderDetails.items.length > 0 && (
                  <div className="pickup-code-order-items">
                    <h4>Состав заказа:</h4>
                    <div className="pickup-code-items-list">
                      {orderDetails.items.map((item, index) => (
                        <div key={index} className="pickup-code-item">
                          <span className="pickup-code-item__name">{item.productName}</span>
                          <span className="pickup-code-item__quantity">× {item.quantity}</span>
                          <span className="pickup-code-item__price">{item.priceAtSale * item.quantity} ₽</span>
                        </div>
                      ))}
                    </div>
                    <div className="pickup-code-total">
                      <span>Итого:</span>
                      <strong>{orderDetails.amount?.finalAmount || orderDetails.amount?.total || 0} ₽</strong>
                    </div>
                  </div>
                )}

                <div className="pickup-code-actions">
                  <button 
                    className="admin-btn" 
                    onClick={handleClose}
                    disabled={completing}
                  >
                    Отмена
                  </button>
                  <button 
                    className="admin-btn admin-btn--success" 
                    onClick={handleComplete}
                    disabled={completing}
                  >
                    {completing ? 'Выдача...' : 'Выдать заказ'}
                  </button>
                </div>
              </div>
            ) : (
              <div className="pickup-code-result pickup-code-result--error">
                <div className="pickup-code-result__icon">✗</div>
                <h3>Код недействителен</h3>
                <p>{verificationResult.message}</p>
                
                {verificationResult.orderId && (
                  <div className="pickup-code-order-info">
                    <div className="pickup-code-order-row">
                      <span>Заказ:</span>
                      <strong>#{verificationResult.orderId}</strong>
                    </div>
                    {verificationResult.customerName && (
                      <div className="pickup-code-order-row">
                        <span>Клиент:</span>
                        <strong>{verificationResult.customerName}</strong>
                      </div>
                    )}
                    {verificationResult.currentStatus && (
                      <div className="pickup-code-order-row">
                        <span>Текущий статус:</span>
                        <strong>{verificationResult.currentStatus}</strong>
                      </div>
                    )}
                  </div>
                )}

                <div className="pickup-code-actions">
                  <button 
                    className="admin-btn admin-btn--primary" 
                    onClick={() => {
                      setVerificationResult(null);
                      setPickupCode('');
                      setError(null);
                    }}
                  >
                    Попробовать снова
                  </button>
                </div>
              </div>
            )}
          </>
        )}
      </div>
    </Modal>
  );
};

export default PickupCodeModal;
