import React, { useEffect } from 'react';
import { useWarehouses } from '../hooks';

const WarehousesTab = () => {
  const {
    warehouses, pharmacies, loading, error,
    showForm, form, setForm, editingId,
    loadData, openCreate, openEdit, closeForm, handleSubmit, handleDelete
  } = useWarehouses();

  useEffect(() => {
    loadData();
  }, [loadData]);

  const getPharmacyName = (pharmacyId) => {
    if (!pharmacyId) return 'Общий склад';
    const pharmacy = pharmacies.find(p => p.id === pharmacyId);
    return pharmacy ? pharmacy.name : `Аптека #${pharmacyId}`;
  };

  return (
    <div className="admin-content">
      {error && <div className="admin-error">{error}</div>}

      <button className="admin-btn admin-btn--primary" onClick={openCreate}>
        + Добавить склад
      </button>

      {loading ? (
        <div className="admin-loading">Загрузка...</div>
      ) : warehouses.length === 0 ? (
        <div className="admin-empty">Склады не найдены</div>
      ) : (
        <div className="admin-table">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Название</th>
                <th>Адрес</th>
                <th>Привязка</th>
                <th>Действия</th>
              </tr>
            </thead>
            <tbody>
              {warehouses.map(w => (
                <tr key={w.id}>
                  <td>#{w.id}</td>
                  <td>{w.name}</td>
                  <td>{w.address}</td>
                  <td>
                    <span className={`admin-badge ${w.shared ? 'admin-badge--shared' : 'admin-badge--pharmacy'}`}>
                      {w.shared ? 'Общий' : getPharmacyName(w.pharmacyId)}
                    </span>
                  </td>
                  <td>
                    <button className="admin-btn admin-btn--small" onClick={() => openEdit(w)}>Редактировать</button>
                    <button className="admin-btn admin-btn--small admin-btn--danger" onClick={() => handleDelete(w.id)}>Удалить</button>
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
            <h3>{editingId ? 'Редактировать' : 'Добавить'} склад</h3>

            <div className="admin-form__row">
              <div className="admin-form__group">
                <label>Название *</label>
                <input
                  type="text"
                  value={form.name}
                  onChange={e => setForm({ ...form, name: e.target.value })}
                  required
                />
              </div>
              <div className="admin-form__group">
                <label>Привязка к аптеке</label>
                <select
                  value={form.pharmacyId}
                  onChange={e => setForm({ ...form, pharmacyId: e.target.value })}
                >
                  <option value="">— Общий склад —</option>
                  {pharmacies.map(p => (
                    <option key={p.id} value={p.id}>
                      {p.name}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            <div className="admin-form__group">
              <label>Адрес *</label>
              <input
                type="text"
                value={form.address}
                onChange={e => setForm({ ...form, address: e.target.value })}
                required
              />
            </div>

            <div className="admin-form__actions">
              <button type="submit" className="admin-btn admin-btn--primary">
                {editingId ? 'Сохранить' : 'Создать'}
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

export default WarehousesTab;
