package org.beatrice.diploma_new_pharmacy.domain.pharmacy.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ProductStockResponse(
        @Schema(description = "ID товара", example = "1")
        Integer productId,

        @Schema(description = "Общее доступное количество (с учетом резервов)", example = "95")
        Integer totalAvailable,

        @Schema(description = "Есть ли информация об остатках (false = 'уточняется')", example = "true")
        boolean tracked
) {
}
