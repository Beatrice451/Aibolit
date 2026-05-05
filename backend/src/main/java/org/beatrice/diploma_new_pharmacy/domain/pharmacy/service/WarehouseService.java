package org.beatrice.diploma_new_pharmacy.domain.pharmacy.service;

import lombok.RequiredArgsConstructor;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.dto.WarehouseRequest;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.dto.WarehouseResponse;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.model.Warehouse;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.repository.PharmacyRepository;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.repository.WarehouseRepository;
import org.beatrice.diploma_new_pharmacy.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final PharmacyRepository pharmacyRepository;

    public List<WarehouseResponse> getAllWarehouses() {
        return warehouseRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public List<WarehouseResponse> getSharedWarehouses() {
        return warehouseRepository.findByPharmacyIdIsNull().stream()
                .map(this::toResponse)
                .toList();
    }

    public List<WarehouseResponse> getWarehousesByPharmacy(Integer pharmacyId) {
        return warehouseRepository.findByPharmacyId(pharmacyId).stream()
                .map(this::toResponse)
                .toList();
    }

    public WarehouseResponse getWarehouseById(Integer id) {
        return toResponse(getWarehouseEntityById(id));
    }

    public Warehouse getWarehouseEntityById(Integer id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Warehouse with id " + id + " not found"));
    }

    public WarehouseResponse createWarehouse(WarehouseRequest request) {
        Warehouse warehouse = new Warehouse();
        warehouse.setName(request.name());
        warehouse.setAddress(request.address());

        if (request.pharmacyId() != null) {
            warehouse.setPharmacy(pharmacyRepository.findById(request.pharmacyId())
                    .orElseThrow(() -> new NotFoundException("Pharmacy with id " + request.pharmacyId() + " not found")));
        }

        return toResponse(warehouseRepository.save(warehouse));
    }

    public WarehouseResponse updateWarehouse(Integer id, WarehouseRequest request) {
        Warehouse warehouse = getWarehouseEntityById(id);

        if (request.name() != null) {
            warehouse.setName(request.name());
        }
        if (request.address() != null) {
            warehouse.setAddress(request.address());
        }
        if (request.pharmacyId() != null) {
            warehouse.setPharmacy(pharmacyRepository.findById(request.pharmacyId())
                    .orElseThrow(() -> new NotFoundException("Pharmacy with id " + request.pharmacyId() + " not found")));
        }

        return toResponse(warehouse);
    }

    public void deleteWarehouse(Integer id) {
        warehouseRepository.deleteById(id);
    }

    private WarehouseResponse toResponse(Warehouse warehouse) {
        return new WarehouseResponse(
                warehouse.getId(),
                warehouse.getName(),
                warehouse.getAddress(),
                warehouse.getPharmacy() != null ? warehouse.getPharmacy().getId() : null,
                warehouse.getPharmacy() == null
        );
    }
}