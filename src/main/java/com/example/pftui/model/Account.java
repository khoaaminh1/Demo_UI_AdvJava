package com.example.pftui.model;

import java.util.UUID;

public class Account {
    private UUID id;
    private String name;
    private AccountType type;
    private String currency;

    public Account() {}
    public Account(UUID id, String name, AccountType type, String currency) {
        this.id = id; this.name = name; this.type = type; this.currency = currency;
    }
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public AccountType getType() { return type; }
    public void setType(AccountType type) { this.type = type; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
