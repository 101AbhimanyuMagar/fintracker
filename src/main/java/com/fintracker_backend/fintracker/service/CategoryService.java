package com.fintracker_backend.fintracker.service;

import java.util.List;


import com.fintracker_backend.fintracker.dto.CategoryRequestDTO;
import com.fintracker_backend.fintracker.entity.Category;
import com.fintracker_backend.fintracker.entity.CategoryType;

public interface CategoryService {

    Category createCategory(CategoryRequestDTO request, Long userId);

    List<Category> getAllCategories(Long userId);

    List<Category> getCategoriesByType(Long userId, CategoryType type);

    List<Category> getSubCategories(Long parentId);

    Category getCategoryById(Long id);

    void deleteCategory(Long id, Long userId);
}
