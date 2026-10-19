package com.fitness.activityservice.service;

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
                .uri("/api/users/{userId}/validate", userId)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block());
        } catch (WebClientResponseException.NotFound e) {
            log.warn("User validation returned 404 for userId: {}", userId);
            throw new UserValidationException("User not found: " + userId);
        } catch (WebClientResponseException.BadRequest e) {
            log.warn("Invalid user validation request for userId: {}", userId);
            throw new UserValidationException("Invalid user validation request: " + userId);
        } catch (WebClientResponseException e) {
            log.error("UserService returned HTTP error {} for userId: {}", e.getStatusCode(), userId);
            throw new UserValidationException("Unable to validate user due to user service error: " + e.getStatusCode());
        } catch (Exception e) {
            log.error("Failed to connect to UserService or validate user: {}", userId, e);
            throw new UserValidationException("Unable to validate user at this time. Please try again later.");
        }
    }
    
}
