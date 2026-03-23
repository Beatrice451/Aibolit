package org.beatrice.diploma_new_pharmacy.domain.product.dto.command;

import jakarta.validation.constraints.DecimalMin;
import lombok.Builder;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;

@Builder
public record UpdateProductCommand(
        @Nullable
        Integer categoryId,

        @Nullable
        String name,

        @Nullable
        String description,

        @Nullable
        String manufacturer,

        @Nullable
        String imageUrl,

        BigDecimal price,

        Boolean isActive
) {
}
