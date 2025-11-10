package ai.presight.common.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

    private final Key key;
    private final long expirationTime;

    public JwtUtil(
            @Value("${jwt.secret:${JWT_SECRET:}}") String secret,
            @Value("${jwt.expiration:${JWT_EXPIRATION:3600000}}") long expirationTime
    ) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(" JWT secret not configured. Please set jwt.secret in YAML or env variable.");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationTime = expirationTime;
        log.info("JwtUtil initialized with expiration={} ms", expirationTime);
    }

    public String generateToken(String username) {
        String token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
        log.debug(" Generated JWT for user={} exp={}", username, new Date(System.currentTimeMillis() + expirationTime));
        return token;
    }

    public String extractUsername(String token) {
        try {
            String username = getClaims(token).getSubject();
            log.debug("Extracted username={} from JWT", username);
            return username;
        } catch (JwtException e) {
            log.error(" Error extracting username: {}", e.getMessage());
            throw e;
        }
    }

    public boolean validateToken(String token, String username) {
        try {
            String extracted = extractUsername(token);
            boolean expired = isTokenExpired(token);
            boolean valid = extracted.equals(username) && !expired;
            log.info("Validating token for user={} → match={} expired={} → valid={}", username, extracted.equals(username), expired, valid);
            return valid;
        } catch (JwtException e) {
            log.error(" Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private boolean isTokenExpired(String token) {
        Date exp = getClaims(token).getExpiration();
        boolean expired = exp.before(new Date());
        log.debug("Token expires={} (expired={})", exp, expired);
        return expired;
    }
}
