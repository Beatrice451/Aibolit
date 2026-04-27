package org.beatrice.diploma_new_pharmacy.domain.order.service;

import lombok.RequiredArgsConstructor;
import org.beatrice.diploma_new_pharmacy.domain.order.dto.response.PickupCodeVerificationResponse;
import org.beatrice.diploma_new_pharmacy.domain.order.model.Order;
import org.beatrice.diploma_new_pharmacy.domain.order.model.OrderStatus;
import org.beatrice.diploma_new_pharmacy.domain.order.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PickupCodeService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int MAX_RETRIES = 100;

    private final OrderRepository orderRepository;

    /**
     * Generates a unique 6-digit pickup code.
     * Retries up to MAX_RETRIES times if a collision occurs.
     *
     * @return a unique 6-digit pickup code
     * @throws IllegalStateException if unable to generate a unique code after MAX_RETRIES attempts
     */
    public String generateUniqueCode() {
        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            String code = String.format("%06d", RANDOM.nextInt(1_000_000));
            if (!orderRepository.existsByPickupCode(code)) {
                return code;
            }
        }

        throw new IllegalStateException("Failed to generate unique pickup code after " + MAX_RETRIES + " attempts");
    }

    /**
     * Verifies a pickup code and returns information about the associated order.
     *
     * @param pickupCode the pickup code to verify
     * @return verification response containing order details and validity status
     */
    public PickupCodeVerificationResponse verifyCode(String pickupCode) {
        Optional<Order> orderOpt = orderRepository.findByPickupCode(pickupCode);

        if (orderOpt.isEmpty()) {
            return new PickupCodeVerificationResponse(
                    false,
                    null,
                    null,
                    null,
                    null,
                    "Код не найден"
            );
        }

        Order order = orderOpt.get();

        if (order.getOrderStatus() != OrderStatus.READY) {
            return new PickupCodeVerificationResponse(
                    false,
                    order.getId(),
                    order.getFirstName() + " " + order.getLastName(),
                    order.getPharmacy().getName(),
                    order.getOrderStatus(),
                    "Неверный статус заказа"
            );
        }

        return new PickupCodeVerificationResponse(
                true,
                order.getId(),
                order.getFirstName() + " " + order.getLastName(),
                order.getPharmacy().getName(),
                order.getOrderStatus(),
                "Код действителен"
        );
    }
}
