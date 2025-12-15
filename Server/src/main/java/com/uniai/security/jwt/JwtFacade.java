package com.uniai.security.jwt;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Facade for retrieving authenticated user information from SecurityContext.
 * This provides a clean abstraction over Spring Security's authentication mechanism.
 */
@Component
public class JwtFacade {

    /**
     * Get the authenticated user's email from the security context.
     *
     * @return the authenticated user's email (lowercase)
     * @throws IllegalStateException if no authenticated user is found
     */
    public String getAuthenticatedUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null) {
            throw new IllegalStateException("No authenticated user found");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof String) {
            return (String) principal;
        }

        throw new IllegalStateException("Invalid authentication principal type");
    }
}
