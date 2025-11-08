package ai.presight.orderservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ai.presight.orderservice.entity.OrderItemEntity;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Long> {}
