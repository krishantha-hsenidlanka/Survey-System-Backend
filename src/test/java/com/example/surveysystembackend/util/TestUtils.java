package com.example.surveysystembackend.util;

import com.example.surveysystembackend.DTO.Authentication.LoginRequestDTO;
import com.example.surveysystembackend.DTO.Authentication.SignupRequestDTO;
import com.example.surveysystembackend.DTO.User.ChangePasswordRequestDTO;

public class TestUtils {

    public static LoginRequestDTO createLoginRequestDTO() {
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setUsername("testUser");
        loginRequestDTO.setPassword("testPassword");
        return loginRequestDTO;
    }

    public static SignupRequestDTO createSignupRequestDTO() {
        SignupRequestDTO signupRequestDTO = new SignupRequestDTO();
        signupRequestDTO.setUsername("testUser");
        signupRequestDTO.setEmail("test@example.com");
        signupRequestDTO.setPassword("testPassword");
        return signupRequestDTO;
    }

    public static ChangePasswordRequestDTO createChangePasswordRequestDTO() {
        ChangePasswordRequestDTO changePasswordRequestDTO = new ChangePasswordRequestDTO();
        changePasswordRequestDTO.setCurrentPassword("oldPassword");
        changePasswordRequestDTO.setNewPassword("newPassword");
        return changePasswordRequestDTO;
    }
}
