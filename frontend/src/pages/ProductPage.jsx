import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import Header from '../components/Header';
import ProductReviews from '../components/ProductReviews';
import StarRating from '../components/StarRating';
import productApi from '../api/productService';
import cartApi from '../api/cartService';
import { showNotification } from '../components/NotificationSystem';
import { FaPills, FaShoppingCart, FaTruck, FaCreditCard } from 'react-icons/fa';
import '../scss/pages/_product.scss';

const getImageUrl = (imageUrl) => {
  if (!imageUrl) return null;
  if (imageUrl.startsWith('http://') || imageUrl.startsWith('https://')) {
    return imageUrl;
  }
  return `/media/${imageUrl}`;
};

const findCategoryPath = (categories, targetId, path = []) => {
  for (const category of categories) {
    const currentPath = [...path, { id: category.id, name: category.name }];
    if (category.id === targetId) {
      return currentPath;
    }
    if (category.children && category.children.length > 0) {
      const found = findCategoryPath(category.children, targetId, currentPath);
      if (found) return found;
    }
  }
  return null;
};

const ProductPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [product, setProduct] = useState(null);
  const [categoriesTree, setCategoriesTree] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [quantity, setQuantity] = useState(1);
  const [adding, setAdding] = useState(false);
  const [inCart, setInCart] = useState(false);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        const [productData, categories] = await Promise.all([
          productApi.getProductById(id),
          productApi.getCategoriesTree()
        ]);
        console.log('Product data:', productData);
        console.log('Categories tree:', categories);
        setProduct(productData);
        setCategoriesTree(categories);
      } catch (err) {
        setError('Не удалось загрузить товар');
        console.error('Error fetching product:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [id]);

  const getCategoryPath = () => {
    if (!product?.categoryId || categoriesTree.length === 0) return [];
    return findCategoryPath(categoriesTree, product.categoryId) || [];
  };

  const incrementQuantity = () => {
    setQuantity(prev => prev + 1);
  };

  const decrementQuantity = () => {
    if (quantity > 1) {
      setQuantity(prev => prev - 1);
    }
  };

  const handleDecrementQuantity = async () => {
    if (quantity > 1) {
      setQuantity(prev => prev - 1);
    } else {
      // Удаляем товар из корзины
      try {
        await cartApi.removeItem(product.id);
        showNotification(`"${product.name}" удален из корзины`, 'success');
        setInCart(false);
        setQuantity(1);
      } catch (err) {
        console.error('Error removing from cart:', err);
        showNotification('Не удалось удалить из корзины', 'error');
      }
    }
  };

  const handleAddToCart = async () => {
    if (inCart) {
      // Если товар уже в корзине, переходим к корзине
      navigate('/cart');
      return;
    }

    setAdding(true);
    try {
      await cartApi.addItem(product.id, quantity);
      showNotification(`Добавлено ${quantity} шт. "${product.name}" в корзину!`, 'success');
      setInCart(true);
    } catch (err) {
      console.error('Error adding to cart:', err);
      showNotification('Не удалось добавить в корзину', 'error');
    } finally {
      setAdding(false);
    }
  };

  if (loading) {
    return (
      <>
        <Header />
        <div className="product-page">
          <div className="container">
            <div className="product-page__loading">Загрузка...</div>
          </div>
        </div>
      </>
    );
  }

  if (error || !product) {
    return (
      <>
        <Header />
        <div className="product-page">
          <div className="container">
            <div className="product-page__error">{error || 'Товар не найден'}</div>
          </div>
        </div>
      </>
    );
  }

  const categoryPath = getCategoryPath();

  return (
    <>
      <Header />
      <div className="product-page">
        <div className="container">
          <div className="product-page__content">
            <div className="product-page__breadcrumb">
              <Link to="/" className="product-page__breadcrumb-link">Главная</Link>
              {categoryPath.map((cat, index) => (
                <React.Fragment key={cat.id}>
                  <span className="product-page__breadcrumb-separator">&gt;</span>
                  <Link
                    to={`/?categoryId=${cat.id}`}
                    className="product-page__breadcrumb-link"
                  >
                    {cat.name}
                  </Link>
                </React.Fragment>
              ))}
              <span className="product-page__breadcrumb-separator">&gt;</span>
              <span className="product-page__breadcrumb-current">{product.name}</span>
            </div>

            <h1 className="product-page__title">{product.name}</h1>

            <div className="product-page__rating">
              <StarRating rating={product.averageRating || 0} size={20} />
              <span className="product-page__rating-count">
                ({product.reviewCount || 0} отзывов)
              </span>
            </div>

            <div className="product-page__product-row">
              <div className="product-page__image-section">
                <div className="product-page__image">
                  {product.imageUrl ? (
                    <img src={getImageUrl(product.imageUrl)} alt={product.name} />
                  ) : (
                    <div className="product-page__image-placeholder">
                      <FaPills />
                    </div>
                  )}
                </div>
              </div>

              <div className="product-page__details-section">
                {product.manufacturer && (
                  <div className="product-page__detail-row">
                    <span className="product-page__detail-label">Производитель:</span>
                    <span className="product-page__detail-value">{product.manufacturer}</span>
                  </div>
                )}

                {product.form && (
                  <div className="product-page__detail-row">
                    <span className="product-page__detail-label">Форма выпуска:</span>
                    <span className="product-page__detail-value">{product.form}</span>
                  </div>
                )}

                {product.dosage !== undefined && product.dosage !== null && (
                  <div className="product-page__detail-row">
                    <span className="product-page__detail-label">Дозировка:</span>
                    <span className="product-page__detail-value">{product.dosage} мг</span>
                  </div>
                )}

                {product.quantity !== undefined && product.quantity !== null && (
                  <div className="product-page__detail-row">
                    <span className="product-page__detail-label">Количество в упаковке:</span>
                    <span className="product-page__detail-value">{product.quantity} шт.</span>
                  </div>
                )}

                {product.requiresPrescription !== undefined && (
                  <div className="product-page__detail-row">
                    <span className="product-page__detail-label">Рецепт:</span>
                    <span className="product-page__detail-value">
                      {product.requiresPrescription ? 'Требуется' : 'Не требуется'}
                    </span>
                  </div>
                )}
              </div>

              <div className="product-page__purchase-section">
                <div className="product-page__purchase-card">
                  <span className="product-page__price-label">Цена</span>
                  <div className="product-page__price-block">
                    <span className="product-page__price">{Number(product.price).toFixed(2).split('.')[0]}</span>
                    <span className="product-page__price-decimal">.{Number(product.price).toFixed(2).split('.')[1]}</span>
                    <span className="product-page__price-currency"> ₽</span>
                  </div>

                  {inCart ? (
                    <div className="product-page__cart-controls">
                      <div className="product-page__quantity-selector">
                        <button
                          className="product-page__quantity-btn"
                          onClick={handleDecrementQuantity}
                        >
                          -
                        </button>
                        <span className="product-page__quantity-value">{quantity}</span>
                        <button
                          className="product-page__quantity-btn"
                          onClick={incrementQuantity}
                        >
                          +
                        </button>
                      </div>
                      <button
                        className="product-page__add-to-cart-btn"
                        onClick={handleAddToCart}
                        disabled={adding}
                      >
                        <span>{adding ? 'Добавление...' : 'перейти к оформлению'}</span>
                        <FaShoppingCart />
                      </button>
                    </div>
                  ) : (
                    <button
                      className="product-page__add-to-cart-btn"
                      onClick={handleAddToCart}
                      disabled={adding}
                    >
                      <span>{adding ? 'Добавление...' : 'В корзину'}</span>
                      <FaShoppingCart />
                    </button>
                  )}
                </div>

                <div className="product-page__info-card">
                  <div className="product-page__info-row">
                    <FaCreditCard className="product-page__info-icon" />
                    <span>оплата при получении</span>
                  </div>
                </div>
              </div>
            </div>

            {product.description && (
              <div className="product-page__description">
                <h3>Описание</h3>
                <p>{product.description}</p>
              </div>
            )}

            <ProductReviews productId={product.id} />
          </div>
        </div>
      </div>
    </>
  );
};

export default ProductPage;