import { useState, useCallback } from 'react';
import adminApi from '../../../api/adminService';

export const useProducts = () => {
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const [searchQuery, setSearchQuery] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [editingProduct, setEditingProduct] = useState(null);
  const [form, setForm] = useState({
    name: '',
    categoryId: '',
    price: '',
    description: '',
    manufacturer: '',
    imageUrl: '',
    // Product type
    isMedicine: false,
    // Medicine-specific fields
    dosage: '',
    requiresPrescription: false,
    form: '',
    quantity: ''
  });
  const [uploading, setUploading] = useState(false);

  const loadData = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const filters = searchQuery ? { search: searchQuery } : {};
      const [productsData, categoriesData] = await Promise.all([
        adminApi.getProducts(0, 50, filters).catch(() => ({ content: [] })),
        adminApi.getCategories().catch(() => [])
      ]);
      setProducts(productsData.content || []);
      setCategories(categoriesData || []);
    } catch (err) {
      console.error('[Admin Products] Error:', err);
      setError('Ошибка загрузки данных');
    } finally {
      setLoading(false);
    }
  }, [searchQuery]);

  const handleImageUpload = async (file) => {
    if (!file) return;
    setUploading(true);
    try {
      const result = await adminApi.uploadFile(file);
      let imagePath = null;
      if (typeof result === 'string') {
        imagePath = result;
      } else if (result && typeof result === 'object') {
        imagePath = result.url || result.path || result.imageUrl || result.filename || JSON.stringify(result);
      }
      setForm(prev => ({ ...prev, imageUrl: imagePath }));
    } catch (err) {
      console.error('Upload error:', err);
      alert('Ошибка загрузки изображения');
    } finally {
      setUploading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const data = {
        name: form.name,
        categoryId: form.categoryId ? parseInt(form.categoryId) : null,
        price: parseFloat(form.price),
        description: form.description,
        manufacturer: form.manufacturer,
        imageUrl: form.imageUrl
      };

      // Add medicine-specific fields if it's a medicine
      if (form.isMedicine) {
        data.dosage = parseInt(form.dosage);
        data.requiresPrescription = form.requiresPrescription;
        data.form = form.form;
        data.quantity = parseInt(form.quantity);
      }

      if (editingProduct) {
        await adminApi.updateProduct(editingProduct.id, data);
      } else {
        await adminApi.addProduct(data);
      }

      setShowForm(false);
      setEditingProduct(null);
      setForm({ 
        name: '', 
        categoryId: '', 
        price: '', 
        description: '', 
        manufacturer: '', 
        imageUrl: '',
        isMedicine: false,
        dosage: '',
        requiresPrescription: false,
        form: '',
        quantity: ''
      });
      await loadData();
    } catch (err) {
      setError('Ошибка сохранения товара');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Удалить товар?')) return;
    try {
      await adminApi.deleteProduct(id);
      await loadData();
    } catch (err) {
      console.error(err);
    }
  };

  const openCreate = () => {
    setEditingProduct(null);
    setForm({ 
      name: '', 
      categoryId: '', 
      price: '', 
      description: '', 
      manufacturer: '', 
      imageUrl: '',
      isMedicine: false,
      dosage: '',
      requiresPrescription: false,
      form: '',
      quantity: ''
    });
    setShowForm(true);
  };

  const openEdit = (product) => {
    setEditingProduct(product);
    setForm({
      name: product.name,
      categoryId: product.categoryId?.toString() || '',
      price: product.price?.toString() || '',
      description: product.description || '',
      manufacturer: product.manufacturer || '',
      imageUrl: product.imageUrl || '',
      isMedicine: product.isMedicine || false,
      dosage: product.dosage?.toString() || '',
      requiresPrescription: product.requiresPrescription || false,
      form: product.form || '',
      quantity: product.quantity?.toString() || ''
    });
    setShowForm(true);
  };

  const closeForm = () => {
    setShowForm(false);
    setEditingProduct(null);
    setForm({ name: '', categoryId: '', price: '', description: '', manufacturer: '', imageUrl: '' });
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

  const getImageUrl = (url) => {
    if (!url || typeof url !== 'string') return null;
    if (url.startsWith('http')) return url;
    const cleanUrl = url.startsWith('/') ? url : '/' + url;
    return `/media${cleanUrl}`;
  };

  return {
    products,
    categories,
    loading,
    error,
    searchQuery,
    setSearchQuery,
    showForm,
    editingProduct,
    form,
    setForm,
    uploading,
    loadData,
    handleImageUpload,
    handleSubmit,
    handleDelete,
    openCreate,
    openEdit,
    closeForm,
    flattenCategories,
    getImageUrl
  };
};