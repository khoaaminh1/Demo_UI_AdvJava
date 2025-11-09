package com.example.pftui.service;

import com.example.pftui.model.User;
import com.example.pftui.repository.UserRepository;
import com.example.pftui.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Random;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return new CustomUserDetails(user);
    }

    public User registerUser(String email, String password, String fullName) {
        if (userRepository.existsByEmail(email)) {
            User existingUser = userRepository.findByEmail(email).orElseThrow();
            if (!existingUser.isEnabled()) {
                // If user exists but is not enabled, resend verification code
                generateAndSendVerificationCode(existingUser);
                return existingUser;
            }
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFullName(fullName);

        generateAndSendVerificationCode(user);
        return user;
    }

    public void generateAndSend2faCode(User user) {
        String code = String.format("%06d", new Random().nextInt(999999));
        user.setVerificationCode(code);
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);
        emailService.send2faCode(user.getEmail(), code);
    }

    public void generateAndSendVerificationCode(User user) {
        String code = String.format("%06d", new Random().nextInt(999999));
        user.setVerificationCode(code);
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(10)); // Code expires in 10 minutes
        userRepository.save(user);
        emailService.sendVerificationCode(user.getEmail(), code);
    }

    public boolean verify2faCode(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        if (user.getVerificationCode() == null || !user.getVerificationCode().equals(code)) {
            throw new IllegalArgumentException("Invalid authentication code.");
        }

        if (user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Authentication code has expired.");
        }

        // Clear the code after successful verification
        user.setVerificationCode(null);
        user.setVerificationCodeExpiresAt(null);
        userRepository.save(user);
        return true;
    }

    public boolean verifyUser(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        if (user.isEnabled()) {
            throw new IllegalStateException("Account is already verified.");
        }

        if (user.getVerificationCode() == null || !user.getVerificationCode().equals(code)) {
            throw new IllegalArgumentException("Invalid verification code.");
        }

        if (user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Verification code has expired.");
        }

        user.setEnabled(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiresAt(null);
        userRepository.save(user);
        return true;
    }

    public void updateLastLogin(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
        }
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public void setTwoFactorEnabled(String userId, boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setTwoFactorEnabled(enabled);
        userRepository.save(user);
    }

    public void changePassword(String userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if the current password is correct
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Incorrect current password");
        }

        // Encode and set the new password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}

