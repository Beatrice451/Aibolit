package org.beatrice.diploma_new_pharmacy.order.repository;

import org.beatrice.diploma_new_pharmacy.order.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
}
