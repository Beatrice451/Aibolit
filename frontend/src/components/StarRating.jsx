import React from 'react';
import { FaStar, FaStarHalfAlt, FaRegStar } from 'react-icons/fa';

const StarRating = ({ rating, onRate, interactive = false, size = 20 }) => {
  const stars = [];
  const fullStars = Math.floor(rating);
  const hasHalfStar = rating % 1 >= 0.5;
  const emptyStars = 5 - fullStars - (hasHalfStar ? 1 : 0);

  for (let i = 0; i < fullStars; i++) {
    stars.push(
      <FaStar
        key={`full-${i}`}
        size={size}
        className={interactive ? 'star-rating__star--interactive' : ''}
        onClick={() => interactive && onRate && onRate(i + 1)}
        style={interactive ? { cursor: 'pointer' } : {}}
      />
    );
  }

  if (hasHalfStar) {
    stars.push(
      <FaStarHalfAlt
        key="half"
        size={size}
        className={interactive ? 'star-rating__star--interactive' : ''}
        onClick={() => interactive && onRate && onRate(fullStars + 1)}
        style={interactive ? { cursor: 'pointer' } : {}}
      />
    );
  }

  for (let i = 0; i < emptyStars; i++) {
    stars.push(
      <FaRegStar
        key={`empty-${i}`}
        size={size}
        className={interactive ? 'star-rating__star--interactive' : ''}
        onClick={() => interactive && onRate && onRate(fullStars + (hasHalfStar ? 1 : 0) + i + 1)}
        style={interactive ? { cursor: 'pointer' } : {}}
      />
    );
  }

  return <div className="star-rating">{stars}</div>;
};

export default StarRating;