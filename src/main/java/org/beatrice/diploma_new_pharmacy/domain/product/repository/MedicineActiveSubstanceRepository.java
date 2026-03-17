package org.beatrice.diploma_new_pharmacy.domain.product.repository;

import org.beatrice.diploma_new_pharmacy.domain.product.model.MedicineActiveSubstance;
import org.beatrice.diploma_new_pharmacy.domain.product.model.MedicineActiveSubstanceId;
import org.springframework.data.jpa.repository.JpaRepository;

interface MedicineActiveSubstanceRepository extends JpaRepository<MedicineActiveSubstance, MedicineActiveSubstanceId> {
}
