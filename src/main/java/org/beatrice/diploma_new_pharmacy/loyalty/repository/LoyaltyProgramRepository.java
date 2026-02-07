package org.beatrice.diploma_new_pharmacy.loyalty.repository;

import org.beatrice.diploma_new_pharmacy.loyalty.model.LoyaltyProgram;
import org.springframework.data.jpa.repository.JpaRepository;

interface LoyaltyProgramRepository extends JpaRepository<LoyaltyProgram, Integer> {
}
