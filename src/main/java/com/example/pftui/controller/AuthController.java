package com.example.pftui.controller;

import com.example.pftui.model.User;
import com.example.pftui.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.pftui.security.CustomUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                           @RequestParam(value = "logout", required = false) String logout,
                           Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid email or password");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully");
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String email,
                          @RequestParam String password,
                          @RequestParam String fullName,
                          Model model, // Use Model instead of RedirectAttributes
                          RedirectAttributes redirectAttributes) {
        try {
            userService.registerUser(email, password, fullName);
            model.addAttribute("message", "Registration successful! A verification code has been sent to your email.");
            model.addAttribute("email", email);
            model.addAttribute("showVerifyForm", true); // Flag to show verification form
            return "register"; // Return to the same page
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }



    @GetMapping("/verify")
    public String verifyPage(@RequestParam String email, @RequestParam(required = false) String error, Model model) {
        model.addAttribute("email", email);
        model.addAttribute("showVerifyForm", true);
        if (error != null && error.equals("NotVerified")) {
            model.addAttribute("message", "Your account is not verified. Please check your email for the verification code.");
        }
        return "register"; // Show verify form on the register page
    }

    @PostMapping("/verify")
    public String verify(@RequestParam String email,
                       @RequestParam String code,
                       RedirectAttributes redirectAttributes) {
        try {
            boolean isVerified = userService.verifyUser(email, code);
            if (isVerified) {
                redirectAttributes.addFlashAttribute("message", "Account verified successfully! Please login.");
                return "redirect:/login";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addAttribute("email", email);
            return "redirect:/verify";
        }
        return "redirect:/login";
    }

    @PostMapping("/resend-code")
    public String resendCode(@RequestParam String email, Model model, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
            if (user.isEnabled()) {
                redirectAttributes.addFlashAttribute("error", "This account is already verified.");
                return "redirect:/login";
            }
            userService.generateAndSendVerificationCode(user);
            model.addAttribute("message", "A new verification code has been sent to your email.");
            model.addAttribute("email", email);
            model.addAttribute("showVerifyForm", true);
            return "register";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/logout-success")
    public String logoutSuccess() {
        return "redirect:/login?logout";
    }

    // --- 2FA Endpoints ---

    @GetMapping("/login-2fa")
    public String login2faPage(@RequestParam String email, Model model) {
        model.addAttribute("email", email);
        return "login-2fa";
    }

    @PostMapping("/login/2fa")
    public String verify2fa(@RequestParam String email,
                          @RequestParam String code,
                          HttpServletRequest request,
                          RedirectAttributes redirectAttributes) {
        try {
            boolean isVerified = userService.verify2faCode(email, code);
            if (isVerified) {
                // Manually authenticate user after 2FA success
                User user = userService.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
                UserDetails userDetails = new CustomUserDetails(user);
                Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                userService.updateLastLogin(email);
                return "redirect:/dashboard";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addAttribute("email", email);
            return "redirect:/login-2fa";
        }
        return "redirect:/login";
    }
}

