package ai.presight.orderservice.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemRequest {
    private String skuCode;
    private Integer quantity;
    private Double price;
}
