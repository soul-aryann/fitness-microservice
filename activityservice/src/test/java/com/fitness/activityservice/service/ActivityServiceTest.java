package com.fitness.activityservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fitness.activityservice.dto.ActivityRequest;
import com.fitness.activityservice.dto.ActivityResponse;
import com.fitness.activityservice.exception.ActivityNotFoundException;
import com.fitness.activityservice.exception.UserValidationException;
import com.fitness.activityservice.model.Activity;
import com.fitness.activityservice.model.ActivityType;
import com.fitness.activityservice.repository.ActivityRepository;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private UserValidationService userValidationService;

    @InjectMocks
    private ActivityService activityService;

    @Test
    void trackActivity_Success() {
        ActivityRequest request = new ActivityRequest();
        request.setUserId("user-1");
        request.setType(ActivityType.RUNNING);
        request.setDuration(30);
        request.setCaloriesBurned(300);
        request.setStartTime(LocalDateTime.now());

        when(userValidationService.validateUser("user-1")).thenReturn(true);

        Activity savedActivity = Activity.builder()
                .id("act-1")
                .userId("user-1")
                .type(ActivityType.RUNNING)
                .duration(30)
                .caloriesBurned(300)
                .startTime(request.getStartTime())
                .build();

        when(activityRepository.save(any(Activity.class))).thenReturn(savedActivity);

        ActivityResponse response = activityService.trackActivity(request);

        assertNotNull(response);
        assertEquals("act-1", response.getId());
        assertEquals("user-1", response.getUserId());
        assertEquals(ActivityType.RUNNING, response.getType());
        verify(activityRepository).save(any(Activity.class));
    }

    @Test
    void trackActivity_InvalidUser() {
        ActivityRequest request = new ActivityRequest();
        request.setUserId("invalid-user");

        when(userValidationService.validateUser("invalid-user")).thenReturn(false);

        assertThrows(UserValidationException.class, () -> activityService.trackActivity(request));
    }

    @Test
    void getUserActivities_ReturnsList() {
        Activity activity = Activity.builder()
                .id("act-1")
                .userId("user-1")
                .type(ActivityType.WALKING)
                .build();

        when(activityRepository.findByUserId("user-1")).thenReturn(List.of(activity));

        List<ActivityResponse> list = activityService.getUserActivities("user-1");

        assertEquals(1, list.size());
        assertEquals("act-1", list.get(0).getId());
    }

    @Test
    void getActivityById_Success() {
        Activity activity = Activity.builder()
                .id("act-1")
                .userId("user-1")
                .type(ActivityType.CYCLING)
                .build();

        when(activityRepository.findById("act-1")).thenReturn(Optional.of(activity));

        ActivityResponse response = activityService.getActivityById("act-1");

        assertNotNull(response);
        assertEquals("act-1", response.getId());
    }

    @Test
    void getActivityById_NotFound() {
        when(activityRepository.findById("non-existent")).thenReturn(Optional.empty());

        assertThrows(ActivityNotFoundException.class, () -> activityService.getActivityById("non-existent"));
    }
}
