package org.beatrice.diploma_new_pharmacy.domain.order.repository;

import org.beatrice.diploma_new_pharmacy.domain.order.model.OrderOwner;
import org.beatrice.diploma_new_pharmacy.domain.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrderOwnerRepository extends JpaRepository<OrderOwner, Integer> {
    Optional<OrderOwner> findByUser(User user);

    Optional<OrderOwner> findByGuestUuid(UUID uuid);

}
