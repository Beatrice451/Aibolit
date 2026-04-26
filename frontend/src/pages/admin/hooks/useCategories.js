import { useState, useCallback } from 'react';
import adminApi from '../../../api/adminService';

export const useCategories = () => {
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ name: '', parentId: '' });
  const [expanded, setExpanded] = useState({});

  const loadCategories = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await adminApi.getCategories();
      setCategories(data || []);
    } catch (err) {
      console.error('[Admin Categories] Error:', err);
      setError('Ошибка загрузки категорий');
    } finally {
      setLoading(false);
    }
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const data = {
        name: form.name,
        parentId: form.parentId ? parseInt(form.parentId) : null
      };
      await adminApi.addCategory(data);
      setShowForm(false);
      setForm({ name: '', parentId: '' });
      await loadCategories();
    } catch (err) {
      setError('Ошибка сохранения категории');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const toggle = (categoryId) => {
    setExpanded(prev => ({ ...prev, [categoryId]: !prev[categoryId] }));
  };

  const flattenCategories = (nodes, depth = 0, parentPath = '') => {
    let result = [];
    nodes.forEach(node => {
      const path = parentPath ? `${parentPath} > ${node.name}` : node.name;
      result.push({ id: node.id, name: node.name, fullPath: path });
      if (node.children?.length) {
        result = result.concat(flattenCategories(node.children, depth + 1, path));
      }
    });
    return result;
  };

  const openCreate = () => {
    setForm({ name: '', parentId: '' });
    setShowForm(true);
  };

  const closeForm = () => {
    setShowForm(false);
    setForm({ name: '', parentId: '' });
  };

  return {
    categories,
    loading,
    error,
    showForm,
    form,
    setForm,
    expanded,
    loadCategories,
    handleSubmit,
    toggle,
    flattenCategories,
    openCreate,
    closeForm
  };
};