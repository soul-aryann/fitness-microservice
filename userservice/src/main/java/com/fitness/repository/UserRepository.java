package com.fitness.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fitness.model.User;

public interface UserRepository extends JpaRepository< User, String>{   
    boolean existsByEmail(String email);
}
