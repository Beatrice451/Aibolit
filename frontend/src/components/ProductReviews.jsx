import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import productApi from '../api/productService';
import authApi from '../api/authService';
import StarRating from './StarRating';
import { showNotification } from './NotificationSystem';
import ConfirmDialog from './ConfirmDialog';
import { FaUser, FaTrash } from 'react-icons/fa';
import '../scss/components/_product-reviews.scss';

const ProductReviews = ({ productId, showAllLink = true }) => {
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(true);
  const [user, setUser] = useState(null);
  const [isAdmin, setIsAdmin] = useState(false);
  const [deletingId, setDeletingId] = useState(null);
  const [confirmDeleteId, setConfirmDeleteId] = useState(null);

  const [newComment, setNewComment] = useState('');
  const [newRating, setNewRating] = useState(0);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    fetchReviews();
    fetchCurrentUser();
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

  const fetchCurrentUser = async () => {
    try {
      const userData = await authApi.getCurrentUser();
      setUser(userData);

      const adminStatus = await authApi.isAdmin();
      setIsAdmin(!!adminStatus);
    } catch (err) {
      setUser(null);
      setIsAdmin(false);
    }
  };

  const myReview = user && reviews.find(r => r.userId === user.id);
  const otherReviews = reviews.filter(r => !user || r.userId !== user.id);
  const canDelete = (review) => isAdmin || (user && review.userId === user.id);

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

  const handleDeleteReview = async (reviewId) => {
    setDeletingId(reviewId);
    setConfirmDeleteId(null);
    try {
      await productApi.deleteReview(reviewId);
      showNotification('Отзыв удален', 'success');
      fetchReviews();
    } catch (err) {
      showNotification('Не удалось удалить отзыв', 'error');
    } finally {
      setDeletingId(null);
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

      {!user && (
        <div className="product-reviews__login-prompt">
          <Link to="/login">Войдите</Link>, чтобы оставить отзыв
        </div>
      )}

      {user && !myReview && (
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

      <ConfirmDialog
        isOpen={confirmDeleteId !== null}
        message="Вы уверены, что хотите удалить этот отзыв?"
        onConfirm={() => handleDeleteReview(confirmDeleteId)}
        onCancel={() => setConfirmDeleteId(null)}
      />

      {loading ? (
        <div className="product-reviews__loading">Загрузка отзывов...</div>
      ) : reviews.length === 0 ? (
        <div className="product-reviews__empty">Отзывов пока нет. Будьте первым!</div>
      ) : (
        <>
          {myReview && (
            <div className="product-reviews__my-review">
              <div className="product-reviews__my-review-header">
                <span className="product-reviews__my-review-label">Мой отзыв</span>
                <StarRating rating={myReview.rating} size={16} />
              </div>
              <p className="product-reviews__my-review-comment">{myReview.comment}</p>
              <div className="product-reviews__item-footer">
                <span className="product-reviews__item-date">
                  {formatDate(myReview.createdAt)}
                </span>
                <button
                  className="product-reviews__item-delete"
                  onClick={() => setConfirmDeleteId(myReview.reviewId)}
                  disabled={deletingId === myReview.reviewId}
                >
                  <FaTrash /> Удалить
                </button>
              </div>
            </div>
          )}

          {otherReviews.length > 0 && (
            <div className="product-reviews__list">
              {otherReviews.map((review) => (
                <div key={review.reviewId} className="product-reviews__item">
                  <div className="product-reviews__item-header">
                    <div className="product-reviews__item-user">
                      <FaUser className="product-reviews__item-icon" />
                      <span>{review.username}</span>
                    </div>
                    <StarRating rating={review.rating} size={16} />
                  </div>
                  <p className="product-reviews__item-comment">{review.comment}</p>
                  <div className="product-reviews__item-footer">
                    <span className="product-reviews__item-date">
                      {formatDate(review.createdAt)}
                    </span>
                    {canDelete(review) && (
                      <button
                        className="product-reviews__item-delete"
                        onClick={() => setConfirmDeleteId(review.reviewId)}
                        disabled={deletingId === review.reviewId}
                      >
                        <FaTrash /> Удалить
                      </button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </>
      )}

      {showAllLink && reviews.length > 0 && (
        <Link to={`/product/${productId}/reviews`} className="product-reviews__all-link">
          Показать все отзывы
        </Link>
      )}
    </div>
  );
};

export default ProductReviews;
