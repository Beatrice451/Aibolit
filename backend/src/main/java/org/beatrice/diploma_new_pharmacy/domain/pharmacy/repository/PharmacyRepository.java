package org.beatrice.diploma_new_pharmacy.domain.pharmacy.repository;

import org.beatrice.diploma_new_pharmacy.domain.pharmacy.model.Pharmacy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PharmacyRepository extends JpaRepository<Pharmacy, Integer> {
    List<Pharmacy> findByIsActiveTrue();
}
