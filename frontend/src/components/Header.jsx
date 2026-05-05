import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import authApi from '../api/authService';
import { FaShoppingCart, FaUser, FaCog } from 'react-icons/fa';

const Header = () => {
  const [isAdmin, setIsAdmin] = useState(false);
  const token = localStorage.getItem('accessToken');

  useEffect(() => {
    if (token) {
      authApi.isAdmin().then(setIsAdmin).catch(() => setIsAdmin(false));
    }
  }, [token]);

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
              <FaShoppingCart /> Корзина
            </Link>
            
            {token ? (
              <>
                {isAdmin && (
                  <Link to="/admin" className="header__admin">
                    <FaCog /> Админ
                  </Link>
                )}
                <Link to="/profile" className="header__profile">
                  <FaUser /> Профиль
                </Link>
              </>
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