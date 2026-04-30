package org.beatrice.diploma_new_pharmacy.domain.product.controller;

import lombok.RequiredArgsConstructor;
import org.beatrice.diploma_new_pharmacy.domain.auth.security.SecurityUser;
import org.beatrice.diploma_new_pharmacy.domain.product.dto.request.AddReviewRequest;
import org.beatrice.diploma_new_pharmacy.domain.product.dto.response.MedicineResponse;
import org.beatrice.diploma_new_pharmacy.domain.product.dto.response.ProductResponse;
import org.beatrice.diploma_new_pharmacy.domain.product.dto.response.ReviewResponse;
import org.beatrice.diploma_new_pharmacy.domain.product.service.ProductService;
import org.beatrice.diploma_new_pharmacy.domain.product.service.ReviewService;
import org.beatrice.diploma_new_pharmacy.domain.product.specification.ProductFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
class ProductController {

    private final ProductService productService;
    private final ReviewService reviewService;

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getProducts(
            @ModelAttribute ProductFilter filter,
            Pageable pageable
    ) {
        Page<ProductResponse> products = productService.getProductsByFilter(filter, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/medicines")
    public ResponseEntity<List<MedicineResponse>> getAllMedicines() {
        return ResponseEntity.ok(productService.getAllMedicines());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Integer id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/{id}/reviews")
    public ResponseEntity<Page<ReviewResponse>> getProductReviews(@PathVariable Integer id, Pageable pageable) {
        return ResponseEntity.ok(reviewService.getReviewsByProduct(id, pageable));
    }

    @PostMapping("/{id}/reviews")
    public ResponseEntity<ReviewResponse> addReview(
            @PathVariable Integer id, @RequestBody AddReviewRequest request, @AuthenticationPrincipal
            SecurityUser user
    ) {
        ReviewResponse review = reviewService.addReview(user.user().getId(), id, request.comment(), request.rating());
        URI location = ServletUriComponentsBuilder
        .fromCurrentRequest()
        .path("/{reviewId}")
        .buildAndExpand(review.reviewId())
        .toUri();
        return ResponseEntity.created(location).body(review);
    }

}
