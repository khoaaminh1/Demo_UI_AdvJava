package com.example.pftui.model;

import java.util.UUID;

public class Category {
    private UUID id;
    private String name;
    private CategoryType type;
    private String icon; // remix icon class

    public Category() {}
    public Category(UUID id, String name, CategoryType type, String icon) {
        this.id = id; this.name = name; this.type = type; this.icon = icon;
    }
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public CategoryType getType() { return type; }
    public void setType(CategoryType type) { this.type = type; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
}
