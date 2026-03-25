package org.beatrice.diploma_new_pharmacy.domain.order.specification;

import org.beatrice.diploma_new_pharmacy.domain.order.model.OrderStatus;

public record OrderFilter(
        OrderStatus orderStatus,
        Integer pharmacyId,
        Integer userId,
        String email,
        String phone
) {
}
