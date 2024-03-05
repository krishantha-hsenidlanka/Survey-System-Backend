package com.example.surveysystembackend.DTO.User;

import com.example.surveysystembackend.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private String id;
    private String username;
    private String email;
    private Set<String> roles;
    private Boolean enabled;
    private Date registrationDate;
    private String verificationToken;
    private Date verificationTokenExpirationDate;
}
