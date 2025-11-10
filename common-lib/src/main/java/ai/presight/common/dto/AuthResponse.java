/**
 * -----------------------------------------------------------
 * @Project     : Order & Inventory Microservices System
 * @Author      : Karthikeyan Balachandran
 * @Created On  : 10-Nov-2025
 * -----------------------------------------------------------
 */

package ai.presight.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standard authentication response object shared across all services.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String message;
}
