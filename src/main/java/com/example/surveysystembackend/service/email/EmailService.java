package com.example.surveysystembackend.service.email;

import com.example.surveysystembackend.model.User;

public interface EmailService {
    void sendVerificationEmail(User user);
}

