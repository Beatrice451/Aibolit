package org.beatrice.diploma_new_pharmacy.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.beatrice.diploma_new_pharmacy.domain.user.dto.response.RoleResponse;
import org.beatrice.diploma_new_pharmacy.domain.user.dto.response.UserResponse;
import org.beatrice.diploma_new_pharmacy.domain.user.service.UserService;
import org.beatrice.diploma_new_pharmacy.domain.user.specification.UserFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
class AdminUserController {

    private final UserService userService;

    @PutMapping("/{userId}/roles/{roleId}")
    public ResponseEntity<UserResponse> assignRole(@PathVariable Integer userId, @PathVariable Integer roleId) {
        return ResponseEntity.ok(userService.addUserRole(userId, roleId));
    }

    @DeleteMapping("/{userId}/roles/{roleId}")
    public ResponseEntity<UserResponse> unassignRole(@PathVariable Integer userId, @PathVariable Integer roleId) {
        return ResponseEntity.ok(userService.deleteUserRole(userId, roleId));
    }

    @GetMapping
    public ResponseEntity<Page<UserResponse>> getUsers(UserFilter filter, Pageable pageable) {
        return ResponseEntity.ok(userService.getUsers(filter, pageable));
    }

    @GetMapping("/roles")
    public ResponseEntity<List<RoleResponse>> getRoles() {
        return ResponseEntity.ok(userService.getRoles());
    }

    @PatchMapping("/{userId}/restore")
    public ResponseEntity<UserResponse> restoreUser(@PathVariable Integer userId) {
        userService.restoreUser(userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<UserResponse> deleteUser(@PathVariable Integer userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
