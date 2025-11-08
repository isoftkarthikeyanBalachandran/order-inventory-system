package ai.presight.orderservice.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ai.presight.orderservice.entity.OrderEntity;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    Optional<OrderEntity> findByOrderNumber(String orderNumber);
}
