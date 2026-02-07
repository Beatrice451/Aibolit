package org.beatrice.diploma_new_pharmacy.cart.repository;

import org.beatrice.diploma_new_pharmacy.cart.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

interface CartItemRepository extends JpaRepository<CartItem, Integer> {
}
