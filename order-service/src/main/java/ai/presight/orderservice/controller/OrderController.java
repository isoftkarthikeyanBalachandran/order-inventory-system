/**
 * -----------------------------------------------------------
 * @Project     : Order & Inventory Microservices System
 * @Author      : Karthikeyan Balachandran
 * @Created On  : 09-Nov-2025
 * -----------------------------------------------------------
 */
package ai.presight.orderservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ai.presight.orderservice.model.OrderRequest;
import ai.presight.orderservice.model.OrderResponse;
import ai.presight.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j

public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@RequestBody OrderRequest request) {
        log.info("Received order placement request for {} items", request.getItems().size());
        OrderResponse response = orderService.placeOrder(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{orderNumber}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable String orderNumber) {
        log.info("Fetching order {}", orderNumber);
        OrderResponse response = orderService.getOrder(orderNumber);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{orderNumber}")
    public ResponseEntity<OrderResponse> updateOrder(@PathVariable String orderNumber,
                                                     @RequestBody OrderRequest request) {
        log.info(" Received update request for order {}", orderNumber);
        OrderResponse response = orderService.updateOrder(orderNumber, request);
        log.info(" Order updated successfully → {}", response.getOrderNumber());
        return ResponseEntity.ok(response);
    }
    
    
}
