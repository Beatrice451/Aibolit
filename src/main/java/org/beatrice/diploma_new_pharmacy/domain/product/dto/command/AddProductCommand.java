package org.beatrice.diploma_new_pharmacy.domain.product.dto.command;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record AddProductCommand(
        Integer categoryId,
        String name,
        String description,
        String manufacturer,
        String imageUrl,
        BigDecimal price
) {
}
