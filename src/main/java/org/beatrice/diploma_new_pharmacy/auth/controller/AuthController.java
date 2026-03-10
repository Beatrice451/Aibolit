package org.beatrice.diploma_new_pharmacy.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.beatrice.diploma_new_pharmacy.auth.dto.AccessTokenResponse;
import org.beatrice.diploma_new_pharmacy.auth.dto.AuthRequest;
import org.beatrice.diploma_new_pharmacy.auth.dto.UserRegistrationRequest;
import org.beatrice.diploma_new_pharmacy.auth.service.AuthenticationService;
import org.beatrice.diploma_new_pharmacy.auth.service.RegistrationService;
import org.beatrice.diploma_new_pharmacy.auth.service.model.LoginCommand;
import org.beatrice.diploma_new_pharmacy.auth.service.model.RegistrationCommand;
import org.beatrice.diploma_new_pharmacy.auth.service.model.TokenPair;
import org.beatrice.diploma_new_pharmacy.auth.util.RefreshCookieFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {


    private final AuthenticationService authenticationService;
    private final RefreshCookieFactory refreshCookieFactory;
    private final RegistrationService registrationService;


    @PostMapping("/login")
    public ResponseEntity<AccessTokenResponse> login(@RequestBody AuthRequest request) {
        TokenPair tokenPairResponse = authenticationService.login(new LoginCommand(request.email(), request.password()));

        ResponseCookie cookie = refreshCookieFactory.create(tokenPairResponse.refreshToken());

        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new AccessTokenResponse(tokenPairResponse.accessToken()));
    }


    /**
     * Registers a new user in the system.
     *
     * <p>
     * Accepts user registration data and delegates registration procces to the authenticaton service.
     * If the user is successfully created, returns HTTP 201 (Created) with empty body.
     * </p>
     * <p>
     * If the user with the same unique credentials (e.g., email or phone) already exists,
     * returns HTTP 409 (Conflict) with an error message.
     * </p>
     *
     * @param request the registration request containing required data
     * @return a {@link ResponseEntity} with HTTP 201 if registration succeeds, HTTP 409 otherwise.
     * @see AuthenticationService
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRegistrationRequest request) {
        registrationService.registerUser(new RegistrationCommand(
                request.name(),
                request.email(),
                request.phone(),
                request.password()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @PostMapping("/refresh")
    public ResponseEntity<AccessTokenResponse> refresh(@CookieValue("refreshToken") String refreshToken) {
        TokenPair token = authenticationService.refresh(refreshToken);

        ResponseCookie cookie = refreshCookieFactory.create(token.refreshToken());

        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new AccessTokenResponse(token.accessToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(value = "refreshToken", required = false) String refreshToken) {
        if (refreshToken != null) {
            authenticationService.logout(refreshToken);
        }

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, refreshCookieFactory.delete().toString())
                .build();
    }
}
