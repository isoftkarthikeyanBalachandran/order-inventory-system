package ai.presight.inventoryservice.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConfigVerifier {

    private final InventoryThresholdConfig thresholdConfig;

    @PostConstruct
    public void logConfigValue() {
        log.info("Loaded ConfigVerifier  Value → inventory.threshold = {}", thresholdConfig.getThreshold());
    }
}
