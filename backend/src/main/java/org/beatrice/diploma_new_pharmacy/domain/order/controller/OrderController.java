package org.beatrice.diploma_new_pharmacy.domain.order.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.beatrice.diploma_new_pharmacy.domain.order.dto.OrderIdentity;
import org.beatrice.diploma_new_pharmacy.domain.order.dto.command.CreateOrderCommand;
import org.beatrice.diploma_new_pharmacy.domain.order.dto.request.CreateOrderRequest;
import org.beatrice.diploma_new_pharmacy.domain.order.dto.response.OrderResponse;
import org.beatrice.diploma_new_pharmacy.domain.order.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrdersByIdentity(OrderIdentity identity) {
        var orders = orderService.getOrdersByIdentity(identity);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Integer id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            OrderIdentity identity
    ) {
        var cmd = new CreateOrderCommand(
                identity,
                request.pharmacyId(),
                request.phone(),
                request.email(),
                request.firstName(),
                request.lastName()
        );
        OrderResponse response = orderService.createOrder(cmd);
        return ResponseEntity.ok(response);
    }
}
