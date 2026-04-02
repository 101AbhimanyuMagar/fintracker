package com.fintracker_backend.fintracker.controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
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

    @PostMapping
    public ResponseEntity<Category> createCategory(
            @RequestBody CategoryRequestDTO request,
            @RequestParam Long userId
    ) {
        return ResponseEntity.ok(
                categoryService.createCategory(request, userId)
        );
    }

    @GetMapping
    public ResponseEntity<List<Category>> getAll(
            @RequestParam Long userId
    ) {
        return ResponseEntity.ok(
                categoryService.getAllCategories(userId)
        );
    }

    @GetMapping("/type")
    public ResponseEntity<List<Category>> getByType(
            @RequestParam Long userId,
            @RequestParam CategoryType type
    ) {
        return ResponseEntity.ok(
                categoryService.getCategoriesByType(userId, type)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(
            @PathVariable Long id,
            @RequestParam Long userId
    ) {
        categoryService.deleteCategory(id, userId);
        return ResponseEntity.ok("Category deleted");
    }
}
