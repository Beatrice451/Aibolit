import React, { useEffect } from 'react';
import { useStocks } from '../hooks';

const StocksTab = () => {
  const {
    stocks, loading, error,
    showForm, form, setForm,
    editingProductId, editingWarehouseId,
    loadData, openCreate, openEdit, closeForm, handleSubmit, handleDelete,
    getProductName, getWarehouseName
  } = useStocks();

  useEffect(() => {
    loadData();
  }, [loadData]);

  return (
    <div className="admin-content">
      {error && <div className="admin-error">{error}</div>}

      <button className="admin-btn admin-btn--primary" onClick={openCreate}>
        + Добавить остаток
      </button>

      {loading ? (
        <div className="admin-loading">Загрузка...</div>
      ) : stocks.length === 0 ? (
        <div className="admin-empty">Остатки не найдены</div>
      ) : (
        <div className="admin-table">
          <table>
            <thead>
              <tr>
                <th>Товар</th>
                <th>Склад</th>
                <th>Количество</th>
                <th>Зарезервировано</th>
                <th>Доступно</th>
                <th>Действия</th>
              </tr>
            </thead>
            <tbody>
              {stocks.map(s => (
                <tr key={`${s.productId}-${s.warehouseId}`}>
                  <td>{getProductName(s.productId)}</td>
                  <td>{getWarehouseName(s.warehouseId)}</td>
                  <td>{s.quantity}</td>
                  <td>{s.reserved}</td>
                  <td>{Math.max(s.quantity - s.reserved, 0)}</td>
                  <td>
                    <button className="admin-btn admin-btn--small" onClick={() => openEdit(s)}>Редактировать</button>
                    <button className="admin-btn admin-btn--small admin-btn--danger" onClick={() => handleDelete(s.productId, s.warehouseId)}>Удалить</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {showForm && (
        <div className="admin-form-overlay">
          <form className="admin-form" onSubmit={handleSubmit}>
            <h3>{editingProductId ? 'Редактировать' : 'Добавить'} остаток</h3>

            {!editingProductId && (
              <div className="admin-form__group">
                <label>ID товара *</label>
                <input
                  type="number"
                  value={form.productId}
                  onChange={e => setForm({ ...form, productId: e.target.value })}
                  required
                  min="1"
                />
              </div>
            )}

            {!editingWarehouseId && (
              <div className="admin-form__group">
                <label>ID склада *</label>
                <input
                  type="number"
                  value={form.warehouseId}
                  onChange={e => setForm({ ...form, warehouseId: e.target.value })}
                  required
                  min="1"
                />
              </div>
            )}

            {editingProductId && editingWarehouseId && (
              <p className="admin-form__info">
                Товар: {getProductName(editingProductId)}<br />
                Склад: {getWarehouseName(editingWarehouseId)}
              </p>
            )}

            <div className="admin-form__group">
              <label>Количество *</label>
              <input
                type="number"
                value={form.quantity}
                onChange={e => setForm({ ...form, quantity: e.target.value })}
                required
                min="0"
              />
            </div>

            <div className="admin-form__actions">
              <button type="submit" className="admin-btn admin-btn--primary">
                {editingProductId ? 'Сохранить' : 'Создать'}
              </button>
              <button type="button" className="admin-btn" onClick={closeForm}>
                Отмена
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
};

export default StocksTab;
