import { useState, useCallback } from 'react';
import adminApi from '../../../api/adminService';

export const useUsers = () => {
  const [users, setUsers] = useState([]);
  const [roles, setRoles] = useState([]);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [filters, setFilters] = useState({
    email: '',
    isDeleted: '',
    role: '',
    name: ''
  });

  const loadUsers = useCallback(async (pageNum = 0) => {
    setLoading(true);
    try {
      const data = await adminApi.getUsers(pageNum, 20, filters);
      setUsers(data.content || []);
      setTotalPages(data.totalPages || 0);
      setPage(pageNum);
    } catch (err) {
      console.error('[Admin Users] Error:', err);
    } finally {
      setLoading(false);
    }
  }, [filters]);

  const loadRoles = useCallback(async () => {
    try {
      const data = await adminApi.getRoles();
      setRoles(data || []);
    } catch (err) {
      console.error('[Admin Users] Error loading roles:', err);
    }
  }, []);

  const addRole = async (userId, roleId) => {
    try {
      await adminApi.assignRole(userId, roleId);
      await loadUsers(page);
    } catch (err) {
      console.error('Error adding role:', err);
      alert('Ошибка добавления роли');
    }
  };

  const removeRole = async (userId, roleId) => {
    if (!window.confirm('Удалить эту роль у пользователя?')) return;
    try {
      await adminApi.removeRole(userId, roleId);
      await loadUsers(page);
    } catch (err) {
      console.error('Error removing role:', err);
      alert('Ошибка удаления роли');
    }
  };

  const deleteUser = async (userId) => {
    if (!window.confirm('Удалить пользователя?')) return;
    try {
      await adminApi.deleteUser(userId);
      await loadUsers(page);
    } catch (err) {
      console.error('Error deleting user:', err);
      alert('Ошибка удаления пользователя');
    }
  };

  const restoreUser = async (userId) => {
    try {
      await adminApi.restoreUser(userId);
      await loadUsers(page);
    } catch (err) {
      console.error('Error restoring user:', err);
      alert('Ошибка восстановления пользователя');
    }
  };

  const resetFilters = () => {
    setFilters({ email: '', isDeleted: '', role: '', name: '' });
  };

  return {
    users,
    roles,
    loading,
    page,
    totalPages,
    filters,
    setFilters,
    loadUsers,
    loadRoles,
    addRole,
    removeRole,
    deleteUser,
    restoreUser,
    resetFilters
  };
};