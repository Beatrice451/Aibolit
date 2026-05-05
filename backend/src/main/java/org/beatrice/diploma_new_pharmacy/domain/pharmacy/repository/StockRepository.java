package org.beatrice.diploma_new_pharmacy.domain.pharmacy.repository;

import jakarta.persistence.LockModeType;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.model.Stock;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.model.StockId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, StockId> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Stock s WHERE s.id.productId = :productId AND s.id.warehouseId = :warehouseId")
    Optional<Stock> findByIdForUpdate(@Param("productId") Integer productId, @Param("warehouseId") Integer warehouseId);
}