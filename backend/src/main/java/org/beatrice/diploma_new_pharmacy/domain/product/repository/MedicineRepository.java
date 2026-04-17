package org.beatrice.diploma_new_pharmacy.domain.product.repository;

import org.beatrice.diploma_new_pharmacy.domain.product.model.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicineRepository extends JpaRepository<Medicine, Integer> {
}
