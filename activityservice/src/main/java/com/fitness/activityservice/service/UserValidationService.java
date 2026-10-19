package com.fitness.activityservice.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fitness.activityservice.exception.UserValidationException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserValidationService {
    private final WebClient userServiceWebClient;

    public boolean validateUser(String userId){
        log.info("Calling User Validation API for userId: {}", userId);
        try {
            return Boolean.TRUE.equals(userServiceWebClient.get()
                .uri("/api/users/{userId}/validate",userId)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block());
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 404){
                throw new UserValidationException("User not found: " + userId);
            }else if(e.getStatusCode() == HttpStatus.BAD_REQUEST){
                throw new UserValidationException("Invalid user validation request: " + userId);
            }
        }
        return false;
    }
    
}
