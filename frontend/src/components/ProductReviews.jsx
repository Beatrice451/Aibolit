import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import productApi from '../api/productService';
import authApi from '../api/authService';
import StarRating from './StarRating';
import { showNotification } from './NotificationSystem';
import { FaUser, FaTrash } from 'react-icons/fa';
import '../scss/components/_product-reviews.scss';

const ProductReviews = ({ productId, showAllLink = true }) => {
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(true);
  const [user, setUser] = useState(null);
  const [hasReviewed, setHasReviewed] = useState(false);

  const [newComment, setNewComment] = useState('');
  const [newRating, setNewRating] = useState(0);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    fetchReviews();
    checkUserReview();
  }, [productId]);

  const fetchReviews = async () => {
    try {
      const data = await productApi.getProductReviews(productId, { page: 0, size: 10 });
      setReviews(data.content || []);
    } catch (err) {
      console.error('Error fetching reviews:', err);
    } finally {
      setLoading(false);
    }
  };

  const checkUserReview = async () => {
    try {
      const userData = await authApi.getCurrentUser();
      setUser(userData);
    } catch (err) {
      setUser(null);
    }
  };

  const handleSubmitReview = async (e) => {
    e.preventDefault();

    if (!newRating) {
      showNotification('Пожалуйста, выберите оценку', 'error');
      return;
    }

    if (!newComment.trim()) {
      showNotification('Пожалуйста, напишите отзыв', 'error');
      return;
    }

    setSubmitting(true);
    try {
      await productApi.addReview(productId, newComment.trim(), newRating);
      showNotification('Отзыв успешно добавлен!', 'success');
      setNewComment('');
      setNewRating(0);
      setHasReviewed(true);
      fetchReviews();
    } catch (err) {
      console.error('Error adding review:', err);
      if (err.response?.status === 400) {
        showNotification('Вы уже оставляли отзыв на этот товар', 'error');
      } else {
        showNotification('Не удалось добавить отзыв', 'error');
      }
    } finally {
      setSubmitting(false);
    }
  };

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('ru-RU', {
      day: 'numeric',
      month: 'long',
      year: 'numeric'
    });
  };

  return (
    <div className="product-reviews">
      <h3 className="product-reviews__title">Отзывы</h3>

      {user && !hasReviewed && (
        <form className="product-reviews__form" onSubmit={handleSubmitReview}>
          <div className="product-reviews__form-rating">
            <span>Ваша оценка:</span>
            <StarRating
              rating={newRating}
              onRate={(rating) => setNewRating(rating)}
              interactive={true}
              size={24}
            />
          </div>
          <textarea
            className="product-reviews__form-input"
            placeholder="Напишите ваш отзыв..."
            value={newComment}
            onChange={(e) => setNewComment(e.target.value)}
            rows={4}
          />
          <button
            type="submit"
            className="product-reviews__form-btn"
            disabled={submitting}
          >
            {submitting ? 'Отправка...' : 'Отправить отзыв'}
          </button>
        </form>
      )}

      {user && hasReviewed && (
        <div className="product-reviews__already">
          Вы уже оставили отзыв на этот товар
        </div>
      )}

      {!user && (
        <div className="product-reviews__login-prompt">
          <Link to="/login">Войдите</Link>, чтобы оставить отзыв
        </div>
      )}

      {loading ? (
        <div className="product-reviews__loading">Загрузка отзывов...</div>
      ) : reviews.length === 0 ? (
        <div className="product-reviews__empty">Отзывов пока нет. Будьте первым!</div>
      ) : (
        <div className="product-reviews__list">
          {reviews.map((review) => (
            <div key={review.reviewId} className="product-reviews__item">
              <div className="product-reviews__item-header">
                <div className="product-reviews__item-user">
                  <FaUser className="product-reviews__item-icon" />
                  <span>{review.username}</span>
                </div>
                <StarRating rating={review.rating} size={16} />
              </div>
              <p className="product-reviews__item-comment">{review.comment}</p>
              <span className="product-reviews__item-date">
                {formatDate(review.createdAt)}
              </span>
            </div>
          ))}
        </div>
      )}

      {showAllLink && (
        <Link to={`/product/${productId}/reviews`} className="product-reviews__all-link">
          Показать все отзывы
        </Link>
      )}
    </div>
  );
};

export default ProductReviews;