package org.beatrice.diploma_new_pharmacy.domain.pharmacy.repository;

import org.beatrice.diploma_new_pharmacy.domain.pharmacy.model.Pharmacy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PharmacyRepository extends JpaRepository<Pharmacy, Integer> {
}
