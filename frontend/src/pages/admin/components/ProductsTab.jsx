import React, { useEffect } from 'react';
import { useProducts } from '../hooks';

const CategoryTreeItem = ({ category, expanded, onToggle, depth }) => {
  const hasChildren = category.children && category.children.length > 0;
  const isExpanded = expanded[category.id];

  return (
    <div className="category-tree-item" style={{ marginLeft: depth * 20 }}>
      <div className="category-tree-item__row">
        {hasChildren && (
          <button
            className="category-tree-item__toggle"
            onClick={() => onToggle(category.id)}
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

const ProductForm = ({ form, setForm, onSubmit, onCancel, loading, uploading, onUpload, isEditing }) => {
  return (
    <div className="admin-form-overlay">
      <form className="admin-form" onSubmit={onSubmit}>
        <h3>{isEditing ? 'Редактировать' : 'Добавить'} товар</h3>

        <div className="admin-form__group">
          <label>Название *</label>
          <input
            type="text"
            value={form.name}
            onChange={e => setForm({ ...form, name: e.target.value })}
            required
          />
        </div>

        <div className="admin-form__group">
          <label>Категория *</label>
          <select
            value={form.categoryId}
            onChange={e => setForm({ ...form, categoryId: e.target.value })}
            required
          >
            <option value="">Выберите категорию</option>
          </select>
        </div>

        <div className="admin-form__group">
          <label>Цена *</label>
          <input
            type="number"
            step="0.01"
            value={form.price}
            onChange={e => setForm({ ...form, price: e.target.value })}
            required
          />
        </div>

        <div className="admin-form__group">
          <label>Описание</label>
          <textarea
            value={form.description}
            onChange={e => setForm({ ...form, description: e.target.value })}
          />
        </div>

        <div className="admin-form__group">
          <label>Производитель</label>
          <input
            type="text"
            value={form.manufacturer}
            onChange={e => setForm({ ...form, manufacturer: e.target.value })}
          />
        </div>

        <div className="admin-form__group">
          <label>Изображение</label>
          {form.imageUrl && (
            <div className="admin-form__preview">
              <img src={form.imageUrl} alt="Preview" />
            </div>
          )}
          <input
            type="file"
            accept="image/*"
            onChange={(e) => onUpload(e.target.files?.[0])}
            disabled={uploading}
          />
          {uploading && <span className="admin-form__uploading">Загрузка...</span>}
        </div>

        <div className="admin-form__actions">
          <button type="submit" className="admin-btn admin-btn--primary" disabled={loading || uploading}>
            Сохранить
          </button>
          <button type="button" className="admin-btn" onClick={onCancel}>
            Отмена
          </button>
        </div>
      </form>
    </div>
  );
};

const ProductsTab = () => {
  const {
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
  } = useProducts();

  useEffect(() => {
    loadData();
  }, [loadData]);

  const flatCategories = flattenCategories(categories);

  return (
    <div className="admin-content">
      {error && <div className="admin-error">{error}</div>}

      <form className="admin-search" onSubmit={(e) => { e.preventDefault(); loadData(); }}>
        <input
          type="text"
          placeholder="Поиск по названию..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
        />
        <button type="submit" className="admin-btn">Найти</button>
      </form>

      <button className="admin-btn admin-btn--primary" onClick={openCreate}>
        + Добавить товар
      </button>

      {showForm && (
        <ProductForm
          form={form}
          setForm={setForm}
          onSubmit={handleSubmit}
          onCancel={closeForm}
          loading={loading}
          uploading={uploading}
          onUpload={handleImageUpload}
          isEditing={editingProduct}
        />
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
                  <button className="admin-btn admin-btn--small" onClick={() => openEdit(product)}>Ред</button>
                  <button className="admin-btn admin-btn--small admin-btn--danger" onClick={() => handleDelete(product.id)}>Удал</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default ProductsTab;