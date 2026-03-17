package org.beatrice.diploma_new_pharmacy.domain.product.specification;

import org.beatrice.diploma_new_pharmacy.domain.product.model.Product;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class ProductSpecifications {
    public static Specification<Product> search(String searchTerm) {
        return ((root, query, criteriaBuilder) -> {
            if (searchTerm == null || searchTerm.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            String pattern = "%" + searchTerm.toLowerCase() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern)
            );
        });
    }

    public static Specification<Product> hasCategoryId(Integer categoryId) {
        return ((root, query, criteriaBuilder) ->
                categoryId == null ? criteriaBuilder.conjunction() : criteriaBuilder.equal(root.get("category").get("id"), categoryId)
        );
    }

    public static Specification<Product> manufacturerLike(String manufacturer) {
        return ((root, query, criteriaBuilder) -> {
            if (manufacturer == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("manufacturer")), "%" + manufacturer.toLowerCase() + "%");
        });
    }

    public static Specification<Product> priceBetween(BigDecimal min, BigDecimal max) {
        return ((root, query, criteriaBuilder) -> {
            if (min == null && max == null) return criteriaBuilder.conjunction();
            if (min != null && max != null) return criteriaBuilder.between(root.get("price"), min, max);
            if (min != null) return criteriaBuilder.greaterThanOrEqualTo(root.get("price"), min);
            return criteriaBuilder.lessThanOrEqualTo(root.get("price"), max);
        });
    }

    public static Specification<Product> activeOnly() {
    return (root, query, criteriaBuilder) -> criteriaBuilder.isTrue(root.get("isActive"));
}
}
