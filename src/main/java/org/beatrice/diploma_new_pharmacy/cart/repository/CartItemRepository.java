package org.beatrice.diploma_new_pharmacy.cart.repository;

import jakarta.persistence.LockModeType;
import org.beatrice.diploma_new_pharmacy.cart.model.Cart;
import org.beatrice.diploma_new_pharmacy.cart.model.CartItem;
import org.beatrice.diploma_new_pharmacy.product.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    CartItem findCartItemByProduct(Product product);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<CartItem> findCartItemByProductAndCart(Product product, Cart cart);
}
