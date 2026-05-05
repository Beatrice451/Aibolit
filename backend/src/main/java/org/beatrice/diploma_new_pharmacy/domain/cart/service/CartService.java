package org.beatrice.diploma_new_pharmacy.domain.cart.service;

import lombok.RequiredArgsConstructor;
import org.beatrice.diploma_new_pharmacy.domain.cart.dto.CartResponse;
import org.beatrice.diploma_new_pharmacy.domain.cart.mapper.CartMapper;
import org.beatrice.diploma_new_pharmacy.domain.cart.model.Cart;
import org.beatrice.diploma_new_pharmacy.domain.cart.model.CartItem;
import org.beatrice.diploma_new_pharmacy.domain.cart.repository.CartItemRepository;
import org.beatrice.diploma_new_pharmacy.domain.cart.repository.CartRepository;
import org.beatrice.diploma_new_pharmacy.domain.order.dto.OrderIdentity;
import org.beatrice.diploma_new_pharmacy.domain.order.model.OrderOwner;
import org.beatrice.diploma_new_pharmacy.domain.order.service.OrderOwnerService;
import org.beatrice.diploma_new_pharmacy.domain.product.model.Product;
import org.beatrice.diploma_new_pharmacy.domain.product.repository.ProductRepository;
import org.beatrice.diploma_new_pharmacy.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartMapper cartMapper;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderOwnerService orderOwnerService;


    public CartResponse addItemToCart(OrderIdentity identity, Integer productId, Short quantity) {
        updateCartItemQuantity(identity, productId, quantity, true);
        return getCartResponse(identity);
    }

    public CartResponse setItemInCart(OrderIdentity identity, Integer productId, Short newQuantity) {
        updateCartItemQuantity(identity, productId, newQuantity, false);
        return getCartResponse(identity);
    }

    private void updateCartItemQuantity(OrderIdentity identity, Integer productId, Short quantity, boolean isAddition) {
        Cart cart = getOrCreateCart(identity);
        Product product = productRepository.findProductById(productId)
                .orElseThrow(() -> new NotFoundException("Product does not exist"));

        int totalQuantityInCart = quantity;
        if (isAddition) {
            CartItem existing = cartItemRepository.findCartItemByProductAndCart(product, cart).orElse(null);
            if (existing != null) {
                totalQuantityInCart += existing.getQuantity();
            }
        }

        CartItem cartItem = cartItemRepository.findCartItemByProductAndCart(product, cart)
                .map(existingItem -> {
                    if (isAddition) {
                        existingItem.setQuantity((short) (existingItem.getQuantity() + quantity));
                    } else {
                        existingItem.setQuantity(quantity);
                    }
                    return existingItem;
                })
                .orElseGet(() -> {
                    CartItem item = new CartItem();
                    item.setCart(cart);
                    item.setProduct(product);
                    item.setQuantity(quantity);
                    return item;
                });
        cartItemRepository.save(cartItem);
    }

    public BigDecimal countTotalCartAmount(Cart cart) {
        var cartItems = cart.getItems();
        return cartItems.stream()
                .map(cartItem -> cartItem.getProduct().getPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

    }

    public void truncateCart(Cart cart) {
        cart.getItems().clear();
    }

    public void truncateCart(OrderIdentity identity) {
        Cart cart = getOrCreateCart(identity);
        truncateCart(cart);
    }


    public CartResponse getCartResponse(OrderIdentity identity) {
        Cart cart = getOrCreateCart(identity);
        return cartMapper.toDto(cart);
    }

    public Cart getCart(OrderIdentity identity) {
        return getOrCreateCart(identity);
    }

    private Cart getOrCreateCart(OrderIdentity identity) {
        OrderOwner owner = orderOwnerService.resolveOrderOwner(identity);
        return cartRepository.findCartByOrderOwner(owner)
                .orElseGet(() -> createCart(owner));
    }


    private Cart createCart(OrderOwner orderOwner) {
        Cart cart = new Cart();
        cart.setOrderOwner(orderOwner);
        return cartRepository.save(cart);
    }


}
