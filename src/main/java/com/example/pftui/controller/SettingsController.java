package com.example.pftui.controller;

import com.example.pftui.model.User;
import com.example.pftui.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import com.example.pftui.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class SettingsController {

    @Autowired
    private UserService userService;

    @GetMapping("/settings")
    public String settings(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            // Fetch the latest user data from the database to ensure it's up-to-date
            User currentUser = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
            model.addAttribute("currentUser", currentUser);
        }
        
        model.addAttribute("active", "settings");
        return "settings";
    }

    @PostMapping("/settings/change-password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails)) {
            return "redirect:/login";
        }
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        String userId = userDetails.getUser().getId();

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "New passwords do not match.");
            return "redirect:/settings";
        }

        try {
            userService.changePassword(userId, currentPassword, newPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/settings";
    }

    @PostMapping("/settings/2fa/enable")
    public String enable2fa(RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails)) {
            return "redirect:/login";
        }
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        String userId = userDetails.getUser().getId();

        try {
            userService.setTwoFactorEnabled(userId, true);
            redirectAttributes.addFlashAttribute("successMessage", "Two-Factor Authentication has been enabled.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not enable 2FA: " + e.getMessage());
        }

        return "redirect:/settings";
    }

    @PostMapping("/settings/2fa/disable")
    public String disable2fa(RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails)) {
            return "redirect:/login";
        }
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        String userId = userDetails.getUser().getId();

        try {
            userService.setTwoFactorEnabled(userId, false);
            redirectAttributes.addFlashAttribute("successMessage", "Two-Factor Authentication has been disabled.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not disable 2FA: " + e.getMessage());
        }

        return "redirect:/settings";
    }
}
