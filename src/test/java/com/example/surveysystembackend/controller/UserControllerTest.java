package com.example.surveysystembackend.controller;

import com.example.surveysystembackend.DTO.User.UserDTO;
import com.example.surveysystembackend.service.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import javax.persistence.EntityNotFoundException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    void testGetAllUsers() {
        log.info("Setting up test for Get All Users");

        // Arrange
        List<UserDTO> users = Collections.singletonList(new UserDTO());
        when(userService.getAllUsers()).thenReturn(users);

        // Act
        ResponseEntity<List<UserDTO>> responseEntity = userController.getAllUsers();

        // Assert
        assertEquals(ResponseEntity.ok(users), responseEntity);
        verify(userService, times(1)).getAllUsers();
        log.info("Get All Users - Response: {}", responseEntity);
    }

    @Test
    void testGetUserById() {
        log.info("Setting up test for Get User By ID");

        // Arrange
        String userId = "123";
        UserDTO userDTO = new UserDTO();
        userDTO.setId(userId);

        when(userService.getUserById(userId)).thenReturn(userDTO);

        // Act
        ResponseEntity<UserDTO> responseEntity = userController.getUserById(userId);

        // Assert
        assertEquals(ResponseEntity.ok(userDTO), responseEntity);
        verify(userService, times(1)).getUserById(userId);
        log.info("Get User By ID - Response: {}", responseEntity);
    }

    @Test
    void testUpdateUserDetails() {
        log.info("Setting up test for Update User Details");

        // Arrange
        String userId = "123";
        UserDTO updatedUser = new UserDTO();

        when(userService.updateUserDetails(userId, updatedUser)).thenReturn(true);

        // Act
        ResponseEntity<?> responseEntity = userController.updateUserDetails(userId, updatedUser);

        // Assert
        assertEquals(ResponseEntity.ok("{\"message\":\"User details updated successfully!\"}"), responseEntity);
        verify(userService, times(1)).updateUserDetails(userId, updatedUser);
        log.info("Update User Details - Response: {}", responseEntity);
    }

    @Test
    void testUpdateUserDetails_UserNotFound() {
        log.info("Setting up test for Update User Details - User Not Found");

        // Arrange
        String userId = "123";
        UserDTO updatedUser = new UserDTO();

        when(userService.updateUserDetails(userId, updatedUser)).thenReturn(false);

        // Act and Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> userController.updateUserDetails(userId, updatedUser));

        assertEquals("User not found for ID: " + userId, exception.getMessage());
        verify(userService, times(1)).updateUserDetails(userId, updatedUser);
        log.info("Update User Details - User Not Found - Exception Message: {}", exception.getMessage());
    }

}
