package org.beatrice.diploma_new_pharmacy.domain.product.dto.command;

import lombok.Builder;

@Builder
public record AddCategoryCommand(
        String name,
        Integer parentId
) {
}
