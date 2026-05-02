package org.beatrice.diploma_new_pharmacy.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRegistrationRequest(
        @Email(message = "Invalid email format")
        @NotBlank(message = "Email required")
        String email,

        @Pattern(regexp = "^(\\+7|8)\\d{10}$")
        @NotBlank(message = "Phone required")
        String phone,

        @NotBlank(message = "Password required")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        String password,

        String firstName,
        String lastName
) {
}
