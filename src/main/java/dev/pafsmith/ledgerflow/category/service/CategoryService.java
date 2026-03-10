package dev.pafsmith.ledgerflow.category.service;

import dev.pafsmith.ledgerflow.category.entity.Category;
import dev.pafsmith.ledgerflow.category.enums.CategoryType;
import dev.pafsmith.ledgerflow.category.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CategoryService {
  private final CategoryRepository categoryRepository;

  public CategoryService(CategoryRepository categoryRepository) {
    this.categoryRepository = categoryRepository;
  }

  public List<Category> getCategoriesForUser(UUID userId) {
    return categoryRepository.findByUserId(userId);
  }

  public List<Category> getCategoriesForUserByType(UUID userId, CategoryType type) {
    return categoryRepository.findByUserIdAndType(userId, type);
  }
}
