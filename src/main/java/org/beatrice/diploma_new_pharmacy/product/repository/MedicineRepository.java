package org.beatrice.diploma_new_pharmacy.product.repository;

import org.beatrice.diploma_new_pharmacy.product.model.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;

interface MedicineRepository extends JpaRepository<Medicine, Integer> {
}
