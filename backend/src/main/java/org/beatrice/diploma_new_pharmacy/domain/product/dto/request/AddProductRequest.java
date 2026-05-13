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
        BigDecimal price,

        @Schema(description = "Тип продукта: PRODUCT или MEDICINE", example = "PRODUCT", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String productType,

        @Schema(description = "Дозировка в миллиграммах (только для лекарств)", example = "500", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Integer dosage,

        @Schema(description = "Требуется ли рецепт (только для лекарств)", example = "false", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Boolean requiresPrescription,

        @Schema(description = "Форма выпуска (только для лекарств)", example = "таблетки", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String form,

        @Schema(description = "Количество единиц в упаковке (только для лекарств)", example = "20", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Short quantity
) {
}
