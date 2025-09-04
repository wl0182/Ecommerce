package com.wassimlagnaoui.Ecommerce.Controller;

import com.wassimlagnaoui.Ecommerce.Configuration.CustomerUserDetails;
import com.wassimlagnaoui.Ecommerce.Configuration.JwtUtil;
import com.wassimlagnaoui.Ecommerce.Domain.Customer;
import com.wassimlagnaoui.Ecommerce.Domain.Role;
import com.wassimlagnaoui.Ecommerce.Repository.CustomerRepository;
import com.wassimlagnaoui.Ecommerce.Service.CustomerUserDetailsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final CustomerUserDetailsService userDetailsService;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager,
                         CustomerUserDetailsService userDetailsService,
                         CustomerRepository customerRepository,
                         PasswordEncoder passwordEncoder,
                         JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and get JWT token")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
                )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            CustomerUserDetails customerDetails = (CustomerUserDetails) userDetails;

            // Generate JWT token with additional claims
            String token = jwtUtil.generateTokenWithUserInfo(
                userDetails.getUsername(),
                customerDetails.getRole(),
                Long.valueOf(customerDetails.getId().hashCode()) // Convert String ID to Long for compatibility
            );

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("type", "Bearer");
            response.put("id", customerDetails.getId());
            response.put("username", customerDetails.getUsername());
            response.put("name", customerDetails.getName());
            response.put("role", customerDetails.getRole());
            response.put("expiresIn", jwtUtil.getExpirationTime());

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid email or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Authentication failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new customer")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        try {
            // Check if email already exists
            if (userDetailsService.existsByEmail(registerRequest.getEmail())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Email is already in use");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            // Create new customer
            Customer customer = new Customer();
            customer.setName(registerRequest.getName());
            customer.setEmail(registerRequest.getEmail());
            customer.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            customer.setRole(Role.CUSTOMER);
            customer.setEnabled(true);
            customer.setTotalSpent(0.0);

            Customer savedCustomer = customerRepository.save(customer);

            // Generate JWT token for the new user
            UserDetails userDetails = userDetailsService.loadUserByUsername(savedCustomer.getEmail());
            CustomerUserDetails customerDetails = (CustomerUserDetails) userDetails;

            String token = jwtUtil.generateTokenWithUserInfo(
                userDetails.getUsername(),
                customerDetails.getRole(),
                Long.valueOf(customerDetails.getId().hashCode())
            );

            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully");
            response.put("token", token);
            response.put("type", "Bearer");
            response.put("id", savedCustomer.getId());
            response.put("username", savedCustomer.getEmail());
            response.put("name", savedCustomer.getName());
            response.put("role", savedCustomer.getRole().name());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Registration failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/validate")
    @Operation(summary = "Validate JWT token")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");

            if (jwtUtil.isTokenValid(token)) {
                String username = jwtUtil.extractUsername(token);
                String role = jwtUtil.extractUserRole(token);

                Map<String, Object> response = new HashMap<>();
                response.put("valid", true);
                response.put("username", username);
                response.put("role", role);
                response.put("remainingTime", jwtUtil.getTokenRemainingTime(token));

                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("valid", false);
                response.put("message", "Token is invalid or expired");

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("valid", false);
            response.put("message", "Token validation failed");

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    // DTOs for request bodies
    public static class LoginRequest {
        private String email;
        private String password;

        // Getters and setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class RegisterRequest {
        private String name;
        private String email;
        private String password;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
