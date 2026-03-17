package org.beatrice.diploma_new_pharmacy.admin.dto;

import java.math.BigDecimal;

public record AddProductRequest(
        String categoryName,
        String name,
        String description,
        String manufacturer,
        BigDecimal price
) {
}
