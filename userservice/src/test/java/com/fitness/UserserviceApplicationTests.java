package com.fitness;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fitness.service.UserService;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
		"eureka.client.enabled=false",
		"spring.cloud.discovery.enabled=false",
		"OIDC_ISSUER_URI=https://issuer.example.test/",
		"OIDC_JWKS_URI=https://issuer.example.test/jwks",
		"USER_API_AUDIENCE=user-api",
		"security.jwt.issuer-uri=https://issuer.example.test/",
		"security.jwt.jwks-uri=https://issuer.example.test/jwks",
		"security.jwt.audience=user-api"
})
@AutoConfigureMockMvc
class UserserviceApplicationTests {
	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserService userService;

	@Test
	void contextLoads() {
	}

	@Test
	void profileRequiresAuthentication() throws Exception {
		mockMvc.perform(get("/api/users/user-1"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void profileCanOnlyBeReadByTokenSubject() throws Exception {
		mockMvc.perform(get("/api/users/user-2")
					.with(jwt().jwt(token -> token.subject("user-1"))))
				.andExpect(status().isForbidden());
	}

	@Test
	void authenticatedUserCanReadOwnProfile() throws Exception {
		mockMvc.perform(get("/api/users/user-1")
					.with(jwt().jwt(token -> token.subject("user-1"))))
				.andExpect(status().isOk());
	}

	@Test
	void userValidationRequiresInternalActivityScope() throws Exception {
		mockMvc.perform(get("/api/users/user-1/validate")
					.with(jwt().jwt(token -> token.subject("service-client"))))
				.andExpect(status().isForbidden());
	}
}
