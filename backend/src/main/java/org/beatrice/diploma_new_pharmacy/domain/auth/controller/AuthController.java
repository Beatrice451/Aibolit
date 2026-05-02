package org.beatrice.diploma_new_pharmacy.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.beatrice.diploma_new_pharmacy.domain.auth.dto.AccessTokenResponse;
import org.beatrice.diploma_new_pharmacy.domain.auth.dto.AuthRequest;
import org.beatrice.diploma_new_pharmacy.domain.auth.dto.UserRegistrationRequest;
import org.beatrice.diploma_new_pharmacy.domain.auth.service.AuthenticationService;
import org.beatrice.diploma_new_pharmacy.domain.auth.service.RegistrationService;
import org.beatrice.diploma_new_pharmacy.domain.auth.service.model.LoginCommand;
import org.beatrice.diploma_new_pharmacy.domain.auth.service.model.RegistrationCommand;
import org.beatrice.diploma_new_pharmacy.domain.auth.service.model.TokenPair;
import org.beatrice.diploma_new_pharmacy.domain.auth.util.RefreshCookieFactory;
import org.beatrice.diploma_new_pharmacy.exception.ErrorResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Аутентификация", description = "Эндпоинты для регистрации, входа, обновления токена и выхода")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final RefreshCookieFactory refreshCookieFactory;
    private final RegistrationService registrationService;


    @Operation(
            summary = "Вход пользователя",
            description = "Аутентификация пользователя по email и паролю. При успехе возвращает access token и " +
                    "устанавливает refresh token в cookie."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешная аутентификация", content = @Content(schema =
            @Schema(implementation = AccessTokenResponse.class))),
            @ApiResponse(responseCode = "400", description = "Невалидный запрос (некорректный формат email)",
                         content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Неверные учетные данные (неправильный email или пароль)"
                    , content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<AccessTokenResponse> login(@Valid @RequestBody AuthRequest request) {
        TokenPair tokenPairResponse = authenticationService.login(new LoginCommand(
                request.email(),
                request.password()
        ));

        ResponseCookie cookie = refreshCookieFactory.create(tokenPairResponse.refreshToken());

        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new AccessTokenResponse(tokenPairResponse.accessToken()));
    }


    @Operation(
            summary = "Регистрация нового пользователя",
            description = "Регистрация нового пользователя в системе. Требуется указать email, телефон и пароль " +
                    "(минимум 8 символов). При успехе возвращает 201 Created."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Пользователь успешно зарегистрирован"),
            @ApiResponse(responseCode = "400", description = "Невалидный запрос (некорректный email, телефон или " +
                    "пароль < 8 символов)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Пользователь с указанным email или телефоном уже " +
                    "существует", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRegistrationRequest request) {
        registrationService.registerUser(new RegistrationCommand(
                request.email(),
                request.phone(),
                request.password(),
                request.firstName(),
                request.lastName()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @Operation(
            summary = "Обновление токена доступа",
            description = "Обновление access token с помощью refresh token. Refresh token передается в cookie. При " +
                    "успехе возвращает новый access token и обновляет refresh token."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Токен успешно обновлен", content = @Content(schema =
            @Schema(implementation = AccessTokenResponse.class))),
            @ApiResponse(responseCode = "401", description = "Refresh token отсутствует, истек или отозван", content
                    = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/refresh")
    public ResponseEntity<AccessTokenResponse> refresh(@CookieValue(value = "refreshToken", required = false) String refreshToken) {
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        TokenPair token = authenticationService.refresh(refreshToken);

        ResponseCookie cookie = refreshCookieFactory.create(token.refreshToken());

        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new AccessTokenResponse(token.accessToken()));
    }

    @Operation(
            summary = "Выход пользователя",
            description = "Выход пользователя из системы. Удаляет refresh token из базы данных и очищает cookie."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Успешный выход")
    })
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
