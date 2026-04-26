import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import adminApi from '../api/adminService';
import authApi from '../api/authService';
import axiosInstance from '../api/axiosInstance';

const API_BASE_URL = '';

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
  const [expandedCategories, setExpandedCategories] = useState({});

  const [orders, setOrders] = useState([]);
  const [ordersLoading, setOrdersLoading] = useState(false);
  const [ordersPage, setOrdersPage] = useState(0);
  const [ordersTotalPages, setOrdersTotalPages] = useState(0);
  const [orderFilters, setOrderFilters] = useState({
    status: '',
    email: '',
    phone: '',
    showCompleted: false,
    showCancelled: false
  });
  const [expandedOrders, setExpandedOrders] = useState({});
  const [orderStatusChanges, setOrderStatusChanges] = useState({});
  const [statusModal, setStatusModal] = useState({ open: false, orderId: null, newStatus: null });
  const [orderItemsImages, setOrderItemsImages] = useState({});

  const [users, setUsers] = useState([]);
  const [usersLoading, setUsersLoading] = useState(false);
  const [usersPage, setUsersPage] = useState(0);
  const [usersTotalPages, setUsersTotalPages] = useState(0);
  const [roles, setRoles] = useState([]);
  const [userFilters, setUserFilters] = useState({
    email: '',
    isDeleted: '',
    role: ''
  });

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

  const loadOrders = useCallback(async (page = 0) => {
    setOrdersLoading(true);
    try {
      const activeFilters = {
        orderStatus: orderFilters.status || null,
        email: orderFilters.email || null,
        phone: orderFilters.phone || null,
      };

      if (!orderFilters.showCompleted) {
        activeFilters.excludeCompleted = true;
      }
      if (!orderFilters.showCancelled) {
        activeFilters.excludeCancelled = true;
      }

      console.log('[Admin] Loading orders with filters:', activeFilters);

      const data = await adminApi.getOrders(page, 20, activeFilters);
      setOrders(data.content || []);
      setOrdersTotalPages(data.totalPages || 0);
      setOrdersPage(page);
    } catch (err) {
      console.error('[Admin] Error loading orders:', err);
    } finally {
      setOrdersLoading(false);
    }
  }, [orderFilters]);

  const loadUsers = useCallback(async (page = 0) => {
    setUsersLoading(true);
    try {
      const data = await adminApi.getUsers(page, 20, userFilters);
      setUsers(data.content || []);
      setUsersTotalPages(data.totalPages || 0);
      setUsersPage(page);
    } catch (err) {
      console.error('[Admin] Error loading users:', err);
    } finally {
      setUsersLoading(false);
    }
  }, [userFilters]);

  const loadRoles = useCallback(async () => {
    try {
      const data = await adminApi.getRoles();
      setRoles(data || []);
    } catch (err) {
      console.error('[Admin] Error loading roles:', err);
    }
  }, []);

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

  useEffect(() => {
    if (activeTab === 'orders') {
      loadOrders(0);
    }
  }, [activeTab, orderFilters.showCompleted, orderFilters.showCancelled, orderFilters.status, orderFilters.email, orderFilters.phone]);

  useEffect(() => {
    if (activeTab === 'users') {
      loadUsers(0);
      loadRoles();
    }
  }, [activeTab, userFilters.email, userFilters.isDeleted, userFilters.role]);

  useEffect(() => {
    const loadImages = async () => {
      const newImages = {};
      for (const order of orders) {
        if (expandedOrders[order.id] && order.items) {
          for (const item of order.items) {
            if (!orderItemsImages[item.productId]) {
              try {
                const response = await axiosInstance.get(`/api/products/${item.productId}`);
                newImages[item.productId] = response.data.imageUrl;
              } catch {
                newImages[item.productId] = null;
              }
            }
          }
        }
      }
      if (Object.keys(newImages).length > 0) {
        setOrderItemsImages(prev => ({ ...prev, ...newImages }));
      }
    };
    loadImages();
  }, [expandedOrders]);

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

  const handleAddRole = async (userId, roleId) => {
    try {
      await adminApi.assignRole(userId, roleId);
      await loadUsers(usersPage);
    } catch (err) {
      console.error('Error adding role:', err);
      alert('Ошибка добавления роли');
    }
  };

  const handleRemoveRole = async (userId, roleId) => {
    if (!window.confirm('Удалить эту роль у пользователя?')) return;
    try {
      await adminApi.removeRole(userId, roleId);
      await loadUsers(usersPage);
    } catch (err) {
      console.error('Error removing role:', err);
      alert('Ошибка удаления роли');
    }
  };

  const handleDeleteUser = async (userId) => {
    if (!window.confirm('Удалить этого пользователя? Восстановление будет невозможно.')) return;
    try {
      await adminApi.deleteUser(userId);
      await loadUsers(usersPage);
    } catch (err) {
      console.error('Error deleting user:', err);
      alert('Ошибка удаления пользователя');
    }
  };

  const handleRestoreUser = async (userId) => {
    try {
      await adminApi.restoreUser(userId);
      await loadUsers(usersPage);
    } catch (err) {
      console.error('Error restoring user:', err);
      alert('Ошибка восстановления пользователя');
    }
  };

  const handleOrderStatusChange = (orderId, newStatus) => {
    setStatusModal({ open: true, orderId, newStatus });
  };

  const confirmStatusChange = async () => {
    const { orderId, newStatus } = statusModal;
    try {
      await adminApi.updateOrderStatus(orderId, newStatus);
      await loadOrders(ordersPage);
    } catch (err) {
      console.error('Error updating order status:', err);
      alert('Ошибка изменения статуса заказа');
    } finally {
      setStatusModal({ open: false, orderId: null, newStatus: null });
    }
  };

  const cancelStatusChange = () => {
    setStatusModal({ open: false, orderId: null, newStatus: null });
  };

  const getProductImage = async (productId) => {
    if (orderItemsImages[productId]) return orderItemsImages[productId];
    try {
      const response = await axiosInstance.get(`/api/products/${productId}`);
      const imageUrl = response.data.imageUrl;
      setOrderItemsImages(prev => ({ ...prev, [productId]: imageUrl }));
      return imageUrl;
    } catch {
      return null;
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
    // Add /media/ prefix
    const cleanUrl = url.startsWith('/') ? url : '/' + url;
    return `/media${cleanUrl}`;
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
            <button
              className={`admin-tab ${activeTab === 'orders' ? 'active' : ''}`}
              onClick={() => setActiveTab('orders')}
            >
              Заказы
            </button>
            <button
              className={`admin-tab ${activeTab === 'users' ? 'active' : ''}`}
              onClick={() => setActiveTab('users')}
            >
              Пользователи
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
              
              <div className="admin-categories-tree">
                {categoriesList.map(category => (
                  <CategoryTreeItem 
                    key={category.id} 
                    category={category} 
                    expanded={expandedCategories}
                    onToggle={setExpandedCategories}
                    depth={0}
                  />
                ))}
              </div>
            </div>
          )}

          {activeTab === 'orders' && (
            <div className="admin-content">
              <div className="orders-filters">
                <div className="orders-filters__row">
                  <select
                    value={orderFilters.status}
                    onChange={e => setOrderFilters({...orderFilters, status: e.target.value})}
                  >
                    <option value="">Все статусы</option>
                    <option value="NEW">Новый</option>
                    <option value="ASSEMBLING">Сборка</option>
                    <option value="READY">Готов к выдаче</option>
                    <option value="DELIVERY_PENDING">Ожидает доставки</option>
                    <option value="DELIVERY_DELAYED">Доставка задерживается</option>
                    <option value="COMPLETED">Выдан</option>
                    <option value="CANCELLED_USER">Отменён пользователем</option>
                    <option value="EXPIRED">Истёк</option>
                  </select>

                  <input
                    type="text"
                    placeholder="Email"
                    value={orderFilters.email}
                    onChange={e => setOrderFilters({...orderFilters, email: e.target.value})}
                  />

                  <input
                    type="text"
                    placeholder="Телефон"
                    value={orderFilters.phone}
                    onChange={e => setOrderFilters({...orderFilters, phone: e.target.value})}
                  />

                  <button className="admin-btn" onClick={() => loadOrders(0)}>Найти</button>
                  <button className="admin-btn" onClick={() => setOrderFilters({
                    status: '',
                    email: '',
                    phone: '',
                    showCompleted: false,
                    showCancelled: false
                  })}>Сбросить</button>
                </div>

                <div className="orders-filters__switches">
                  <label className="orders-filters__switch">
                    <input
                      type="checkbox"
                      checked={orderFilters.showCompleted}
                      onChange={e => setOrderFilters({...orderFilters, showCompleted: e.target.checked})}
                    />
                    <span>Показать завершённые</span>
                  </label>
                  <label className="orders-filters__switch">
                    <input
                      type="checkbox"
                      checked={orderFilters.showCancelled}
                      onChange={e => setOrderFilters({...orderFilters, showCancelled: e.target.checked})}
                    />
                    <span>Показать отменённые</span>
                  </label>
                </div>
              </div>

              {ordersLoading ? (
                <div className="admin-loading">Загрузка заказов...</div>
              ) : orders.length === 0 ? (
                <div className="admin-empty">Заказы не найдены</div>
              ) : (
                <>
                  <div className="orders-table">
                    <table>
                      <thead>
                        <tr>
                          <th></th>
                          <th>ID</th>
                          <th>Дата</th>
                          <th>Клиент</th>
                          <th>Email</th>
                          <th>Телефон</th>
                          <th>Сумма</th>
                          <th>Статус</th>
                        </tr>
                      </thead>
                      <tbody>
                        {orders.map(order => (
                          <React.Fragment key={order.id}>
                          <tr
                            className={`order-row ${expandedOrders[order.id] ? 'order-row--expanded' : ''}`}
                            onClick={() => setExpandedOrders(prev => ({ ...prev, [order.id]: !prev[order.id] }))}
                          >
                            <td className="order-row__toggle">
                              <span className="order-row__arrow">{expandedOrders[order.id] ? '▼' : '▶'}</span>
                            </td>
                            <td>#{order.id}</td>
                            <td>{order.createdAt ? new Date(order.createdAt).toLocaleDateString('ru-RU') : '-'}</td>
                            <td>{order.clientName || order.email?.split('@')[0] || '-'}</td>
                            <td>{order.email || '-'}</td>
                            <td>{order.phone || '-'}</td>
                            <td className="order-row__amount">{order.amount?.total || 0} ₽</td>
                            <td className="order-row__status" onClick={e => e.stopPropagation()}>
                              <div className="order-status-wrapper">
                                <select
                                  className={`order-status-select order-status-select--${order.orderStatus?.toLowerCase()}`}
                                  value={order.orderStatus}
                                  onChange={e => handleOrderStatusChange(order.id, e.target.value)}
                                >
                                  <option value="NEW">Новый</option>
                                  <option value="ASSEMBLING">Сборка</option>
                                  <option value="READY">Готов к выдаче</option>
                                  <option value="DELIVERY_PENDING">Ожидает доставки</option>
                                  <option value="DELIVERY_DELAYED">Доставка задерживается</option>
                                  <option value="COMPLETED">Выдан</option>
                                  <option value="CANCELLED_USER">Отменён (польз.)</option>
                                  <option value="EXPIRED">Истёк</option>
                                </select>
                              </div>
                            </td>
                          </tr>
                          {expandedOrders[order.id] && order.items && order.items.length > 0 && (
                            <tr className="order-items-row">
                              <td colSpan="8">
                                <div className="order-items-list">
                                  {order.items.map((item, idx) => {
                                    // TODO: Сделать нормально - получать imageUrl сразу в заказе
                                    const imageUrl = orderItemsImages[item.productId];
                                    return (
                                    <div key={idx} className="order-item">
                                      <div className="order-item__image">
                                        {imageUrl ? (
                                          <img src={`/media/${imageUrl}`} alt={item.name} />
                                        ) : (
                                          <span className="order-item__placeholder">💊</span>
                                        )}
                                      </div>
                                      <div className="order-item__info">
                                        <span className="order-item__name">{item.name}</span>
                                        <span className="order-item__qty">× {item.quantity}</span>
                                      </div>
                                      <div className="order-item__price">
                                        {item.priceAtSale} ₽
                                      </div>
                                      <div className="order-item__total">
                                        {item.total || (item.priceAtSale * item.quantity)} ₽
                                      </div>
                                    </div>
                                  );})}
                                </div>
                              </td>
                            </tr>
                          )}
                          </React.Fragment>
                        ))}
                      </tbody>
                    </table>
                  </div>

                  {ordersTotalPages > 1 && (
                    <div className="admin-pagination">
                      <button
                        className="admin-btn"
                        disabled={ordersPage === 0}
                        onClick={() => loadOrders(ordersPage - 1)}
                      >
                        Предыдущая
                      </button>
                      <span className="admin-pagination__info">
                        Страница {ordersPage + 1} из {ordersTotalPages}
                      </span>
                      <button
                        className="admin-btn"
                        disabled={ordersPage >= ordersTotalPages - 1}
                        onClick={() => loadOrders(ordersPage + 1)}
                      >
                        Следующая
                      </button>
                    </div>
                  )}
                </>
              )}
            </div>
          )}

          {activeTab === 'users' && (
            <div className="admin-content">
              <div className="users-filters">
                <div className="users-filters__row">
                  <input
                    type="text"
                    placeholder="Email"
                    value={userFilters.email}
                    onChange={e => setUserFilters({...userFilters, email: e.target.value})}
                  />

                  <select
                    value={userFilters.isDeleted}
                    onChange={e => setUserFilters({...userFilters, isDeleted: e.target.value})}
                  >
                    <option value="">Все статусы</option>
                    <option value="false">Активные</option>
                    <option value="true">Удалённые</option>
                  </select>

                  <select
                    value={userFilters.role}
                    onChange={e => setUserFilters({...userFilters, role: e.target.value})}
                  >
                    <option value="">Все роли</option>
                    {roles.map(role => (
                      <option key={role.id} value={role.roleName}>{role.roleName}</option>
                    ))}
                  </select>

                  <button className="admin-btn" onClick={() => loadUsers(0)}>Найти</button>
                  <button className="admin-btn" onClick={() => setUserFilters({ email: '', isDeleted: '', role: '' })}>Сбросить</button>
                </div>
              </div>

              {usersLoading ? (
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
                          <tr key={user.id} className={user.isDeleted ? 'user-row--deleted' : ''}>
                            <td>#{user.id}</td>
                            <td>{user.firstName || user.lastName ? `${user.firstName || ''} ${user.lastName || ''}`.trim() : '-'}</td>
                            <td>{user.email}</td>
                            <td>{user.phone || '-'}</td>
                            <td>
                              <div className="user-roles">
                                {user.roles?.map(role => (
                                  <span key={role.id} className="user-role">
                                    {role.roleName}
                                    {role.roleName !== 'ADMIN' && (
                                      <button
                                        className="user-role__remove"
                                        onClick={() => handleRemoveRole(user.id, role.id)}
                                        title="Удалить роль"
                                      >
                                        ×
                                      </button>
                                    )}
                                  </span>
                                ))}
                                <select
                                  className="user-role-add"
                                  value=""
                                  onChange={e => e.target.value && handleAddRole(user.id, Number(e.target.value))}
                                >
                                  <option value="">+ Роль</option>
                                  {roles.filter(r => !user.roles?.some(ur => ur.id === r.id)).map(role => (
                                    <option key={role.id} value={role.id}>{role.roleName}</option>
                                  ))}
                                </select>
                              </div>
                            </td>
                            <td>
                              <span className={`user-status user-status--${user.isDeleted ? 'deleted' : 'active'}`}>
                                {user.isDeleted ? 'Удалён' : 'Активен'}
                              </span>
                            </td>
                            <td>
                              {!user.isDeleted && (
                                <button
                                  className="admin-btn admin-btn--danger"
                                  onClick={() => handleDeleteUser(user.id)}
                                >
                                  Удалить
                                </button>
                              )}
                              {user.isDeleted && (
                                <button
                                  className="admin-btn"
                                  onClick={() => handleRestoreUser(user.id)}
                                >
                                  Восстановить
                                </button>
                              )}
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>

                  {usersTotalPages > 1 && (
                    <div className="admin-pagination">
                      <button
                        className="admin-btn"
                        disabled={usersPage === 0}
                        onClick={() => loadUsers(usersPage - 1)}
                      >
                        Предыдущая
                      </button>
                      <span className="admin-pagination__info">
                        Страница {usersPage + 1} из {usersTotalPages}
                      </span>
                      <button
                        className="admin-btn"
                        disabled={usersPage >= usersTotalPages - 1}
                        onClick={() => loadUsers(usersPage + 1)}
                      >
                        Следующая
                      </button>
                    </div>
                  )}
                </>
              )}
            </div>
          )}
        </div>
      </div>

      {statusModal.open && (
        <div className="admin-modal-overlay" onClick={cancelStatusChange}>
          <div className="admin-modal" onClick={e => e.stopPropagation()}>
            <h3>Подтверждение изменения статуса</h3>
            <p>Вы уверены, что хотите изменить статус заказа #{statusModal.orderId} на <strong>{getStatusLabel(statusModal.newStatus)}</strong>?</p>
            <div className="admin-modal__actions">
              <button className="admin-btn admin-btn--primary" onClick={confirmStatusChange}>Подтвердить</button>
              <button className="admin-btn" onClick={cancelStatusChange}>Отмена</button>
            </div>
          </div>
        </div>
      )}
    </>
  );
};

const CategoryTreeItem = ({ category, expanded, onToggle, depth }) => {
  const hasChildren = category.children && category.children.length > 0;
  const isExpanded = expanded[category.id];
  
  return (
    <div className="category-tree-item" style={{ marginLeft: depth * 20 }}>
      <div className="category-tree-item__row">
        {hasChildren && (
          <button 
            className="category-tree-item__toggle"
            onClick={() => onToggle(prev => ({ ...prev, [category.id]: !prev[category.id] }))}
          >
            {isExpanded ? '▼' : '▶'}
          </button>
        )}
        <span className="category-tree-item__name">{category.name}</span>
        <span className="category-tree-item__id">#{category.id}</span>
      </div>
      {isExpanded && hasChildren && (
        <div className="category-tree-item__children">
          {category.children.map(child => (
            <CategoryTreeItem 
              key={child.id} 
              category={child} 
              expanded={expanded}
              onToggle={onToggle}
              depth={depth + 1}
            />
          ))}
        </div>
      )}
    </div>
  );
};

const getStatusLabel = (status) => {
  const labels = {
    NEW: 'Новый',
    ASSEMBLING: 'Сборка',
    READY: 'Готов',
    DELIVERY_PENDING: 'Ожидает доставки',
    DELIVERY_DELAYED: 'Задержка',
    COMPLETED: 'Выдан',
    CANCELLED_USER: 'Отменён',
    EXPIRED: 'Истёк'
  };
  return labels[status] || status;
};

export default AdminPage;