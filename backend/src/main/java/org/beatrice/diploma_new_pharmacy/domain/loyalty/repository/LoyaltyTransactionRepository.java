package org.beatrice.diploma_new_pharmacy.domain.loyalty.repository;

import org.beatrice.diploma_new_pharmacy.domain.loyalty.model.LoyaltyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

interface LoyaltyTransactionRepository extends JpaRepository<LoyaltyTransaction, Integer> {
}
