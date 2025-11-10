package ai.presight.iamservice.controller;

import ai.presight.common.dto.LoginRequest;
import ai.presight.common.dto.AuthResponse;
import ai.presight.common.security.JwtUtil;
import ai.presight.iamservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * IAM Authentication Controller
 * Handles login and token validation requests.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * Login endpoint: validates user credentials and returns a JWT token.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        if (userService.validateUser(request.username(), request.password())) {
            String token = jwtUtil.generateToken(request.username());
            return ResponseEntity.ok(new AuthResponse(token, "Login successful"));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new AuthResponse(null, "Invalid credentials"));
    }

   
  
    @PostMapping("/validate")
    public ResponseEntity<Void> validate(@RequestBody String token) {
        try {
            String username = jwtUtil.extractUsername(token);
            if (jwtUtil.validateToken(token, username)) {
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
