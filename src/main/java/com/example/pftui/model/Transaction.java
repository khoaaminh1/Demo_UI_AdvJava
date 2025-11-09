package com.example.pftui.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "transactions")
public class Transaction {
    @Id
    private String id;
    private String userId; // Link to User
    private String accountId; // Reference to Account
    private String categoryId; // Reference to Category
    private BigDecimal amount;
    private LocalDate date;
    private String merchant;
    private String note;
    private boolean recurring;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Transient fields - populated when needed
    @Transient
    private Account account;
    @Transient
    private Category category;

    public Transaction() {
        this.recurring = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Transaction(String id, String userId, String accountId, String categoryId, BigDecimal amount, LocalDate date, String merchant, String note, boolean recurring) {
        this();
        this.id = id; 
        this.userId = userId;
        this.accountId = accountId; 
        this.categoryId = categoryId; 
        this.amount = amount;
        this.date = date; 
        this.merchant = merchant; 
        this.note = note; 
        this.recurring = recurring;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getMerchant() { return merchant; }
    public void setMerchant(String merchant) { this.merchant = merchant; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public boolean isRecurring() { return recurring; }
    public void setRecurring(boolean recurring) { this.recurring = recurring; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
