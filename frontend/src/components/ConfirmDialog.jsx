import React from 'react';
import Modal from './Modal';

const ConfirmDialog = ({ isOpen, message, onConfirm, onCancel, confirmText = 'Удалить', cancelText = 'Отмена' }) => {
  return (
    <Modal isOpen={isOpen} onClose={onCancel} title="Подтверждение">
      <p className="confirm-dialog__message">{message}</p>
      <div className="confirm-dialog__actions">
        <button className="confirm-dialog__btn confirm-dialog__btn--confirm" onClick={onConfirm}>
          {confirmText}
        </button>
        <button className="confirm-dialog__btn confirm-dialog__btn--cancel" onClick={onCancel}>
          {cancelText}
        </button>
      </div>
    </Modal>
  );
};

export default ConfirmDialog;
