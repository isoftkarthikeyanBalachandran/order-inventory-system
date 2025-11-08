package ai.presight.orderservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;
import java.util.List;


@Component
public class JwtUtil {

    private static final long EXPIRATION_TIME_MS = 60 * 60 * 1000; // 1 hour
    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    /**
     * Generate a JWT token for a given username and roles.
     */
    public String generateToken(String username, List<String> roles) {
        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME_MS))
                .signWith(key)
                .compact();
    }

    /**
     * Extract username (subject) from token.
     */
    public String extractUsername(String token) {
        return getAllClaims(token).getSubject();
    }

    /**
     * Validate token’s signature and expiry.
     */
    public boolean isTokenValid(String token, String username) {
        try {
            final String extracted = extractUsername(token);
            return (extracted.equals(username) && !isTokenExpired(token));
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return getAllClaims(token).getExpiration().before(new Date());
    }

    private Claims getAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
