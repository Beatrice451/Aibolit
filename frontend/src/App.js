import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import './scss/app.scss';
import { useEffect } from 'react';
import authApi from './api/authService';

import Login from './pages/Login';
import Register from './pages/Register';
import Profile from './pages/Profile';
import Cart from './pages/Cart';
import MainPage from './pages/MainPage';
import AdminPage from './pages/AdminPage';

function App() {
  useEffect(() => {
    authApi.initAuth();
  }, []);

  return (
    <Router>
      <Routes>
        <Route path="/" element={<MainPage />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/profile" element={<Profile />} />
        <Route path="/cart" element={<Cart />} />
        <Route path="/admin" element={<AdminPage />} />
      </Routes>
    </Router>
  );
}

export default App;