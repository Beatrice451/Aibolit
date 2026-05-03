package org.beatrice.diploma_new_pharmacy.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResendVerificationRequest(

        @NotBlank
        @Email
        String email
) {
}
