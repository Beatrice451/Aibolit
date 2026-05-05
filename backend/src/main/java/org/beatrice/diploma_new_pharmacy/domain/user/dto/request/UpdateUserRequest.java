package org.beatrice.diploma_new_pharmacy.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name must be less than 100 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 100, message = "Last name must be less than 100 characters")
        String lastName,

        @NotBlank(message = "Phone is required")
        @Size(max = 20, message = "Phone must be less than 20 characters")
        String phone,

        Integer preferredPharmacyId
) {
}