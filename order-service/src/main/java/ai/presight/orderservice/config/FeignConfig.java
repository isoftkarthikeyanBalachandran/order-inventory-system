package ai.presight.orderservice.config;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ai.presight.orderservice.exception.ResourceNotFoundException;
import feign.Response;
import org.springframework.http.HttpStatus;

@Configuration
public class FeignConfig {

    /**
     * Custom Feign error decoder to map HTTP errors from Inventory Service
     * into domain-specific exceptions for clear responses.
     */
    @Bean
    public ErrorDecoder errorDecoder() {
        return new ErrorDecoder() {
            @Override
            public Exception decode(String methodKey, Response response) {
                HttpStatus status = HttpStatus.valueOf(response.status());

                return switch (status) {
                    case NOT_FOUND -> new ResourceNotFoundException("Inventory item not found");
                    case BAD_REQUEST -> new IllegalArgumentException("Bad request to Inventory Service");
                    case UNAUTHORIZED -> new SecurityException("Unauthorized access to Inventory Service");
                    case FORBIDDEN -> new SecurityException("Forbidden access to Inventory Service");
                    default -> new RuntimeException("Unexpected error from Inventory Service: " + status);
                };
            }
        };
    }
}
