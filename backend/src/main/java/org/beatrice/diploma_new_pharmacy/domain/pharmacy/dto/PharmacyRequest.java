package org.beatrice.diploma_new_pharmacy.domain.pharmacy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PharmacyRequest(
        @Schema(description = "Название аптеки", example = "Аптека №1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Название аптеки обязательно")
        String name,

        @Schema(description = "Адрес аптеки", example = "ул. Ленина, 15", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Адрес аптеки обязателен")
        String address,

        @Schema(description = "Номер телефона", example = "+7 (495) 123-45-67", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @NotBlank(message = "Номер телефона не может быть пустым")
        @Size(max = 20, message = "Номер телефона не должен превышать 20 символов")
        String phone,

        @Schema(description = "Активна ли аптека", example = "true", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Boolean isActive
) {
}
