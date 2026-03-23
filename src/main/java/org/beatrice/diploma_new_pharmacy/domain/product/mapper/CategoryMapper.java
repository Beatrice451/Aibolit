package org.beatrice.diploma_new_pharmacy.domain.product.mapper;

import org.beatrice.diploma_new_pharmacy.domain.product.dto.response.CategoryResponse;
import org.beatrice.diploma_new_pharmacy.domain.product.model.Category;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryMapper {
    public static CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
            category.getId(),
            category.getName(),
            category.getChildren().stream()
                    .map(CategoryMapper::toResponse)
                    .toList()
        );
    }

    public static List<CategoryResponse> toTree(List<Category> categories) {
        // Строим дерево: фильтруем корни и мапим
        Map<Integer, CategoryResponse> map = new HashMap<>();
        for (Category cat : categories) {
            map.put(cat.getId(), new CategoryResponse(cat.getId(), cat.getName(), new ArrayList<>()));
        }

        List<CategoryResponse> roots = new ArrayList<>();
        for (Category cat : categories) {
            if (cat.getParent() != null) {
                map.get(cat.getParent().getId()).children().add(map.get(cat.getId()));
            } else {
                roots.add(map.get(cat.getId()));
            }
        }
        return roots;
    }
}
