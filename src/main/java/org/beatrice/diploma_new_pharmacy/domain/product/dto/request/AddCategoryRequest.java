package org.beatrice.diploma_new_pharmacy.domain.product.dto.request;

public record AddCategoryRequest(
        String name,
        Integer parentId
) {
}
