import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import authApi from '../api/authService';

const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const result = await authApi.login(email, password);
      
      if (result.accessToken) {
        localStorage.setItem('accessToken', result.accessToken);
        authApi.setAuthHeader(result.accessToken);
        navigate('/profile');
      }
    } catch (err) {
      const status = err.response?.status;
      let message;
      if (status === 418) {
        message = 'Аккаунт удалён. Восстановление невозможно.';
      } else if (status === 401) {
        message = 'Неверный email или пароль.';
      } else {
        message = err.response?.data?.message || 'Ошибка при входе. Проверьте email и пароль.';
      }
      setError(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <Header />
      <div className="login-page">
        <div className="container">
          <div className="login-block">
            <div className="login-block__header">
              <div className="login-block__icon">💊⚕️</div>
              <h1 className="login-block__title">
                Аптека <span>Айболит</span>
              </h1>
              <p className="login-block__subtitle">Ваше здоровье — наша забота</p>
            </div>

            <form onSubmit={handleSubmit} className="login-form">
              <div className="login-form__group">
                <label className="login-form__label">📧 Email</label>
                <input
                  type="email"
                  className="login-form__input"
                  placeholder="example@mail.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                />
              </div>

              <div className="login-form__group">
                <label className="login-form__label">🔒 Пароль</label>
                <input
                  type="password"
                  className="login-form__input"
                  placeholder="Введите пароль"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                />
              </div>

              {error && <div className="login-form__error">{error}</div>}

              <button 
                type="submit" 
                className="login-form__btn login-form__btn--primary"
                disabled={loading}
              >
                {loading ? 'Вход...' : 'Войти в кабинет'}
              </button>

              <div className="login-form__footer">
                Нет аккаунта? <Link to="/register">Зарегистрироваться</Link>
              </div>
            </form>
          </div>
        </div>
      </div>
    </>
  );
};

export default Login;