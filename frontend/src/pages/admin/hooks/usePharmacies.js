import { useState, useCallback } from 'react';
import adminApi from '../../../api/adminService';

export const usePharmacies = () => {
  const [pharmacies, setPharmacies] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ name: '', address: '', phone: '', isActive: true });
  const [editingId, setEditingId] = useState(null);

  const loadData = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await adminApi.getPharmacies(true);
      setPharmacies(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.response?.data?.message || 'Ошибка загрузки аптек');
    } finally {
      setLoading(false);
    }
  }, []);

  const openCreate = () => {
    setEditingId(null);
    setForm({ name: '', address: '', phone: '', isActive: true });
    setShowForm(true);
  };

  const openEdit = (pharmacy) => {
    setEditingId(pharmacy.id);
    setForm({
      name: pharmacy.name || '',
      address: pharmacy.address || '',
      phone: pharmacy.phone || '',
      isActive: pharmacy.isActive !== false
    });
    setShowForm(true);
  };

  const closeForm = () => {
    setShowForm(false);
    setEditingId(null);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.name.trim() || !form.address.trim()) return;

    try {
      const payload = {
        name: form.name.trim(),
        address: form.address.trim(),
        phone: form.phone.trim() || null,
        isActive: form.isActive
      };

      if (editingId) {
        await adminApi.updatePharmacy(editingId, payload);
      } else {
        await adminApi.createPharmacy(payload);
      }

      closeForm();
      await loadData();
    } catch (err) {
      setError(err.response?.data?.message || 'Ошибка сохранения');
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Деактивировать эту аптеку?')) return;
    try {
      await adminApi.deletePharmacy(id);
      await loadData();
    } catch (err) {
      setError(err.response?.data?.message || 'Ошибка деактивации');
    }
  };

  return {
    pharmacies, loading, error,
    showForm, form, setForm, editingId,
    loadData, openCreate, openEdit, closeForm, handleSubmit, handleDelete
  };
};
