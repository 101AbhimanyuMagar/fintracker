package com.fintracker_backend.fintracker.service;

import java.util.List;


import com.fintracker_backend.fintracker.dto.CategoryRequestDTO;
import com.fintracker_backend.fintracker.entity.Category;
import com.fintracker_backend.fintracker.entity.CategoryType;

public interface CategoryService {

    Category createCategory(CategoryRequestDTO request, String email);

    List<Category> getAllCategories(String email);

    List<Category> getCategoriesByType(String email, CategoryType type);

    List<Category> getSubCategories(Long parentId);

    Category getCategoryById(Long id);

    void deleteCategory(Long id, String email);
}
