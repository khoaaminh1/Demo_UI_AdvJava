package com.example.pftui.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "budgets")
public class Budget {
    @Id
    private String id;
    private String userId; // Link to User
    private String categoryId; // Reference to Category
    private int month; // 1..12
    private int year;
    private BigDecimal limitAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Transient field - populated when needed
    @Transient
    private Category category;

    public Budget() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public Budget(String id, String userId, String categoryId, int month, int year, BigDecimal limitAmount) {
        this();
        this.id = id; 
        this.userId = userId;
        this.categoryId = categoryId; 
        this.month = month; 
        this.year = year; 
        this.limitAmount = limitAmount;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public BigDecimal getLimitAmount() { return limitAmount; }
    public void setLimitAmount(BigDecimal limitAmount) { this.limitAmount = limitAmount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
