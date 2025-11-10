/**
 * -----------------------------------------------------------
 * @Project     : Order & Inventory Microservices System
 * @Author      : Karthikeyan Balachandran
 * @Created On  : 10-Nov-2025
 * -----------------------------------------------------------
 */

package ai.presight.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;


@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        log.debug("[JwtAuthFilter] Processing request: {}", path);

        if (path.startsWith("/actuator") || path.startsWith("/api/v1/auth")) {
            log.debug("Skipping JWT filter for {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn(" Missing or invalid Authorization header for {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);
        log.debug("Extracted token: {}", token.substring(0, Math.min(token.length(), 20)) + "...");

        try {
            final String username = jwtUtil.extractUsername(token);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (jwtUtil.validateToken(token, username)) {
                    var userDetails = User.withUsername(username)
                            .password("")
                            .authorities("ROLE_USER")
                            .build();

                    var authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.info(" JWT validated successfully for user={}", username);
                } else {
                    log.warn(" Invalid JWT token for user={}", username);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
            }
        } catch (Exception e) {
            log.error(" JWT processing error: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        log.debug("Continuing request chain for {}", path);
        filterChain.doFilter(request, response);
    }
}
