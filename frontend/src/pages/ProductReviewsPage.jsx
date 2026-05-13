import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import Pagination from '../components/Pagination';
import StarRating from '../components/StarRating';
import productApi from '../api/productService';
import '../scss/pages/_product-reviews.scss';

const ProductReviewsPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();

  const [product, setProduct] = useState(null);
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(true);
  const [totalPages, setTotalPages] = useState(0);
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize] = useState(10);

  useEffect(() => {
    fetchProduct();
    fetchReviews(currentPage);
  }, [id, currentPage]);

  const fetchProduct = async () => {
    try {
      const data = await productApi.getProductById(id);
      setProduct(data);
    } catch (err) {
      console.error('Error fetching product:', err);
    }
  };

  const fetchReviews = async (page) => {
    try {
      setLoading(true);
      const data = await productApi.getProductReviews(id, { page, size: pageSize });
      setReviews(data.content || []);
      setTotalPages(data.totalPages || 0);
    } catch (err) {
      console.error('Error fetching reviews:', err);
    } finally {
      setLoading(false);
    }
  };

  const handlePageChange = (page) => {
    setCurrentPage(page);
  };

  const handleGoBack = () => {
    navigate(-1);
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
    <>
      <Header />
      <div className="product-reviews-page">
        <div className="container">
          <button className="product-reviews-page__back-btn" onClick={handleGoBack}>
            ← Вернуться к товару
          </button>

          {product && (
            <div className="product-reviews-page__header">
              <h1 className="product-reviews-page__title">Отзывы о товаре</h1>
              <h2 className="product-reviews-page__product-name">{product.name}</h2>

              {(product.averageRating > 0 || product.reviewCount > 0) && (
                <div className="product-reviews-page__rating-summary">
                  <span className="product-reviews-page__rating-value">
                    {product.averageRating?.toFixed(1) || '0.0'}
                  </span>
                  <StarRating rating={product.averageRating || 0} size={24} />
                  <span className="product-reviews-page__rating-count">
                    на основе {product.reviewCount} отзывов
                  </span>
                </div>
              )}
            </div>
          )}

          {loading ? (
            <div className="product-reviews-page__loading">Загрузка отзывов...</div>
          ) : reviews.length === 0 ? (
            <div className="product-reviews-page__empty">
              Отзывов пока нет. Будьте первым, кто оставит отзыв!
            </div>
          ) : (
            <>
              <div className="product-reviews-page__list">
                {reviews.map((review) => (
                  <div key={review.reviewId} className="product-reviews-page__item">
                    <div className="product-reviews-page__item-header">
                      <div className="product-reviews-page__item-user">
                        <span className="product-reviews-page__item-avatar">👤</span>
                        <span className="product-reviews-page__item-name">{review.username}</span>
                      </div>
                      <StarRating rating={review.rating} size={18} />
                    </div>
                    <p className="product-reviews-page__item-comment">{review.comment}</p>
                    <span className="product-reviews-page__item-date">
                      {formatDate(review.createdAt)}
                    </span>
                  </div>
                ))}
              </div>

              {totalPages > 1 && (
                <Pagination
                  currentPage={currentPage}
                  totalPages={totalPages}
                  onPageChange={handlePageChange}
                />
              )}
            </>
          )}
        </div>
      </div>
    </>
  );
};

export default ProductReviewsPage;