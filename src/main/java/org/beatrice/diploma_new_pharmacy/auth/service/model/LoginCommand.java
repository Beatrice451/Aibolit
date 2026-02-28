package org.beatrice.diploma_new_pharmacy.auth.service.model;

public record LoginCommand(String email, String rawPassword) {
}
