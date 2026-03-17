package org.beatrice.diploma_new_pharmacy.domain.pharmacy.repository;

import org.beatrice.diploma_new_pharmacy.domain.pharmacy.model.Stock;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.model.StockId;
import org.springframework.data.jpa.repository.JpaRepository;

interface StockRepository extends JpaRepository<Stock, StockId> {
}
