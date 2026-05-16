package org.beatrice.diploma_new_pharmacy.domain.pharmacy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StockRequest(
        @Schema(description = "ID товара", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "ID товара обязателен")
        Integer productId,

        @Schema(description = "ID склада", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "ID склада обязателен")
        Integer warehouseId,

        @Schema(description = "Количество на складе", example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Количество обязательно")
        @Min(value = 0, message = "Количество не может быть отрицательным")
        Integer quantity
) {
}
