import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import adminApi from '../api/adminService';
import authApi from '../api/authService';

const API_BASE_URL = 'http://localhost:1488';

const AdminPage = () => {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('products');
  const [products, setProducts] = useState([]);
  const [categoriesList, setCategoriesList] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [accessDenied, setAccessDenied] = useState(false);
  const [accessDeniedReason, setAccessDeniedReason] = useState(null);
  const [checkingAccess, setCheckingAccess] = useState(true);
  
  const [showProductForm, setShowProductForm] = useState(false);
  const [editingProduct, setEditingProduct] = useState(null);
  const [productForm, setProductForm] = useState({
    name: '',
    categoryId: '',
    price: '',
    description: '',
    manufacturer: '',
    imageUrl: ''
  });

  const [showCategoryForm, setShowCategoryForm] = useState(false);
  const [categoryForm, setCategoryForm] = useState({ name: '', parentId: '' });

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
      setCategoriesList(categoriesData || []);
    } catch (err) {
      console.error('[Admin] Error:', err);
    } finally {
      setLoading(false);
    }
  }, [searchQuery]);

  const checkAccessAndLoad = useCallback(async () => {
    const token = localStorage.getItem('accessToken');
    if (!token) {
      setAccessDenied(true);
      setAccessDeniedReason('no-auth');
      setCheckingAccess(false);
      return;
    }
    
    const adminStatus = await authApi.isAdmin();
    if (!adminStatus) {
      setAccessDenied(true);
      setAccessDeniedReason('not-admin');
      setCheckingAccess(false);
      return;
    }
    
    setCheckingAccess(false);
    loadData();
  }, [navigate, loadData]);

  useEffect(() => {
    checkAccessAndLoad();
  }, [checkAccessAndLoad]);

  useEffect(() => {
    if (!checkingAccess && !accessDenied) {
      loadData();
    }
  }, [loadData, checkingAccess, accessDenied, activeTab]);

  if (accessDenied) {
    return (
      <>
        <Header />
        <div className="admin-forbidden">
          <div className="admin-forbidden__content">
            <img 
              src="/sticker.webp" 
              alt="Доступ запрещён" 
              className="admin-forbidden__image"
            />
            <h1 className="admin-forbidden__code">401</h1>
            <p className="admin-forbidden__text">
              {accessDeniedReason === 'no-auth' 
                ? 'Требуется авторизация' 
                : 'Доступ запрещён'}
            </p>
          </div>
        </div>
      </>
    );
  }

  if (checkingAccess) {
    return (
      <>
        <Header />
        <div className="admin-page">
          <div className="container">Проверка прав доступа...</div>
        </div>
      </>
    );
  }

  const handleSearch = (e) => {
    e.preventDefault();
    loadData();
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

  const handleImageUpload = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;
    
    setUploading(true);
    try {
      const result = await adminApi.uploadFile(file);
      console.log('[Admin] Upload result:', result);
      // Handle different response formats
      let imagePath = null;
      if (typeof result === 'string') {
        imagePath = result;
      } else if (result && typeof result === 'object') {
        imagePath = result.url || result.path || result.imageUrl || result.filename || JSON.stringify(result);
      }
      console.log('[Admin] Image path:', imagePath);
      setProductForm(prev => ({ ...prev, imageUrl: imagePath }));
    } catch (err) {
      console.error('Upload error:', err);
      alert('Ошибка загрузки изображения');
    } finally {
      setUploading(false);
    }
  };

  const handleProductSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const data = {
        name: productForm.name,
        categoryId: parseInt(productForm.categoryId),
        price: parseFloat(productForm.price),
        description: productForm.description,
        manufacturer: productForm.manufacturer,
        imageUrl: productForm.imageUrl
      };
      
      if (editingProduct) {
        await adminApi.updateProduct(editingProduct.id, data);
      } else {
        await adminApi.addProduct(data);
      }
      
      setShowProductForm(false);
      setEditingProduct(null);
      setProductForm({ name: '', categoryId: '', price: '', description: '', manufacturer: '', imageUrl: '' });
      await loadData();
    } catch (err) {
      setError('Ошибка сохранения товара');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteProduct = async (id) => {
    if (!window.confirm('Удалить товар?')) return;
    
    try {
      await adminApi.deleteProduct(id);
      await loadData();
    } catch (err) {
      console.error(err);
    }
  };

  const handleCategorySubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const data = {
        name: categoryForm.name,
        parentId: categoryForm.parentId ? parseInt(categoryForm.parentId) : null
      };
      await adminApi.addCategory(data);
      setShowCategoryForm(false);
      setCategoryForm({ name: '', parentId: '' });
      await loadData();
    } catch (err) {
      setError('Ошибка сохранения категории');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const editProduct = (product) => {
    setEditingProduct(product);
    setProductForm({
      name: product.name,
      categoryId: product.categoryId?.toString() || '',
      price: product.price?.toString() || '',
      description: product.description || '',
      manufacturer: product.manufacturer || '',
      imageUrl: product.imageUrl || ''
    });
    setShowProductForm(true);
  };

  const getImageUrl = (url) => {
    if (!url || typeof url !== 'string') return null;
    if (url.startsWith('http')) return url;
    // Keep original path as-is
    const cleanUrl = url.startsWith('/') ? url : '/' + url;
    return `${API_BASE_URL}${cleanUrl}`;
  };

  const flatCategories = flattenCategories(categoriesList);

  return (
    <>
      <Header />
      <div className="admin-page">
        <div className="container">
          <h1>Админ-панель</h1>
          
          <div className="admin-tabs">
            <button 
              className={`admin-tab ${activeTab === 'products' ? 'active' : ''}`}
              onClick={() => setActiveTab('products')}
            >
              Товары
            </button>
            <button 
              className={`admin-tab ${activeTab === 'categories' ? 'active' : ''}`}
              onClick={() => setActiveTab('categories')}
            >
              Категории
            </button>
          </div>

          {error && <div className="admin-error">{error}</div>}

          {activeTab === 'products' && (
            <div className="admin-content">
              <form className="admin-search" onSubmit={handleSearch}>
                <input 
                  type="text" 
                  placeholder="Поиск по названию..." 
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                />
                <button type="submit" className="admin-btn">Найти</button>
              </form>
              
              <button className="admin-btn admin-btn--primary" onClick={() => {
                setEditingProduct(null);
                setProductForm({ name: '', categoryId: '', price: '', description: '', manufacturer: '', imageUrl: '' });
                setShowProductForm(true);
              }}>
                + Добавить товар
              </button>
              
              {showProductForm && (
                <div className="admin-form-overlay">
                  <form className="admin-form" onSubmit={handleProductSubmit}>
                    <h3>{editingProduct ? 'Редактировать' : 'Добавить'} товар</h3>
                    
                    <div className="admin-form__group">
                      <label>Название *</label>
                      <input 
                        type="text" 
                        value={productForm.name}
                        onChange={e => setProductForm({...productForm, name: e.target.value})}
                        required 
                      />
                    </div>
                    
                    <div className="admin-form__group">
                      <label>Категория *</label>
                      <select 
                        value={productForm.categoryId}
                        onChange={e => setProductForm({...productForm, categoryId: e.target.value})}
                        required
                      >
                        <option value="">Выберите категорию</option>
                        {flatCategories.map(cat => (
                          <option key={cat.id} value={cat.id}>{cat.fullPath}</option>
                        ))}
                      </select>
                    </div>
                    
                    <div className="admin-form__group">
                      <label>Цена *</label>
                      <input 
                        type="number" 
                        step="0.01"
                        value={productForm.price}
                        onChange={e => setProductForm({...productForm, price: e.target.value})}
                        required 
                      />
                    </div>
                    
                    <div className="admin-form__group">
                      <label>Описание</label>
                      <textarea 
                        value={productForm.description}
                        onChange={e => setProductForm({...productForm, description: e.target.value})}
                      />
                    </div>
                    
                    <div className="admin-form__group">
                      <label>Производитель</label>
                      <input 
                        type="text" 
                        value={productForm.manufacturer}
                        onChange={e => setProductForm({...productForm, manufacturer: e.target.value})}
                      />
                    </div>
                    
                    <div className="admin-form__group">
                      <label>Изображение</label>
                      {productForm.imageUrl && (
                        <div className="admin-form__preview">
                          <img src={getImageUrl(productForm.imageUrl)} alt="Preview" />
                        </div>
                      )}
                      <input 
                        type="file" 
                        accept="image/*"
                        onChange={handleImageUpload}
                        disabled={uploading}
                      />
                      {uploading && <span className="admin-form__uploading">Загрузка...</span>}
                    </div>
                    
                    <div className="admin-form__actions">
                      <button type="submit" className="admin-btn admin-btn--primary" disabled={loading || uploading}>
                        Сохранить
                      </button>
                      <button type="button" className="admin-btn" onClick={() => {
                        setShowProductForm(false);
                        setEditingProduct(null);
                        setProductForm({ name: '', categoryId: '', price: '', description: '', manufacturer: '', imageUrl: '' });
                      }}>
                        Отмена
                      </button>
                    </div>
                  </form>
                </div>
              )}
              
              <div className="admin-table">
                <table>
                  <thead>
                    <tr>
                      <th>Изобр.</th>
                      <th>Название</th>
                      <th>Цена</th>
                      <th>Категория</th>
                      <th>Действия</th>
                    </tr>
                  </thead>
                  <tbody>
                    {products.map(product => (
                      <tr key={product.id}>
                        <td>
                          {product.imageUrl ? (
                            <img src={getImageUrl(product.imageUrl)} alt={product.name} className="admin-table__img" />
                          ) : '💊'}
                        </td>
                        <td>{product.name}</td>
                        <td>{product.price} ₽</td>
                        <td>{product.categoryName}</td>
                        <td>
                          <button className="admin-btn admin-btn--small" onClick={() => editProduct(product)}>Ред</button>
                          <button className="admin-btn admin-btn--small admin-btn--danger" onClick={() => handleDeleteProduct(product.id)}>Удал</button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {activeTab === 'categories' && (
            <div className="admin-content">
              <button className="admin-btn admin-btn--primary" onClick={() => setShowCategoryForm(true)}>
                + Добавить категорию
              </button>
              
              {showCategoryForm && (
                <div className="admin-form-overlay">
                  <form className="admin-form" onSubmit={handleCategorySubmit}>
                    <h3>Добавить категорию</h3>
                    
                    <div className="admin-form__group">
                      <label>Название *</label>
                      <input 
                        type="text" 
                        value={categoryForm.name}
                        onChange={e => setCategoryForm({...categoryForm, name: e.target.value})}
                        required 
                      />
                    </div>
                    
                    <div className="admin-form__group">
                      <label>Родительская категория</label>
                      <select 
                        value={categoryForm.parentId}
                        onChange={e => setCategoryForm({...categoryForm, parentId: e.target.value})}
                      >
                        <option value="">Нет (корневая)</option>
                        {flatCategories.map(cat => (
                          <option key={cat.id} value={cat.id}>{cat.fullPath}</option>
                        ))}
                      </select>
                    </div>
                    
                    <div className="admin-form__actions">
                      <button type="submit" className="admin-btn admin-btn--primary" disabled={loading}>
                        Сохранить
                      </button>
                      <button type="button" className="admin-btn" onClick={() => setShowCategoryForm(false)}>
                        Отмена
                      </button>
                    </div>
                  </form>
                </div>
              )}
              
              <div className="admin-table">
                <table>
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Название</th>
                      <th>Подкатегории</th>
                    </tr>
                  </thead>
                  <tbody>
                    {categoriesList.map(category => (
                      <tr key={category.id}>
                        <td>{category.id}</td>
                        <td>{category.name}</td>
                        <td>{category.children?.length || 0}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </div>
      </div>
    </>
  );
};

export default AdminPage;