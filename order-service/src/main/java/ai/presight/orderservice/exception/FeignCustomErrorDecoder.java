/**
 * -----------------------------------------------------------
 * @Project     : Order & Inventory Microservices System
 * @Author      : Karthikeyan Balachandran
 * @Created On  : 09-Nov-2025
 * -----------------------------------------------------------
 */


package ai.presight.orderservice.exception;

import org.springframework.http.HttpStatus;

import ai.presight.common.exception.ResourceNotFoundException;
import feign.Response;
import feign.codec.ErrorDecoder;

public class FeignCustomErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String methodKey, Response response) {
        HttpStatus status = HttpStatus.valueOf(response.status());
        switch (status) {
            case NOT_FOUND:
                return new ResourceNotFoundException("Inventory item not found");
            case BAD_REQUEST:
                return new IllegalStateException("Invalid request to inventory");
            default:
                return new Exception("Unexpected error: " + status);
        }
    }
}
