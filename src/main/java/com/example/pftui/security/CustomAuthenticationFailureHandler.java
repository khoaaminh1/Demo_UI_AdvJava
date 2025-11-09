package com.example.pftui.security;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        // Check if the exception is because the user is disabled (not verified)
        if (exception instanceof DisabledException) {
            String email = request.getParameter("username"); // Spring Security's default username parameter
            // Redirect to the verification page, passing the email
            getRedirectStrategy().sendRedirect(request, response, "/verify?email=" + email + "&error=NotVerified");
        } else {
            // For all other errors (bad credentials, etc.), redirect to login with a generic error
            super.setDefaultFailureUrl("/login?error");
            super.onAuthenticationFailure(request, response, exception);
        }
    }
}

