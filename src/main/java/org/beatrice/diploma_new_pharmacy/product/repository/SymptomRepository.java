package org.beatrice.diploma_new_pharmacy.product.repository;

import org.beatrice.diploma_new_pharmacy.product.model.Symptom;
import org.springframework.data.jpa.repository.JpaRepository;

interface SymptomRepository extends JpaRepository<Symptom, Integer> {
}
