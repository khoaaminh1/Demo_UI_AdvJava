package com.example.pftui.model;

import java.math.BigDecimal;
import java.util.UUID;

public class Budget {
    private UUID id;
    private Category category;
    private int month; // 1..12
    private int year;
    private BigDecimal limitAmount;

    public Budget() {}
    public Budget(UUID id, Category category, int month, int year, BigDecimal limitAmount) {
        this.id = id; this.category = category; this.month = month; this.year = year; this.limitAmount = limitAmount;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public BigDecimal getLimitAmount() { return limitAmount; }
    public void setLimitAmount(BigDecimal limitAmount) { this.limitAmount = limitAmount; }
}
