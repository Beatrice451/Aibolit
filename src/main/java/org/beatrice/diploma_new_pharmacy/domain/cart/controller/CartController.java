package org.beatrice.diploma_new_pharmacy.domain.cart.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.beatrice.diploma_new_pharmacy.domain.cart.CartFacade;
import org.beatrice.diploma_new_pharmacy.domain.cart.dto.CartResponse;
import org.beatrice.diploma_new_pharmacy.domain.cart.dto.request.AddProductToCartRequest;
import org.beatrice.diploma_new_pharmacy.domain.cart.dto.request.SetProductInCartRequest;
import org.beatrice.diploma_new_pharmacy.domain.order.dto.OrderIdentity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/cart")
class CartController {


    private final CartFacade cartFacade;

    @GetMapping
    public ResponseEntity<CartResponse> getCart(OrderIdentity identity) {
        CartResponse response = cartFacade.getCart(identity);
        return ResponseEntity.ok(response);
    }


    @PostMapping
    public ResponseEntity<CartResponse> addItemToCart(@Valid @RequestBody AddProductToCartRequest request, OrderIdentity identity) {
        var response = cartFacade.addItem(identity, request.productId(), request.quantity());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<?> setItemInCart(@PathVariable Integer productId, @Valid @RequestBody SetProductInCartRequest request, OrderIdentity identity) {
        var response = cartFacade.setItemQuantity(identity, productId, request.quantity());
        return ResponseEntity.ok(response);
    }
}
