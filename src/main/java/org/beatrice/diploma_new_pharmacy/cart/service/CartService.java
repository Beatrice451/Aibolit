package org.beatrice.diploma_new_pharmacy.cart.service;

import lombok.RequiredArgsConstructor;
import org.beatrice.diploma_new_pharmacy.cart.dto.CartIdentity;
import org.beatrice.diploma_new_pharmacy.cart.dto.CartResponse;
import org.beatrice.diploma_new_pharmacy.cart.mapper.CartMapper;
import org.beatrice.diploma_new_pharmacy.cart.model.Cart;
import org.beatrice.diploma_new_pharmacy.cart.model.CartItem;
import org.beatrice.diploma_new_pharmacy.cart.repository.CartItemRepository;
import org.beatrice.diploma_new_pharmacy.cart.repository.CartRepository;
import org.beatrice.diploma_new_pharmacy.order.model.OrderOwner;
import org.beatrice.diploma_new_pharmacy.order.model.OrderOwnerType;
import org.beatrice.diploma_new_pharmacy.order.repository.OrderOwnerRepository;
import org.beatrice.diploma_new_pharmacy.product.model.Product;
import org.beatrice.diploma_new_pharmacy.product.repository.ProductRepository;
import org.beatrice.diploma_new_pharmacy.user.model.User;
import org.beatrice.diploma_new_pharmacy.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class CartService {
    private final OrderOwnerRepository orderOwnerRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final CartMapper cartMapper;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;


    // TODO extract common code from the two methods below
    public CartResponse addItemToCart(CartIdentity identity, Integer productId, Short quantity) {
        Cart cart = getOrCreateCart(identity);
        Product product = productRepository.findProductById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product does not exist"));
        CartItem cartItem = cartItemRepository.findCartItemByProductAndCart(product, cart)
                .map(existingItem -> {
                    existingItem.setQuantity((short) (existingItem.getQuantity() + quantity));
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
        return cartMapper.toDto(cart);
    }

    public CartResponse setItemInCart(CartIdentity identity, Integer productId, Short newQuantity) {
        Cart cart = getOrCreateCart(identity);
        Product product = productRepository.findProductById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product does not exist"));
        CartItem cartItem = cartItemRepository.findCartItemByProductAndCart(product, cart)
                .map(existingItem -> {
                    existingItem.setQuantity(newQuantity);
                    return existingItem;
                })
                .orElseGet(() -> {
                    CartItem item = new CartItem();
                    item.setCart(cart);
                    item.setProduct(product);
                    item.setQuantity(newQuantity);
                    return item;
                });
        cartItemRepository.save(cartItem);
        return cartMapper.toDto(cart);
    }


    public CartResponse getCartResponse(CartIdentity identity) {
        Cart cart = getOrCreateCart(identity);
        return cartMapper.toDto(cart);
    }

    private Cart getOrCreateCart(CartIdentity identity) {
        OrderOwner owner = resolveOrderOwner(identity);
        return cartRepository.findCartByOrderOwner(owner)
                .orElseGet(() -> createCart(owner));
    }


    private OrderOwner resolveOrderOwner(CartIdentity identity) {
        if (identity.isGuest())
            return findOrCreateGuestOrderOwner(identity.guestUuid());
        else if (identity.isUser())
            return findOrCreateUserOrderOwner(identity.userId());
        else
            throw new IllegalStateException("Identity is invalid");
    }


    private OrderOwner createUserOrderOwner(User user) {
        OrderOwner orderOwner = new OrderOwner();
        orderOwner.setUser(user);
        orderOwner.setOwnerType(OrderOwnerType.USER);
        return orderOwnerRepository.save(orderOwner);
    }

    private OrderOwner createGuestOrderOwner(UUID guestUuid) {
        OrderOwner orderOwner = new OrderOwner();
        orderOwner.setGuestUuid(guestUuid);
        orderOwner.setOwnerType(OrderOwnerType.GUEST);
        return orderOwnerRepository.save(orderOwner);
    }

    private Cart createCart(OrderOwner orderOwner) {
        Cart cart = new Cart();
        cart.setOrderOwner(orderOwner);
        return cartRepository.save(cart);
    }

    private OrderOwner findOrCreateUserOrderOwner(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found")); // TODO replace with custom exception

        return orderOwnerRepository.findByUser(user)
                .orElseGet(() -> createUserOrderOwner(user));
    }

    private OrderOwner findOrCreateGuestOrderOwner(UUID guestUuid) {
        return orderOwnerRepository.findByGuestUuid(guestUuid)
                .orElseGet(() -> createGuestOrderOwner(guestUuid));
    }
}
