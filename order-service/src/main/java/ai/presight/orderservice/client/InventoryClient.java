/**
 * -----------------------------------------------------------
 * @Project     : Order & Inventory Microservices System
 * @Author      : Karthikeyan Balachandran
 * @Created On  : 09-Nov-2025
 * -----------------------------------------------------------
 */
package ai.presight.orderservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ai.presight.common.config.FeignConfig;

@FeignClient(
	    name = "inventory-service",
	    path = "/api/v1/inventory",
	    configuration = FeignConfig.class
	)
	public interface InventoryClient {

	    @GetMapping("/check")
	    boolean isInStock(@RequestParam("sku") String skuCode,
	                      @RequestParam("qty") int qty,
	                      @RequestHeader(value = "Authorization", required = false) String authHeader);

	    @PostMapping("/deduct")
	    void deductStock(@RequestParam("skuCode") String skuCode,
	                     @RequestParam("qty") int qty,
	                     @RequestHeader(value = "Authorization", required = false) String authHeader);
	}
