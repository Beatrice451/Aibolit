package org.beatrice.diploma_new_pharmacy.domain.order.service;

import lombok.RequiredArgsConstructor;
import org.beatrice.diploma_new_pharmacy.domain.order.dto.OrderIdentity;
import org.beatrice.diploma_new_pharmacy.domain.order.model.OrderOwner;
import org.beatrice.diploma_new_pharmacy.domain.order.model.OrderOwnerType;
import org.beatrice.diploma_new_pharmacy.domain.order.repository.OrderOwnerRepository;
import org.beatrice.diploma_new_pharmacy.domain.user.model.User;
import org.beatrice.diploma_new_pharmacy.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderOwnerService {
    private final OrderOwnerRepository orderOwnerRepository;
    private final UserRepository userRepository;

    public OrderOwner resolveOrderOwner(OrderIdentity identity) {
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
