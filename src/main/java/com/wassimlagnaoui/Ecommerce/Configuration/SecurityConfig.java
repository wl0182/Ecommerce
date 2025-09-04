package com.wassimlagnaoui.Ecommerce.Configuration;

import com.wassimlagnaoui.Ecommerce.Service.CustomerUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomerUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public SecurityConfig(CustomerUserDetailsService userDetailsService,
                         JwtAuthenticationFilter jwtAuthenticationFilter,
                         JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
                         JwtAccessDeniedHandler jwtAccessDeniedHandler,
                         PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)
            )
            .authorizeHttpRequests(authz -> authz
                // Public endpoints - No authentication required
                .requestMatchers(
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
                ).permitAll()

                // Public product endpoints - Read-only access
                .requestMatchers(
                    "/api/products",
                    "/api/products/{id}",
                    "/api/products/search/**",
                    "/api/products/category/**"
                ).permitAll()

                // Admin-only endpoints
                .requestMatchers(
                    "/api/products/admin/**",
                    "/api/customers/admin/**",
                    "/api/orders/admin/**",
                    "/api/admin/**"
                ).hasRole("ADMIN")

                // Product management (Admin only)
                .requestMatchers(
                    "POST", "/api/products",
                    "PUT", "/api/products/**",
                    "DELETE", "/api/products/**"
                ).hasRole("ADMIN")

                // Customer-specific endpoints
                .requestMatchers(
                    "/api/customers/profile",
                    "/api/customers/me",
                    "/api/orders/my-orders",
                    "/api/orders/my-orders/**",
                    "/api/reviews/my-reviews"
                ).hasAnyRole("CUSTOMER", "ADMIN")

                // Order management - Customer can create, Admin can manage all
                .requestMatchers("POST", "/api/orders").hasAnyRole("CUSTOMER", "ADMIN")
                .requestMatchers("GET", "/api/orders/**").hasAnyRole("CUSTOMER", "ADMIN")
                .requestMatchers("PUT", "/api/orders/**").hasRole("ADMIN")
                .requestMatchers("DELETE", "/api/orders/**").hasRole("ADMIN")

                // Customer management
                .requestMatchers("GET", "/api/customers").hasRole("ADMIN")
                .requestMatchers("POST", "/api/customers").hasRole("ADMIN")
                .requestMatchers("PUT", "/api/customers/**").hasAnyRole("CUSTOMER", "ADMIN")
                .requestMatchers("DELETE", "/api/customers/**").hasRole("ADMIN")

                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .authenticationProvider(daoAuthenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
