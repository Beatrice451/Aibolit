import React from 'react';
import { Link } from 'react-router-dom';

const Header = () => {
  const token = localStorage.getItem('accessToken');

  return (
    <div className="header">
      <div className="container">
        <div className="header__inner">
          <Link to="/" className="header__logo">
            <div>
              <h1>Аптека Айболит</h1>
              <p>Ваше здоровье — наша забота</p>
            </div>
          </Link>
          
          <div className="header__links">
            <Link to="/cart" className="header__cart">
              🛒 Корзина
            </Link>
            
            {token ? (
              <Link to="/profile" className="header__profile">
                👤 Профиль
              </Link>
            ) : (
              <>
                <Link to="/login" className="header__login">
                  Вход
                </Link>
                <Link to="/register" className="header__register">
                  Регистрация
                </Link>
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Header;