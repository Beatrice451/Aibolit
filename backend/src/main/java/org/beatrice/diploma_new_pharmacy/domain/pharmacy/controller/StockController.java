package org.beatrice.diploma_new_pharmacy.domain.pharmacy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.dto.ProductStockResponse;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.dto.StockRequest;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.dto.StockResponse;
import org.beatrice.diploma_new_pharmacy.domain.pharmacy.service.StockService;
import org.beatrice.diploma_new_pharmacy.exception.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Остатки", description = "Управление остатками товаров на складах")
class StockController {

    private final StockService stockService;

    @GetMapping("/api/products/{productId}/stock")
    @Operation(
            summary = "Получить информацию о наличии товара",
            description = "Публичный endpoint. Возвращает общее доступное количество товара на всех складах. Если остатки не отслеживаются — tracked=false."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Информация о наличии получена", content = @Content(schema = @Schema(implementation = ProductStockResponse.class)))
    })
    public ResponseEntity<ProductStockResponse> getProductStock(@PathVariable Integer productId) {
        List<StockResponse> stocks = stockService.getStocksByProduct(productId);

        if (stocks.isEmpty()) {
            return ResponseEntity.ok(new ProductStockResponse(productId, null, false));
        }

        int totalAvailable = stocks.stream()
                .mapToInt(s -> Math.max(s.quantity() - s.reserved(), 0))
                .sum();

        return ResponseEntity.ok(new ProductStockResponse(productId, totalAvailable, true));
    }

    @GetMapping("/api/stocks")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "Получить все остатки",
            description = "Возвращает список всех остатков на всех складах. Требуется роль ADMIN или MANAGER."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список остатков получен", content = @Content(schema = @Schema(implementation = List.class))),
            @ApiResponse(responseCode = "403", description = "Нет доступа (не ADMIN или MANAGER)", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<StockResponse>> getAllStocks() {
        return ResponseEntity.ok(stockService.getAllStocks());
    }

    @GetMapping("/api/stocks/product/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "Получить остатки товара",
            description = "Возвращает остатки конкретного товара по всем складам. Требуется роль ADMIN или MANAGER."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Остатки товара получены", content = @Content(schema = @Schema(implementation = List.class))),
            @ApiResponse(responseCode = "403", description = "Нет доступа (не ADMIN или MANAGER)", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<StockResponse>> getStocksByProduct(@PathVariable Integer productId) {
        return ResponseEntity.ok(stockService.getStocksByProduct(productId));
    }

    @GetMapping("/api/stocks/{productId}/{warehouseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "Получить остаток",
            description = "Возвращает остаток конкретного товара на конкретном складе. Требуется роль ADMIN или MANAGER."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Остаток найден", content = @Content(schema = @Schema(implementation = StockResponse.class))),
            @ApiResponse(responseCode = "403", description = "Нет доступа (не ADMIN или MANAGER)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Остаток не найден", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<StockResponse> getStock(@PathVariable Integer productId, @PathVariable Integer warehouseId) {
        return ResponseEntity.ok(stockService.getStock(productId, warehouseId));
    }

    @PostMapping("/api/stocks")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "Создать остаток",
            description = "Создает запись об остатке товара на складе. Требуется роль ADMIN или MANAGER."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Остаток создан", content = @Content(schema = @Schema(implementation = StockResponse.class))),
            @ApiResponse(responseCode = "400", description = "Невалидный запрос или остаток уже существует", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Нет доступа (не ADMIN или MANAGER)", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<StockResponse> createStock(@Valid @RequestBody StockRequest request) {
        StockResponse stock = stockService.createStock(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{productId}/{warehouseId}")
                .buildAndExpand(stock.productId(), stock.warehouseId())
                .toUri();
        return ResponseEntity.created(location).body(stock);
    }

    @PatchMapping("/api/stocks/{productId}/{warehouseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "Обновить остаток",
            description = "Частично обновляет остаток товара на складе. Требуется роль ADMIN или MANAGER."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Остаток обновлен", content = @Content(schema = @Schema(implementation = StockResponse.class))),
            @ApiResponse(responseCode = "400", description = "Невалидный запрос", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Нет доступа (не ADMIN или MANAGER)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Остаток не найден", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<StockResponse> updateStock(
            @PathVariable Integer productId,
            @PathVariable Integer warehouseId,
            @Valid @RequestBody StockRequest request
    ) {
        return ResponseEntity.ok(stockService.updateStock(productId, warehouseId, request));
    }

    @DeleteMapping("/api/stocks/{productId}/{warehouseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "Удалить остаток",
            description = "Удаляет запись об остатке товара на складе. Требуется роль ADMIN или MANAGER."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Остаток удален"),
            @ApiResponse(responseCode = "403", description = "Нет доступа (не ADMIN или MANAGER)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Остаток не найден", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteStock(@PathVariable Integer productId, @PathVariable Integer warehouseId) {
        stockService.deleteStock(productId, warehouseId);
        return ResponseEntity.noContent().build();
    }
}
