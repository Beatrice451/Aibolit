package org.beatrice.diploma_new_pharmacy.domain.pharmacy.dto;

public record PharmacyRequest(
        String name,
        String address,
        String phoneNumber,
        Boolean isActive
) {
}
