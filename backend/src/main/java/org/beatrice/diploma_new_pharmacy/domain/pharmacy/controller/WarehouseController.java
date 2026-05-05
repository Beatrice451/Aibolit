package org.beatrice.diploma_new_pharmacy.domain.pharmacy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.dto.WarehouseRequest;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.dto.WarehouseResponse;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.service.WarehouseService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
@Tag(name = "Склады", description = "Управление складами")
class WarehouseController {

    private final WarehouseService warehouseService;

    @GetMapping
    @Operation(summary = "Получить все склады")
    public ResponseEntity<List<WarehouseResponse>> getAllWarehouses() {
        return ResponseEntity.ok(warehouseService.getAllWarehouses());
    }

    @GetMapping("/shared")
    @Operation(summary = "Получить общие склады")
    public ResponseEntity<List<WarehouseResponse>> getSharedWarehouses() {
        return ResponseEntity.ok(warehouseService.getSharedWarehouses());
    }

    @GetMapping("/pharmacy/{pharmacyId}")
    @Operation(summary = "Получить склады аптеки")
    public ResponseEntity<List<WarehouseResponse>> getWarehousesByPharmacy(@PathVariable Integer pharmacyId) {
        return ResponseEntity.ok(warehouseService.getWarehousesByPharmacy(pharmacyId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить склад по ID")
    public ResponseEntity<WarehouseResponse> getWarehouseById(@PathVariable Integer id) {
        return ResponseEntity.ok(warehouseService.getWarehouseById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Создать склад")
    public ResponseEntity<WarehouseResponse> createWarehouse(@Valid @RequestBody WarehouseRequest request) {
        var warehouse = warehouseService.createWarehouse(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(warehouse.id())
                .toUri();
        return ResponseEntity.created(location).body(warehouse);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Обновить склад")
    public ResponseEntity<WarehouseResponse> updateWarehouse(
            @PathVariable Integer id,
            @RequestBody WarehouseRequest request
    ) {
        return ResponseEntity.ok(warehouseService.updateWarehouse(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Удалить склад")
    public ResponseEntity<Void> deleteWarehouse(@PathVariable Integer id) {
        warehouseService.deleteWarehouse(id);
        return ResponseEntity.noContent().build();
    }
}