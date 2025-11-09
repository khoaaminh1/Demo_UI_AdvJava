package com.example.pftui.repository;

import com.example.pftui.model.Category;
import com.example.pftui.model.CategoryType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends MongoRepository<Category, String> {
    List<Category> findByUserId(String userId);
    List<Category> findByUserIdOrIsSystemTrue(String userId);
    List<Category> findByUserIdAndType(String userId, CategoryType type);
    List<Category> findByIsSystemTrue();
    long countByUserId(String userId);
}

