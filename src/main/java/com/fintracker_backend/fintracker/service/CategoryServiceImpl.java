package com.fintracker_backend.fintracker.service;

import java.util.List;
import org.springframework.stereotype.Service;

import com.fintracker_backend.fintracker.dto.CategoryRequestDTO;
import com.fintracker_backend.fintracker.entity.Category;
import com.fintracker_backend.fintracker.entity.CategoryType;
import com.fintracker_backend.fintracker.entity.User;
import com.fintracker_backend.fintracker.exception.AccessDeniedException;
import com.fintracker_backend.fintracker.exception.BadRequestException;
import com.fintracker_backend.fintracker.exception.ResourceNotFoundException;
import com.fintracker_backend.fintracker.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserService userService;

    @Override
public Category createCategory(CategoryRequestDTO request, Long userId) {

    if (request.getName() == null || request.getName().trim().isEmpty()) {
        throw new BadRequestException("Category name is required");
    }

    User user = userService.getUserById(userId);

    // Duplicate check
    categoryRepository.findByNameAndUserId(request.getName(), userId)
            .ifPresent(c -> {
                throw new BadRequestException("Category already exists with name: " + request.getName());
            });

    Category parent = null;

    if (request.getParentId() != null) {
        parent = categoryRepository.findById(request.getParentId())
                .filter(cat -> cat.getUser().getId().equals(userId)) // 🔥 SECURITY FIX
                .orElseThrow(() -> new ResourceNotFoundException("Parent category not found or unauthorized"));
    }

    Category category = Category.builder()
            .name(request.getName())
            .type(request.getType())
            .user(user)
            .parent(parent)
            .build();

    return categoryRepository.save(category);
}

    @Override
    public List<Category> getAllCategories(Long userId) {
        return categoryRepository.findByUserId(userId);
    }

    @Override
    public List<Category> getCategoriesByType(Long userId, CategoryType type) {
        return categoryRepository.findByUserIdAndType(userId, type);
    }

    @Override
    public List<Category> getSubCategories(Long parentId) {
        return categoryRepository.findByParentId(parentId);
    }

    @Override
public Category getCategoryById(Long id) {
    return categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
}

    @Override
public void deleteCategory(Long id, Long userId) {

    Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

    // Security check
    if (!category.getUser().getId().equals(userId)) {
        throw new AccessDeniedException("Unauthorized to delete this category");
    }

    // Check if it has subcategories
    List<Category> children = categoryRepository.findByParentId(id);
    if (!children.isEmpty()) {
        throw new BadRequestException("Cannot delete category with subcategories");
    }

    categoryRepository.delete(category);
}
}
