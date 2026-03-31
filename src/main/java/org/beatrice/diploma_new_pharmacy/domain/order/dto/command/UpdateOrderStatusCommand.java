package org.beatrice.diploma_new_pharmacy.domain.order.dto.command;

import org.beatrice.diploma_new_pharmacy.domain.order.model.OrderStatus;

public record UpdateOrderStatusCommand(OrderStatus orderStatus) {

}
