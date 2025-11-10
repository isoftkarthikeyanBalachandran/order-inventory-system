/**
 * -----------------------------------------------------------
 * @Project     : Order & Inventory Microservices System
 * @Author      : Karthikeyan Balachandran
 * @Created On  : 10-Nov-2025
 * -----------------------------------------------------------
 */
package ai.presight.inventoryservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
@RefreshScope
@Component
public class InventoryThresholdConfig {

    @Value("${inventory.threshold:5}")
    private int threshold;

    /** Log once when bean initializes */
    @jakarta.annotation.PostConstruct
    public void logThresholdOnStart() {
        log.info("InventoryThresholdConfig initialized with value = {}", threshold);
    }

    /** Log automatically whenever ConfigMap refresh triggers a bean reload */
    @EventListener(org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent.class)
    public void onRefresh() {
        log.info("ConfigMap refreshed! New threshold value = {}", threshold);
    }
}
