package com.fitness.activityservice.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fitness.activityservice.dto.ActivityRequest;
import com.fitness.activityservice.dto.ActivityResponse;
import com.fitness.activityservice.service.ActivityService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;


@RestController
@RequestMapping("/api/activities")
@AllArgsConstructor
public class ActivityController {

        private final ActivityService activityService;
        @PostMapping
        public ResponseEntity<ActivityResponse> trackActivity (@Valid @RequestBody ActivityRequest request, @AuthenticationPrincipal Jwt jwt) {
            if (!jwt.getSubject().equals(request.getUserId())) {
                throw new AccessDeniedException("Access denied");
            }
            return ResponseEntity.ok(activityService.trackActivity(request));
        }

        @GetMapping
        public ResponseEntity<List<ActivityResponse>> getUserActivities(@RequestHeader("X-User-ID") String userId, @AuthenticationPrincipal Jwt jwt){
            if (!jwt.getSubject().equals(userId)) {
                throw new AccessDeniedException("Access denied");
            }
            return ResponseEntity.ok(activityService.getUserActivities(userId));
        }
        @GetMapping("/{activityId}")
        public ResponseEntity<ActivityResponse> getActivity(@PathVariable String activityId, @AuthenticationPrincipal Jwt jwt){
            return ResponseEntity.ok(activityService.getActivityById(activityId, jwt.getSubject()));
        }
        
}
