package org.beatrice.diploma_new_pharmacy.domain.order.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

public record CreateOrderRequest(
        Integer pharmacyId,

        @Pattern(regexp = "^(\\+7|8)\\d{10}$")
        String phone,

        @Email
        String email,

        String firstName,
        String lastName
) {
}
