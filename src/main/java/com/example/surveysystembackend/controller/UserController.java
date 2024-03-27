package com.example.surveysystembackend.controller;

import com.example.surveysystembackend.DTO.User.UserDTO;
import com.example.surveysystembackend.service.user.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        log.info("API hit: GET /api/users");
        List<UserDTO> users = userService.getAllUsers();
        log.info("All users fetched successfully");
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable String userId) {
        log.info("API hit: GET /api/users/{}", userId);
        UserDTO user = userService.getUserById(userId);
        log.info("User fetched successfully for ID: {}", userId);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUserDetails(@PathVariable String userId, @Valid @RequestBody UserDTO updatedUser) {
        log.info("API hit: PUT /api/users/{}", userId);
        boolean success = userService.updateUserDetails(userId, updatedUser);
        if (success) {
            log.info("User details updated successfully for ID: {}", userId);
            return ResponseEntity.ok("{\"message\":\"User details updated successfully!\"}");
        } else {
            log.warn("User not found for ID: {}", userId);
            throw new EntityNotFoundException("User not found for ID: " + userId);
        }
    }

    @PutMapping("/{userId}/status")
    public ResponseEntity<?> updateUserStatus(@PathVariable String userId, @RequestParam boolean enabled) {
        log.info("API hit: PUT /api/users/{}/status", userId);
        boolean success = userService.updateUserStatus(userId, enabled);
        if (success) {
            log.info("User status updated successfully for ID: {}", userId);
            return ResponseEntity.ok("User status updated successfully!");
        } else {
            log.warn("User not found for ID: {}", userId);
            throw new EntityNotFoundException("User not found for ID: " + userId);
        }
    }

    @PutMapping("/{userId}/roles")
    public ResponseEntity<?> updateRoles(@PathVariable String userId, @RequestBody List<String> userRoles) {
        log.info("API hit: PUT /api/users/{}/roles", userId);
        boolean success = userService.updateRoles(userId, userRoles);
        if (success) {
            log.info("User roles updated successfully for ID: {}", userId);
            return ResponseEntity.ok("User roles updated successfully!");
        } else {
            log.warn("User not found for ID: {}", userId);
            throw new EntityNotFoundException("User not found for ID: " + userId);
        }
    }
}
