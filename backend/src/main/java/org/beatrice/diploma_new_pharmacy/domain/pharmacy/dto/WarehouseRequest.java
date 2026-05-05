package org.beatrice.diploma_new_pharmacy.domain.pharmacy.dto;

public record WarehouseRequest(
        String name,
        String address,
        Integer pharmacyId
) {
}