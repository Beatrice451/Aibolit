package org.beatrice.diploma_new_pharmacy.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.beatrice.diploma_new_pharmacy.domain.user.dto.UserResponse;
import org.beatrice.diploma_new_pharmacy.domain.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
class AdminUserController {

    private final UserService userService;

    @PutMapping("/{userId}/role/{roleId}")
    public ResponseEntity<UserResponse> assignRole(@PathVariable Integer userId, @PathVariable Integer roleId) {
        return ResponseEntity.ok(userService.addUserRole(userId, roleId));
    }

    @DeleteMapping("/{userId}/role/{roleId}")
    public ResponseEntity<UserResponse> unassignRole(@PathVariable Integer userId, @PathVariable Integer roleId) {
        return ResponseEntity.ok(userService.deleteUserRole(userId, roleId));
    }
}
