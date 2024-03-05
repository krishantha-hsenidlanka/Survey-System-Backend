package com.example.surveysystembackend.repository;
import com.example.surveysystembackend.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    @Query(value = "{ 'enabled' : true }", count = true)
    long countByEnabledTrue();
    List<User> findByEnabledTrue();
    Optional<User> findByVerificationToken(String verificationToken);
}