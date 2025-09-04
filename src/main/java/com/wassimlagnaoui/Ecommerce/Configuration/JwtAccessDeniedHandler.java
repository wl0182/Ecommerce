package com.wassimlagnaoui.Ecommerce.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger logger = LoggerFactory.getLogger(JwtAccessDeniedHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request,
                      HttpServletResponse response,
                      AccessDeniedException accessDeniedException) throws IOException, ServletException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : "anonymous";

        logger.warn("Access denied for user '{}' accessing '{}': {}",
                   username, request.getServletPath(), accessDeniedException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        errorResponse.put("status", HttpServletResponse.SC_FORBIDDEN);
        errorResponse.put("error", "Forbidden");
        errorResponse.put("message", "Access denied. Insufficient privileges.");
        errorResponse.put("path", request.getServletPath());
        errorResponse.put("details", getAccessDeniedDetails(request, authentication));

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }

    private String getAccessDeniedDetails(HttpServletRequest request, Authentication authentication) {
        if (authentication == null) {
            return "User is not authenticated";
        }

        String path = request.getServletPath();
        String userRole = authentication.getAuthorities().toString();

        if (path.contains("/admin/")) {
            return String.format("Admin access required. Current role: %s", userRole);
        } else if (path.contains("/api/")) {
            return String.format("Insufficient permissions for this resource. Current role: %s", userRole);
        }

        return String.format("Access denied for user '%s' with role %s",
                           authentication.getName(), userRole);
    }
}
