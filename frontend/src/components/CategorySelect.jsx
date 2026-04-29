import React, { useState, useRef, useEffect } from 'react';

const CategorySelect = ({ categories, value, onChange, required }) => {
  const [isOpen, setIsOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const dropdownRef = useRef(null);

  const selectedCategory = categories.find(cat => cat.id === parseInt(value));
  const displayText = selectedCategory ? selectedCategory.name : 'Выберите категорию';

  const filteredCategories = categories.filter(cat =>
    cat.name.toLowerCase().includes(searchQuery.toLowerCase())
  );

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setIsOpen(false);
        setSearchQuery('');
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleSelect = (categoryId) => {
    onChange(categoryId);
    setIsOpen(false);
    setSearchQuery('');
  };

  return (
    <div className="category-select" ref={dropdownRef}>
      <div 
        className={`category-select__trigger ${!value ? 'category-select__trigger--placeholder' : ''}`}
        onClick={() => setIsOpen(!isOpen)}
      >
        {displayText}
        <span className="category-select__arrow">{isOpen ? '▲' : '▼'}</span>
      </div>

      {isOpen && (
        <div className="category-select__dropdown">
          <input
            type="text"
            className="category-select__search"
            placeholder="Поиск категории..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            onClick={(e) => e.stopPropagation()}
            autoFocus
          />
          <div className="category-select__list">
            {filteredCategories.length > 0 ? (
              filteredCategories.map(cat => (
                <div
                  key={cat.id}
                  className={`category-select__item ${cat.id === parseInt(value) ? 'category-select__item--selected' : ''}`}
                  onClick={() => handleSelect(cat.id)}
                  style={{ paddingLeft: `${12 + cat.depth * 16}px` }}
                >
                  {cat.name}
                </div>
              ))
            ) : (
              <div className="category-select__empty">Категории не найдены</div>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default CategorySelect;
