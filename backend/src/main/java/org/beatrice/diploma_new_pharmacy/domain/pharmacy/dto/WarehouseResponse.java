package org.beatrice.diploma_new_pharmacy.domain.pharmacy.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record WarehouseResponse(
        @Schema(description = "ID склада", example = "1")
        Integer id,

        @Schema(description = "Название склада", example = "Центральный склад")
        String name,

        @Schema(description = "Адрес склада", example = "ул. Складская, 10")
        String address,

        @Schema(description = "ID аптеки (null для общего склада)", example = "1")
        Integer pharmacyId,

        @Schema(description = "Является ли склад общим (не привязан к аптеке)", example = "false")
        boolean isShared
) {
}