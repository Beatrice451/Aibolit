package org.beatrice.diploma_new_pharmacy.pharmacy.repository;

import org.beatrice.diploma_new_pharmacy.pharmacy.model.Pharmacy;
import org.springframework.data.jpa.repository.JpaRepository;

interface PharmacyRepository extends JpaRepository<Pharmacy, Integer> {
}
