import React, { useEffect } from 'react';
import { usePharmacies } from '../hooks';

const PharmaciesTab = () => {
  const {
    pharmacies, loading, error,
    showForm, form, setForm, editingId,
    loadData, openCreate, openEdit, closeForm, handleSubmit, handleDelete
  } = usePharmacies();

  useEffect(() => {
    loadData();
  }, [loadData]);

  return (
    <div className="admin-content">
      {error && <div className="admin-error">{error}</div>}

      <button className="admin-btn admin-btn--primary" onClick={openCreate}>
        + Добавить аптеку
      </button>

      {loading ? (
        <div className="admin-loading">Загрузка...</div>
      ) : pharmacies.length === 0 ? (
        <div className="admin-empty">Аптеки не найдены</div>
      ) : (
        <div className="admin-table">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Название</th>
                <th>Адрес</th>
                <th>Телефон</th>
                <th>Статус</th>
                <th>Действия</th>
              </tr>
            </thead>
            <tbody>
              {pharmacies.map(p => (
                <tr key={p.id}>
                  <td>#{p.id}</td>
                  <td>{p.name}</td>
                  <td>{p.address}</td>
                  <td>{p.phone || '—'}</td>
                  <td>
                    <span className={`admin-badge ${p.isActive ? 'admin-badge--active' : 'admin-badge--inactive'}`}>
                      {p.isActive ? 'Активна' : 'Неактивна'}
                    </span>
                  </td>
                  <td>
                    <button className="admin-btn admin-btn--small" onClick={() => openEdit(p)}>Редактировать</button>
                    <button className="admin-btn admin-btn--small admin-btn--danger" onClick={() => handleDelete(p.id)}>Удалить</button>
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
            <h3>{editingId ? 'Редактировать' : 'Добавить'} аптеку</h3>

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
                <label>Телефон</label>
                <input
                  type="text"
                  value={form.phone}
                  onChange={e => setForm({ ...form, phone: e.target.value })}
                />
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

            <label className="admin-form__checkbox-label">
              <input
                type="checkbox"
                checked={form.isActive}
                onChange={e => setForm({ ...form, isActive: e.target.checked })}
              />
              Аптека активна
            </label>

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

export default PharmaciesTab;
