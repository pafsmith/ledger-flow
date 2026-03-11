package dev.pafsmith.ledgerflow.common.exception;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> handleResourceNotFound(
      ResourceNotFoundException ex,
      HttpServletRequest request) {
    ApiErrorResponse error = new ApiErrorResponse(
        Instant.now(),
        HttpStatus.NOT_FOUND.value(),
        HttpStatus.NOT_FOUND.getReasonPhrase(),
        ex.getMessage(),
        request.getRequestURI());

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ApiErrorResponse> handleBadRequest(
      BadRequestException ex,
      HttpServletRequest request) {
    ApiErrorResponse error = new ApiErrorResponse(
        Instant.now(),
        HttpStatus.BAD_REQUEST.value(),
        HttpStatus.BAD_REQUEST.getReasonPhrase(),
        ex.getMessage(),
        request.getRequestURI());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<ApiErrorResponse> handleForbidden(
      ForbiddenException ex,
      HttpServletRequest request) {
    ApiErrorResponse error = new ApiErrorResponse(
        Instant.now(),
        HttpStatus.FORBIDDEN.value(),
        HttpStatus.FORBIDDEN.getReasonPhrase(),
        ex.getMessage(),
        request.getRequestURI());

    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidationException(
      MethodArgumentNotValidException ex,
      HttpServletRequest request) {
    Map<String, String> validationErrors = new LinkedHashMap<>();

    ex.getBindingResult().getFieldErrors()
        .forEach(error -> validationErrors.put(error.getField(), error.getDefaultMessage()));

    Map<String, Object> response = new LinkedHashMap<>();
    response.put("timestamp", Instant.now());
    response.put("status", HttpStatus.BAD_REQUEST.value());
    response.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
    response.put("message", "Validation failed");
    response.put("path", request.getRequestURI());
    response.put("validationErrors", validationErrors);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiErrorResponse> handleGenericException(
      Exception ex,
      HttpServletRequest request) {
    ApiErrorResponse error = new ApiErrorResponse(
        Instant.now(),
        HttpStatus.INTERNAL_SERVER_ERROR.value(),
        HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
        "An unexpected error occurred",
        request.getRequestURI());

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
  }
}
