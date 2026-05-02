package org.beatrice.diploma_new_pharmacy.domain.order.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.beatrice.diploma_new_pharmacy.domain.order.dto.OrderIdentity;
import org.beatrice.diploma_new_pharmacy.domain.order.dto.command.CreateOrderCommand;
import org.beatrice.diploma_new_pharmacy.domain.order.dto.request.CreateOrderRequest;
import org.beatrice.diploma_new_pharmacy.domain.order.dto.response.OrderResponse;
import org.beatrice.diploma_new_pharmacy.domain.order.service.OrderService;
import org.beatrice.diploma_new_pharmacy.exception.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Заказы", description = "Эндпоинты для работы с заказами пользователей")
class OrderController {

    private final OrderService orderService;

    @Operation(
            summary = "Получить список заказов",
            description = "Возвращает список заказов текущего пользователя или гостя (по sessionId)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список заказов получен", content = @Content(schema = @Schema(implementation = List.class)))
    })
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrdersByIdentity(OrderIdentity identity) {
        var orders = orderService.getOrdersByIdentity(identity);
        return ResponseEntity.ok(orders);
    }

        @Operation(
            summary = "Получить заказ по ID",
            description = "Возвращает информацию о конкретном заказе. Пользователь может получить только свои заказы."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Заказ найден", content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "403", description = "Нет доступа к чужому заказу", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Заказ с указанным ID не найден", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Integer id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @Operation(
            summary = "Создать заказ",
            description = "Создает новый заказ на основе товаров в корзине. Для гостя (без авторизации) обязательны: phone, email, firstName, lastName."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Заказ успешно создан", content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Невалидный запрос (пустая корзина, не указаны данные получателя для гостя)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Аптека с указанным ID не найдена", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request, OrderIdentity identity) {
        var cmd = new CreateOrderCommand(identity, request.pharmacyId(), request.phone(), request.email(), request.firstName(), request.lastName());
        OrderResponse response = orderService.createOrder(cmd);
        return ResponseEntity.ok(response);
    }
}
