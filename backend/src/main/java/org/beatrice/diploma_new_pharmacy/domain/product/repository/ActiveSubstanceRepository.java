package org.beatrice.diploma_new_pharmacy.domain.product.repository;

import org.beatrice.diploma_new_pharmacy.domain.product.model.ActiveSubstance;
import org.springframework.data.jpa.repository.JpaRepository;

interface ActiveSubstanceRepository extends JpaRepository<ActiveSubstance, Integer> {
}
