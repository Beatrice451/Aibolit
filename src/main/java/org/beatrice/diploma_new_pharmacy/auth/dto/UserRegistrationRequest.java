package org.beatrice.diploma_new_pharmacy.auth.dto;

public record UserRegistrationRequest(String name, String email, String phone, String password) {
}
