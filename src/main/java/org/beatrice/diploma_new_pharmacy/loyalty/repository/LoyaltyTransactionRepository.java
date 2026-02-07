package org.beatrice.diploma_new_pharmacy.loyalty.repository;

import org.beatrice.diploma_new_pharmacy.loyalty.model.LoyaltyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

interface LoyaltyTransactionRepository extends JpaRepository<LoyaltyTransaction, Integer> {
}
