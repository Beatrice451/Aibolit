package org.beatrice.diploma_new_pharmacy.domain.order.dto;

public record OrderReadyForPickupEvent(
        Integer orderId,
        String userEmail,
        String userName,
        String pickupCode,
        String pharmacyName,
        String pharmacyAddress
) {
}
