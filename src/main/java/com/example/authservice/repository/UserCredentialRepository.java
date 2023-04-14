package com.example.authservice.repository;

import com.example.authservice.entity.UserCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCredentialRepository extends JpaRepository<UserCredential, Long> {
    Optional<UserCredential> findByName(String username);

    boolean existsByEmail(String email);

    Optional<UserCredential> findByEmail(String email);
}