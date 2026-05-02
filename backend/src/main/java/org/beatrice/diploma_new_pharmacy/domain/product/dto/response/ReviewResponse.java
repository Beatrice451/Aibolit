package org.beatrice.diploma_new_pharmacy.domain.product.dto.response;

import java.time.Instant;

public record ReviewResponse(
        Integer reviewId,
        Short rating,
        String comment,
        String username,
        Instant createdAt
) {
}
