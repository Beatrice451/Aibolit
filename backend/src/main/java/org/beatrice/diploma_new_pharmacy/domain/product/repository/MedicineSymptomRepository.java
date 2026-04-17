package org.beatrice.diploma_new_pharmacy.domain.product.repository;

import org.beatrice.diploma_new_pharmacy.domain.product.model.MedicineSymptom;
import org.beatrice.diploma_new_pharmacy.domain.product.model.MedicineSymptomId;
import org.springframework.data.jpa.repository.JpaRepository;

interface MedicineSymptomRepository extends JpaRepository<MedicineSymptom, MedicineSymptomId> {
}
