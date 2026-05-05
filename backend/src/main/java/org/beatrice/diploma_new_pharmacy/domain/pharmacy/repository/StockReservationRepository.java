package org.beatrice.diploma_new_pharmacy.domain.pharmacy.repository;

import org.beatrice.diploma_new_pharmacy.domain.pharmacy.model.StockReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockReservationRepository extends JpaRepository<StockReservation, Integer> {
    List<StockReservation> findByOrderId(Integer orderId);
    List<StockReservation> findByProductIdAndWarehouseId(Integer productId, Integer warehouseId);
}