package org.beatrice.diploma_new_pharmacy.domain.order.dto.response;

import org.beatrice.diploma_new_pharmacy.domain.order.model.OrderStatus;

public record PickupCodeVerificationResponse(
        boolean isValid,
        Integer orderId,
        String customerName,
        String pharmacyName,
        OrderStatus currentStatus,
        String message
) {

}
