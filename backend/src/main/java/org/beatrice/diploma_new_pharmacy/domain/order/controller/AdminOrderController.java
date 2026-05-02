package org.beatrice.diploma_new_pharmacy.domain.order.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.beatrice.diploma_new_pharmacy.domain.order.dto.request.UpdateOrderStatusRequest;
import org.beatrice.diploma_new_pharmacy.domain.order.dto.request.VerifyPickupCodeRequest;
import org.beatrice.diploma_new_pharmacy.domain.order.dto.response.OrderResponse;
import org.beatrice.diploma_new_pharmacy.domain.order.dto.response.PickupCodeVerificationResponse;
import org.beatrice.diploma_new_pharmacy.domain.order.service.OrderService;
import org.beatrice.diploma_new_pharmacy.domain.order.service.PickupCodeService;
import org.beatrice.diploma_new_pharmacy.domain.order.specification.OrderFilter;
import org.beatrice.diploma_new_pharmacy.exception.ErrorResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/order")
@RequiredArgsConstructor
@Tag(name = "Админ: Заказы", description = "Эндпоинты для управления заказами (требуется роль ADMIN)")
class AdminOrderController {
    private final OrderService orderService;
    private final PickupCodeService pickupCodeService;

    @Operation(
            summary = "Получить список заказов",
            description = "Возвращает страницу заказов с возможностью фильтрации по статусу, аптеке и дате. Требуется роль ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список заказов получен", content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "403", description = "Нет доступа (не ADMIN)", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getOrders(@ModelAttribute OrderFilter filter, Pageable pageable) {
        return ResponseEntity.ok(orderService.getOrders(filter, pageable));
    }

    @Operation(
            summary = "Обновить статус заказа",
            description = "Изменяет статус заказа. Нельзя изменить статус завершенного или отмененного заказа. Требуется роль ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Статус заказа обновлен", content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "403", description = "Нет доступа (не ADMIN)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Заказ не найден", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Заказ уже в завершенном или отмененном статусе", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}")
    public ResponseEntity<OrderResponse> updateOrderStatus(@PathVariable Integer id, @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, request));
    }

    @Operation(
            summary = "Проверить код получения",
            description = "Проверяет корректность кода получения заказа. Требуется роль ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Код проверен", content = @Content(schema = @Schema(implementation = PickupCodeVerificationResponse.class))),
            @ApiResponse(responseCode = "403", description = "Нет доступа (не ADMIN)", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/verify-pickup-code")
    public ResponseEntity<PickupCodeVerificationResponse> verifyPickupCode(@RequestBody VerifyPickupCodeRequest request) {
        return ResponseEntity.ok(pickupCodeService.verifyCode(request.pickupCode()));
    }
}
