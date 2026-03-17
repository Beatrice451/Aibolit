package org.beatrice.diploma_new_pharmacy.domain.cart.dto;

import java.math.BigDecimal;

public record CartItemDto(
        Integer productId,
        String productName,
        String productImage,
        BigDecimal price,
        int quantity
) {
}
