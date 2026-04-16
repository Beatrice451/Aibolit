import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import authApi from '../api/authService';

const Profile = () => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchUser = async () => {
      const token = localStorage.getItem('accessToken');
      if (!token) {
        navigate('/login');
        return;
      }

      authApi.setAuthHeader(token);
      
      try {
        const userData = await authApi.getCurrentUser();
        setUser(userData);
      } catch (err) {
        console.error('Failed to fetch user:', err);
        localStorage.removeItem('accessToken');
        navigate('/login');
      } finally {
        setLoading(false);
      }
    };

    fetchUser();
  }, [navigate]);

  const handleLogout = async () => {
    await authApi.logout();
    navigate('/login');
  };

  if (loading) {
    return (
      <>
        <Header />
        <div className="loading">Загрузка...</div>
      </>
    );
  }

  if (!user) {
    return null;
  }

  return (
    <>
      <Header />
      <div className="profile-page">
        <div className="container">
          <div className="profile-block">
            <div className="profile-block__header">
              <div className="profile-block__avatar">
                🧑‍⚕️
              </div>
              <h2 className="profile-block__name">{user.email}</h2>
              <p className="profile-block__date">Личный кабинет</p>
            </div>

            <div className="profile-block__content">
              <div className="profile-card">
                <h3 className="profile-card__title">Личная информация</h3>
                
                <div className="profile-card__row">
                  <span className="profile-card__label">📧 Электронная почта</span>
                  <span className="profile-card__value">{user.email}</span>
                </div>
                
                <div className="profile-card__row">
                  <span className="profile-card__label">📞 Контактный телефон</span>
                  <span className="profile-card__value">{user.phone || 'Не указан'}</span>
                </div>
              </div>

              {user.userRoles && user.userRoles.length > 0 && (
                <div className="profile-card">
                  <h3 className="profile-card__title">Ваши роли</h3>
                  {user.userRoles.map((role, index) => (
                    <div key={index} className="profile-card__row">
                      <span className="profile-card__label">🏷️ Роль</span>
                      <span className="profile-card__value">{role.roleName}</span>
                    </div>
                  ))}
                </div>
              )}

              <button onClick={handleLogout} className="profile-block__logout">
                🚪 Выйти из аккаунта
              </button>
            </div>
          </div>
        </div>
      </div>
    </>
  );
};

export default Profile;