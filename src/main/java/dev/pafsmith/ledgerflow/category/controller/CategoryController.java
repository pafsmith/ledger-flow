package dev.pafsmith.ledgerflow.category.controller;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import dev.pafsmith.ledgerflow.category.dto.CategoryResponse;
import dev.pafsmith.ledgerflow.category.dto.CreateCategoryRequest;
import dev.pafsmith.ledgerflow.category.enums.CategoryType;
import dev.pafsmith.ledgerflow.category.service.CategoryService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Categories", description = "Operations for managing categories")
public class CategoryController {

  private final CategoryService categoryService;

  public CategoryController(CategoryService categoryService) {
    this.categoryService = categoryService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create a category", description = "Creates a transaction category", responses = {
      @ApiResponse(responseCode = "201", description = "Category created"),
      @ApiResponse(responseCode = "400", description = "Validation failed"),
      @ApiResponse(responseCode = "404", description = "Related resource not found")
  })
  public CategoryResponse createCategory(@Valid @RequestBody CreateCategoryRequest request) {
    return categoryService.createCategory(request);
  }

  @GetMapping("/{categoryId}")
  @Operation(summary = "Get a category by id")
  public CategoryResponse getCategoryById(@PathVariable UUID categoryId) {
    return categoryService.getCategoryById(categoryId);
  }

  @GetMapping("/user/{userId}")
  @Operation(summary = "Get a list of categories by userId")
  public List<CategoryResponse> getCategoriesForUser(@PathVariable UUID userId) {
    return categoryService.getCategoriesForUser(userId);
  }

  @GetMapping("/user/{userId}/type/{type}")
  @Operation(summary = "Get a list of categories by userId and type")
  public List<CategoryResponse> getCategoriesForUserByType(
      @PathVariable UUID userId,
      @PathVariable CategoryType type) {
    return categoryService.getCategoriesForUserByType(userId, type);
  }

  @DeleteMapping("/{categoryId}")
  public ResponseEntity<Void> deleteCategory(@PathVariable UUID categoryId,
      @AuthenticationPrincipal UserDetails userDetails) {
    categoryService.deleteCategory(userDetails.getUsername(), categoryId);
    return ResponseEntity.noContent().build();
  }
}
