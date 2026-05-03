import React, { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import authApi from '../api/authService';

const EmailVerification = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [status, setStatus] = useState('loading');
  const [error, setError] = useState('');

  const token = searchParams.get('token');

  useEffect(() => {
    if (!token) {
      setStatus('error');
      setError('Отсутствует токен подтверждения');
      return;
    }

    const verify = async () => {
      try {
        await authApi.verifyEmail(token);
        setStatus('success');
      } catch (err) {
        setStatus('error');
        setError(err.response?.data?.message || 'Ошибка подтверждения email');
      }
    };

    verify();
  }, [token]);

  const handleGoHome = () => navigate('/');
  const handleGoLogin = () => navigate('/login');

  return (
    <>
      <Header />
      <div className="verification-page">
        <div className="container">
          <div className="verification-block">
            {status === 'loading' && (
              <>
                <div className="verification-block__icon">⏳</div>
                <h1>Подтверждение email...</h1>
              </>
            )}

            {status === 'success' && (
              <>
                <div className="verification-block__icon">✅</div>
                <h1>Email подтверждён!</h1>
                <p className="verification-block__text">
                  Ваш аккаунт успешно подтверждён. Теперь вы можете полноценно пользоваться сервисом.
                </p>
                <button className="verification-block__btn" onClick={handleGoHome}>
                  На главную
                </button>
              </>
            )}

            {status === 'error' && (
              <>
                <div className="verification-block__icon">❌</div>
                <h1>Ошибка подтверждения</h1>
                <p className="verification-block__text verification-block__text--error">
                  {error}
                </p>
                <div className="verification-block__actions">
                  <button className="verification-block__btn" onClick={handleGoHome}>
                    На главную
                  </button>
                  <button className="verification-block__btn verification-block__btn--secondary" onClick={handleGoLogin}>
                    Войти
                  </button>
                </div>
              </>
            )}
          </div>
        </div>
      </div>
    </>
  );
};

export default EmailVerification;