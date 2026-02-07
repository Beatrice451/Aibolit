package org.beatrice.diploma_new_pharmacy.order.repository;

import org.beatrice.diploma_new_pharmacy.order.model.OrderOwner;
import org.springframework.data.jpa.repository.JpaRepository;

interface OrderOwnerRepository extends JpaRepository<OrderOwner, Integer> {
}
