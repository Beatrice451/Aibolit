import React, { useState, useCallback, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import NotificationSystem from '../components/NotificationSystem';
import authApi from '../api/authService';
import adminApi from '../api/adminService';
import '../scss/pages/_admin.scss';

import AdminTabs from './admin/components/AdminTabs';
import ProductsTab from './admin/components/ProductsTab';
import OrdersTab from './admin/components/OrdersTab';
import UsersTab from './admin/components/UsersTab';
import CategoriesTab from './admin/components/CategoriesTab';

const AdminPage = () => {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('products');
  const [accessDenied, setAccessDenied] = useState(false);
  const [accessDeniedReason, setAccessDeniedReason] = useState(null);
  const [checkingAccess, setCheckingAccess] = useState(true);
  const [roles, setRoles] = useState([]);

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
  }, [navigate]);

  useEffect(() => {
    checkAccessAndLoad();
  }, [checkAccessAndLoad]);

  useEffect(() => {
    console.log('[AdminPage] activeTab:', activeTab, 'roles.length:', roles.length);
    if (activeTab === 'users' && roles.length === 0) {
      console.log('[AdminPage] Loading roles...');
      adminApi.getRoles()
        .then(data => {
          console.log('[AdminPage] Roles loaded:', data);
          setRoles(data);
        })
        .catch(err => console.error('[AdminPage] Error loading roles:', err));
    }
  }, [activeTab]);

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

  return (
    <>
      <Header />
      <NotificationSystem />
      <div className="admin-page">
        <div className="container">
          <h1>Админ-панель</h1>
          <AdminTabs activeTab={activeTab} onTabChange={setActiveTab} />

          {activeTab === 'products' && <ProductsTab />}
          {activeTab === 'categories' && <CategoriesTab />}
          {activeTab === 'orders' && <OrdersTab />}
          {activeTab === 'users' && <UsersTab roles={roles} />}
        </div>
      </div>
    </>
  );
};

export default AdminPage;