package org.beatrice.diploma_new_pharmacy.domain.product.dto.response;

import java.time.Instant;

public record UserReviewResponse(
        Integer reviewId,
        Short rating,
        String comment,
        Integer productId,
        String productName,
        String productImageUrl,
        Instant createdAt
) {
}