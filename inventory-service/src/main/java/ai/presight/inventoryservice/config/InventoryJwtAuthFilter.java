package ai.presight.inventoryservice.config;

import ai.presight.common.security.JwtAuthFilter;
import ai.presight.common.security.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Collections;

/**
 *  Custom JWT filter for Inventory Service.
 * - Validates token with IAM service
 * - Sets SecurityContext so Spring controllers run correctly
 */
@Slf4j
@Component
public class InventoryJwtAuthFilter extends JwtAuthFilter {

    private final RestTemplate restTemplate = new RestTemplate();

    public InventoryJwtAuthFilter(JwtUtil jwtUtil) {
        super(jwtUtil);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        log.info(" [InventoryJwtAuthFilter] Incoming request: {}", path);

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn(" No Authorization header found → skipping JWT validation for {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        log.info(" Extracted token: {}...", token.substring(0, Math.min(token.length(), 10)));

        try {
            //  Call IAM service to validate JWT
            ResponseEntity<Void> validationResponse = restTemplate.postForEntity(
                    "http://iam-service:8083/api/v1/auth/validate",
                    new HttpEntity<>(token),
                    Void.class
            );

            log.info("IAM validation response: {}", validationResponse.getStatusCode());

            if (validationResponse.getStatusCode().is2xxSuccessful()) {
                //  Token valid → mark request authenticated
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken("validatedUser", null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.info(" Token valid → SecurityContext set → proceeding to controller");
                filterChain.doFilter(request, response);
            } else {
                log.warn(" Token invalid → rejecting request");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }

        } catch (Exception e) {
            log.error(" Error during token validation: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }

        log.info("[InventoryJwtAuthFilter] Completed filtering for {}", path);
    }
}
