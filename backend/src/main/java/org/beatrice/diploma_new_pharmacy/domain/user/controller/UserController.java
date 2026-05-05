package org.beatrice.diploma_new_pharmacy.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.beatrice.diploma_new_pharmacy.domain.auth.security.SecurityUser;
import org.beatrice.diploma_new_pharmacy.domain.user.dto.request.UpdateUserRequest;
import org.beatrice.diploma_new_pharmacy.domain.user.dto.response.UserResponse;
import org.beatrice.diploma_new_pharmacy.domain.user.service.UserService;
import org.beatrice.diploma_new_pharmacy.exception.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Пользователи", description = "Эндпоинты для работы с данными текущего пользователя")
class UserController {

    private final UserService userService;

    @Operation(
            summary = "Получить текущего пользователя",
            description = "Возвращает информацию о текущем аутентифицированном пользователе."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Данные пользователя получены", content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "418", description = "Пользователь отключен (заблокирован)", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SuppressWarnings("deprecation")
    @GetMapping("/whoami")
    public ResponseEntity<UserResponse> whoami(@AuthenticationPrincipal SecurityUser user) {
        UserResponse response = userService.whoami(user);
        if (response == null) {
            return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).build(); // why not?
        }
        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "Обновить данные текущего пользователя",
            description = "Обновляет имя, фамилию, телефон и предпочитаемую аптеку текущего пользователя."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Данные пользователя обновлены", content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Невалидный запрос", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь или аптека не найдены", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateCurrentUser(
            @AuthenticationPrincipal SecurityUser user,
            @Valid @RequestBody UpdateUserRequest request) {
        UserResponse response = userService.updateUser(user.user().getId(), request);
        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "Удалить текущего пользователя",
            description = "Удаляет (деактивирует) текущего аутентифицированного пользователя."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно удален"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteCurrentUser(@AuthenticationPrincipal SecurityUser user) {
        userService.deleteUser(user.user().getId());
        return ResponseEntity.ok().build();
    }
}
