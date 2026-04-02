package com.fintracker_backend.fintracker.repository;

import java.util.List;
import java.util.Optional;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fintracker_backend.fintracker.entity.Category;
import com.fintracker_backend.fintracker.entity.CategoryType;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

   
    List<Category> findByUserId(Long userId);

    List<Category> findByUserIdAndType(Long userId, CategoryType type);

    List<Category> findByParentId(Long parentId);

    Optional<Category> findByNameAndUserId(String name, Long userId);
    
}
