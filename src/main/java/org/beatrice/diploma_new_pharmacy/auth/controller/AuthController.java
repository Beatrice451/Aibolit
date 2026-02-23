package org.beatrice.diploma_new_pharmacy.auth.controller;

import org.beatrice.diploma_new_pharmacy.auth.dto.RefreshRequest;
import org.beatrice.diploma_new_pharmacy.auth.exception.InvalidTokenException;
import org.beatrice.diploma_new_pharmacy.auth.exception.RevokedTokenException;
import org.beatrice.diploma_new_pharmacy.auth.exception.TokenNotFoundException;
import org.beatrice.diploma_new_pharmacy.auth.service.JwtService;
import org.beatrice.diploma_new_pharmacy.auth.SecurityUser;
import org.beatrice.diploma_new_pharmacy.auth.dto.AuthRequest;
import org.beatrice.diploma_new_pharmacy.auth.dto.AuthResponse;
import org.beatrice.diploma_new_pharmacy.auth.model.RefreshToken;
import org.beatrice.diploma_new_pharmacy.auth.repository.RefreshTokenRepository;
import org.beatrice.diploma_new_pharmacy.auth.service.AuthService;
import org.beatrice.diploma_new_pharmacy.auth.service.CustomUserDetailsService;
import org.beatrice.diploma_new_pharmacy.auth.service.RefreshTokenService;
import org.beatrice.diploma_new_pharmacy.user.dto.UserRegistrationRequest;
import org.beatrice.diploma_new_pharmacy.auth.exception.UserAlreadyExistsException;
import org.beatrice.diploma_new_pharmacy.user.model.User;
import org.beatrice.diploma_new_pharmacy.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {


    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;
    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;


    public AuthController(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            CustomUserDetailsService customUserDetailsService,
            RefreshTokenRepository refreshTokenRepository,
            UserRepository userRepository,
            AuthService authService,
            RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.customUserDetailsService = customUserDetailsService;
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(request.email());
        User user = ((SecurityUser) userDetails).getUser();

        String accessToken = jwtService.generateAccessToken(user);

        RefreshToken refreshToken = refreshTokenService.create(user);

        return new AuthResponse(accessToken, refreshToken.getToken());
    }


    @PostMapping("/register")
    public ResponseEntity<?> handleRegistration(@RequestBody UserRegistrationRequest request) {
        try {
            authService.registerUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshRequest request) {
        try {
            AuthResponse token = authService.refresh(request.refreshToken());
            return ResponseEntity.status(HttpStatus.OK)
                    .body(token);
        } catch (InvalidTokenException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Stated token is expired");

        } catch (RevokedTokenException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Stated token is revoked. Every other token will be revoked for security reasons");
        } catch (TokenNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Token not found or invalid");
        }
    }
}
