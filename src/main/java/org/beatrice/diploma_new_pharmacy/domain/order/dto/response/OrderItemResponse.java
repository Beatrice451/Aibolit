package org.beatrice.diploma_new_pharmacy.domain.order.dto.response;

import java.math.BigDecimal;

public record OrderItemResponse(
        Integer productId,
        String name,
        Short quantity,
        BigDecimal priceAtSale,
        BigDecimal total
) {

    @Override
    public BigDecimal total() {
        return priceAtSale.multiply(BigDecimal.valueOf(quantity));
    }
}
