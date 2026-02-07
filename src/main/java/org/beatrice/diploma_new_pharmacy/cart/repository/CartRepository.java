package org.beatrice.diploma_new_pharmacy.cart.repository;

import org.beatrice.diploma_new_pharmacy.cart.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

interface CartRepository extends JpaRepository<Cart, Integer> {
}
