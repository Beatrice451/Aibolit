package org.beatrice.diploma_new_pharmacy.domain.user.service;

import lombok.extern.slf4j.Slf4j;
import org.beatrice.diploma_new_pharmacy.domain.user.exception.EmailAlreadyVerifiedException;
import org.beatrice.diploma_new_pharmacy.domain.user.exception.InvalidVerificationTokenException;
import org.beatrice.diploma_new_pharmacy.domain.user.exception.TooManyRequestsException;
import org.beatrice.diploma_new_pharmacy.domain.user.model.EmailVerificationToken;
import org.beatrice.diploma_new_pharmacy.domain.user.model.User;
import org.beatrice.diploma_new_pharmacy.domain.user.repository.EmailVerificationTokenRepository;
import org.beatrice.diploma_new_pharmacy.domain.user.repository.UserRepository;
import org.beatrice.diploma_new_pharmacy.exception.NotFoundException;
import org.beatrice.diploma_new_pharmacy.mail.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EmailVerificationService {
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final EmailService emailService;
    private final UserRepository userRepository;

    @Value("${app.email-verification.token-validity-hours:1}")
    private int tokenValidityHours;

    @Value("${app.email-verification.base-url}")
    private String baseUrl;


    public void sendVerificationEmail(User user) {
        emailVerificationTokenRepository.invalidateAllUserTokens(user);
        String token = generateVerificationToken();
        var emailVerificationToken = new EmailVerificationToken(user, token, Instant.now().plus(
                tokenValidityHours, ChronoUnit.HOURS
        ));
        emailVerificationTokenRepository.save(emailVerificationToken);


        String verificationUrl = baseUrl + "/api/auth/verify-email?token=" + token;
        emailService.sendVerificationEmail(user.getEmail(), user.getFirstName(), verificationUrl, tokenValidityHours);
    }

    public void verifyEmail(String token) {
        var verificationToken = emailVerificationTokenRepository.findByTokenAndIsUsedFalse(token)
                .orElseThrow(() -> new InvalidVerificationTokenException(
                        "Токен недействителен или уже был ранее использован"));

        if (!verificationToken.isValid()) {
            throw new InvalidVerificationTokenException("Токен недействителен");
        }

        User user = verificationToken.getUser();

        if (user.getEmailVerified()) {
            throw new EmailAlreadyVerifiedException("Email уже подтверждён");
        }

        user.setEmailVerified(true);
        user.setEmailVerifiedAt(Instant.now());

        verificationToken.setIsUsed(true);
        verificationToken.setVerifiedAt(Instant.now());

    }

    public void resendVerificationEmail(String email) {
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new NotFoundException("Пользователя с таким email не существует"));

        if (user.getEmailVerified()) {
            throw new EmailAlreadyVerifiedException("Email уже подтверждён");
        }

        Optional<EmailVerificationToken> recentToken = emailVerificationTokenRepository
                .findFirstByUserAndIsUsedFalseOrderByCreatedAtDesc(user);

        if (recentToken.isPresent()) {
            Instant lastSent = recentToken.get().getCreatedAt();
            long minutesSinceLastSend = ChronoUnit.MINUTES.between(lastSent, Instant.now());

            if (minutesSinceLastSend < 1) {
                throw new TooManyRequestsException("Подождите " + (1 - minutesSinceLastSend) + " минут перед повторной отправкой");
            }
        }

        sendVerificationEmail(user);
    }


    private String generateVerificationToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }


    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredTokens() {
        emailVerificationTokenRepository.deleteExpiredTokens(Instant.now());
        log.info("Expired email verification tokens cleaned up");
    }
}
