package org.beatrice.diploma_new_pharmacy.domain.pharmacy.dto;

public record WarehouseResponse(
        Integer id,
        String name,
        String address,
        Integer pharmacyId,
        boolean isShared
) {
}