import React, { useState } from 'react';

function Sort() {
  const [isOpen, setIsOpen] = useState(false);
  const [activeSort, setActiveSort] = useState('популярности');

  const toggleSort = () => setIsOpen(!isOpen);

  const handleSortClick = (sortType) => {
    setActiveSort(sortType);
    setIsOpen(false);
  };

  return (
    <div className="sort" style={{ minWidth: '160px' }}>
      <div 
        className="sort__label" 
        onClick={toggleSort} 
        style={{ cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '5px' }}
      >
        <svg
          width="12"
          height="12"
          viewBox="0 0 24 24"
          fill="none"
          stroke="#2C2C2C"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
          style={{ 
            transform: isOpen ? 'rotate(180deg)' : 'none', 
            transition: 'transform 0.3s' 
          }}
        >
          <polyline points="6 9 12 15 18 9"></polyline>
        </svg>
        <b style={{ whiteSpace: 'nowrap' }}>Сортировка по:</b>
        <span style={{ minWidth: '80px', display: 'inline-block', textAlign: 'left'}}>{activeSort}</span>
      </div>
      {isOpen && (
        <div className="sort__popup" style={{ position: 'absolute', zIndex: 10, background: '#fff', boxShadow: '0 2px 5px rgba(0,0,0,0.2)' }}>
          <ul style={{ listStyle: 'none', padding: '10px', margin: 0 }}>
            <li 
              className={activeSort === 'популярности' ? 'active' : ''} 
              onClick={() => handleSortClick('популярности')}
              style={{ cursor: 'pointer', padding: '5px' }}
            >
              популярности
            </li>
            <li 
              className={activeSort === 'цене' ? 'active' : ''} 
              onClick={() => handleSortClick('цене')}
              style={{ cursor: 'pointer', padding: '5px' }}
            >
              цене
            </li>
            <li 
              className={activeSort === 'алфавиту' ? 'active' : ''} 
              onClick={() => handleSortClick('алфавиту')}
              style={{ cursor: 'pointer', padding: '5px ' }}
            >
              алфавиту
            </li>
          </ul>
        </div>
      )}
    </div>
  );
}

export default Sort;
