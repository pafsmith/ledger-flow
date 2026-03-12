package dev.pafsmith.ledgerflow.category.dto;

import java.util.UUID;

import dev.pafsmith.ledgerflow.category.enums.CategoryType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request body for creating a category")
public class CreateCategoryRequest {

  @NotNull(message = "User id is required")
  private UUID userId;

  @NotBlank(message = "Category name is required")
  @Size(max = 100, message = "Category name must be 100 characters or fewer")
  private String name;

  @NotNull(message = "Category type is required")
  private CategoryType type;

  public CreateCategoryRequest() {

  }

  public UUID getUserId() {
    return userId;
  }

  public void setUserId(UUID userId) {
    this.userId = userId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public CategoryType getType() {
    return type;
  }

  public void setType(CategoryType type) {
    this.type = type;
  }

}
