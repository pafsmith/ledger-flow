package dev.pafsmith.ledgerflow.auth.service;

import dev.pafsmith.ledgerflow.auth.dto.RegisterRequest;
import dev.pafsmith.ledgerflow.common.exception.BadRequestException;
import dev.pafsmith.ledgerflow.user.entity.User;
import dev.pafsmith.ledgerflow.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private AuthService authService;

  private RegisterRequest request;

  @BeforeEach
  void setUp() {
    request = new RegisterRequest();
    request.setFirstName("Paul");
    request.setLastName("Smith");
    request.setEmail("paul@test.com");
    request.setPassword("password123");
  }

  @Test
  void register_shouldCreateUserSuccessfully() {
    when(userRepository.existsByEmail("paul@test.com")).thenReturn(false);
    when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
      User user = invocation.getArgument(0);
      user.setId(UUID.randomUUID());
      return user;
    });

    var response = authService.register(request);

    assertThat(response.getEmail()).isEqualTo("paul@test.com");
    assertThat(response.getFirstName()).isEqualTo("Paul");
    assertThat(response.getLastName()).isEqualTo("Smith");
    assertThat(response.getMessage()).isEqualTo("User registered successfully");

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(captor.capture());

    User savedUser = captor.getValue();
    assertThat(savedUser.getPasswordHash()).isEqualTo("hashed-password");
  }

  @Test
  void register_shouldThrowWhenEmailAlreadyExists() {
    when(userRepository.existsByEmail("paul@test.com")).thenReturn(true);

    assertThatThrownBy(() -> authService.register(request))
        .isInstanceOf(BadRequestException.class)
        .hasMessage("Email is already registered");

    verify(userRepository, never()).save(any(User.class));
  }
}
