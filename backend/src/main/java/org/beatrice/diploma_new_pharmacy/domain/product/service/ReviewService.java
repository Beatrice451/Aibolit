package org.beatrice.diploma_new_pharmacy.domain.product.service;

import lombok.RequiredArgsConstructor;
import org.beatrice.diploma_new_pharmacy.domain.product.dto.response.ReviewResponse;
import org.beatrice.diploma_new_pharmacy.domain.product.exception.ReviewAlreadyExistsException;
import org.beatrice.diploma_new_pharmacy.domain.product.mapper.ReviewMapper;
import org.beatrice.diploma_new_pharmacy.domain.product.model.Review;
import org.beatrice.diploma_new_pharmacy.domain.product.repository.ReviewRepository;
import org.beatrice.diploma_new_pharmacy.domain.user.service.UserService;
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
    private final ProductService productService;
    private final ReviewMapper reviewMapper;
    private final ReviewRepository reviewRepository;

    public ReviewResponse addReview(
            Integer userId,
            Integer productId,
            String comment,
            Short rating
    ) {
        if (rating < 1 || rating > 5) {
            throw new IllegalStateException("Rating must be between 1 and 5");
        }

        var user = userService.getUserById(userId);
        var product = productService.getProductEntityById(productId);

        if (reviewRepository.existsByUser_IdAndProduct_Id(userId, productId)) {
            throw new ReviewAlreadyExistsException("You can review a product only once");
        }


        var rewiew = new Review(user, product, comment, rating);

        reviewRepository.save(rewiew);

        return reviewMapper.toDto(rewiew);
    }

    public Page<ReviewResponse> getReviewsByProduct(Integer productId, Pageable pageable) {
        Page<Review> page = reviewRepository.findByProduct_Id(productId, pageable);
        return page.map(reviewMapper::toDto);
    }

    public double getAverageRating(Integer productId) {
        Double avg = reviewRepository.findAverageRatingByProductId(productId);
        return avg != null ? avg : 0.0;
    }

    public void deleteReview(Integer reviewId, Integer userId) {
        var review = reviewRepository.findByIdAndUser_Id(reviewId, userId)
                .orElseThrow(() -> new AccessDeniedException("You have no access to edit this review"));

        reviewRepository.delete(review);
    }

}
