package ai.presight.orderservice.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import ai.presight.common.event.LowStockEvent;

@Component
@Slf4j
public class LowStockEventListener {

	@KafkaListener(topics = "low-stock-topic", groupId = "low-stock-consumer")
	public void handleLowStockEvent(LowStockEvent event) {
	    log.warn("⚠️ Low stock alert for SKU={} (remaining={})",
	             event.getSkuCode(), event.getRemainingQty());
	}
}
