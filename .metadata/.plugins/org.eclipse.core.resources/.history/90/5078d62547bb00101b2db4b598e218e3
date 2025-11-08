package ai.presight.orderservice.controller;

import ai.presight.orderservice.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final JwtUtil jwtUtil;

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/token")
    public ResponseEntity<Map<String, String>> generateToken(@RequestParam String username) {
        String token = jwtUtil.generateToken(username, List.of("ROLE_USER"));
        return ResponseEntity.ok(Map.of("token", token));
    }
}
