package dev.pafsmith.ledgerflow.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.pafsmith.ledgerflow.auth.dto.AuthResponse;
import dev.pafsmith.ledgerflow.auth.dto.LoginRequest;
import dev.pafsmith.ledgerflow.auth.service.AuthService;
import dev.pafsmith.ledgerflow.common.BaseControllerTest;
import dev.pafsmith.ledgerflow.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthLoginControllerTest extends BaseControllerTest {

  @Autowired
  private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper()
      .registerModule(new JavaTimeModule());

  @MockitoBean
  private AuthService authService;

  @Test
  @DisplayName("POST /api/auth/login returns 200 when credentials are valid")
  void login_shouldReturnOk() throws Exception {
    LoginRequest request = new LoginRequest();
    request.setEmail("paul@test.com");
    request.setPassword("password123");

    AuthResponse response = new AuthResponse();
    response.setUserId(UUID.randomUUID());
    response.setFirstName("Paul");
    response.setLastName("Smith");
    response.setEmail("paul@test.com");
    response.setToken("test-jwt-token");
    response.setMessage("Login successful");

    when(authService.login(any(LoginRequest.class))).thenReturn(response);

    mockMvc.perform(post("/api/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("paul@test.com"))
        .andExpect(jsonPath("$.token").value("test-jwt-token"))
        .andExpect(jsonPath("$.message").value("Login successful"));
  }

  @Test
  @DisplayName("POST /api/auth/login returns 400 when request is invalid")
  void login_shouldReturnBadRequest_whenValidationFails() throws Exception {
    String invalidJson = """
        {
          "email": "not-an-email",
          "password": ""
        }
        """;

    mockMvc.perform(post("/api/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(invalidJson))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Validation failed"))
        .andExpect(jsonPath("$.validationErrors.email").value("Email must be valid"))
        .andExpect(jsonPath("$.validationErrors.password").value("Password is required"));
  }
}
