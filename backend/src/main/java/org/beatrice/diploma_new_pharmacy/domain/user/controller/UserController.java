package org.beatrice.diploma_new_pharmacy.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.beatrice.diploma_new_pharmacy.domain.auth.security.SecurityUser;
import org.beatrice.diploma_new_pharmacy.domain.user.dto.UserResponse;
import org.beatrice.diploma_new_pharmacy.domain.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
class UserController {

    private final UserService userService;

    @SuppressWarnings("deprecation")
    @GetMapping("/whoami")
    public ResponseEntity<UserResponse> whoami(@AuthenticationPrincipal SecurityUser user) {
        UserResponse response = userService.whoami(user);
        if (response == null) {
            return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).build(); // why not?
        }
        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteCurrentUser(@AuthenticationPrincipal SecurityUser user) {
        userService.deleteUser(user.user().getId());
        return ResponseEntity.ok().build();
    }
}
