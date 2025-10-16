package com.example.pftui.model;

import java.math.BigDecimal;

public class BudgetUsage {
    private String categoryName;
    private BigDecimal spent;
    private BigDecimal limit;
    private int percent;

    public BudgetUsage(String categoryName, BigDecimal spent, BigDecimal limit) {
        this.categoryName = categoryName;
        this.spent = spent;
        this.limit = limit;
        if (limit != null && limit.compareTo(BigDecimal.ZERO) > 0) {
            this.percent = spent.multiply(BigDecimal.valueOf(100)).divide(limit, 0, java.math.RoundingMode.HALF_UP).intValue();
        } else {
            this.percent = 0;
        }
    }

    public String getCategoryName() { return categoryName; }
    public BigDecimal getSpent() { return spent; }
    public BigDecimal getLimit() { return limit; }
    public int getPercent() { return percent; }
}
