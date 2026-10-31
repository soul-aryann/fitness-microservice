package com.fitness.activityservice.dto;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fitness.activityservice.model.ActivityType;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ActivityRequest {
    @NotNull(message = "Activity type is required")
    private ActivityType type;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Max(value = 600, message = "Duration must not exceed 600 minutes")
    private Integer duration;

    @PositiveOrZero(message = "Calories burned must be zero or greater")
    private Integer caloriesBurned;

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    @Size(max = 2, message = "At most two additional metrics are allowed")
    private Map<String,Object> additionalMetrics;

    @AssertTrue(message = "Additional metrics contain unsupported keys or values")
    @JsonIgnore
    public boolean isAdditionalMetricsValid() {
        if (additionalMetrics == null) {
            return true;
        }

        for (Map.Entry<String, Object> metric : additionalMetrics.entrySet()) {
            if ("distance_meters".equals(metric.getKey())) {
                if (!(metric.getValue() instanceof Number distance)) {
                    return false;
                }
                double meters = distance.doubleValue();
                if (!Double.isFinite(meters) || meters < 0 || meters > 1_000_000) {
                    return false;
                }
            } else if ("notes".equals(metric.getKey())) {
                if (!(metric.getValue() instanceof String notes) || notes.length() > 500) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }
}
