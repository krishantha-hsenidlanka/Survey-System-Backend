package com.example.surveysystembackend.service.user;

import com.example.surveysystembackend.DTO.User.UserDTO;
import com.example.surveysystembackend.model.ERole;
import com.example.surveysystembackend.model.Role;
import com.example.surveysystembackend.model.User;
import com.example.surveysystembackend.repository.RoleRepository;
import com.example.surveysystembackend.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ModelMapper modelMapper;

    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(user -> modelMapper.map(user, UserDTO.class))
                .collect(Collectors.toList());
    }


    public Optional<UserDTO> getUserById(String userId) {
        return userRepository.findById(userId)
                .map(user -> modelMapper.map(user, UserDTO.class));
    }

        public boolean updateUserDetails(String userId, UserDTO updatedUser) {
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
                                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                                    } else {
                                        throw new RuntimeException("Error: Invalid role name - " + roleName);
                                    }
                                })
                                .collect(Collectors.toSet());

                        user.setRoles(roles);
                    }







                    userRepository.save(user);
                    return true;
                })
                .orElse(false);
    }


    public boolean updateUserStatus(String userId, boolean enabled) {
        return userRepository.findById(userId)
                .map(user -> {
                    user.setEnabled(enabled);
                    userRepository.save(user);
                    return true;
                })
                .orElse(false);
    }

    public boolean updateRoles(String userId, List<String> userRoles) {
        return userRepository.findById(userId)
                .map(user -> {
                    Set<Role> roles = userRoles.stream()
                            .map(roleName -> roleRepository.findByName(ERole.valueOf(roleName))
                                    .orElseThrow(() -> new RuntimeException("Error: Role is not found.")))
                            .collect(Collectors.toSet());

                    user.setRoles(roles);
                    userRepository.save(user);
                    return true;
                })
                .orElse(false);
    }


}
