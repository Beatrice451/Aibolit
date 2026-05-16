package org.beatrice.diploma_new_pharmacy.domain.product.controller;

import org.beatrice.diploma_new_pharmacy.domain.auth.security.SecurityUser;
import org.beatrice.diploma_new_pharmacy.domain.product.dto.response.UserReviewResponse;
import org.beatrice.diploma_new_pharmacy.domain.product.service.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
class ReviewController {
    private final ReviewService reviewService;

    @GetMapping("/me")
    public ResponseEntity<Page<UserReviewResponse>> getMyReviews(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal SecurityUser user
    ) {
        return ResponseEntity.ok(reviewService.getReviewsByUser(user.user().getId(), pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Integer id, @AuthenticationPrincipal SecurityUser user) {
        boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        reviewService.deleteReview(id, user.user().getId(), isAdmin);
        return ResponseEntity.noContent().build();
    }
}
