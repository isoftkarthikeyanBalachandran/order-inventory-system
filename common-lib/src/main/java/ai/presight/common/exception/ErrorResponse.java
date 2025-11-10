package ai.presight.common.exception;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Standard API error response model.
 * Used across all exception handlers to ensure consistent error structure.
 */
@Getter
@Builder
public class ErrorResponse {

    /**
     * The time when the error occurred.
     */
    private LocalDateTime timestamp;

    /**
     * Short error code or type, such as NOT_FOUND or BAD_REQUEST.
     */
    private String error;

    /**
     * Descriptive message explaining the reason for the error.
     */
    private String message;

    /**
     * The request path where the error was triggered.
     */
    private String path;
}
