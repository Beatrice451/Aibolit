package org.beatrice.diploma_new_pharmacy.domain.order.dto;

import org.beatrice.diploma_new_pharmacy.domain.order.model.OrderStatus;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.dto.PharmacyResponse;

import java.time.Instant;
import java.util.List;


public record OrderResponse(
        Integer id,
        PharmacyResponse pharmacy,
        List<OrderItemDto> items,
        OrderStatus orderStatus,
        OrderAmountResponse amount,
        Instant createdAt,
        Instant updatedAt
) {
}
