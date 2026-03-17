package org.beatrice.diploma_new_pharmacy.admin.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record AddProductCommand(
        String categoryName,
        String name,
        String description,
        String manufacturer,
        BigDecimal price
) {
}
