package org.beatrice.diploma_new_pharmacy.domain.product.dto.request;

import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

public record UpdateProductRequest(
        Integer categoryId,
        String name,
        String description,
        String manufacturer,
        String imageUrl,
        Boolean isActive,

        @DecimalMin(value = "0.0")
        BigDecimal price
) {
}
