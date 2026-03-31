package org.beatrice.diploma_new_pharmacy.domain.order.dto.request;

import org.beatrice.diploma_new_pharmacy.domain.order.model.OrderStatus;

public record UpdateOrderStatusRequest(
        OrderStatus orderStatus
) {
}
