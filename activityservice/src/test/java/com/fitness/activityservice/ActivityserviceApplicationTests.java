package com.fitness.activityservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import com.fitness.activityservice.exception.ActivityNotFoundException;
import com.fitness.activityservice.dto.ActivityResponse;
import com.fitness.activityservice.service.ActivityService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest(properties = {
		"eureka.client.enabled=false",
		"spring.cloud.discovery.enabled=false",
		"OIDC_ISSUER_URI=https://issuer.example.test/",
		"OIDC_JWKS_URI=https://issuer.example.test/jwks",
		"OIDC_TOKEN_URI=https://issuer.example.test/token",
		"ACTIVITY_API_AUDIENCE=activity-api",
		"ACTIVITY_SERVICE_CLIENT_ID=activity-service-test",
		"ACTIVITY_SERVICE_CLIENT_SECRET=test-secret",
		"security.jwt.issuer-uri=https://issuer.example.test/",
		"security.jwt.jwks-uri=https://issuer.example.test/jwks",
		"security.jwt.audience=activity-api"
})
@AutoConfigureMockMvc
class ActivityserviceApplicationTests {
	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ActivityService activityService;

	@Test
	void contextLoads() {
	}

	@Test
	void activitiesRequireAuthentication() throws Exception {
		mockMvc.perform(get("/api/activities").header("X-User-ID", "user-1"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void activityListIgnoresSpoofedOwnerHeader() throws Exception {
		given(activityService.getUserActivities("user-1")).willReturn(List.of());

		mockMvc.perform(get("/api/activities")
					.header("X-User-ID", "user-2")
					.with(jwt().jwt(token -> token.subject("user-1"))))
				.andExpect(status().isOk());

		verify(activityService).getUserActivities("user-1");
	}

	@Test
	void activityCreateUsesTokenSubjectInsteadOfRequestOwner() throws Exception {
		ActivityResponse response = new ActivityResponse();
		response.setUserId("user-1");
		given(activityService.trackActivity(any(), eq("user-1"))).willReturn(response);

		mockMvc.perform(post("/api/activities")
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
						{"userId":"user-2","type":"RUNNING","duration":30,"caloriesBurned":200,"startTime":"2026-10-31T10:00:00"}
						""")
					.with(jwt().jwt(token -> token.subject("user-1"))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.userId").value("user-1"));

		verify(activityService).trackActivity(any(), eq("user-1"));
	}

	@Test
	void activityCreateRejectsMissingRequiredFieldsBeforeServiceCall() throws Exception {
		mockMvc.perform(post("/api/activities")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{}")
					.with(jwt().jwt(token -> token.subject("user-1"))))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

		verifyNoInteractions(activityService);
	}

	@Test
	void activityCreateRejectsInvalidEnumBeforeServiceCall() throws Exception {
		mockMvc.perform(post("/api/activities")
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
						{"type":"FLYING","duration":30,"caloriesBurned":0,"startTime":"2026-10-31T10:00:00"}
						""")
					.with(jwt().jwt(token -> token.subject("user-1"))))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

		verifyNoInteractions(activityService);
	}

	@Test
	void activityCreateRejectsDurationOutsideDocumentedRangeBeforeServiceCall() throws Exception {
		String validRequestPrefix = "{\"type\":\"RUNNING\",\"caloriesBurned\":0,\"startTime\":\"2026-10-31T10:00:00\",\"duration\":";

		for (int duration : List.of(-1, 0, 601)) {
			mockMvc.perform(post("/api/activities")
						.contentType(MediaType.APPLICATION_JSON)
						.content(validRequestPrefix + duration + "}")
						.with(jwt().jwt(token -> token.subject("user-1"))))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
		}

		verifyNoInteractions(activityService);
	}

	@Test
	void activityCreateRejectsOversizedOrUnknownMetricsBeforeServiceCall() throws Exception {
		String oversizedNotes = "n".repeat(501);
		String oversizedMetricsRequest = """
				{"type":"RUNNING","duration":30,"caloriesBurned":0,"startTime":"2026-10-31T10:00:00","additionalMetrics":{"notes":"%s"}}
				""".formatted(oversizedNotes);

		for (String requestBody : List.of(
				oversizedMetricsRequest,
				"""
				{"type":"RUNNING","duration":30,"caloriesBurned":0,"startTime":"2026-10-31T10:00:00","additionalMetrics":{"unsupported":true}}
				""")) {
			mockMvc.perform(post("/api/activities")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody)
						.with(jwt().jwt(token -> token.subject("user-1"))))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
		}

		verifyNoInteractions(activityService);
	}

	@Test
	void authenticatedUserCanListOwnActivities() throws Exception {
		given(activityService.getUserActivities("user-1")).willReturn(List.of());

		mockMvc.perform(get("/api/activities")
					.header("X-User-ID", "user-1")
					.with(jwt().jwt(token -> token.subject("user-1"))))
				.andExpect(status().isOk());
	}

	@Test
	void activityDetailCannotConfirmAnotherUsersRecord() throws Exception {
		given(activityService.getActivityById("activity-1", "user-1"))
				.willThrow(new ActivityNotFoundException("Activity not found"));

		mockMvc.perform(get("/api/activities/activity-1")
					.with(jwt().jwt(token -> token.subject("user-1"))))
				.andExpect(status().isNotFound());

		verify(activityService).getActivityById("activity-1", "user-1");
	}
}
