package org.beatrice.diploma_new_pharmacy.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.beatrice.diploma_new_pharmacy.domain.auth.model.RefreshToken;
import org.beatrice.diploma_new_pharmacy.domain.auth.repository.RefreshTokenRepository;
import org.beatrice.diploma_new_pharmacy.domain.auth.security.JwtService;
import org.beatrice.diploma_new_pharmacy.domain.auth.security.SecurityUser;
import org.beatrice.diploma_new_pharmacy.domain.auth.service.model.LoginCommand;
import org.beatrice.diploma_new_pharmacy.domain.auth.service.model.TokenPair;
import org.beatrice.diploma_new_pharmacy.domain.user.model.User;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final TokenRevocationService tokenRevocationService;


    public TokenPair login(LoginCommand cmd) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(cmd.email(), cmd.rawPassword()));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(cmd.email());
        User user = ((SecurityUser) userDetails).user();

        String accessToken = jwtService.generateAccessToken(user);

        RefreshToken refreshToken = refreshTokenService.create(user);

        return new TokenPair(accessToken, refreshToken.getToken());
    }

    @Transactional
    public void logout(String refreshToken) {
        tokenRevocationService.revokeByTokenValue(refreshToken, "Logout");
    }


    @Transactional
    public TokenPair refresh(String refreshToken) {
        RefreshToken newToken = refreshTokenService.replaceToken(refreshToken);
        String accessToken = jwtService.generateAccessToken(newToken.getUser());
        return new TokenPair(accessToken, newToken.getToken());
    }
}
