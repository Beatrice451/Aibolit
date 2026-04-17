package org.beatrice.diploma_new_pharmacy.domain.loyalty.repository;

import org.beatrice.diploma_new_pharmacy.domain.loyalty.model.LoyaltyAccount;
import org.springframework.data.jpa.repository.JpaRepository;

interface LoyaltyAccountRepository extends JpaRepository<LoyaltyAccount, Integer> {
}
