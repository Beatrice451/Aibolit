import React, { useState, useEffect } from 'react';
import Header from '../components/Header';
import Category from '../components/Category';
import Sort from '../components/Sort';
import Tabletblock from '../components/Tabletblock';
import Pagination from '../components/Pagination';
import productApi from '../api/productService';

const API_BASE_URL = '';

const MainPage = () => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize] = useState(20);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [selectedCategoryId, setSelectedCategoryId] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');

  const fetchProducts = async (page = 0, size = pageSize, categoryId = selectedCategoryId, search = searchQuery) => {
    try {
      setLoading(true);
      const filters = {};
      if (categoryId) {
        filters.categoryId = categoryId;
      }
      if (search) {
        filters.search = search;
      }
      const data = await productApi.getProducts(filters, { page, size });
      setProducts(data.content || []);
      setTotalPages(data.totalPages || 0);
      setTotalElements(data.totalElements || 0);
      setCurrentPage(data.number ?? page);
    } catch (err) {
      setError('Ошибка при загрузке товаров');
      console.error('Error fetching products:', err.response?.data || err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProducts(currentPage);
  }, []);

  const handlePageChange = (newPage) => {
    if (newPage >= 0 && newPage < totalPages) {
      fetchProducts(newPage, pageSize, selectedCategoryId);
    }
  };

  const handleCategorySelect = (categoryId) => {
    setSelectedCategoryId(categoryId);
    setCurrentPage(0);
    fetchProducts(0, pageSize, categoryId, searchQuery);
  };

  const handleSearch = (e) => {
    e.preventDefault();
    setCurrentPage(0);
    fetchProducts(0, pageSize, selectedCategoryId, searchQuery);
  };

  const handleSearchChange = (e) => {
    setSearchQuery(e.target.value);
  };

  if (error) {
    return (
      <div className="wrapper">
        <div className="container">{error}</div>
      </div>
    );
  }

  return (
    <div className="wrapper">
      <Header />
      <div className="content">
        <div className="container">
          <div className="content__top">
            <Category onSelectCategory={handleCategorySelect} selectedCategoryId={selectedCategoryId} />
            <Sort />
          </div>
          <form className="catalog-search" onSubmit={handleSearch}>
            <input 
              type="text" 
              placeholder="Поиск по названию..." 
              value={searchQuery}
              onChange={handleSearchChange}
            />
            <button type="submit">Найти</button>
          </form>
          <h2 className="content__title">Все лекарственные средства</h2>
          
          <div className="content__items">
            {loading && products.length > 0 && (
              <div style={{
                position: 'absolute',
                top: 0,
                left: 0,
                right: 0,
                bottom: 0,
                background: 'rgba(255,255,255,0.7)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                zIndex: 5
              }}>
                Загрузка...
              </div>
            )}
            
            {products.length > 0 ? (
              products.map((product) => (
                <Tabletblock
                  key={product.id}
                  title={product.name}
                  price={product.price}
                  id={product.id}
                  imageUrl={
                    product.imageUrl?.startsWith('http')
                      ? product.imageUrl
                      : product.imageUrl
                        ? `/media/${product.imageUrl.startsWith('/') ? '' : ''}${product.imageUrl}`
                        : '/placeholder.png'
                  }
                />
              ))
            ) : !loading ? (
              <p>Товары не найдены</p>
            ) : null}
          </div>
          
          {totalPages > 1 && (
            <Pagination
              currentPage={currentPage}
              totalPages={totalPages}
              onPageChange={handlePageChange}
            />
          )}
        </div>
      </div>
    </div>
  );
};

export default MainPage;