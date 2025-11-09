package com.example.pftui.service;

import com.example.pftui.model.Category;
import com.example.pftui.model.CategoryType;
import com.example.pftui.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }
    
    public List<Category> getAllCategoriesForUser(String userId) {
        // Returns both user's custom categories and system categories
        return categoryRepository.findByUserIdOrIsSystemTrue(userId);
    }
    
    public List<Category> getCategoriesByType(String userId, CategoryType type) {
        return categoryRepository.findByUserIdAndType(userId, type);
    }
    
    public List<Category> getSystemCategories() {
        return categoryRepository.findByIsSystemTrue();
    }
    
    public Optional<Category> getCategoryById(String id) {
        return categoryRepository.findById(id);
    }
    
    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }
    
    public Category updateCategory(Category category) {
        return categoryRepository.save(category);
    }
    
    public void deleteCategory(String id) {
        categoryRepository.deleteById(id);
    }
    
    public long countCategoriesByUser(String userId) {
        return categoryRepository.countByUserId(userId);
    }
}

