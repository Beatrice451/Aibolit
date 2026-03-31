package org.beatrice.diploma_new_pharmacy.domain.cart.dto;

import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CartResponse(
        Integer id,
        @Nullable UUID guestUuid, // not null only if the user is a guest
        List<CartItemResponse> items,
        BigDecimal totalPrice,
        int totalItems,
        Instant updatedAt
) {

    @Override
    public BigDecimal totalPrice() {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return items.stream()
                .map(item -> item.price().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public int totalItems() {
        if (items == null) {
            return 0;
        }

        return items.stream().mapToInt(CartItemResponse::quantity).sum();
    }
}
