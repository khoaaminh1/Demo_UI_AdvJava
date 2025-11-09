package com.example.pftui.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.pftui.model.*;
import com.example.pftui.service.FakeDataService;

import java.math.BigDecimal;
import java.util.UUID;

@Controller
@RequestMapping("/accounts")
public class AccountController {
    private final FakeDataService data;
    public AccountController(FakeDataService data) { this.data = data; }

    @GetMapping
    public String list(@RequestParam(required = false) UUID editId, Model model) {
        // Calculate current balances for all accounts
        data.calculateAccountBalances();
        
        var accounts = data.getAccounts();
        
        // Calculate statistics
        long activeCount = accounts.stream()
            .filter(a -> a.getStatus() == AccountStatus.ACTIVE)
            .count();
        long inactiveCount = accounts.stream()
            .filter(a -> a.getStatus() == AccountStatus.INACTIVE)
            .count();
        BigDecimal totalBalance = accounts.stream()
            .map(Account::getCurrentBalance)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        model.addAttribute("active", "accounts");
        model.addAttribute("accounts", accounts);
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("inactiveCount", inactiveCount);
        model.addAttribute("totalBalance", totalBalance);
        
        // If editing, add the account to edit
        if (editId != null) {
            var accountToEdit = accounts.stream()
                .filter(a -> a.getId().equals(editId))
                .findFirst()
                .orElse(null);
            model.addAttribute("editAccount", accountToEdit);
        }
        
        return "accounts";
    }

    @PostMapping
    public String create(@RequestParam String name, 
                        @RequestParam AccountType type, 
                        @RequestParam(defaultValue="USD") String currency,
                        @RequestParam(required = false) BigDecimal initialBalance) {
        BigDecimal balance = initialBalance != null ? initialBalance : BigDecimal.ZERO;
        data.addAccount(new Account(UUID.randomUUID(), name, type, currency, balance, AccountStatus.ACTIVE));
        return "redirect:/accounts";
    }
    
    @PostMapping("/{id}/update")
    public String update(@PathVariable UUID id,
                        @RequestParam String name,
                        @RequestParam AccountType type,
                        @RequestParam String currency,
                        @RequestParam BigDecimal initialBalance,
                        @RequestParam AccountStatus status) {
        var account = data.getAccounts().stream()
            .filter(a -> a.getId().equals(id))
            .findFirst()
            .orElse(null);
            
        if (account != null) {
            account.setName(name);
            account.setType(type);
            account.setCurrency(currency);
            account.setInitialBalance(initialBalance);
            account.setStatus(status);
        }
        
        return "redirect:/accounts";
    }
    
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id) {
        data.getAccounts().removeIf(a -> a.getId().equals(id));
        return "redirect:/accounts";
    }
    
    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable UUID id) {
        var account = data.getAccounts().stream()
            .filter(a -> a.getId().equals(id))
            .findFirst()
            .orElse(null);
            
        if (account != null) {
            account.setStatus(account.getStatus() == AccountStatus.ACTIVE 
                ? AccountStatus.INACTIVE 
                : AccountStatus.ACTIVE);
        }
        
        return "redirect:/accounts";
    }
}
