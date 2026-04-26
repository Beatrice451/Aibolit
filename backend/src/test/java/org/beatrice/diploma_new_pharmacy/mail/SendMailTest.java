package org.beatrice.diploma_new_pharmacy.mail;

import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

public class SendMailTest {

    private final JavaMailSender mailSender = new JavaMailSenderImpl();

    @Test
    public void sendMail() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("test-5a7zwlotx@srv1.mail-tester.com");
        message.setSubject("Test message");
        message.setText("This is a test message");
        message.setFrom("noreply@chebupitsa.ru");
        mailSender.send(message);
    }
}
