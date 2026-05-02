package org.beatrice.diploma_new_pharmacy.domain.auth.service.model;

public record RegistrationCommand(
        String email,
        String phone,
        String password,
        String firstName,
        String lastName
) {
}
