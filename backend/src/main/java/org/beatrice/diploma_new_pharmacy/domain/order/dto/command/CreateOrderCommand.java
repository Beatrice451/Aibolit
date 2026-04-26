package org.beatrice.diploma_new_pharmacy.domain.order.dto.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import org.beatrice.diploma_new_pharmacy.domain.order.dto.OrderIdentity;

public record CreateOrderCommand(
        OrderIdentity identity,
        Integer pharmacyId,

        @Pattern(regexp = "^(\\+7|8)\\d{10}$")
        String phone,

        @Email
        String email,

        String firstName,
        String lastName
) {
}
