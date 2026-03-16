package org.beatrice.diploma_new_pharmacy.cart.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.beatrice.diploma_new_pharmacy.cart.dto.AddProductToCartRequest;
import org.beatrice.diploma_new_pharmacy.cart.dto.CartIdentity;
import org.beatrice.diploma_new_pharmacy.cart.dto.CartResponse;
import org.beatrice.diploma_new_pharmacy.cart.dto.SetProductInCartRequest;
import org.beatrice.diploma_new_pharmacy.cart.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/cart")
class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponse> getCart(CartIdentity identity) {
        CartResponse response = cartService.getCartResponse(identity);
        return ResponseEntity.ok(response);
    }


    @PostMapping
    public ResponseEntity<CartResponse> addItemToCart(@Valid @RequestBody AddProductToCartRequest request, CartIdentity identity) {
        var response = cartService.addItemToCart(identity, request.productId(), request.quantity());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<?> setItemInCart(@PathVariable Integer productId, @Valid @RequestBody SetProductInCartRequest request, CartIdentity identity) {
        var response = cartService.setItemInCart(identity, productId, request.quantity());
        return ResponseEntity.ok(response);
    }
}
