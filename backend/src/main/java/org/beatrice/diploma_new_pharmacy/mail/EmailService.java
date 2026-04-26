package org.beatrice.diploma_new_pharmacy.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.beatrice.diploma_new_pharmacy.domain.order.dto.OrderReadyForPickupEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String mail;

    @Async("emailTaskExecutor")
    public void sendReadyForPickupEmail(OrderReadyForPickupEvent event) {
        try {
            sendEmail(event.userEmail(), "Ваш заказ с номером " + event.orderId() + " готов к выдаче", "Добавить нормальный текст");
            log.info("Email sent to {} for order {}", event.userEmail(), event.orderId());
        } catch (Exception e) {
            log.error("Failed to send email for order {}", event.orderId(), e);
            // TODO добавить ретраи
        }
    }

    private void sendEmail(String to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        message.setFrom(mail);
        mailSender.send(message);
    }
}
