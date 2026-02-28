package org.beatrice.diploma_new_pharmacy.auth.service;

import org.beatrice.diploma_new_pharmacy.auth.SecurityUser;
import org.beatrice.diploma_new_pharmacy.auth.model.RefreshToken;
import org.beatrice.diploma_new_pharmacy.auth.service.model.LoginCommand;
import org.beatrice.diploma_new_pharmacy.auth.service.model.TokenPair;
import org.beatrice.diploma_new_pharmacy.user.model.User;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthenticationService(AuthenticationManager authenticationManager, CustomUserDetailsService customUserDetailsService, JwtService jwtService, RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.customUserDetailsService = customUserDetailsService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    public TokenPair login(LoginCommand cmd) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(cmd.email(), cmd.rawPassword()));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(cmd.email());
        User user = ((SecurityUser) userDetails).getUser();

        String accessToken = jwtService.generateAccessToken(user);

        RefreshToken refreshToken = refreshTokenService.create(user);

        return new TokenPair(accessToken, refreshToken.getToken());
    }


    @Transactional
    public TokenPair refresh(String refreshToken) {
        RefreshToken stored = refreshTokenService.validate(refreshToken);
        String accessToken = jwtService.generateAccessToken(stored.getUser());
        RefreshToken newToken = refreshTokenService.create(stored.getUser());
        return new TokenPair(accessToken, newToken.getToken());
    }
}
