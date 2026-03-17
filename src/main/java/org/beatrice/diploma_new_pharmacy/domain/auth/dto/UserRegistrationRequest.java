package org.beatrice.diploma_new_pharmacy.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserRegistrationRequest(
        String name,

        @Email(message = "Invalid email format")
        @NotBlank(message = "Email required")
        String email,

        @Pattern(regexp = "^(\\+7|8)\\d{10}$")
        @NotBlank(message = "Phone required")
        String phone,

        @NotBlank(message = "Password required")
        String password
) {
}
