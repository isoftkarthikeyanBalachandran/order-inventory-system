/**
 * -----------------------------------------------------------
 * @Project     : Order & Inventory Microservices System
 * @Author      : Karthikeyan Balachandran
 * @Created On  : 09-Nov-2025
 * -----------------------------------------------------------
 */
	

package ai.presight.orderservice.service;

import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import ai.presight.orderservice.client.InventoryClient;
import ai.presight.orderservice.entity.OrderEntity;
import ai.presight.orderservice.entity.OrderItemEntity;
import ai.presight.orderservice.model.OrderItemResponse;
import ai.presight.orderservice.model.OrderRequest;
import ai.presight.orderservice.model.OrderResponse;
import ai.presight.orderservice.repository.OrderRepository;
import ai.presight.common.util.UtilFunction;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;

    // ============================================================
    // PLACE ORDER
    // ============================================================
    @Transactional
    @Retryable(
        retryFor = { RuntimeException.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000)
    )
    @CircuitBreaker(name = "inventoryService", fallbackMethod = "fallbackPlaceOrder")
    public OrderResponse placeOrder(OrderRequest request) {
        String orderNumber = UUID.randomUUID().toString();
        log.info("🟢 Placing multi-item order: {}", orderNumber);
        log.info("📦 Incoming request: {}", UtilFunction.objectToJSON(request));

        // extract Authorization header if available
        String token = null;
        try {
            var attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attr != null) {
                HttpServletRequest httpRequest = attr.getRequest();
                token = httpRequest.getHeader("Authorization");
            }
        } catch (Exception e) {
            log.warn(" Could not extract Authorization header: {}", e.getMessage());
        }

        OrderEntity order = new OrderEntity();
        order.setOrderNumber(orderNumber);
        order.setStatus("PENDING");

        double totalAmount = 0.0;

        // Validate and prepare items
        for (var itemReq : request.getItems()) {
            boolean available = inventoryClient.isInStock(itemReq.getSkuCode(), itemReq.getQuantity(), token);
            if (!available) {
                throw new RuntimeException("Insufficient stock for SKU: " + itemReq.getSkuCode());
            }

            double lineTotal = itemReq.getPrice() * itemReq.getQuantity();
            totalAmount += lineTotal;

            OrderItemEntity item = OrderItemEntity.builder()
                    .skuCode(itemReq.getSkuCode())
                    .quantity(itemReq.getQuantity())
                    .price(itemReq.getPrice())
                    .order(order)
                    .build();

            order.getItems().add(item);
        }

        order.setStatus("PLACED");
        orderRepository.save(order);
        log.info(" Order {} saved successfully. TotalAmount={}", orderNumber, totalAmount);

        // Deduct stock after successful save
        for (var itemReq : request.getItems()) {
            inventoryClient.deductStock(itemReq.getSkuCode(), itemReq.getQuantity(), token);
        }

        return OrderResponse.builder()
                .orderNumber(orderNumber)
                .message("Order placed successfully")
                .status("PLACED")
                .totalAmount(totalAmount)
                .build();
    }

    // ============================================================
    // GET ORDER
    // ============================================================
    public OrderResponse getOrder(String orderNumber) {
        var order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderNumber));

        double totalAmount = order.getItems().stream()
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum();

        var itemResponses = order.getItems().stream()
                .map(i -> OrderItemResponse.builder()
                        .skuCode(i.getSkuCode())
                        .quantity(i.getQuantity())
                        .price(i.getPrice())
                        .lineTotal(i.getPrice() * i.getQuantity())
                        .build())
                .toList();

        return OrderResponse.builder()
                .orderNumber(order.getOrderNumber())
                .message("Fetched successfully")
                .status(order.getStatus())
                .totalAmount(totalAmount)
                .items(itemResponses)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    // ============================================================
    // UPDATE ORDER
    // ============================================================
    @Transactional
    public OrderResponse updateOrder(String orderNumber, OrderRequest request) {
        log.info(" Updating order: {}", orderNumber);

        OrderEntity order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderNumber));

        // Apply updates only to provided fields
        if (request.getStatus() != null) {
            log.info("🔁 Updating status → {}", request.getStatus());
            order.setStatus(request.getStatus());
        }

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            log.info("🛒 Updating {} items", request.getItems().size());
            order.getItems().clear();

            request.getItems().forEach(itemReq -> {
                OrderItemEntity item = OrderItemEntity.builder()
                        .skuCode(itemReq.getSkuCode())
                        .quantity(itemReq.getQuantity())
                        .price(itemReq.getPrice())
                        .order(order)
                        .build();
                order.getItems().add(item);
            });
        }

        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
        log.info("💾 Order {} updated successfully in DB", order.getOrderNumber());

        double totalAmount = order.getItems().stream()
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum();

        var itemResponses = order.getItems().stream()
                .map(i -> OrderItemResponse.builder()
                        .skuCode(i.getSkuCode())
                        .quantity(i.getQuantity())
                        .price(i.getPrice())
                        .lineTotal(i.getPrice() * i.getQuantity())
                        .build())
                .toList();

        return OrderResponse.builder()
                .orderNumber(order.getOrderNumber())
                .message("Order updated successfully")
                .status(order.getStatus())
                .totalAmount(totalAmount)
                .items(itemResponses)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    // ============================================================
    // FALLBACK
    // ============================================================
    public OrderResponse fallbackPlaceOrder(OrderRequest request, Throwable ex) {
        log.error(" Inventory service unavailable, fallback triggered: {}", ex.getMessage());
        return OrderResponse.builder()
                .orderNumber("N/A")
                .status("FAILED")
                .message("Inventory temporarily unavailable. Please retry.")
                .build();
    }
}
