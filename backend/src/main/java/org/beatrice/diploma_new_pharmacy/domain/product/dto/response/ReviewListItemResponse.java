package org.beatrice.diploma_new_pharmacy.domain.product.dto.response;

import java.time.Instant;

public record ReviewListItemResponse(
        Integer reviewId,
        Short rating,
        String comment,
        String username,
        Integer userId,
        Instant createdAt
) {
}