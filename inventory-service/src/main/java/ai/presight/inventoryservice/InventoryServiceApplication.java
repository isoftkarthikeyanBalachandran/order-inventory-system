/**
 * -----------------------------------------------------------
 * @Project     : Order & Inventory Microservices System
 * @Author      : Karthikeyan Balachandran
 * @Created On  : 10-Nov-2025
 * -----------------------------------------------------------
 */

package ai.presight.inventoryservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication(scanBasePackages = "ai.presight")
@EntityScan(basePackages = "ai.presight.inventoryservice.model")
@EnableJpaRepositories(basePackages = "ai.presight.inventoryservice.repository")
@EnableDiscoveryClient
@Slf4j
public class InventoryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(InventoryServiceApplication.class, args);
	}
	
}
