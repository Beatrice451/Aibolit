import { useState, useCallback } from 'react';
import adminApi from '../../../api/adminService';

export const useStocks = () => {
  const [stocks, setStocks] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ productId: '', warehouseId: '', quantity: '' });
  const [editingProductId, setEditingProductId] = useState(null);
  const [editingWarehouseId, setEditingWarehouseId] = useState(null);
  const [products, setProducts] = useState([]);
  const [warehouses, setWarehouses] = useState([]);

  const loadData = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [stockData, warehouseData] = await Promise.all([
        adminApi.getStocks(),
        adminApi.getWarehouses()
      ]);
      setStocks(Array.isArray(stockData) ? stockData : []);
      setWarehouses(Array.isArray(warehouseData) ? warehouseData : []);

      const productIds = [...new Set((Array.isArray(stockData) ? stockData : []).map(s => s.productId))];
      if (productIds.length > 0) {
        const productPromises = productIds.map(id =>
          adminApi.getProducts(0, 1, { id }).catch(() => null)
        );
        const productResults = await Promise.all(productPromises);
        const flatProducts = productResults
          .filter(Boolean)
          .flatMap(r => r.content || []);
        setProducts(flatProducts);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Ошибка загрузки остатков');
    } finally {
      setLoading(false);
    }
  }, []);

  const openCreate = () => {
    setEditingProductId(null);
    setEditingWarehouseId(null);
    setForm({ productId: '', warehouseId: '', quantity: '' });
    setShowForm(true);
  };

  const openEdit = (stock) => {
    setEditingProductId(stock.productId);
    setEditingWarehouseId(stock.warehouseId);
    setForm({
      productId: stock.productId.toString(),
      warehouseId: stock.warehouseId.toString(),
      quantity: stock.quantity.toString()
    });
    setShowForm(true);
  };

  const closeForm = () => {
    setShowForm(false);
    setEditingProductId(null);
    setEditingWarehouseId(null);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.productId || !form.warehouseId || form.quantity === '') return;

    const quantity = parseInt(form.quantity);
    if (isNaN(quantity) || quantity < 0) return;

    try {
      const payload = {
        productId: parseInt(form.productId),
        warehouseId: parseInt(form.warehouseId),
        quantity
      };

      if (editingProductId && editingWarehouseId) {
        await adminApi.updateStock(editingProductId, editingWarehouseId, payload);
      } else {
        await adminApi.createStock(payload);
      }

      closeForm();
      await loadData();
    } catch (err) {
      setError(err.response?.data?.message || 'Ошибка сохранения');
    }
  };

  const handleDelete = async (productId, warehouseId) => {
    if (!window.confirm('Удалить запись об остатке? Это равносильно статусу "уточняется".')) return;
    try {
      await adminApi.deleteStock(productId, warehouseId);
      await loadData();
    } catch (err) {
      setError(err.response?.data?.message || 'Ошибка удаления');
    }
  };

  const getProductName = (productId) => {
    const product = products.find(p => p.id === productId);
    return product ? product.name : `Товар #${productId}`;
  };

  const getWarehouseName = (warehouseId) => {
    const warehouse = warehouses.find(w => w.id === warehouseId);
    return warehouse ? warehouse.name : `Склад #${warehouseId}`;
  };

  return {
    stocks, products, warehouses, loading, error,
    showForm, form, setForm,
    editingProductId, editingWarehouseId,
    loadData, openCreate, openEdit, closeForm, handleSubmit, handleDelete,
    getProductName, getWarehouseName
  };
};
