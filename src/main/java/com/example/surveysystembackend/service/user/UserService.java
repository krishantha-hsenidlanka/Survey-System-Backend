package com.example.surveysystembackend.service.user;

import com.example.surveysystembackend.DTO.User.UserDTO;
import com.example.surveysystembackend.exception.CustomRuntimeException;
import com.example.surveysystembackend.model.ERole;
import com.example.surveysystembackend.model.Role;
import com.example.surveysystembackend.model.User;
import com.example.surveysystembackend.repository.RoleRepository;
import com.example.surveysystembackend.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ModelMapper modelMapper;

    public List<UserDTO> getAllUsers() {
        try {
            log.info("Attempting to get all users");
            List<User> users = userRepository.findAll();
            if (users.isEmpty()) {
                throw new EntityNotFoundException("No users found");
            }
            return users.stream()
                    .map(user -> modelMapper.map(user, UserDTO.class))
                    .collect(Collectors.toList());
        } catch (EntityNotFoundException e) {
            log.warn("No users found: {}", e.getMessage());
            throw e;
        }
        catch (Exception e) {
            log.error("Error getting all users: {}", e.getMessage(), e);
            throw new RuntimeException("Error getting all users", e);
        }
    }

    public UserDTO getUserById(String userId) {
        try {
            log.info("Attempting to get user by ID: {}", userId);
            return userRepository.findById(userId)
                    .map(user -> {
                        log.info("User found: {}", userId);
                        return modelMapper.map(user, UserDTO.class);
                    })
                    .orElseThrow(() -> {
                        return new EntityNotFoundException("User not found for ID: " + userId);
                    });
        } catch (EntityNotFoundException e) {
            log.warn("User not found for ID: {}", userId);
            throw e;
        } catch (Exception e) {
            log.error("Error getting user by ID: {}", e.getMessage(), e);
            throw new RuntimeException("Error getting user by ID", e);
        }
    }


    public boolean updateUserDetails(String userId, UserDTO updatedUser) {
        try {
            log.info("Attempting to update user details, User Id: {}", userId);
            return userRepository.findById(userId)
                    .map(user -> {
                        // Map individual fields excluding null values
                        if (updatedUser.getUsername() != null) {
                            user.setUsername(updatedUser.getUsername());
                        }
                        if (updatedUser.getEmail() != null) {
                            user.setEmail(updatedUser.getEmail());
                        }
                        if (updatedUser.getEnabled() != null) {
                            user.setEnabled(updatedUser.getEnabled());
                        }
                        if (updatedUser.getRegistrationDate() != null) {
                            user.setRegistrationDate(updatedUser.getRegistrationDate());
                        }

                        if (updatedUser.getRoles() != null) {
                            Set<Role> roles = updatedUser.getRoles().stream()
                                    .map(roleName -> {
                                        Optional<ERole> eRoleOptional = Arrays.stream(ERole.values())
                                                .filter(role -> role.name().equals(roleName))
                                                .findFirst();

                                        if (eRoleOptional.isPresent()) {
                                            return roleRepository.findByName(eRoleOptional.get())
                                                    .orElseThrow(() -> {
                                                        log.error("Error: Role is not found.");
                                                        throw new CustomRuntimeException("Error: Role is not found.", HttpStatus.BAD_REQUEST);
                                                    });
                                        } else {
                                            log.error("Error: Invalid role name - {}", roleName);
                                            throw new CustomRuntimeException("Error: Invalid role name - " + roleName, HttpStatus.BAD_REQUEST);
                                        }
                                    })
                                    .collect(Collectors.toSet());

                            user.setRoles(roles);
                        }

                        userRepository.save(user);
                        log.info("User details updated successfully, User Id: {}", userId);
                        return true;
                    })
                    .orElseThrow(() -> {
                        log.warn("User not found, User Id: {}", userId);
                        throw new EntityNotFoundException("User not found, User Id: " + userId);
                    });
        } catch (EntityNotFoundException e) {
            log.warn("User not found for ID: {}", userId);
            throw e;
        }
        catch (CustomRuntimeException e) {
            log.error("Error updating user details: {}", e.getMessage(), e);
            throw e;
        }
        catch (Exception e) {
            log.error("Error updating user details: {}", e.getMessage(), e);
            throw new RuntimeException("Error updating user details", e);
        }
    }

    public boolean updateUserStatus(String userId, boolean enabled) {
        try {
            log.info("Attempting to update user status, User Id: {}", userId);
            return userRepository.findById(userId)
                    .map(user -> {
                        user.setEnabled(enabled);
                        userRepository.save(user);
                        log.info("User status updated successfully, User Id: {}", userId);
                        return true;
                    })
                    .orElseThrow(() -> {
                        throw new EntityNotFoundException("User not found, User Id: " + userId);
                    });
        } catch (EntityNotFoundException e) {
            log.warn("User not found, User Id: {}", userId);
            throw e;
        } catch (Exception e) {
            log.error("Error updating user status: {}", e.getMessage(), e);
            throw new RuntimeException("Error updating user status", e);
        }
    }

    public boolean updateRoles(String userId, List<String> userRoles) {
        try {
            log.info("Attempting to update user roles, User Id: {}", userId);
            return userRepository.findById(userId)
                    .map(user -> {
                        Set<Role> roles = userRoles.stream()
                                .map(roleName -> roleRepository.findByName(ERole.valueOf(roleName))
                                        .orElseThrow(() -> {
                                            log.error("Error: Role is not found.");
                                            throw new CustomRuntimeException("Error: Role is not found.", HttpStatus.BAD_REQUEST);
                                        }))
                                .collect(Collectors.toSet());

                        user.setRoles(roles);
                        userRepository.save(user);
                        log.info("User roles updated successfully, User Id: {}", userId);
                        return true;
                    })
                    .orElseThrow(() -> {
                        log.warn("User not found, User Id: {}", userId);
                        throw new EntityNotFoundException("User not found, User Id: " + userId);
                    });
        } catch (EntityNotFoundException e) {
            log.warn("User not found, User Id: {}", userId);
            throw e;
        } catch (CustomRuntimeException e) {
            log.error("Error updating user roles: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Error updating user roles: {}", e.getMessage(), e);
            throw new RuntimeException("Error updating user roles", e);
        }
    }
}
