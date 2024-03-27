package com.example.surveysystembackend.repository;

import com.example.surveysystembackend.model.ERole;
import com.example.surveysystembackend.model.Role;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RoleRepository extends MongoRepository<Role, String> {
    Optional<Role> findByName(ERole name);
}