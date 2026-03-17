package org.beatrice.diploma_new_pharmacy.domain.order.dto;

import org.beatrice.diploma_new_pharmacy.domain.cart.model.Cart;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.model.Pharmacy;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;

public record CreateOrderCommand(
        OrderIdentity identity,
        Integer pharmacyId
) {
}
