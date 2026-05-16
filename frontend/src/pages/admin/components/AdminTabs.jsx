import React from 'react';

const AdminTabs = ({ activeTab, onTabChange }) => {
  const tabs = [
    { id: 'products', label: 'Товары' },
    { id: 'categories', label: 'Категории' },
    { id: 'orders', label: 'Заказы' },
    { id: 'users', label: 'Пользователи' },
    { id: 'pharmacies', label: 'Аптеки' },
    { id: 'warehouses', label: 'Склады' },
    { id: 'stocks', label: 'Остатки' }
  ];

  return (
    <div className="admin-tabs">
      {tabs.map(tab => (
        <button
          key={tab.id}
          className={`admin-tab ${activeTab === tab.id ? 'active' : ''}`}
          onClick={() => onTabChange(tab.id)}
        >
          {tab.label}
        </button>
      ))}
    </div>
  );
};

export default AdminTabs;