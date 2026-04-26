package org.beatrice.diploma_new_pharmacy.domain.order.service;

import lombok.RequiredArgsConstructor;
import org.beatrice.diploma_new_pharmacy.domain.order.dto.OrderReadyForPickupEvent;
import org.beatrice.diploma_new_pharmacy.mail.EmailService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OrderEventListener {
    private final EmailService emailService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderReadyForPickup(OrderReadyForPickupEvent event) {
        emailService.sendReadyForPickupEmail(event);
    }
}
