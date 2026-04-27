package org.beatrice.diploma_new_pharmacy.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.beatrice.diploma_new_pharmacy.domain.order.dto.OrderReadyForPickupEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async("emailTaskExecutor")
    public void sendReadyForPickupEmail(OrderReadyForPickupEvent event) {
        try {
            Context context = new Context();
            context.setVariable("firstName", extractFirstName(event.userName()));
            context.setVariable("lastName", extractLastName(event.userName()));
            context.setVariable("orderNumber", event.orderId());
            context.setVariable("pickupCode", event.pickupCode());
            context.setVariable("pharmacyName", event.pharmacyName());
            context.setVariable("pharmacyAddress", event.pharmacyAddress());

            String htmlContent = templateEngine.process("order-ready-email", context);

            sendHtmlEmail(
                    event.userEmail(),
                    "Ваш заказ готов к выдаче",
                    htmlContent
            );

            log.info("Email sent to {} for order {}", event.userEmail(), event.orderId());
        } catch (Exception e) {
            log.error("Failed to send email for order {}", event.orderId(), e);
            // TODO добавить ретраи
        }
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true); // true = HTML

        mailSender.send(message);
    }

    private String extractFirstName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "";
        }
        String[] parts = fullName.trim().split("\\s+", 2);
        return parts[0];
    }

    private String extractLastName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "";
        }
        String[] parts = fullName.trim().split("\\s+", 2);
        return parts.length > 1 ? parts[1] : "";
    }
}
