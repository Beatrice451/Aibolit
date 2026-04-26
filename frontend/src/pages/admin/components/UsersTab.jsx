import React, { useEffect } from 'react';
import { useUsers } from '../hooks';
import adminApi from '../../../api/adminService';

const UsersTab = ({ roles = [] }) => {
  const {
    users,
    loading,
    page,
    totalPages,
    filters,
    setFilters,
    loadUsers,
    addRole,
    removeRole,
    deleteUser,
    restoreUser,
    resetFilters
  } = useUsers();

  useEffect(() => {
    loadUsers(0);
  }, [loadUsers]);

  return (
    <div className="admin-content">
      <div className="users-filters">
        <div className="users-filters__row">
          <input
            type="text"
            placeholder="Поиск по имени..."
            value={filters.name}
            onChange={e => setFilters({ ...filters, name: e.target.value })}
          />
          <input
            type="text"
            placeholder="Поиск по email..."
            value={filters.email}
            onChange={e => setFilters({ ...filters, email: e.target.value })}
          />
          <select
            value={filters.isDeleted}
            onChange={e => setFilters({ ...filters, isDeleted: e.target.value })}
          >
            <option value="">Все пользователи</option>
            <option value="false">Активные</option>
            <option value="true">Удалённые</option>
          </select>
          <select
            value={filters.role}
            onChange={e => setFilters({ ...filters, role: e.target.value })}
          >
            <option value="">Все роли</option>
            {roles.map(role => (
              <option key={role.id} value={role.id}>{role.roleName}</option>
            ))}
          </select>
          <button className="admin-btn" onClick={() => loadUsers(0)}>Найти</button>
          <button className="admin-btn" onClick={resetFilters}>Сбросить</button>
        </div>
      </div>

      {loading ? (
        <div className="admin-loading">Загрузка пользователей...</div>
      ) : users.length === 0 ? (
        <div className="admin-empty">Пользователи не найдены</div>
      ) : (
        <>
          <div className="users-table">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Имя</th>
                  <th>Email</th>
                  <th>Телефон</th>
                  <th>Роли</th>
                  <th>Статус</th>
                  <th>Действия</th>
                </tr>
              </thead>
              <tbody>
                {users.map(user => (
                  <tr key={user.id}>
                    <td>#{user.id}</td>
                    <td>{user.firstName} {user.lastName}</td>
                    <td>{user.email}</td>
                    <td>{user.phone || '-'}</td>
                    <td>
                      <div className="user-roles">
                        {user.roles?.map(role => (
                          <span key={role.id} className="user-role">
                            {role.roleName}
                            <button
                              className="user-role__remove"
                              onClick={() => removeRole(user.id, role.id)}
                            >
                              ×
                            </button>
                          </span>
                        ))}
                        {roles.filter(r => !user.roles?.some(ur => ur.id === r.id)).length > 0 && (
                          <select
                            className="user-role-add"
                            value=""
                            onChange={(e) => {
                              if (e.target.value) {
                                addRole(user.id, parseInt(e.target.value));
                              }
                            }}
                          >
                            <option value="">+ роль</option>
                            {roles.filter(r => !user.roles?.some(ur => ur.id === r.id)).map(role => (
                              <option key={role.id} value={role.id}>{role.roleName}</option>
                            ))}
                          </select>
                        )}
                      </div>
                    </td>
                    <td>
                      {user.isDeleted ? (
                        <span className="user-status user-status--deleted">Удалён</span>
                      ) : (
                        <span className="user-status user-status--active">Активен</span>
                      )}
                    </td>
                    <td>
                      {user.isDeleted ? (
                        <button
                          className="admin-btn admin-btn--small"
                          onClick={() => restoreUser(user.id)}
                        >
                          Восстановить
                        </button>
                      ) : (
                        <button
                          className="admin-btn admin-btn--small admin-btn--danger"
                          onClick={() => {
                            if (window.confirm(`Удалить пользователя ${user.firstName} ${user.lastName}?`)) {
                              adminApi.deleteUser(user.id).then(() => loadUsers(page)).catch(err => {
                                console.error('Error deleting user:', err);
                                alert('Ошибка удаления пользователя');
                              });
                            }
                          }}
                        >
                          Удалить
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {totalPages > 1 && (
            <div className="admin-pagination">
              <button
                className="admin-btn"
                disabled={page === 0}
                onClick={() => loadUsers(page - 1)}
              >
                Предыдущая
              </button>
              <span className="admin-pagination__info">
                Страница {page + 1} из {totalPages}
              </span>
              <button
                className="admin-btn"
                disabled={page >= totalPages - 1}
                onClick={() => loadUsers(page + 1)}
              >
                Следующая
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
};

export default UsersTab;