import React, { useEffect } from 'react';
import { useProducts } from '../hooks';
import CategorySelect from '../../../components/CategorySelect';

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

const ProductForm = ({ form, setForm, onSubmit, onCancel, loading, uploading, onUpload, isEditing, categories }) => {
  return (
    <div className="admin-form-overlay">
      <form className="admin-form admin-form--compact" onSubmit={onSubmit}>
        <h3>{isEditing ? 'Редактировать' : 'Добавить'} товар</h3>

        {/* Segmented Control for Product Type */}
        <div className="admin-form__group">
          <label>Тип товара *</label>
          <div className="segmented-control">
            <button
              type="button"
              className={`segmented-control__button ${!form.isMedicine ? 'segmented-control__button--active' : ''}`}
              onClick={() => setForm({ ...form, isMedicine: false })}
            >
              Обычный товар
            </button>
            <button
              type="button"
              className={`segmented-control__button ${form.isMedicine ? 'segmented-control__button--active' : ''}`}
              onClick={() => setForm({ ...form, isMedicine: true })}
            >
              Лекарственное средство
            </button>
          </div>
        </div>

        {/* Two-column layout for basic fields */}
        <div className="admin-form__row">
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
            <CategorySelect
              categories={categories || []}
              value={form.categoryId}
              onChange={(categoryId) => setForm({ ...form, categoryId: categoryId.toString() })}
              required
            />
          </div>
        </div>

        <div className="admin-form__row">
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
            <label>Производитель</label>
            <input
              type="text"
              value={form.manufacturer}
              onChange={e => setForm({ ...form, manufacturer: e.target.value })}
            />
          </div>
        </div>

        <div className="admin-form__group">
          <label>Описание</label>
          <textarea
            value={form.description}
            onChange={e => setForm({ ...form, description: e.target.value })}
            rows="2"
          />
        </div>

        {/* Medicine-specific fields */}
        {form.isMedicine && (
          <>
            <div className="admin-form__divider">
              <span>Дополнительные поля для лекарственного средства</span>
            </div>

            <div className="admin-form__row">
              <div className="admin-form__group">
                <label>Дозировка (мг) *</label>
                <input
                  type="number"
                  value={form.dosage}
                  onChange={e => setForm({ ...form, dosage: e.target.value })}
                  required={form.isMedicine}
                  placeholder="500"
                />
              </div>

              <div className="admin-form__group">
                <label>Форма выпуска *</label>
                <input
                  type="text"
                  value={form.form}
                  onChange={e => setForm({ ...form, form: e.target.value })}
                  required={form.isMedicine}
                  placeholder="таблетки"
                />
              </div>
            </div>

            <div className="admin-form__row">
              <div className="admin-form__group">
                <label>Количество в упаковке *</label>
                <input
                  type="number"
                  value={form.quantity}
                  onChange={e => setForm({ ...form, quantity: e.target.value })}
                  required={form.isMedicine}
                  placeholder="20"
                />
              </div>

              <div className="admin-form__group">
                <label className="admin-form__checkbox-label">
                  <input
                    type="checkbox"
                    checked={form.requiresPrescription}
                    onChange={e => setForm({ ...form, requiresPrescription: e.target.checked })}
                  />
                  <span>Требуется рецепт</span>
                </label>
              </div>
            </div>
          </>
        )}

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
          categories={flatCategories}
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