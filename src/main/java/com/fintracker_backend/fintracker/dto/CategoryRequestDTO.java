package com.fintracker_backend.fintracker.dto;

import com.fintracker_backend.fintracker.entity.CategoryType;

import lombok.Data;

@Data
public class CategoryRequestDTO {

    private String name;

    private CategoryType type; // INCOME / EXPENSE

    private Long parentId; // optional (for subcategory)
}
