package ai.presight.orderservice.model;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {
	
	private String status;  
    private List<OrderItemRequest> items;
}
