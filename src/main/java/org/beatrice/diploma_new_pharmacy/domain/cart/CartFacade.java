package org.beatrice.diploma_new_pharmacy.domain.cart;


import lombok.RequiredArgsConstructor;
import org.beatrice.diploma_new_pharmacy.domain.order.dto.OrderIdentity;
import org.beatrice.diploma_new_pharmacy.domain.cart.dto.CartResponse;
import org.beatrice.diploma_new_pharmacy.domain.cart.service.CartService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartFacade {

    private final CartService cartService;

    public CartResponse addItem(OrderIdentity identity, Integer productId, short quantity) {
        return cartService.addItemToCart(identity, productId, quantity);
    }

    public CartResponse setItemQuantity(OrderIdentity identity, Integer productId, short quantity) {
        return cartService.setItemInCart(identity, productId, quantity);
    }

    public CartResponse getCart(OrderIdentity identity) {
        return cartService.getCartResponse(identity);
    }
}
