package org.beatrice.diploma_new_pharmacy.domain.pharmacy.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.beatrice.diploma_new_pharmacy.domain.order.model.Order;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.dto.StockRequest;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.dto.StockResponse;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.exception.InsufficientStockException;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.exception.StockNotFoundException;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.model.ReservationStatus;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.model.Stock;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.model.StockId;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.model.StockReservation;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.model.Warehouse;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.repository.StockReservationRepository;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.repository.StockRepository;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.repository.WarehouseRepository;
import org.beatrice.diploma_new_pharmacy.domain.product.repository.ProductRepository;
import org.beatrice.diploma_new_pharmacy.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final StockRepository stockRepository;
    private final StockReservationRepository stockReservationRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;

    @Override
    public Integer getAvailableStock(Integer productId, Integer warehouseId) throws StockNotFoundException {
        if (productId == null || warehouseId == null) {
            throw new IllegalArgumentException("productId and warehouseId cannot be null");
        }

        StockId id = new StockId();
        id.setProductId(productId);
        id.setWarehouseId(warehouseId);
        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new StockNotFoundException(
                        "Stock not found for productId=" + productId + " and warehouseId=" + warehouseId));

        Integer reservedQuantity = stockReservationRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                .stream()
                .filter(r -> ReservationStatus.ACTIVE.equals(r.getStatus()))
                .mapToInt(StockReservation::getQuantity)
                .sum();

        int available = stock.getQuantity() - reservedQuantity;
        return Math.max(available, 0);
    }

    @Override
    public boolean hasEnoughStock(Integer productId, Integer warehouseId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            return false;
        }
        try {
            Integer available = getAvailableStock(productId, warehouseId);
            return available != null && available >= quantity;
        } catch (StockNotFoundException e) {
            return false;
        }
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void reserveStock(Integer productId, Integer warehouseId, Integer quantity, Order order)
            throws InsufficientStockException, StockNotFoundException {

        if (productId == null || warehouseId == null || quantity == null || order == null) {
            throw new IllegalArgumentException("None of the parameters can be null");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        // Get stock with pessimistic lock
        Stock stock = stockRepository.findByIdForUpdate(productId, warehouseId)
                .orElseThrow(() -> new StockNotFoundException(
                        "Stock not found for productId=" + productId + " and warehouseId=" + warehouseId));

        Integer available = getAvailableStock(productId, warehouseId);
        if (available == null || available < quantity) {
            throw new InsufficientStockException(String.format(
                    "Insufficient stock for productId=%d, warehouseId=%d. Required: %d, Available: %d",
                    productId, warehouseId, quantity, available));
        }

        // Create reservation
        StockReservation reservation = new StockReservation();
        reservation.setProduct(productRepository.findById(productId).orElse(null));
        reservation.setWarehouse(warehouseRepository.findById(warehouseId).orElse(null));
        reservation.setOrder(order);
        reservation.setQuantity(quantity);
        reservation.setReservedAt(Instant.now());
        reservation.setStatus(ReservationStatus.ACTIVE);

        stockReservationRepository.save(reservation);
        log.info("Reserved {} units of product {} at warehouse {} for order {}",
                quantity, productId, warehouseId, order.getId());
    }

    @Override
    @Transactional
    public void releaseReservation(Order order) {
        if (order == null || order.getId() == null) return;

        List<StockReservation> reservations = stockReservationRepository.findByOrderId(order.getId());
        List<StockReservation> activeReservations = reservations.stream()
                .filter(r -> ReservationStatus.ACTIVE.equals(r.getStatus()))
                .toList();

        for (StockReservation reservation : activeReservations) {
            reservation.setStatus(ReservationStatus.RELEASED);
            stockReservationRepository.save(reservation);
            log.info("Released reservation {} for order {}", reservation.getId(), order.getId());
        }
    }

    @Override
    @Transactional
    public void completeReservation(Order order) {
        if (order == null || order.getId() == null) return;

        List<StockReservation> reservations = stockReservationRepository.findByOrderId(order.getId());
        List<StockReservation> activeReservations = reservations.stream()
                .filter(r -> ReservationStatus.ACTIVE.equals(r.getStatus()))
                .toList();

        for (StockReservation reservation : activeReservations) {
            // Decrease physical stock
StockId pid = new StockId();
        pid.setProductId(reservation.getProduct().getId());
        pid.setWarehouseId(reservation.getWarehouse().getId());
        Stock physical = stockRepository.findById(pid).orElse(null);

            if (physical != null) {
                physical.setQuantity(physical.getQuantity() - reservation.getQuantity());
                stockRepository.save(physical);
            }

            reservation.setStatus(ReservationStatus.COMPLETED);
            stockReservationRepository.save(reservation);
            log.info("Completed reservation {} for order {}, decreased stock", 
                    reservation.getId(), order.getId());
        }
    }

    @Override
    public List<Object> getActiveReservations(Integer productId, Integer warehouseId) {
        return stockReservationRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                .stream()
                .filter(r -> ReservationStatus.ACTIVE.equals(r.getStatus()))
                .collect(Collectors.toList());
    }

    @Override
    public Integer getWarehouseForOrder(Order order) {
        if (order == null || order.getId() == null) {
            return null;
        }

        List<StockReservation> reservations = stockReservationRepository.findByOrderId(order.getId());
        return reservations.isEmpty() ? null : reservations.getFirst().getWarehouse().getId();
    }

    @Override
    public Warehouse findMainWarehouseForPharmacy(Integer pharmacyId) {
        if (pharmacyId == null) {
            return null;
        }
        List<Warehouse> warehouses = warehouseRepository.findByPharmacyId(pharmacyId);
        return warehouses.isEmpty() ? null : warehouses.getFirst();
    }

    @Override
    public List<Warehouse> findSharedWarehouses() {
        return warehouseRepository.findByPharmacyIdIsNull();
    }

    @Override
    public boolean hasEnoughStockWithFallback(Integer productId, Integer pharmacyId, Integer quantity) {
        if (productId == null || pharmacyId == null || quantity == null || quantity <= 0) {
            return false;
        }

        Warehouse mainWarehouse = findMainWarehouseForPharmacy(pharmacyId);
        if (mainWarehouse != null) {
            try {
                Integer available = getAvailableStock(productId, mainWarehouse.getId());
                if (available != null && available >= quantity) {
                    return true;
                }
            } catch (StockNotFoundException _) {
            }
        }

        List<Warehouse> sharedWarehouses = findSharedWarehouses();
        for (Warehouse shared : sharedWarehouses) {
            try {
                Integer available = getAvailableStock(productId, shared.getId());
                if (available != null && available >= quantity) {
                    return true;
                }
            } catch (StockNotFoundException _) {
            }
        }

        return false;
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void reserveStockWithFallback(Integer productId, Integer pharmacyId, Integer quantity, Order order)
            throws InsufficientStockException, StockNotFoundException {

        if (productId == null || pharmacyId == null || quantity == null || order == null) {
            throw new IllegalArgumentException("None of the parameters can be null");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        Warehouse mainWarehouse = findMainWarehouseForPharmacy(pharmacyId);
        Warehouse selectedWarehouse = null;

        if (mainWarehouse != null) {
            try {
                Integer available = getAvailableStock(productId, mainWarehouse.getId());
                if (available != null && available >= quantity) {
                    selectedWarehouse = mainWarehouse;
                }
            } catch (StockNotFoundException _) {
            }
        }

        if (selectedWarehouse == null) {
            List<Warehouse> sharedWarehouses = findSharedWarehouses();
            for (Warehouse shared : sharedWarehouses) {
                try {
                    Integer available = getAvailableStock(productId, shared.getId());
                    if (available != null && available >= quantity) {
                        selectedWarehouse = shared;
                        break;
                    }
                } catch (StockNotFoundException _) {
                }
            }
        }

        if (selectedWarehouse == null) {
            throw new InsufficientStockException(String.format(
                    "Insufficient stock for productId=%d at pharmacyId=%d. Required: %d",
                    productId, pharmacyId, quantity));
        }

        reserveStock(productId, selectedWarehouse.getId(), quantity, order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockResponse> getAllStocks() {
        return stockRepository.findAll().stream()
                .map(this::toStockResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockResponse> getStocksByProduct(Integer productId) {
        return stockRepository.findAll().stream()
                .filter(s -> s.getId().getProductId().equals(productId))
                .map(this::toStockResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public StockResponse getStock(Integer productId, Integer warehouseId) {
        StockId id = new StockId();
        id.setProductId(productId);
        id.setWarehouseId(warehouseId);

        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Stock not found for productId=" + productId + " and warehouseId=" + warehouseId));

        return toStockResponse(stock);
    }

    @Override
    @Transactional
    public StockResponse createStock(StockRequest request) {
        StockId id = new StockId();
        id.setProductId(request.productId());
        id.setWarehouseId(request.warehouseId());

        if (stockRepository.findById(id).isPresent()) {
            throw new IllegalArgumentException(
                    "Stock already exists for productId=" + request.productId()
                            + " and warehouseId=" + request.warehouseId());
        }

        Stock stock = new Stock();
        stock.setId(id);
        stock.setProduct(productRepository.findById(request.productId())
                .orElseThrow(() -> new NotFoundException("Product with id " + request.productId() + " not found")));
        stock.setWarehouse(warehouseRepository.findById(request.warehouseId())
                .orElseThrow(() -> new NotFoundException("Warehouse with id " + request.warehouseId() + " not found")));
        stock.setQuantity(request.quantity());

        return toStockResponse(stockRepository.save(stock));
    }

    @Override
    @Transactional
    public StockResponse updateStock(Integer productId, Integer warehouseId, StockRequest request) {
        StockId id = new StockId();
        id.setProductId(productId);
        id.setWarehouseId(warehouseId);

        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Stock not found for productId=" + productId + " and warehouseId=" + warehouseId));

        if (request.quantity() != null) {
            stock.setQuantity(request.quantity());
        }

        return toStockResponse(stockRepository.save(stock));
    }

    @Override
    @Transactional
    public void deleteStock(Integer productId, Integer warehouseId) {
        StockId id = new StockId();
        id.setProductId(productId);
        id.setWarehouseId(warehouseId);

        if (!stockRepository.findById(id).isPresent()) {
            throw new NotFoundException(
                    "Stock not found for productId=" + productId + " and warehouseId=" + warehouseId);
        }

        stockRepository.deleteById(id);
    }

    private StockResponse toStockResponse(Stock stock) {
        Integer reserved = stockReservationRepository.findByProductIdAndWarehouseId(
                        stock.getId().getProductId(), stock.getId().getWarehouseId())
                .stream()
                .filter(r -> ReservationStatus.ACTIVE.equals(r.getStatus()))
                .mapToInt(StockReservation::getQuantity)
                .sum();

        return new StockResponse(
                stock.getId().getProductId(),
                stock.getProduct().getName(),
                stock.getId().getWarehouseId(),
                stock.getWarehouse().getName(),
                stock.getQuantity(),
                reserved
        );
    }
}