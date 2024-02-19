package com.example.surveysystembackend.controller;

import com.example.surveysystembackend.DTO.UserDTO;
import com.example.surveysystembackend.model.User;
import com.example.surveysystembackend.service.user.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
@AllArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable String userId) {
        return userService.getUserById(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUserDetails(@PathVariable String userId, @Valid @RequestBody UserDTO updatedUser) {
        boolean success = userService.updateUserDetails(userId, updatedUser);
        return success ? ResponseEntity.ok(new String("User details updated successfully!"))
                : ResponseEntity.notFound().build();
    }

    @PutMapping("/{userId}/status")
    public ResponseEntity<?> updateUserStatus(@PathVariable String userId, @RequestParam boolean enabled) {
        boolean success = userService.updateUserStatus(userId, enabled);
        return success ? ResponseEntity.ok(new String("User status updated successfully!"))
                : ResponseEntity.notFound().build();
    }

    @PutMapping("/{userId}/roles")
    public ResponseEntity<?> updateRoles(@PathVariable String userId, @RequestBody List<String> userRoles) {
        boolean success = userService.updateRoles(userId, userRoles);
        return success ? ResponseEntity.ok(new String("User roles updated successfully!"))
                : ResponseEntity.notFound().build();
    }
}
