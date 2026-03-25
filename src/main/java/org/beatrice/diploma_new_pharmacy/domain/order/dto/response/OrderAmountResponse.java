package org.beatrice.diploma_new_pharmacy.domain.order.dto.response;

import java.math.BigDecimal;

public record OrderAmountResponse(
        BigDecimal total,
        BigDecimal discount,
        BigDecimal finalAmount
) {
}
