package com.example.pftui.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "categories")
public class Category {
    @Id
    private String id;
    private String userId; // Link to User (null for system categories)
    private String name;
    private CategoryType type;
    private String icon; // remix icon class
    private boolean isSystem; // true for default categories
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Category() {
        this.isSystem = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public Category(String id, String userId, String name, CategoryType type, String icon, boolean isSystem) {
        this();
        this.id = id; 
        this.userId = userId;
        this.name = name; 
        this.type = type; 
        this.icon = icon;
        this.isSystem = isSystem;
    }
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public CategoryType getType() { return type; }
    public void setType(CategoryType type) { this.type = type; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public boolean isSystem() { return isSystem; }
    public void setSystem(boolean system) { isSystem = system; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
