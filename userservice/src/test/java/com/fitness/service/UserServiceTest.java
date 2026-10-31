package com.fitness.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.fitness.dto.RegisterRequest;
import com.fitness.dto.UserResponse;
import com.fitness.exception.EmailAlreadyExistsException;
import com.fitness.exception.UserNotFoundException;
import com.fitness.model.User;
import com.fitness.model.UserRole;
import com.fitness.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void register_HashesPasswordAndDoesNotReturnIt() throws JsonProcessingException {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFirstName("John");
        request.setLastName("Doe");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        User savedUser = new User();
        savedUser.setId("user-123");
        savedUser.setEmail("test@example.com");
        savedUser.setPassword("encodedPassword");
        savedUser.setFirstName("John");
        savedUser.setLastName("Doe");
        savedUser.setRole(UserRole.USER);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = userService.register(request);

        assertNotNull(response);
        assertEquals("user-123", response.getId());
        assertEquals("test@example.com", response.getEmail());
        assertEquals(UserRole.USER, response.getRole());
        ArgumentCaptor<User> savedUserCaptor = ArgumentCaptor.forClass(User.class);
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(savedUserCaptor.capture());
        assertEquals("encodedPassword", savedUserCaptor.getValue().getPassword());
        assertFalse(new ObjectMapper().findAndRegisterModules()
                .writeValueAsString(response).contains("\"password\""));
    }

    @Test
    void register_EmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> userService.register(request));
    }

    @Test
    void getUserProfile_DoesNotReturnStoredPassword() throws JsonProcessingException {
        User user = new User();
        user.setId("user-123");
        user.setEmail("test@example.com");
        user.setPassword("$2a$12$storedHash");
        user.setRole(UserRole.USER);

        when(userRepository.findById("user-123")).thenReturn(Optional.of(user));

        UserResponse response = userService.getUserProfile("user-123");

        assertNotNull(response);
        assertEquals("user-123", response.getId());
        assertEquals(UserRole.USER, response.getRole());
        assertFalse(new ObjectMapper().findAndRegisterModules()
                .writeValueAsString(response).contains("\"password\""));
    }

    @Test
    void getUserProfile_NotFound() {
        when(userRepository.findById("non-existent")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserProfile("non-existent"));
    }

    @Test
    void existByUserId_TrueAndFalse() {
        when(userRepository.existsById("user-1")).thenReturn(true);
        when(userRepository.existsById("user-2")).thenReturn(false);

        assertTrue(userService.existByUserId("user-1"));
        assertFalse(userService.existByUserId("user-2"));
    }
}
