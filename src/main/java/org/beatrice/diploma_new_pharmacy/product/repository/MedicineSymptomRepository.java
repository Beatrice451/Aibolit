package org.beatrice.diploma_new_pharmacy.product.repository;

import org.beatrice.diploma_new_pharmacy.product.model.MedicineSymptom;
import org.beatrice.diploma_new_pharmacy.product.model.MedicineSymptomId;
import org.springframework.data.jpa.repository.JpaRepository;

interface MedicineSymptomRepository extends JpaRepository<MedicineSymptom, MedicineSymptomId> {
}
