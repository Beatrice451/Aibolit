package org.beatrice.diploma_new_pharmacy.product.repository;

import org.beatrice.diploma_new_pharmacy.product.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

interface CategoryRepository extends JpaRepository<Category, Integer> {
}
