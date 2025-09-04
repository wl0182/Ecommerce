package com.wassimlagnaoui.Ecommerce.Configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {

    private final Jwt jwt = new Jwt();
    private final Cors cors = new Cors();
    private final Auth auth = new Auth();

    public Jwt getJwt() {
        return jwt;
    }

    public Cors getCors() {
        return cors;
    }

    public Auth getAuth() {
        return auth;
    }

    public static class Jwt {
        private String secret = "myVerySecureSecretKeyForJWTTokenGenerationThatShouldBeAtLeast256Bits";
        private long expiration = 86400000; // 24 hours in milliseconds
        private String tokenPrefix = "Bearer ";
        private String headerString = "Authorization";

        // Getters and setters
        public String getSecret() { return secret; }
        public void setSecret(String secret) { this.secret = secret; }
        public long getExpiration() { return expiration; }
        public void setExpiration(long expiration) { this.expiration = expiration; }
        public String getTokenPrefix() { return tokenPrefix; }
        public void setTokenPrefix(String tokenPrefix) { this.tokenPrefix = tokenPrefix; }
        public String getHeaderString() { return headerString; }
        public void setHeaderString(String headerString) { this.headerString = headerString; }
    }

    public static class Cors {
        private List<String> allowedOrigins = List.of("http://localhost:3000", "http://localhost:8080");
        private List<String> allowedMethods = List.of("GET", "POST", "PUT", "DELETE", "OPTIONS");
        private List<String> allowedHeaders = List.of("*");
        private List<String> exposedHeaders = List.of("Authorization");
        private boolean allowCredentials = true;

        // Getters and setters
        public List<String> getAllowedOrigins() { return allowedOrigins; }
        public void setAllowedOrigins(List<String> allowedOrigins) { this.allowedOrigins = allowedOrigins; }
        public List<String> getAllowedMethods() { return allowedMethods; }
        public void setAllowedMethods(List<String> allowedMethods) { this.allowedMethods = allowedMethods; }
        public List<String> getAllowedHeaders() { return allowedHeaders; }
        public void setAllowedHeaders(List<String> allowedHeaders) { this.allowedHeaders = allowedHeaders; }
        public List<String> getExposedHeaders() { return exposedHeaders; }
        public void setExposedHeaders(List<String> exposedHeaders) { this.exposedHeaders = exposedHeaders; }
        public boolean isAllowCredentials() { return allowCredentials; }
        public void setAllowCredentials(boolean allowCredentials) { this.allowCredentials = allowCredentials; }
    }

    public static class Auth {
        private List<String> publicEndpoints = List.of(
            "/api/auth/**",
            "/api/public/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/api-docs/**",
            "/",
            "/health",
            "/actuator/**",
            "/error"
        );

        private List<String> publicProductEndpoints = List.of(
            "/api/products",
            "/api/products/{id}",
            "/api/products/search/**",
            "/api/products/category/**"
        );

        // Getters and setters
        public List<String> getPublicEndpoints() { return publicEndpoints; }
        public void setPublicEndpoints(List<String> publicEndpoints) { this.publicEndpoints = publicEndpoints; }
        public List<String> getPublicProductEndpoints() { return publicProductEndpoints; }
        public void setPublicProductEndpoints(List<String> publicProductEndpoints) { this.publicProductEndpoints = publicProductEndpoints; }
    }
}
