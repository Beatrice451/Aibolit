import { useState, useCallback } from 'react';
import adminApi from '../../../api/adminService';

export const useWarehouses = () => {
  const [warehouses, setWarehouses] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ name: '', address: '', pharmacyId: '' });
  const [editingId, setEditingId] = useState(null);
  const [pharmacies, setPharmacies] = useState([]);

  const loadData = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [warehouseData, pharmacyData] = await Promise.all([
        adminApi.getWarehouses(),
        adminApi.getPharmacies(true)
      ]);
      setWarehouses(Array.isArray(warehouseData) ? warehouseData : []);
      setPharmacies(Array.isArray(pharmacyData) ? pharmacyData : []);
    } catch (err) {
      setError(err.response?.data?.message || 'Ошибка загрузки складов');
    } finally {
      setLoading(false);
    }
  }, []);

  const openCreate = () => {
    setEditingId(null);
    setForm({ name: '', address: '', pharmacyId: '' });
    setShowForm(true);
  };

  const openEdit = (warehouse) => {
    setEditingId(warehouse.id);
    setForm({
      name: warehouse.name || '',
      address: warehouse.address || '',
      pharmacyId: warehouse.pharmacyId ? warehouse.pharmacyId.toString() : ''
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
        pharmacyId: form.pharmacyId ? parseInt(form.pharmacyId) : null
      };

      if (editingId) {
        await adminApi.updateWarehouse(editingId, payload);
      } else {
        await adminApi.createWarehouse(payload);
      }

      closeForm();
      await loadData();
    } catch (err) {
      setError(err.response?.data?.message || 'Ошибка сохранения');
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Удалить этот склад?')) return;
    try {
      await adminApi.deleteWarehouse(id);
      await loadData();
    } catch (err) {
      setError(err.response?.data?.message || 'Ошибка удаления');
    }
  };

  return {
    warehouses, pharmacies, loading, error,
    showForm, form, setForm, editingId,
    loadData, openCreate, openEdit, closeForm, handleSubmit, handleDelete
  };
};
