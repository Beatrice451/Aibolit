package org.beatrice.diploma_new_pharmacy.domain.product.service;

import lombok.RequiredArgsConstructor;
import org.beatrice.diploma_new_pharmacy.domain.product.dto.response.ReviewListItemResponse;
import org.beatrice.diploma_new_pharmacy.domain.product.dto.response.UserReviewResponse;
import org.beatrice.diploma_new_pharmacy.domain.product.exception.ProductNotFoundException;
import org.beatrice.diploma_new_pharmacy.domain.product.exception.ReviewAlreadyExistsException;
import org.beatrice.diploma_new_pharmacy.domain.product.mapper.ReviewMapper;
import org.beatrice.diploma_new_pharmacy.domain.product.model.Review;
import org.beatrice.diploma_new_pharmacy.domain.product.repository.ProductRepository;
import org.beatrice.diploma_new_pharmacy.domain.product.repository.ReviewRepository;
import org.beatrice.diploma_new_pharmacy.domain.user.service.UserService;
import org.beatrice.diploma_new_pharmacy.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {
    private final UserService userService;
    private final ReviewMapper reviewMapper;
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;

    public ReviewListItemResponse addReview(
            Integer userId,
            Integer productId,
            String comment,
            Short rating
    ) {
        if (rating < 1 || rating > 5) {
            throw new IllegalStateException("Rating must be between 1 and 5");
        }

        var user = userService.getUserById(userId);
        var product = productRepository.findProductById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        if (reviewRepository.existsByUser_IdAndProduct_Id(userId, productId)) {
            throw new ReviewAlreadyExistsException("You can review a product only once");
        }


        var rewiew = new Review(user, product, comment, rating);

        reviewRepository.save(rewiew);

        return reviewMapper.toListItemDto(rewiew);
    }

    public Page<UserReviewResponse> getReviewsByUser(Integer userId, Pageable pageable) {
        Page<Review> page = reviewRepository.findByUser_Id(userId, pageable);
        return page.map(reviewMapper::toUserReviewDto);
    }

    public Page<ReviewListItemResponse> getReviewsByProduct(Integer productId, Pageable pageable) {
        Page<Review> page = reviewRepository.findByProduct_Id(productId, pageable);
        return page.map(reviewMapper::toListItemDto);
    }

    public double getAverageRating(Integer productId) {
        Double avg = reviewRepository.findAverageRatingByProductId(productId);
        return avg != null ? avg : 0.0;
    }

    public int getReviewCount(Integer productId) {
        return reviewRepository.countByProduct_Id(productId);
    }

    public void deleteReview(Integer reviewId, Integer userId, boolean isAdmin) {
        Review review;

        if (isAdmin) {
            review = reviewRepository.findById(reviewId)
                    .orElseThrow(() -> new NotFoundException("Review with id " + reviewId + " not found"));
        } else {
            review = reviewRepository.findByIdAndUser_Id(reviewId, userId)
                    .orElseThrow(() -> new AccessDeniedException("You have no access to delete this review"));
        }

        reviewRepository.delete(review);
    }

}
