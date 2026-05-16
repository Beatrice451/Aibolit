package org.beatrice.diploma_new_pharmacy.domain.pharmacy.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record StockResponse(
        @Schema(description = "ID товара", example = "1")
        Integer productId,

        @Schema(description = "Название товара", example = "Аспирин")
        String productName,

        @Schema(description = "ID склада", example = "1")
        Integer warehouseId,

        @Schema(description = "Название склада", example = "Центральный склад")
        String warehouseName,

        @Schema(description = "Количество на складе", example = "100")
        Integer quantity,

        @Schema(description = "Зарезервированное количество", example = "5")
        Integer reserved
) {
}
