package org.beatrice.diploma_new_pharmacy.product.repository;

import org.beatrice.diploma_new_pharmacy.product.model.MedicineActiveSubstance;
import org.beatrice.diploma_new_pharmacy.product.model.MedicineActiveSubstanceId;
import org.springframework.data.jpa.repository.JpaRepository;

interface MedicineActiveSubstanceRepository extends JpaRepository<MedicineActiveSubstance, MedicineActiveSubstanceId> {
}
