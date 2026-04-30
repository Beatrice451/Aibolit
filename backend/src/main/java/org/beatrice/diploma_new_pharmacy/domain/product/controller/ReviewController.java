package org.beatrice.diploma_new_pharmacy.domain.product.controller;

import org.beatrice.diploma_new_pharmacy.domain.auth.security.SecurityUser;
import org.beatrice.diploma_new_pharmacy.domain.product.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
class ReviewController {
    private final ReviewService reviewService;

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Integer id, @AuthenticationPrincipal SecurityUser user) {
        reviewService.deleteReview(id, user.user().getId());
        return ResponseEntity.noContent().build();
    }
}
