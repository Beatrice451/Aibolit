package org.beatrice.diploma_new_pharmacy.domain.pharmacy.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record PharmacyResponse(
        @Schema(description = "ID аптеки", example = "1")
        Integer id,

        @Schema(description = "Название аптеки", example = "Аптека №1")
        String name,

        @Schema(description = "Адрес аптеки", example = "ул. Ленина, 15")
        String address,

        @Schema(description = "Номер телефона", example = "+7 (495) 123-45-67")
        String phone,

        @Schema(description = "Активна ли аптека", example = "true")
        Boolean isActive
) {
}
