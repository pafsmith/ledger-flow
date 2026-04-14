package dev.pafsmith.ledgerflow.category.service;

import dev.pafsmith.ledgerflow.category.dto.CategoryResponse;
import dev.pafsmith.ledgerflow.category.dto.CreateCategoryRequest;
import dev.pafsmith.ledgerflow.category.entity.Category;
import dev.pafsmith.ledgerflow.category.enums.CategoryType;
import dev.pafsmith.ledgerflow.category.repository.CategoryRepository;
import dev.pafsmith.ledgerflow.common.exception.BadRequestException;
import dev.pafsmith.ledgerflow.common.exception.ForbiddenException;
import dev.pafsmith.ledgerflow.common.exception.ResourceNotFoundException;
import dev.pafsmith.ledgerflow.user.entity.User;
import dev.pafsmith.ledgerflow.user.repository.UserRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CategoryService {
  private final CategoryRepository categoryRepository;
  private final UserRepository userRepository;

  public CategoryService(CategoryRepository categoryRepository, UserRepository userRepository) {
    this.categoryRepository = categoryRepository;
    this.userRepository = userRepository;
  }

  public CategoryResponse createCategory(UUID userId, CreateCategoryRequest request) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    if (categoryRepository.existsByUserIdAndNameIgnoreCase(user.getId(), request.getName())) {
      throw new BadRequestException("Category with that name already exists for this user");
    }

    Category category = new Category();
    category.setUser(user);
    category.setName(request.getName().trim());
    category.setType(request.getType());
    category.setSystemDefined(false);

    Category savedCategory = categoryRepository.save(category);

    return mapToResponse(savedCategory);
  }

  public CategoryResponse getCategoryById(UUID userId, UUID categoryId) {
    Category category = categoryRepository.findById(categoryId)
        .orElseThrow(() -> new ResourceNotFoundException("Category Not found"));

    if (category.getUser().getId().equals(userId)) {
      return mapToResponse(category);
    } else {
      throw new ForbiddenException("Category does not belong to user");
    }
  }

  public List<CategoryResponse> getCategoriesForUser(UUID userId) {
    return categoryRepository.findByUserId(userId)
        .stream()
        .map(this::mapToResponse)
        .toList();
  }

  public List<CategoryResponse> getCategoriesForUserByType(UUID userId, CategoryType type) {
    return categoryRepository.findByUserIdAndType(userId, type)
        .stream()
        .map(this::mapToResponse)
        .toList();
  }

  private CategoryResponse mapToResponse(Category category) {
    CategoryResponse response = new CategoryResponse();
    response.setId(category.getId());
    response.setUserId(category.getUser().getId());
    response.setName(category.getName());
    response.setType(category.getType());
    response.setSystemDefined(category.isSystemDefined());
    response.setCreatedAt(category.getCreatedAt());
    response.setUpdatedAt(category.getUpdatedAt());
    return response;
  }

  public void deleteCategory(String userId, UUID categoryId) {

    Category category = categoryRepository.findById(categoryId)
        .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

    User user = userRepository.findById(UUID.fromString(userId))
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    if (user.getId().equals(category.getUser().getId())) {
      categoryRepository.deleteById(categoryId);
    } else {
      throw new ForbiddenException("You do not have permission to delete this category");
    }

  }
}
