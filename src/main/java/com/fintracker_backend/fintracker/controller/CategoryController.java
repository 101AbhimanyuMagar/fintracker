package com.fintracker_backend.fintracker.controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;
import lombok.RequiredArgsConstructor;
import com.fintracker_backend.fintracker.dto.CategoryRequestDTO;
import com.fintracker_backend.fintracker.entity.Category;
import com.fintracker_backend.fintracker.entity.CategoryType;
import com.fintracker_backend.fintracker.service.CategoryService;


@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // ➕ Create Category
    @PostMapping
    public ResponseEntity<Category> createCategory(
            @RequestBody CategoryRequestDTO request,
            Authentication authentication
    ) {
        String email = authentication.getName();

        return ResponseEntity.ok(
                categoryService.createCategory(request, email)
        );
    }


        @GetMapping("/subcategories/{parentId}")
        public ResponseEntity<List<Category>> getSubCategories(
                @PathVariable Long parentId
        ) {
        return ResponseEntity.ok(
                categoryService.getSubCategories(parentId)
        );
        }

    // 📄 Get All Categories
    @GetMapping
    public ResponseEntity<List<Category>> getAll(Authentication authentication) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                categoryService.getAllCategories(email)
        );
    }

    // 📂 Get By Type (INCOME / EXPENSE)
    @GetMapping("/type")
    public ResponseEntity<List<Category>> getByType(
            @RequestParam CategoryType type,
            Authentication authentication
    ) {
        String email = authentication.getName();

        return ResponseEntity.ok(
                categoryService.getCategoriesByType(email, type)
        );
    }

    // ❌ Delete Category
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String email = authentication.getName();

        categoryService.deleteCategory(id, email);

        return ResponseEntity.ok("Category deleted");
    }
}