package dev.pafsmith.ledgerflow.auth.controller;

import dev.pafsmith.ledgerflow.auth.dto.AuthResponse;
import dev.pafsmith.ledgerflow.auth.dto.RegisterRequest;
import dev.pafsmith.ledgerflow.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
    return authService.register(request);
  }
}
