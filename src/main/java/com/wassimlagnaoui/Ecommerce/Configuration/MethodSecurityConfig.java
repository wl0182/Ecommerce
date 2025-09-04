package com.wassimlagnaoui.Ecommerce.Configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Method-level security configuration for fine-grained access control
 * This enables @PreAuthorize, @PostAuthorize, @Secured annotations
 */
@Configuration
@EnableMethodSecurity(
    prePostEnabled = true,    // Enable @PreAuthorize and @PostAuthorize
    securedEnabled = true,    // Enable @Secured
    jsr250Enabled = true      // Enable @RolesAllowed
)
public class MethodSecurityConfig {
    // This class enables method-level security annotations
    // Controllers and services can now use:
    // @PreAuthorize("hasRole('ADMIN')")
    // @PreAuthorize("hasRole('CUSTOMER') and #userId == authentication.principal.id")
    // @Secured("ROLE_ADMIN")
    // @RolesAllowed("ADMIN")
}
