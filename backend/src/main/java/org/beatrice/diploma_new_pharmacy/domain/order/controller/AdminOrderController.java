package org.beatrice.diploma_new_pharmacy.domain.order.controller;

import lombok.RequiredArgsConstructor;
import org.beatrice.diploma_new_pharmacy.domain.order.dto.request.UpdateOrderStatusRequest;
import org.beatrice.diploma_new_pharmacy.domain.order.dto.request.VerifyPickupCodeRequest;
import org.beatrice.diploma_new_pharmacy.domain.order.dto.response.OrderResponse;
import org.beatrice.diploma_new_pharmacy.domain.order.dto.response.PickupCodeVerificationResponse;
import org.beatrice.diploma_new_pharmacy.domain.order.service.OrderService;
import org.beatrice.diploma_new_pharmacy.domain.order.service.PickupCodeService;
import org.beatrice.diploma_new_pharmacy.domain.order.specification.OrderFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/order")
@RequiredArgsConstructor
class AdminOrderController {
    private final OrderService orderService;
    private final PickupCodeService pickupCodeService;

    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getOrders(@ModelAttribute OrderFilter filter, Pageable pageable) {
        return ResponseEntity.ok(orderService.getOrders(filter, pageable));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<OrderResponse> updateOrderStatus(@PathVariable Integer id, @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, request));
    }

    @PostMapping("/verify-pickup-code")
    public ResponseEntity<PickupCodeVerificationResponse> verifyPickupCode(@RequestBody VerifyPickupCodeRequest request) {
        return ResponseEntity.ok(pickupCodeService.verifyCode(request.pickupCode()));
    }
}
