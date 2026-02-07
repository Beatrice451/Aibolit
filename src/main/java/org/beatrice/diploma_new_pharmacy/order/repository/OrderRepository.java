package org.beatrice.diploma_new_pharmacy.order.repository;

import org.beatrice.diploma_new_pharmacy.order.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

interface OrderRepository extends JpaRepository<Order, Integer> {
}
