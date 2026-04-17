import React, { useState, useEffect, useRef } from 'react';
import productApi from '../api/productService'; // или свой путь к API
import '../scss/components/_category.scss';
import { flattenCategories } from '../utils/flattenCategories';

function Category({ onSelectCategory, selectedCategoryId }) {
  const [isOpen, setIsOpen] = useState(false);
  const [isMobile, setIsMobile] = useState(window.innerWidth <= 768);
  const [categories, setCategories] = useState([]); // плоский массив { id, name }
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const sidebarRef = useRef(null);

  // Используем selectedCategoryId из пропсов как активный
  const activeCategoryId = selectedCategoryId;

  // Загрузка и разворачивание дерева
  useEffect(() => {
    const fetchCategories = async () => {
      try {
        setLoading(true);
        const tree = await productApi.getCategoriesTree();
        let flat = flattenCategories(tree);
        
        // Преобразуем id в числа
        flat = flat.map(cat => ({
          ...cat,
          id: cat.id != null ? Number(cat.id) : null
        }));
        
        flat.unshift({ id: null, name: 'Все' });
        setCategories(flat);
      } catch (err) {
        setError('Не удалось загрузить категории');
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    fetchCategories();
  }, []);

  // Обработчик ресайза (оставляем как было)
  useEffect(() => {
    const handleResize = () => {
      setIsMobile(window.innerWidth <= 768);
      if (window.innerWidth > 768) {
        setIsOpen(false);
      }
    };
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  // Закрытие по клику вне сайдбара
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (sidebarRef.current && !sidebarRef.current.contains(event.target) && isOpen) {
        setIsOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [isOpen]);

  // Блокировка скролла при открытом мобильном меню
  useEffect(() => {
    if (isOpen) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = 'unset';
    }
    return () => {
      document.body.style.overflow = 'unset';
    };
  }, [isOpen]);

  const handleCategoryClick = (categoryId) => {
    const idToSet = categoryId === null ? null : Number(categoryId);
    setIsOpen(false);
    if (onSelectCategory) {
      onSelectCategory(idToSet);
    }
  };

  if (loading) return <div className="category-loading">Загрузка категорий...</div>;
  if (error) return <div className="category-error">{error}</div>;

  const renderCategoryButtons = () => {
    const activeId = activeCategoryId != null ? Number(activeCategoryId) : null;
    return (
      <div className="category">
        {categories.map((cat) => {
          const catId = cat.id != null ? Number(cat.id) : null;
          const isActive = activeId === catId;
          return (
            <button
              key={cat.id}
              className={`category__item ${isActive ? 'category__item--active' : ''}`}
              onClick={() => handleCategoryClick(cat.id)}
            >
              {cat.name}
            </button>
          );
        })}
      </div>
    );
  };
  

  return (
    <>
      {/* Мобильная версия с бургером */}
      {isMobile && (
        <>
          <button 
            className={`burger-btn ${isOpen ? 'burger-btn--active' : ''}`}
            onClick={() => setIsOpen(!isOpen)}
            aria-label="Меню категорий"
          >
            <span className="burger-btn__line"></span>
            <span className="burger-btn__line"></span>
            <span className="burger-btn__line"></span>
          </button>

          {isOpen && <div className="overlay" onClick={() => setIsOpen(false)}></div>}

          <div 
            ref={sidebarRef}
            className={`category-slider ${isOpen ? 'category-slider--open' : ''}`}
          >
            <div className="category-slider__header">
              <h3 className="category-slider__title">Категории товаров</h3>
              <button 
                className="category-slider__close"
                onClick={() => setIsOpen(false)}
              >
                ✕
              </button>
            </div>
            {renderCategoryButtons()}
          </div>
        </>
      )}

      {/* Десктопная версия (без слайдера, просто flex-wrap) */}
      {!isMobile && renderCategoryButtons()}
    </>
  );
}

export default Category;