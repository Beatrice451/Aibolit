package org.beatrice.diploma_new_pharmacy.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.beatrice.diploma_new_pharmacy.config.JwtProperties;
import org.beatrice.diploma_new_pharmacy.domain.auth.exception.InvalidTokenException;
import org.beatrice.diploma_new_pharmacy.domain.auth.exception.RevokedTokenException;
import org.beatrice.diploma_new_pharmacy.domain.auth.exception.TokenNotFoundException;
import org.beatrice.diploma_new_pharmacy.domain.auth.model.RefreshToken;
import org.beatrice.diploma_new_pharmacy.domain.auth.repository.RefreshTokenRepository;
import org.beatrice.diploma_new_pharmacy.domain.user.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {


    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;
    private final TokenRevocationService tokenRevocationService;

    /**
     * @return UUID representing refresh token
     */
//    Why UUID instead of JWT? UUID refresh token is stateful.
//    I can't revoke JWT cause it's stateless but I can revoke UUID (until I store it in DB)
    @Transactional
    public RefreshToken create(User user) {
        return create(user, null);

    }

    @Transactional
    public RefreshToken create(User user, UUID family) {
        String token = UUID.randomUUID().toString();
        Instant expiryDate = Instant.now().plus(jwtProperties.getRefreshTokenTtl(), ChronoUnit.DAYS);
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);
        refreshToken.setTokenFamily(family != null ? family : UUID.randomUUID());
        refreshToken.setExpiryDate(expiryDate);
        refreshToken.setRevoked(false);
        refreshToken.setUser(user);
        return refreshTokenRepository.save(refreshToken);
    }



    @Transactional
    public RefreshToken replaceToken(String previousTokenValue) {
        RefreshToken previousToken = refreshTokenRepository.findByToken(previousTokenValue)
                .orElseThrow(() -> new TokenNotFoundException("Refresh token not found"));
        validate(previousToken);
        previousToken.setIsCurrent(false);
        RefreshToken newToken = create(previousToken.getUser(), previousToken.getTokenFamily());
        tokenRevocationService.revoke(previousToken, "Token replaced", newToken);
        newToken.setIsCurrent(true);
        return newToken;
    }

    @Transactional
    public void validate(RefreshToken token) {
        if (token.getRevoked()) {
            tokenRevocationService.revokeFamily(token.getTokenFamily(), "Token reuse");
            throw new RevokedTokenException("Token reuse detected");
        }

        if (token.getExpiryDate().isBefore(Instant.now())) {
            throw new InvalidTokenException("Token expired");
        }
    }
}
