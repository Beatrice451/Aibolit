import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import './scss/app.scss';
import { useEffect } from 'react';
import authApi from './api/authService';
import NotificationSystem from './components/NotificationSystem';

import Login from './pages/Login';
import Register from './pages/Register';
import Profile from './pages/Profile';
import Cart from './pages/Cart';
import MainPage from './pages/MainPage';
import AdminPage from './pages/AdminPage';
import Checkout from './pages/Checkout';
import OrderConfirmation from './pages/OrderConfirmation';
import EmailVerification from './pages/EmailVerification';
import ProductPage from './pages/ProductPage';
import ProductReviewsPage from './pages/ProductReviewsPage';

function App() {
  useEffect(() => {
    authApi.initAuth();
  }, []);

  return (
    <Router>
      <NotificationSystem />
      <Routes>
        <Route path="/" element={<MainPage />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/profile" element={<Profile />} />
        <Route path="/cart" element={<Cart />} />
        <Route path="/product/:id" element={<ProductPage />} />
        <Route path="/product/:id/reviews" element={<ProductReviewsPage />} />
        <Route path="/checkout" element={<Checkout />} />
        <Route path="/order-confirmation/:orderId" element={<OrderConfirmation />} />
        <Route path="/verify-email" element={<EmailVerification />} />
        <Route path="/admin" element={<AdminPage />} />
      </Routes>
    </Router>
  );
}

export default App;