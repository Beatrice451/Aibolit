package org.beatrice.diploma_new_pharmacy.domain.pharmacy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.dto.WarehouseRequest;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.dto.WarehouseResponse;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.service.WarehouseService;
import org.beatrice.diploma_new_pharmacy.exception.ErrorResponse;
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
    @Operation(
            summary = "Получить все склады",
            description = "Возвращает список всех складов."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список складов получен", content = @Content(schema = @Schema(implementation = List.class)))
    })
    public ResponseEntity<List<WarehouseResponse>> getAllWarehouses() {
        return ResponseEntity.ok(warehouseService.getAllWarehouses());
    }

    @GetMapping("/shared")
    @Operation(
            summary = "Получить общие склады",
            description = "Возвращает список складов, не привязанных к конкретной аптеке."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список общих складов получен", content = @Content(schema = @Schema(implementation = List.class)))
    })
    public ResponseEntity<List<WarehouseResponse>> getSharedWarehouses() {
        return ResponseEntity.ok(warehouseService.getSharedWarehouses());
    }

    @GetMapping("/pharmacy/{pharmacyId}")
    @Operation(
            summary = "Получить склады аптеки",
            description = "Возвращает склады, привязанные к указанной аптеке."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список складов аптеки получен", content = @Content(schema = @Schema(implementation = List.class)))
    })
    public ResponseEntity<List<WarehouseResponse>> getWarehousesByPharmacy(@PathVariable Integer pharmacyId) {
        return ResponseEntity.ok(warehouseService.getWarehousesByPharmacy(pharmacyId));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Получить склад по ID",
            description = "Возвращает информацию о конкретном складе."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Склад найден", content = @Content(schema = @Schema(implementation = WarehouseResponse.class))),
            @ApiResponse(responseCode = "404", description = "Склад с указанным ID не найден", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<WarehouseResponse> getWarehouseById(@PathVariable Integer id) {
        return ResponseEntity.ok(warehouseService.getWarehouseById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "Создать склад",
            description = "Создает новый склад. Требуется роль ADMIN или MANAGER."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Склад успешно создан", content = @Content(schema = @Schema(implementation = WarehouseResponse.class))),
            @ApiResponse(responseCode = "400", description = "Невалидный запрос (не указаны обязательные поля)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Нет доступа (не ADMIN или MANAGER)", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
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
    @Operation(
            summary = "Обновить склад",
            description = "Частично обновляет данные склада. Требуется роль ADMIN или MANAGER."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Склад обновлен", content = @Content(schema = @Schema(implementation = WarehouseResponse.class))),
            @ApiResponse(responseCode = "400", description = "Невалидный запрос", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Нет доступа (не ADMIN или MANAGER)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Склад с указанным ID не найден", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<WarehouseResponse> updateWarehouse(
            @PathVariable Integer id,
            @RequestBody WarehouseRequest request
    ) {
        return ResponseEntity.ok(warehouseService.updateWarehouse(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Удалить склад",
            description = "Удаляет склад. Требуется роль ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Склад удален"),
            @ApiResponse(responseCode = "403", description = "Нет доступа (не ADMIN)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Склад с указанным ID не найден", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteWarehouse(@PathVariable Integer id) {
        warehouseService.deleteWarehouse(id);
        return ResponseEntity.noContent().build();
    }
}