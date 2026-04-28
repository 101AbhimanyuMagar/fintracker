package com.fintracker_backend.fintracker.service;

import java.util.List;
import org.springframework.stereotype.Service;

import com.fintracker_backend.fintracker.dto.CategoryRequestDTO;
import com.fintracker_backend.fintracker.entity.Category;
import com.fintracker_backend.fintracker.entity.CategoryType;
import com.fintracker_backend.fintracker.entity.User;
import com.fintracker_backend.fintracker.exception.BadRequestException;
import com.fintracker_backend.fintracker.exception.ResourceNotFoundException;
import com.fintracker_backend.fintracker.repository.CategoryRepository;
import com.fintracker_backend.fintracker.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    // ➕ Create Category
    @Override
public Category createCategory(CategoryRequestDTO request, String email) {

    log.info("Creating category | user={} | name={} | type={}",
            email, request.getName(), request.getType());
            System.out.println("🔥 Creating category...");

    if (request.getName() == null || request.getName().trim().isEmpty()) {
        log.warn("Category creation failed - empty name | user={}", email);
        throw new BadRequestException("Category name is required");
    }

    if (request.getType() == null) {
        log.warn("Category creation failed - type missing | user={}", email);
        throw new BadRequestException("Category type is required");
    }

    User user = getUserByEmail(email);

    categoryRepository.findByNameAndUserId(request.getName(), user.getId())
            .ifPresent(c -> {
                log.warn("Duplicate category attempt | user={} | name={}", email, request.getName());
                throw new BadRequestException("Category already exists with name: " + request.getName());
            });

    Category parent = null;

    if (request.getParentId() != null) {
        parent = categoryRepository.findById(request.getParentId())
                .filter(cat -> cat.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> {
                    log.warn("Invalid parent category | user={} | parentId={}",
                            email, request.getParentId());
                    return new ResourceNotFoundException("Parent category not found or unauthorized");
                });
    }

    Category category = Category.builder()
            .name(request.getName().trim())
            .type(request.getType())
            .user(user)
            .parent(parent)
            .build();

    Category saved = categoryRepository.save(category);

    log.info("Category created | id={} | user={}", saved.getId(), email);

    return saved;
}
    // 📄 Get All
@Override
public List<Category> getAllCategories(String email) {

    log.debug("Fetching all categories | user={}", email);

    User user = getUserByEmail(email);

    List<Category> categories = categoryRepository.findByUserId(user.getId());

    log.info("Categories fetched | count={} | user={}", categories.size(), email);

    return categories;
}

    // 📂 Get By Type
 @Override
public List<Category> getCategoriesByType(String email, CategoryType type) {

    if (type == null) {
        log.warn("Category type missing | user={}", email);
        throw new BadRequestException("Category type is required");
    }

    log.debug("Fetching categories by type | user={} | type={}", email, type);

    User user = getUserByEmail(email);

    return categoryRepository.findByUserIdAndType(user.getId(), type);
}


    // 📂 Sub Categories
    @Override
    public List<Category> getSubCategories(Long parentId) {
        log.debug("Fetching subcategories | parentId={}", parentId);
        List<Category> subcategories = categoryRepository.findByParentId(parentId);
        log.info("Subcategories fetched | count={} | parentId={}", subcategories.size(), parentId);
        return subcategories;
    }

    // 📄 Get By ID
    @Override
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }

    // ❌ Delete Category
    @Override
public void deleteCategory(Long id, String email) {

    log.info("Deleting category | id={} | user={}", id, email);

    User user = getUserByEmail(email);

    Category category = categoryRepository.findById(id)
            .filter(cat -> cat.getUser().getId().equals(user.getId()))
            .orElseThrow(() -> {
                log.warn("Unauthorized or not found category | id={} | user={}", id, email);
                return new ResourceNotFoundException("Category not found or unauthorized");
            });

    List<Category> children = categoryRepository.findByParentId(id);

    if (!children.isEmpty()) {
        log.warn("Delete failed - category has children | id={}", id);
        throw new BadRequestException("Cannot delete category with subcategories");
    }

    categoryRepository.delete(category);

    log.info("Category deleted successfully | id={} | user={}", id, email);
}

    // =========================
    // 🔥 HELPER METHOD
    // =========================
    private User getUserByEmail(String email) {

    return userRepository.findByEmail(email)
            .orElseThrow(() -> {
                log.error("User not found | email={}", email);
                return new ResourceNotFoundException("User not found");
            });
}
}