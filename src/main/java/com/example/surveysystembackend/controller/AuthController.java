package com.example.surveysystembackend.controller;

import com.example.surveysystembackend.DTO.Authentication.JwtResponseDTO;
import com.example.surveysystembackend.DTO.Authentication.LoginRequestDTO;
import com.example.surveysystembackend.DTO.Authentication.SignupRequestDTO;
import com.example.surveysystembackend.DTO.User.ChangePasswordRequestDTO;
import com.example.surveysystembackend.DTO.User.UserDTO;
import com.example.surveysystembackend.exception.CustomRuntimeException;
import com.example.surveysystembackend.model.ERole;
import com.example.surveysystembackend.model.Role;
import com.example.surveysystembackend.model.User;
import com.example.surveysystembackend.repository.RoleRepository;
import com.example.surveysystembackend.repository.UserRepository;
import com.example.surveysystembackend.security.jwt.JwtUtils;
import com.example.surveysystembackend.service.user.UserDetailsImpl;
import com.example.surveysystembackend.service.user.UserDetailsServiceImpl;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequestDTO loginRequest) {
        log.info("Attempting to authenticate user: {}", loginRequest.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        JwtResponseDTO responseDTO = JwtResponseDTO.builder()
                .token(jwt)
                .id(userDetails.getId())
                .username(userDetails.getUsername())
                .email(userDetails.getEmail())
                .roles(roles)
                .type("Bearer")
                .build();
        log.info("User authenticated successfully: {}", userDetails.getUsername());
        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequestDTO signUpRequest) {
        log.info("Registering new user: {}", signUpRequest.getUsername());

        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            log.error("Error: Username is already taken! Username: {}",signUpRequest.getUsername());
            throw new CustomRuntimeException("Username is already taken! Username: "+ signUpRequest.getUsername(), HttpStatus.BAD_REQUEST );
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            log.error("Error: Email is already in use! Email: {}", signUpRequest.getEmail());
            throw new CustomRuntimeException("Email is already in use! Email: "+ signUpRequest.getEmail(), HttpStatus.BAD_REQUEST );
        }

        User user = User.builder()
                .username(signUpRequest.getUsername())
                .email(signUpRequest.getEmail())
                .password(encoder.encode(signUpRequest.getPassword()))
                .enabled(true)
                .registrationDate(new Date())
                .build();


        Set<String> strRoles = signUpRequest.getRoles();
        Set<Role> roles = new HashSet<>();


        if (strRoles == null) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);

                        break;
                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);
        log.info("User registered successfully: {}", user.getUsername());

        return ResponseEntity.ok(new String ("{\"message\":\"User registered successfully!\"}"));
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequestDTO changePasswordRequest) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        log.info("Changing password for user: {}", user.getUsername());

        if (!passwordEncoder.matches(changePasswordRequest.getCurrentPassword(), user.getPassword())) {
            log.error("Error: Current password is incorrect, user: {}", user.getUsername());
            throw new CustomRuntimeException("Current password is incorrect", HttpStatus.BAD_REQUEST);
        }

        user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        userRepository.save(user);

        log.info("Password changed successfully for user: {}", userDetails.getUsername());
        return ResponseEntity.ok(new String("{\"message\":\"Password changed successfully\"}"));
    }

    @GetMapping("/user-details")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getUserDetails() {
        log.info("Fetching user details");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info(String.valueOf(authentication.getName()));
        if (authentication == null || !authentication.isAuthenticated() || Objects.equals(authentication.getName(), "anonymousUser")) {
            log.error("Unauthorized access to user details");
            throw new CustomRuntimeException("Unauthorized access to user details", HttpStatus.UNAUTHORIZED);
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        UserDTO userDTO = modelMapper.map(user, UserDTO.class);

        log.info("User details fetched successfully: {}", userDetails.getUsername());
        return ResponseEntity.ok(userDTO);
    }


}