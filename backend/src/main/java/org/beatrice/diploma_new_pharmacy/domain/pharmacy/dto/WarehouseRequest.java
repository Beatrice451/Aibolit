package org.beatrice.diploma_new_pharmacy.domain.pharmacy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record WarehouseRequest(
        @Schema(description = "Название склада", example = "Центральный склад", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Название склада обязательно")
        String name,

        @Schema(description = "Адрес склада", example = "ул. Складская, 10", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Адрес склада обязателен")
        String address,

        @Schema(description = "ID аптеки (null для общего склада)", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Integer pharmacyId
) {
}