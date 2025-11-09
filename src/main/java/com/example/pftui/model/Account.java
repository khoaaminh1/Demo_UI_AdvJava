package com.example.pftui.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "accounts")
public class Account {
    @Id
    private String id;
    private String userId; // Link to User
    private String name;
    private AccountType type;
    private String currency;
    private BigDecimal initialBalance; // Số dư ban đầu khi tạo account
    private AccountStatus status; // ACTIVE hoặc INACTIVE
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Transient field - được tính toán từ transactions
    @Transient
    private BigDecimal currentBalance; // Số dư hiện tại

    public Account() {
        this.status = AccountStatus.ACTIVE;
        this.initialBalance = BigDecimal.ZERO;
        this.currentBalance = BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public Account(String id, String userId, String name, AccountType type, String currency, BigDecimal initialBalance, AccountStatus status) {
        this();
        this.id = id; 
        this.userId = userId;
        this.name = name; 
        this.type = type; 
        this.currency = currency;
        this.initialBalance = initialBalance != null ? initialBalance : BigDecimal.ZERO;
        this.status = status != null ? status : AccountStatus.ACTIVE;
    }
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public AccountType getType() { return type; }
    public void setType(AccountType type) { this.type = type; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public BigDecimal getInitialBalance() { return initialBalance; }
    public void setInitialBalance(BigDecimal initialBalance) { this.initialBalance = initialBalance; }
    public AccountStatus getStatus() { return status; }
    public void setStatus(AccountStatus status) { this.status = status; }
    public BigDecimal getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(BigDecimal currentBalance) { this.currentBalance = currentBalance; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
