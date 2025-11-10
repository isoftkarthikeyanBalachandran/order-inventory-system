/**
 * -----------------------------------------------------------
 * @Project     : Order & Inventory Microservices System
 * @Author      : Karthikeyan Balachandran
 * @Created On  : 09-Nov-2025
 * -----------------------------------------------------------
 */
package ai.presight.orderservice.config;

import java.io.IOException;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ai.presight.common.security.JwtAuthFilter;
import ai.presight.common.security.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OrderJwtAuthFilter extends JwtAuthFilter {

    private final RestTemplate restTemplate = new RestTemplate();

    public OrderJwtAuthFilter(JwtUtil jwtUtil) {
        super(jwtUtil);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn(" No Authorization header — skipping filter for {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        log.info(" [OrderJwtAuthFilter] Validating token via IAM service...");

        try {
            ResponseEntity<Void> validationResponse = restTemplate.postForEntity(
                    "http://iam-service:8083/api/v1/auth/validate",
                    new HttpEntity<>(token),
                    Void.class
            );

            if (validationResponse.getStatusCode().is2xxSuccessful()) {
                log.info(" Token validated successfully via IAM. Continuing request...");
                super.doFilterInternal(request, response, filterChain);
            } else {
                log.warn(" IAM validation failed. Status: {}", validationResponse.getStatusCode());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }

        } catch (Exception e) {
            log.error(" Exception during IAM token validation: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}
