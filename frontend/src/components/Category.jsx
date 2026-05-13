import React, { useState, useEffect, useRef } from 'react';
import productApi from '../api/productService';
import '../scss/components/_category.scss';
import { FaTimes, FaChevronDown, FaChevronRight } from 'react-icons/fa';

function Category({ onSelectCategory, selectedCategoryId }) {
  const [isOpen, setIsOpen] = useState(false);
  const [isMobile, setIsMobile] = useState(window.innerWidth <= 768);
  const [categoriesTree, setCategoriesTree] = useState([]);
  const [rootCategories, setRootCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [activeSubmenu, setActiveSubmenu] = useState(null);
  const [activeRootCategory, setActiveRootCategory] = useState(null);
  const dropdownRef = useRef(null);

  const activeCategoryId = selectedCategoryId;

  useEffect(() => {
    const fetchCategories = async () => {
      try {
        setLoading(true);
        const tree = await productApi.getCategoriesTree();
        console.log('Categories tree:', JSON.stringify(tree));
        setCategoriesTree(tree);
        setRootCategories(tree);
      } catch (err) {
        setError('Не удалось загрузить категории');
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    fetchCategories();
  }, []);

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

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target) && isOpen) {
        setIsOpen(false);
        setActiveSubmenu(null);
        setActiveRootCategory(null);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [isOpen]);

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
    setActiveSubmenu(null);
    setActiveRootCategory(null);
    if (onSelectCategory) {
      onSelectCategory(idToSet);
    }
  };

  const handleRootCategoryClick = (categoryId, hasChildren) => {
    console.log('handleRootCategoryClick:', categoryId, hasChildren);
    if (!hasChildren) {
      handleCategoryClick(categoryId);
    }
  };

  const handleRootCategoryHover = (categoryId, hasChildren) => {
    if (hasChildren) {
      setActiveRootCategory(categoryId);
      setActiveSubmenu(categoryId);
    }
  };

  const handleRootCategoryLeave = () => {
    // Не сбрасываем сразу, чтобы можно было навести на подменю
  };

  if (loading) return <div className="category-loading">Загрузка...</div>;
  if (error) return <div className="category-error">{error}</div>;

  const renderDesktopDropdown = () => {
    if (loading) return null;

    console.log('Rendering - activeRootCategory:', activeRootCategory);

    return (
      <div className="category-dropdown" ref={dropdownRef}>
        <button
          className={`category-dropdown__toggle ${isOpen ? 'category-dropdown__toggle--active' : ''}`}
          onClick={() => setIsOpen(!isOpen)}
        >
          Каталог <FaChevronDown />
        </button>

        {isOpen && (
          <div className="category-dropdown__menu">
            {rootCategories.map(rootCategory => {
              const hasChildren = rootCategory.children && rootCategory.children.length > 0;
              const isActive = activeRootCategory === rootCategory.id;
              
              console.log('Rendering item:', rootCategory.id, 'isActive:', isActive, 'hasChildren:', hasChildren);
              
              return (
                <div
                  key={rootCategory.id}
                  className={`category-dropdown__item ${isActive ? 'active' : ''}`}
                  onMouseEnter={() => handleRootCategoryHover(rootCategory.id, hasChildren)}
                  onMouseLeave={handleRootCategoryLeave}
                >
                  <span
                    className="category-dropdown__link"
                    onClick={() => handleRootCategoryClick(rootCategory.id, hasChildren)}
                  >
                    {rootCategory.name}
                    {hasChildren && (
                      <FaChevronRight className="category-dropdown__arrow" />
                    )}
                  </span>

                  {isActive && hasChildren && (
                    <div className="category-dropdown__submenu">
                      {rootCategory.children.map(subCategory => (
                        <span
                          key={subCategory.id}
                          className="category-dropdown__sublink"
                          onClick={() => handleCategoryClick(subCategory.id)}
                        >
                          {subCategory.name}
                        </span>
                      ))}
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        )}
      </div>
    );
  };

  const renderCategoryButtons = () => {
    const activeId = activeCategoryId != null ? Number(activeCategoryId) : null;
    return (
      <div className="category">
        {rootCategories.map(cat => {
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
            className={`category-slider ${isOpen ? 'category-slider--open' : ''}`}
          >
            <div className="category-slider__header">
              <h3 className="category-slider__title">Категории товаров</h3>
              <button
                className="category-slider__close"
                onClick={() => setIsOpen(false)}
              >
                <FaTimes />
              </button>
            </div>
            {renderCategoryButtons()}
          </div>
        </>
      )}

      {!isMobile && renderDesktopDropdown()}
    </>
  );
}

export default Category;