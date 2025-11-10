/**
 * -----------------------------------------------------------
 * @Project     : Order & Inventory Microservices System
 * @Author      : Karthikeyan Balachandran
 * @Created On  : 10-Nov-2025
 * -----------------------------------------------------------
 */

package ai.presight.common.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import ai.presight.common.exception.ResourceNotFoundException;

@Configuration
public class FeignConfig {

    /**
     * Propagate JWT token from SecurityContext to Feign requests.
     */
    @Bean
    public RequestInterceptor requestTokenBearerInterceptor() {
        return (RequestTemplate template) -> {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getCredentials() != null) {
                String token = auth.getCredentials().toString();
                template.header("Authorization", "Bearer " + token);
            }
        };
    }

    /**
     * Custom Feign error decoder to handle and map HTTP errors.
     */
    @Bean
    public ErrorDecoder errorDecoder() {
        return (methodKey, response) -> {
            HttpStatus status = HttpStatus.valueOf(response.status());

            return switch (status) {
                case NOT_FOUND -> new ResourceNotFoundException("Inventory item not found");
                case BAD_REQUEST -> new IllegalArgumentException("Bad request to Inventory Service");
                case UNAUTHORIZED -> new SecurityException("Unauthorized access to Inventory Service");
                case FORBIDDEN -> new SecurityException("Forbidden access to Inventory Service");
                default -> new RuntimeException("Unexpected error from Inventory Service: " + status);
            };
        };
    }
}
