package org.beatrice.diploma_new_pharmacy.domain.order.dto.response;

import org.beatrice.diploma_new_pharmacy.domain.order.model.OrderStatus;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.dto.PharmacyResponse;

import java.time.Instant;
import java.util.List;


public record OrderResponse(
        Integer id,
        PharmacyResponse pharmacy,
        List<OrderItemResponse> items,
        OrderStatus orderStatus,
        OrderAmountResponse amount,
        Instant createdAt,
        Instant updatedAt,
        String phone,
        String email,
        String clientName
) {
}
