package org.beatrice.diploma_new_pharmacy.domain.order.dto;

public record CreateOrderCommand(
        OrderIdentity identity,
        Integer pharmacyId
) {
}
