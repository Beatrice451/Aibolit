package org.beatrice.diploma_new_pharmacy.domain.order.repository;

import org.beatrice.diploma_new_pharmacy.domain.order.model.Order;
import org.beatrice.diploma_new_pharmacy.domain.order.model.OrderOwner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> getOrdersByOrderOwner(OrderOwner orderOwner);

    Page<Order> findAll(Specification<Order> spec, Pageable pageable);
}
