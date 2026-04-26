package org.beatrice.diploma_new_pharmacy.domain.product.specification;

import java.math.BigDecimal;
import java.util.List;

public record ProductFilter(
        String search,
        Integer categoryId,
        List<Integer> substanceIds,
        List<Integer> symptomIds,
        String manufacturer,
        BigDecimal minPrice,
        BigDecimal maxPrice
) {

}


