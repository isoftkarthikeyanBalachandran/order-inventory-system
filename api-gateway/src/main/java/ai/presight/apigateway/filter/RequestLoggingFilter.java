/**
 * -----------------------------------------------------------
 * @Project     : Order & Inventory Microservices System
 * @Author      : Karthikeyan Balachandran
 * @Created On  : 10-Nov-2025
 * -----------------------------------------------------------
 */

package ai.presight.apigateway.filter;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Configuration
public class RequestLoggingFilter {
	 @Bean
	    public GlobalFilter logFilter() {
	        return (exchange, chain) -> {
	            ServerWebExchange request = exchange;
	            log.info("Any Incoming {} {}", request.getRequest().getMethod(), request.getRequest().getURI());
	            return chain.filter(exchange)
	                    .then(Mono.fromRunnable(() ->
	                            log.info("Completed RequestLoggingFilter {} {}", request.getResponse().getStatusCode(),
	                                    request.getRequest().getURI())));
	        };
	    }
	}