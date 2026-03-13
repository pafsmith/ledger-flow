package dev.pafsmith.ledgerflow.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request body for registering a new user")
public class RegisterRequest {
  @NotBlank(message = "First name is required")
  @Size(max = 100, message = "First name must be 100 characters or fewer")
  private String firstName;

  @NotBlank(message = "Last name is required")
  @Size(max = 100, message = "Last name must be 100 characters or fewer")
  private String lastName;

  @NotBlank(message = "Email is required")
  @Email(message = "Email must be valid")
  @Size(max = 255, message = "Email must be 255 characters or fewer")
  private String email;

  @NotBlank(message = "Password is required")
  @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters")
  private String password;

  public RegisterRequest() {

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

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

}
