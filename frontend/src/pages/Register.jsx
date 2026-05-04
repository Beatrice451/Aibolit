import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import authApi from '../api/authService';

const formatPhoneDisplay = (value) => {
  if (!value) return '';
  let digits = value.replace(/\D/g, '');
  if (digits.startsWith('8')) digits = '7' + digits.substring(1);
  else if (digits.length > 0 && !digits.startsWith('7')) digits = '7' + digits;
  if (digits.length === 0) return '';
  let result = '+7';
  if (digits.length > 1) result += ' (' + digits.substring(1, 4);
  if (digits.length > 4) result += ') ' + digits.substring(4, 7);
  if (digits.length > 7) result += '-' + digits.substring(7, 9);
  if (digits.length > 9) result += '-' + digits.substring(9, 11);
  return result;
};

const formatPhoneForApi = (value) => {
  if (!value) return '';
  let digits = value.replace(/\D/g, '');
  if (digits.startsWith('8')) return '+7' + digits.substring(1);
  return digits.startsWith('7') ? '+' + digits : '+7' + digits;
};

const Register = () => {
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    phone: '',
    email: '',
    password: '',
    confirmPassword: ''
  });
  const [phoneDisplay, setPhoneDisplay] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handlePhoneChange = (e) => {
    const formatted = formatPhoneDisplay(e.target.value);
    setPhoneDisplay(formatted);
    const rawDigits = e.target.value.replace(/\D/g, '');
    setFormData(prev => ({ ...prev, phone: rawDigits }));
  };

  const handleChange = (e) => {
    if (e.target.name === 'phone') return;
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    
    // Validate password confirmation
    if (formData.password !== formData.confirmPassword) {
      setError('Пароли не совпадают');
      return;
    }
    
    setLoading(true);

    try {
      const phoneForApi = formatPhoneForApi(phoneDisplay);
      await authApi.register({
        name: `${formData.firstName} ${formData.lastName}`.trim(),
        phone: phoneForApi,
        email: formData.email,
        password: formData.password
      });
      alert(`Поздравляем, ${formData.firstName}! Регистрация успешна. На email отправлено письмо для подтверждения.`);
      navigate('/');
    } catch (err) {
      const message = err.response?.data?.message || 'Ошибка при регистрации.';
      setError(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <Header />
      <div className="register-page">
        <div className="container">
          <div className="register-block">
            <div className="register-block__header">
              <div className="register-block__icon">💊✨</div>
              <h1 className="register-block__title">Добро пожаловать!</h1>
              <p className="register-block__subtitle">Создайте аккаунт</p>
            </div>

            <form onSubmit={handleSubmit} className="register-form">
              <div className="register-form__group">
                <label className="register-form__label">👤 Имя</label>
                <input type="text" name="firstName" className="register-form__input" placeholder="Анна" value={formData.firstName} onChange={handleChange} required />
              </div>

              <div className="register-form__group">
                <label className="register-form__label">👤 Фамилия</label>
                <input type="text" name="lastName" className="register-form__input" placeholder="Иванова" value={formData.lastName} onChange={handleChange} required />
              </div>

              <div className="register-form__group">
                <label className="register-form__label">📞 Телефон</label>
                <input type="tel" name="phone" className="register-form__input" placeholder="+7 (777) 777-77-77" value={phoneDisplay} onChange={handlePhoneChange} required />
              </div>

              <div className="register-form__group">
                <label className="register-form__label">📧 Email</label>
                <input type="email" name="email" className="register-form__input" placeholder="mail@mail.com" value={formData.email} onChange={handleChange} required />
              </div>

              <div className="register-form__group">
                <label className="register-form__label">🔑 Пароль</label>
                <input type="password" name="password" className="register-form__input" placeholder="Минимум 8 символов" value={formData.password} onChange={handleChange} required />
              </div>

              <div className="register-form__group">
                <label className="register-form__label">🔑 Подтверждение пароля</label>
                <input type="password" name="confirmPassword" className="register-form__input" placeholder="Повторите пароль" value={formData.confirmPassword} onChange={handleChange} required />
              </div>

              {error && <div className="register-form__error">{error}</div>}

              <button type="submit" className="register-form__btn register-form__btn--primary" disabled={loading}>
                {loading ? 'Регистрация...' : 'Зарегистрироваться'}
              </button>

              <div className="register-form__footer">
                Уже есть аккаунт? <Link to="/login">Войти</Link>
              </div>
            </form>
          </div>
        </div>
      </div>
    </>
  );
};

export default Register;