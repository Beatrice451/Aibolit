package org.beatrice.diploma_new_pharmacy.domain.product.repository;

import org.beatrice.diploma_new_pharmacy.domain.product.model.Symptom;
import org.springframework.data.jpa.repository.JpaRepository;

interface SymptomRepository extends JpaRepository<Symptom, Integer> {
}
