// components/Pagination.jsx
import React from 'react';
import '../App.css'

const Pagination = ({ currentPage, totalPages, onPageChange }) => {
  // Генерируем массив страниц для отображения (можно с умным пропуском)
  const getPageNumbers = () => {
    const pages = [];
    const maxVisible = 5; // показывать до 5 страниц вокруг текущей
    let start = Math.max(0, currentPage - Math.floor(maxVisible / 2));
    let end = Math.min(totalPages - 1, start + maxVisible - 1);
    if (end - start < maxVisible - 1) {
      start = Math.max(0, end - maxVisible + 1);
    }
    for (let i = start; i <= end; i++) {
      pages.push(i);
    }
    return pages;
  };

  return (
    <div className="pagination">
      <button
        className="pagination__btn"
        disabled={currentPage === 0}
        onClick={() => onPageChange(currentPage - 1)}
      >
        &laquo; Назад
      </button>
      
      {getPageNumbers().map((page) => (
        <button
          key={page}
          className={`pagination__page ${page === currentPage ? 'active' : ''}`}
          onClick={() => onPageChange(page)}
        >
          {page + 1}
        </button>
      ))}
      
      <button
        className="pagination__btn"
        disabled={currentPage === totalPages - 1}
        onClick={() => onPageChange(currentPage + 1)}
      >
        Вперёд &raquo;
      </button>
    </div>
  );
};

export default Pagination;