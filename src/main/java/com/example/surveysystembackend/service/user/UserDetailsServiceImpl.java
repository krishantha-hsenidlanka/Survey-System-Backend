package com.example.surveysystembackend.service.user;

import com.example.surveysystembackend.model.User;
import com.example.surveysystembackend.repository.UserRepository;
import com.example.surveysystembackend.security.jwt.AuthEntryPointJwt;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private final UserRepository userRepository;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            log.info("Attempting to load user by username: {}", username);

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));

            log.info("User loaded from database: {}", user);

            return UserDetailsImpl.build(user);
        } catch (UsernameNotFoundException e) {
            log.error("User not found with username: {}", username);
            throw e;
        } catch (Exception e) {
            log.error("Error loading user by username: {}", e.getMessage(), e);
            throw new RuntimeException("Error loading user by username", e);
        }
    }
}