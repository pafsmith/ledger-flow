package dev.pafsmith.ledgerflow.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import dev.pafsmith.ledgerflow.auth.dto.AuthResponse;
import dev.pafsmith.ledgerflow.auth.dto.CurrentUserResponse;
import dev.pafsmith.ledgerflow.auth.dto.RegisterRequest;
import dev.pafsmith.ledgerflow.auth.service.AuthService;
import dev.pafsmith.ledgerflow.common.BaseControllerTest;
import dev.pafsmith.ledgerflow.common.exception.GlobalExceptionHandler;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest extends BaseControllerTest {

  @Autowired
  private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper()
      .registerModule(new JavaTimeModule());

  @MockitoBean
  private AuthService authService;

  @Test
  @DisplayName("POST /api/auth/register returns 201 when request is valid")
  void register_shouldReturnCreated() throws Exception {
    RegisterRequest request = new RegisterRequest();
    request.setFirstName("Paul");
    request.setLastName("Smith");
    request.setEmail("paul@test.com");
    request.setPassword("password123");

    AuthResponse response = new AuthResponse();
    response.setUserId(UUID.randomUUID());
    response.setFirstName("Paul");
    response.setLastName("Smith");
    response.setEmail("paul@test.com");
    response.setMessage("User registered successfully");

    when(authService.register(any(RegisterRequest.class))).thenReturn(response);

    mockMvc.perform(post("/api/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.email").value("paul@test.com"))
        .andExpect(jsonPath("$.message").value("User registered successfully"));
  }

  @Test
  @DisplayName("POST /api/auth/register returns 400 when request is invalid")
  void register_shouldReturnBadRequest_whenValidationFails() throws Exception {
    String invalidJson = """
        {
          "firstName": "",
          "lastName": "",
          "email": "not-an-email",
          "password": "123"
        }
        """;

    mockMvc.perform(post("/api/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(invalidJson))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Validation failed"))
        .andExpect(jsonPath("$.validationErrors.firstName").value("First name is required"))
        .andExpect(jsonPath("$.validationErrors.lastName").value("Last name is required"))
        .andExpect(jsonPath("$.validationErrors.email").value("Email must be valid"));
  }

  @Test
  @DisplayName("GET /api/auth/me returns current user when authenticated")
  void getCurrentUser_shouldReturnCurrentUser() throws Exception {
    CurrentUserResponse response = new CurrentUserResponse();
    response.setUserId(UUID.randomUUID());
    response.setFirstName("Paul");
    response.setLastName("Smith");
    response.setEmail("paul@test.com");

    when(authService.getCurrentUser("paul@test.com")).thenReturn(response);

    mockMvc.perform(get("/api/auth/me")
        .principal(() -> "paul@test.com"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("paul@test.com"))
        .andExpect(jsonPath("$.firstName").value("Paul"))
        .andExpect(jsonPath("$.lastName").value("Smith"));
  }
}
