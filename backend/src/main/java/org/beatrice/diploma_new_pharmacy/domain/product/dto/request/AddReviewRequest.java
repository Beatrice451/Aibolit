package org.beatrice.diploma_new_pharmacy.domain.product.dto.request;

public record AddReviewRequest(
        Short rating,
        String comment
) {
}
