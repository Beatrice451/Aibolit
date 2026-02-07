package org.beatrice.diploma_new_pharmacy.product.repository;

import org.beatrice.diploma_new_pharmacy.product.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

interface ProductRepository extends JpaRepository<Product, Integer> {
}
