package org.beatrice.diploma_new_pharmacy.cart.repository;

import org.beatrice.diploma_new_pharmacy.cart.model.Cart;
import org.beatrice.diploma_new_pharmacy.order.model.OrderOwner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Integer> {
    Optional<Cart> findCartByOrderOwner(OrderOwner orderOwner);

    Cart getCartById(Integer id);
}
