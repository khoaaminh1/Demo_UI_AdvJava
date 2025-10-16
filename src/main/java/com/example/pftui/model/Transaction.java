package com.example.pftui.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class Transaction {
    private UUID id;
    private Account account;
    private Category category;
    private BigDecimal amount;
    private LocalDate date;
    private String merchant;
    private String note;
    private boolean recurring;

    public Transaction() {}

    public Transaction(UUID id, Account account, Category category, BigDecimal amount, LocalDate date, String merchant, String note, boolean recurring) {
        this.id = id; this.account = account; this.category = category; this.amount = amount;
        this.date = date; this.merchant = merchant; this.note = note; this.recurring = recurring;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
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
}
