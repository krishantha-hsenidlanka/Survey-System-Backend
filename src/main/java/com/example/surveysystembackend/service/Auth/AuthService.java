package com.example.surveysystembackend.service.Auth;

import com.example.surveysystembackend.DTO.Authentication.LoginRequestDTO;
import com.example.surveysystembackend.DTO.Authentication.SignupRequestDTO;
import com.example.surveysystembackend.DTO.User.ChangePasswordRequestDTO;
import org.springframework.http.ResponseEntity;

public interface AuthService {
    ResponseEntity<?> authenticateUser(LoginRequestDTO loginRequest);
    ResponseEntity<?> registerUser(SignupRequestDTO signUpRequest);
    ResponseEntity<?> changePassword(ChangePasswordRequestDTO changePasswordRequest);
    ResponseEntity<?> getUserDetails();
    public boolean verifyUser(String token);

}
