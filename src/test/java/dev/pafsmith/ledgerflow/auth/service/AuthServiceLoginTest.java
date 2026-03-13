package dev.pafsmith.ledgerflow.auth.service;

import dev.pafsmith.ledgerflow.auth.dto.LoginRequest;
import dev.pafsmith.ledgerflow.common.exception.BadRequestException;
import dev.pafsmith.ledgerflow.common.exception.ResourceNotFoundException;
import dev.pafsmith.ledgerflow.user.entity.User;
import dev.pafsmith.ledgerflow.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceLoginTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private JwtService jwtService;

  @InjectMocks
  private AuthService authService;

  private LoginRequest request;
  private User user;

  @BeforeEach
  void setUp() {
    request = new LoginRequest();
    request.setEmail("paul@test.com");
    request.setPassword("password123");

    user = new User();
    user.setId(UUID.randomUUID());
    user.setFirstName("Paul");
    user.setLastName("Smith");
    user.setEmail("paul@test.com");
    user.setPasswordHash("hashed-password");
  }

  @Test
  void login_shouldReturnTokenWhenCredentialsAreValid() {
    when(userRepository.findByEmail("paul@test.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("password123", "hashed-password")).thenReturn(true);
    when(jwtService.generateToken(user)).thenReturn("test-jwt-token");

    var response = authService.login(request);

    assertThat(response.getEmail()).isEqualTo("paul@test.com");
    assertThat(response.getToken()).isEqualTo("test-jwt-token");
    assertThat(response.getMessage()).isEqualTo("Login successful");
  }

  @Test
  void login_shouldThrowWhenUserDoesNotExist() {
    when(userRepository.findByEmail("paul@test.com")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> authService.login(request))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("User not found");
  }

  @Test
  void login_shouldThrowWhenPasswordIsInvalid() {
    when(userRepository.findByEmail("paul@test.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("password123", "hashed-password")).thenReturn(false);

    assertThatThrownBy(() -> authService.login(request))
        .isInstanceOf(BadRequestException.class)
        .hasMessage("Invalid email or password");

    verify(jwtService, never()).generateToken(any(User.class));
  }
}
