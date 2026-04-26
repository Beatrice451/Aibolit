import React, { useEffect } from 'react';
import { useCategories } from '../hooks';

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

const CategoriesTab = () => {
  const {
    categories,
    loading,
    error,
    showForm,
    form,
    setForm,
    expanded,
    loadCategories,
    handleSubmit,
    toggle,
    flattenCategories,
    openCreate,
    closeForm
  } = useCategories();

  useEffect(() => {
    loadCategories();
  }, [loadCategories]);

  const flatCategories = flattenCategories(categories);

  return (
    <div className="admin-content">
      {error && <div className="admin-error">{error}</div>}

      <button className="admin-btn admin-btn--primary" onClick={openCreate}>
        + Добавить категорию
      </button>

      {showForm && (
        <div className="admin-form-overlay">
          <form className="admin-form" onSubmit={handleSubmit}>
            <h3>Добавить категорию</h3>

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
              <label>Родительская категория</label>
              <select
                value={form.parentId}
                onChange={e => setForm({ ...form, parentId: e.target.value })}
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
              <button type="button" className="admin-btn" onClick={closeForm}>
                Отмена
              </button>
            </div>
          </form>
        </div>
      )}

      <div className="admin-categories-tree">
        {categories.map(category => (
          <CategoryTreeItem
            key={category.id}
            category={category}
            expanded={expanded}
            onToggle={toggle}
            depth={0}
          />
        ))}
      </div>
    </div>
  );
};

export default CategoriesTab;