package com.fitness.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fitness.dto.RegisterRequest;
import com.fitness.dto.UserResponse;
import com.fitness.exception.EmailAlreadyExistsException;
import com.fitness.exception.UserNotFoundException;
import com.fitness.model.User;
import com.fitness.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse register(RegisterRequest request){
        if(repository.existsByEmail(request.getEmail())){
            throw new EmailAlreadyExistsException("Email already exists: " + request.getEmail());
        }
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        
        User savedUser = repository.save(user);
        return mapToResponse(savedUser);
    }

    public UserResponse getUserProfile(String userId){
        User user = repository.findById(userId)
                 .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        return mapToResponse(user);
    }

    public boolean existByUserId(String userId){
        log.info("Calling User Validation API for userId: {}", userId);
        return repository.existsById(userId);
    }

    private UserResponse mapToResponse(User user){
        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setEmail(user.getEmail());
        userResponse.setFirstName(user.getFirstName());
        userResponse.setLastName(user.getLastName());
        userResponse.setRole(user.getRole());
        userResponse.setCreatedAt(user.getCreatedAt());
        userResponse.setUpdatedAt(user.getUpdatedAt());
        return userResponse;
    }
}
