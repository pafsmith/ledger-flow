package dev.pafsmith.ledgerflow.auth.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response body for registering a new user")
public class AuthResponse {
  private UUID userId;
  private String firstName;
  private String lastName;
  private String email;
  private String message;
  private String token;

  public AuthResponse() {

  }

  public UUID getUserId() {
    return userId;
  }

  public void setUserId(UUID userId) {
    this.userId = userId;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public String getToken() {
    return token;
  }
}
