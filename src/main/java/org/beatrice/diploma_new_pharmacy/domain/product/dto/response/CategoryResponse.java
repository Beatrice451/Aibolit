package org.beatrice.diploma_new_pharmacy.domain.product.dto.response;

import java.util.List;

public record CategoryResponse(
        Integer id,
        String name,
        List<CategoryResponse> children
) {
}
