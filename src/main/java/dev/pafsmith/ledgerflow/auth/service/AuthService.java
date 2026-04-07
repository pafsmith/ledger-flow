package dev.pafsmith.ledgerflow.auth.service;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import dev.pafsmith.ledgerflow.auth.dto.AuthResponse;
import dev.pafsmith.ledgerflow.auth.dto.CurrentUserResponse;
import dev.pafsmith.ledgerflow.auth.dto.LoginRequest;
import dev.pafsmith.ledgerflow.auth.dto.RegisterRequest;
import dev.pafsmith.ledgerflow.common.exception.BadRequestException;
import dev.pafsmith.ledgerflow.common.exception.ResourceNotFoundException;
import dev.pafsmith.ledgerflow.user.entity.User;
import dev.pafsmith.ledgerflow.user.repository.UserRepository;

@Service
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public AuthService(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      JwtService jwtService) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
  }

  public AuthResponse register(RegisterRequest request) {

    String email = request.getEmail().trim().toLowerCase();

    if (userRepository.existsByEmail(email)) {
      throw new BadRequestException("Email is already registered");
    }

    User user = new User();
    user.setFirstName(request.getFirstName().trim());
    user.setLastName(request.getLastName().trim());
    user.setEmail(email);
    user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

    User savedUser = userRepository.save(user);

    AuthResponse response = new AuthResponse();

    response.setUserId(savedUser.getId());
    response.setFirstName(savedUser.getFirstName());
    response.setLastName(savedUser.getLastName());
    response.setEmail(savedUser.getEmail());
    response.setMessage("User registered successfully");

    return response;

  }

  public AuthResponse login(LoginRequest request) {
    String email = request.getEmail().trim().toLowerCase();

    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
      throw new BadRequestException("Invalid email or password");
    }

    String token = jwtService.generateToken(user);

    AuthResponse response = new AuthResponse();
    response.setUserId(user.getId());
    response.setFirstName(user.getFirstName());
    response.setLastName(user.getLastName());
    response.setEmail(user.getEmail());
    response.setToken(token);
    response.setMessage("Login successful");

    return response;
  }

  public CurrentUserResponse getCurrentUser(String userId) {
    User user = userRepository.findById(UUID.fromString(userId))
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    CurrentUserResponse response = new CurrentUserResponse();
    response.setUserId(user.getId());
    response.setFirstName(user.getFirstName());
    response.setLastName(user.getLastName());
    response.setEmail(user.getEmail());

    return response;
  }
}
