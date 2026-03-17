package com.uniai.shared.infrastructure.jwt;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Facade for retrieving the authenticated user's email from the SecurityContext.
 * Controllers and application services use this instead of importing Spring Security directly.
 */
@Component
public class JwtFacade {

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
