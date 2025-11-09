package com.example.pftui.model;

import java.math.BigDecimal;
import java.util.UUID;

public class Account {
    private UUID id;
    private String name;
    private AccountType type;
    private String currency;
    private BigDecimal initialBalance; // Số dư ban đầu khi tạo account
    private AccountStatus status; // ACTIVE hoặc INACTIVE
    
    // Transient field - được tính toán từ transactions
    private BigDecimal currentBalance; // Số dư hiện tại

    public Account() {
        this.status = AccountStatus.ACTIVE;
        this.initialBalance = BigDecimal.ZERO;
        this.currentBalance = BigDecimal.ZERO;
    }
    
    public Account(UUID id, String name, AccountType type, String currency, BigDecimal initialBalance, AccountStatus status) {
        this.id = id; 
        this.name = name; 
        this.type = type; 
        this.currency = currency;
        this.initialBalance = initialBalance != null ? initialBalance : BigDecimal.ZERO;
        this.status = status != null ? status : AccountStatus.ACTIVE;
        this.currentBalance = BigDecimal.ZERO;
    }
    
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
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
}
