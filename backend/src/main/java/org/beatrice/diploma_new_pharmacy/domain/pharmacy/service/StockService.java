package org.beatrice.diploma_new_pharmacy.domain.pharmacy.service;

import org.beatrice.diploma_new_pharmacy.domain.order.model.Order;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.exception.InsufficientStockException;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.exception.StockNotFoundException;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.model.Warehouse;

import java.util.List;

public interface StockService {

    Integer getAvailableStock(Integer productId, Integer warehouseId) throws StockNotFoundException;

    boolean hasEnoughStock(Integer productId, Integer warehouseId, Integer quantity);

    boolean hasEnoughStockWithFallback(Integer productId, Integer pharmacyId, Integer quantity);

    void reserveStock(Integer productId, Integer warehouseId, Integer quantity, Order order)
            throws InsufficientStockException, StockNotFoundException;

    void reserveStockWithFallback(Integer productId, Integer pharmacyId, Integer quantity, Order order)
            throws InsufficientStockException, StockNotFoundException;

    void releaseReservation(Order order);

    void completeReservation(Order order);

    List<Object> getActiveReservations(Integer productId, Integer warehouseId);

    Warehouse findMainWarehouseForPharmacy(Integer pharmacyId);

    List<Warehouse> findSharedWarehouses();

    Integer getWarehouseForOrder(Order order);
}