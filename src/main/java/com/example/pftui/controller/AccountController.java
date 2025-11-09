package com.example.pftui.controller;

import com.example.pftui.model.Account;
import com.example.pftui.model.AccountStatus;
import com.example.pftui.model.AccountType;
import com.example.pftui.security.CustomUserDetails;
import com.example.pftui.service.AccountService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public String listAccounts(@RequestParam(required = false) String editId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails)) {
            return "redirect:/login";
        }
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        String userId = userDetails.getUser().getId();

        List<Account> accounts = accountService.getAccountsWithBalance(userId);
        
        BigDecimal totalBalance = accounts.stream()
            .filter(a -> a.getStatus() == AccountStatus.ACTIVE)
            .map(Account::getCurrentBalance)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("active", "accounts");
        model.addAttribute("currentUser", userDetails.getUser());
        model.addAttribute("accounts", accounts);
        model.addAttribute("totalBalance", totalBalance);

        if (editId != null) {
            accountService.getAccountById(editId).ifPresent(account -> {
                if (userId.equals(account.getUserId())) {
                    model.addAttribute("editAccount", account);
                }
            });
        }

        return "accounts";
    }

    @PostMapping
    public String saveAccount(@RequestParam(required = false) String id,
                            @RequestParam String name,
                            @RequestParam AccountType type,
                            @RequestParam(defaultValue = "USD") String currency,
                            @RequestParam(required = false) BigDecimal initialBalance,
                            @RequestParam(required = false) AccountStatus status,
                            RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails)) {
            return "redirect:/login";
        }
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        String userId = userDetails.getUser().getId();

        try {
            Account account = (id != null && !id.isEmpty()) 
                ? accountService.getAccountById(id).orElse(new Account()) 
                : new Account();

            if (id != null && !id.isEmpty() && !userId.equals(account.getUserId())) {
                throw new SecurityException("You do not have permission to edit this account.");
            }

            account.setUserId(userId);
            account.setName(name);
            account.setType(type);
            account.setCurrency(currency);
            account.setInitialBalance(initialBalance != null ? initialBalance : BigDecimal.ZERO);
            if (status != null) { // Status might not be present in create form
                account.setStatus(status);
            }

            accountService.createAccount(account);
            redirectAttributes.addFlashAttribute("successMessage", "Account saved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error saving account: " + e.getMessage());
        }

        return "redirect:/accounts";
    }

    @PostMapping("/{id}/delete")
    public String deleteAccount(@PathVariable String id, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails)) {
            return "redirect:/login";
        }
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        String userId = userDetails.getUser().getId();

        try {
            Account account = accountService.getAccountById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

            if (!userId.equals(account.getUserId())) {
                throw new SecurityException("You do not have permission to delete this account.");
            }

            accountService.deleteAccount(id);
            redirectAttributes.addFlashAttribute("successMessage", "Account deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting account: " + e.getMessage());
        }

        return "redirect:/accounts";
    }
    
    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable String id, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails)) {
            return "redirect:/login";
        }
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        String userId = userDetails.getUser().getId();

        try {
            Account account = accountService.getAccountById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

            if (!userId.equals(account.getUserId())) {
                throw new SecurityException("You do not have permission to toggle status for this account.");
            }
            
            account.setStatus(account.getStatus() == AccountStatus.ACTIVE ? AccountStatus.INACTIVE : AccountStatus.ACTIVE);
            accountService.updateAccount(account);
            redirectAttributes.addFlashAttribute("successMessage", "Account status updated!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating status: " + e.getMessage());
        }

        return "redirect:/accounts";
    }
}
