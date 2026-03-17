package org.beatrice.diploma_new_pharmacy.domain.product.repository;

import org.beatrice.diploma_new_pharmacy.domain.product.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    Category findCategoryByName(String name);
}
