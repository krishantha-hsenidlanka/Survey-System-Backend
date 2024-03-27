package com.example.surveysystembackend.controller;

import com.example.surveysystembackend.DTO.Authentication.LoginRequestDTO;
import com.example.surveysystembackend.DTO.Authentication.SignupRequestDTO;
import com.example.surveysystembackend.DTO.User.ChangePasswordRequestDTO;
import com.example.surveysystembackend.exception.CustomRuntimeException;
import com.example.surveysystembackend.service.Auth.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequestDTO loginRequest) {
        log.info("API hit: /login endpoint");
        return authService.authenticateUser(loginRequest);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequestDTO signUpRequest) {
        log.info("API hit: /signup endpoint");
        return authService.registerUser(signUpRequest);
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequestDTO changePasswordRequest) {
        log.info("API hit: /change-password endpoint");
        return authService.changePassword(changePasswordRequest);
    }

    @GetMapping("/user-details")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getUserDetails() {
        log.info("API hit: /user-details endpoint");
        return authService.getUserDetails();
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyUser(@RequestParam String token) {
        log.info("API hit: /verify endpoint");
        if (authService.verifyUser(token)) {
            return ResponseEntity.ok("User verified successfully!");
        } else {
            throw new CustomRuntimeException("Invalid or expired verification token.", HttpStatus.BAD_REQUEST);
        }
    }

}