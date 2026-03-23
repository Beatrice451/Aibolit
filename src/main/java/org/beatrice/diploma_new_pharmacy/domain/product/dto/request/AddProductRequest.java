package org.beatrice.diploma_new_pharmacy.domain.product.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record AddProductRequest(

        @Schema(description = "ID существующей категории", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer categoryId,

        @Schema(description = "Название продукта", example = "Уголь активированный 500 мг", requiredMode = Schema.RequiredMode.REQUIRED)
        String name,

        @Schema(description = "Описание продукта", requiredMode = Schema.RequiredMode.REQUIRED)
        String description,

        @Schema(description = "Производитель", requiredMode = Schema.RequiredMode.REQUIRED)
        String manufacturer,

        @Schema(
                description = "Ссылка на изображение",
                example = "/media/cot.png",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String imageUrl,

        @Schema(description = "Цена продукта", example = "1500.00", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal price
) {
}
