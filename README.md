 
 [Order & Inventory Microservices System ] 

This project demonstrates a modular Spring Boot microservices architecture for managing orders and inventory.
It includes independent services for order processing, inventory management, authentication (IAM), service discovery, and routing.

## Requirement Given:

 Design a highly available and scalable microservices system to manage orders and
 inventory. The system should include the following modules:
    Order Service – Handles creating, querying, and updating orders.
    Inventory Service – Manages product inventory, handles stock deduction, and
    warning when inventory is low.
    API Gateway – Provides a unified entry point, request routing.
 Technical Requirements
    Implement services using Spring Boot and Spring Cloud .
    Inter-service communication via REST API.
    Implement data consistency between Order and Inventory.
    Use Docker for containerization, and deploy to Kubernetes using Helm Charts or
    YAML manifests.
    Configure ConfigMap for dynamic threshold of insufficient inventory and print
    warning logs when breached.
    Consider high concurrency and high availability in your design.
 Deliverables
    Complete project structure and source code (even if partial).
    Dockerfiles for all services and Kubernetes deployment manifests (or Helm
    chart).
    A deployment & architecture diagram


Prerequisites / Software Used:

Tool            | Version	 
Java JDK	    | 17 
Maven	        | 4.0.0
Docker	        | 28.5.1
Kubernetes      | v1.34.1	
PostgreSQL	    | PostgreSQL 16.10
Apache Kafka	| 3.7	
Spring Boot	    | 3.3.4
Spring Cloud	| 2023.x	

## Architecture Overview

![Architecture Diagram](architecture.png)

The system follows a **five-zone microservices architecture** for modularity, scalability, and observability.

## Client Layer
Represents Postman or UI clients invoking the REST endpoints.
Used mainly for testing and validating Order and Inventory APIs.
Communicates only through the API Gateway for authentication and routing.
API Gateway + IAM + Eureka
API Gateway handles all incoming requests and routes them to backend services.
IAM Service validates login credentials and issues JWT tokens.
Eureka Server enables service discovery between Order, Inventory, and Gateway.
Together, they provide unified routing, authentication, and dynamic service lookup.

## Core Microservices Layer
Contains Order Service and Inventory Service.
Handles order creation, updates, and stock management.
Inter-service communication is via REST APIs.
Uses Resilience4j Circuit Breaker for fault tolerance between services.
Ensures data consistency using transactional boundaries and retries.

## Common Services & Infrastructure
Includes PostgreSQL databases for Order and Inventory.
Kafka is used for publishing low-stock alert events.
Spring Cloud Config Server centralizes configuration management.
Common Library holds shared DTOs, utilities, and the JWT security filter.

## Deployment & Observability
All services are containerized with Docker.
Deployed to Kubernetes under the order-system namespace.
ConfigMaps and Secrets manage environment variables securely.
Basic observability is enabled via Spring Boot actuator endpoints and logs.




## Microservices Description

### **Order Service**
-Handles order creation, retrieval, and update operations.
-Calls Inventory Service REST APIs for stock validation and deduction.
-Uses Resilience4j (Circuit Breaker + Retry) to handle partial failures gracefully.
-Applies transactional boundaries at the service level (@Transactional + local commit).
-Persists data in OrderDB (PostgreSQL).

### **Inventory Service**
-Manages product catalog and real-time stock levels.
-Exposes REST endpoints under /api/v1/inventory for stock checks, updates, and threshold validation.
-Integrates with IAM Service via JWT validation using the shared common-lib security filter.
-Uses Spring Cloud ConfigMap for dynamic stock threshold values (LOW_STOCK_THRESHOLD).
-Logs warning messages when stock levels drop below configured thresholds.
-Publishes low-stock alert events asynchronously to Kafka (low-stock-topic) for monitoring and notification   systems.
-Applies Resilience4j Circuit Breaker for fault isolation on inter-service REST calls.
-Persists product and stock data in InventoryDB (PostgreSQL).
-Fully containerized and deployed under the Kubernetes namespace order-system.

### **IAM Service**
- Handles login (username/password) and JWT-based authentication.
- Integrates with API Gateway for user access control.

### **API Gateway**
- Central entry point for all clients.
- Routes to backend services and handles load balancing.
- Integrates with **Eureka Server** for service discovery.

### **Eureka Server**
- Registers all microservices dynamically.
- Enables API Gateway and other services to locate each other.

---

## DB Design


The database schema follows a normalized relational model split across two PostgreSQL databases — orderdb and inventorydb — ensuring modular data ownership per microservice.
-	This separation supports microservice data isolation, transactional integrity, and future polyglot persistence if scaling demands arise.
-	OrderDB manages order lifecycle and line items.
-	orders table stores high-level order metadata (number, status, timestamps).
-	order_items holds individual product details linked via order_id, maintaining one-to-many relationship.
-	Foreign key constraint (ON DELETE CASCADE) ensures cleanup of related items on order deletion.
-	Indexed by order_number and status for faster lookup.
-	InventoryDB manages product catalog and stock levels.
-	products table tracks SKU, name, price, and quantity with ACTIVE status by default.
-	Indexes on sku_code and status optimize frequent stock queries.
-	Designed for real-time updates from order placement and threshold alerts.



-- Create both databases
    CREATE DATABASE orderdb;
    CREATE DATABASE inventorydb;

    --------------------------------------------------------
    -- Connect to orderdb and create order-related tables
    --------------------------------------------------------
    \connect orderdb;

    CREATE TABLE orders (
        id BIGSERIAL PRIMARY KEY,
        order_number VARCHAR(50) UNIQUE NOT NULL,
        status VARCHAR(20) DEFAULT 'PLACED' NOT NULL,
        created_at TIMESTAMP DEFAULT NOW() NOT NULL,
        updated_at TIMESTAMP DEFAULT NOW(),
        version INT DEFAULT 0
    );

    CREATE INDEX idx_orders_number_lower ON orders (LOWER(order_number));
    CREATE INDEX idx_orders_status ON orders (status);

    CREATE TABLE order_items (
        id BIGSERIAL PRIMARY KEY,
        order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
        sku_code VARCHAR(50) NOT NULL,
        quantity INT NOT NULL CHECK (quantity > 0),
        price NUMERIC(10,2) NOT NULL CHECK (price > 0)
    );

    CREATE INDEX idx_order_items_sku ON order_items (sku_code);

    --------------------------------------------------------
    -- Connect to inventorydb and create product tables
    --------------------------------------------------------
    CREATE TABLE products (
        id BIGSERIAL PRIMARY KEY,
        sku_code VARCHAR(50) UNIQUE NOT NULL,
        name VARCHAR(150) NOT NULL,
        price NUMERIC(10,2) NOT NULL,
        quantity INT NOT NULL,
        status VARCHAR(20) DEFAULT 'ACTIVE',
        created_at TIMESTAMP DEFAULT NOW(),
        updated_at TIMESTAMP DEFAULT NOW()
    );

    CREATE INDEX idx_products_sku ON products (sku_code);
    CREATE INDEX idx_products_status ON products (status);


## Kafka Integration

The system includes a Kafka-based asynchronous event pipeline to handle low-stock notifications and potential future audit/event-sourcing features.

   **Topic Configuration
        -Defined in KafkaTopicConfig using TopicBuilder to auto-create the topic low-stock-topic
        -Each event topic can be configured with partition and replica counts for scalability.

    **Producer Integration
        -The InventoryService publishes messages to low-stock-topic whenever a product’s quantity drops below the configured threshold.
        -Uses KafkaTemplate<String, LowStockEvent> for non-blocking event publishing.

    **Event Structure
        -Payload contains SKU, current quantity, threshold, and timestamp.
        -Designed for extension (e.g., sending notifications to external systems, dashboards, or alerting tools).
        -Asynchronous Decoupling
        -Prevents direct synchronous coupling between Inventory and Order services.
        -Allows other consumers (e.g., Notification or Analytics microservices) to subscribe later without changing existing logic.

    **Future Enhancements
        -Add Kafka consumer microservice for alert handling and reporting.
        -Introduce schema registry (Avro/JSON Schema) for consistent message format.
        -Implement retry and dead-letter queue (DLQ) strategies for guaranteed delivery.

---
## Project Structure

![alt text](folderstructure.png)






## How to Run (Local & Kubernetes)

Added build.bat file [text](built.bat)

@echo off
setlocal enabledelayedexpansion

echo ==========================================
echo  Building & Deploying All Microservices
echo ===========================================

:: Step 1: Clean and build all projects
echo.
echo  Running Maven build...
call mvn clean package -DskipTests
if errorlevel 1 (
    echo  Maven build failed. Exiting.
    exit /b 1
)

:: Step 2: Build Docker images
echo.
echo  Building Docker images...
docker build -t eureka-server:latest ./eureka-server
docker build -t config-server:latest ./config-server
docker build -t api-gateway:latest ./api-gateway
docker build -t iam-service:latest ./iam-service
docker build -t order-service:latest ./order-service
docker build -t inventory-service:latest ./inventory-service

:: Step 3: Apply Kubernetes manifests
echo.
echo  Applying Kubernetes configurations...
kubectl apply -f k8s/common-lib/configmap.yml -n order-system
kubectl apply -f k8s/eureka-server/eureka-server.yml -n order-system
kubectl apply -f k8s/config-server/config-server.yml -n order-system
kubectl apply -f k8s/api-gateway/api-gateway.yml -n order-system
kubectl apply -f k8s/iam-service/iam-service.yml -n order-system
kubectl apply -f k8s/order-service/order-service.yml -n order-system
kubectl apply -f k8s/inventory-service/inventory-service.yml -n order-system
kubectl apply -f k8s/iam-service/configmap.yml -n order-system
kubectl apply -f k8s/iam-service/iam-jwt-secret.yml -n order-system
kubectl apply -f /config-server/inventory-configmap.yml -n order-system

:: Step 4: Rollout restart all deployments
echo.
echo  Restarting all deployments...
kubectl rollout restart deployment eureka-server -n order-system
kubectl rollout restart deployment config-server -n order-system
kubectl rollout restart deployment api-gateway -n order-system
kubectl rollout restart deployment iam-service -n order-system
kubectl rollout restart deployment order-service -n order-system
kubectl rollout restart deployment inventory-service -n order-system

:: Step 5: Verify
echo.
echo  Checking pod status...
kubectl get pods -n order-system

echo.
echo  Build & Deployment Complete!
pause







## Major Features implemented

- REST-based inter-service communication between Order and Inventory services through the API Gateway.(Microservices Pattern)
- JWT-based Authentication implemented via IAM Service and shared Common Library for token validation.
(Filter Pattern)
- Resilience4j (CircuitBreaker + Retry) ensures graceful recovery and fault tolerance during service failures.
(Circuit Breaker / Retry Pattern)
- Dynamic Configuration Management using Spring Cloud Config Server and Kubernetes ConfigMaps.
(Configuration Server Pattern)
- Kafka-based Low Stock Alerts asynchronously publish notifications when inventory levels fall below threshold.
(Event-Driven Pattern)
- Transactional Consistency ensured by Spring @Transactional within each service boundary for atomic DB updates.
(Transactional Boundary Pattern)
- PostgreSQL Integration for persistent data storage of orders and inventory with JPA entity mapping.
(Repository Pattern)
- Containerized Deployment using Docker, orchestrated on Kubernetes for scalability and resilience.
(Containerization / Deployment Pattern)
- Centralized Service Discovery via Eureka Server for dynamic registration and routing of services.
(Service Discovery Pattern)
- Health Monitoring and Observability using Spring Boot Actuator for health, readiness, and metrics endpoints.
(Observability Pattern)
- Common Shared Library for reusable JWT utilities, DTOs, logging, and exception handling across services.
(Shared Library / Reuse Pattern)


## Future Enhancements
- Introduce proper Saga / Outbox pattern for distributed transaction management across Order and Inventory services.
- Add Redis caching for frequently accessed inventory lookups to reduce DB load.
- Implement Horizontal Pod Autoscaler (HPA) in Kubernetes to automatically scale services based on CPU / memory utilization.
- Enable centralized observability stack using Prometheus, Grafana, and Loki for metrics, dashboards, and logs.
- Use Helm charts for versioned deployments, configuration overrides, and rollback support.
- Integrate CI/CD pipeline (GitHub Actions or Jenkins) for automated build, test, and deployment.
- Implement OpenTelemetry tracing to track request flow across microservices.
- Add API rate limiting and throttling at the API Gateway for request control.
- Introduce Canary and Blue-Green deployment strategies for safer production rollouts.
- Add audit and activity tracking for order and inventory changes using Kafka topics.
- Integrate security hardening (OAuth2 and role-based permissions).


**Evidence & Verification**

All functionalities were tested and verified against the original requirements.
Below are the supporting artifacts, linked for easy reference.

📂 Artifacts
🧩 Testing & Verification Document
 – Detailed validation steps with screenshots of API requests, Kafka events, ConfigMap threshold logs, and Kubernetes deployment proofs. Evidence.docx https://github.com/isoftkarthikeyanBalachandran/order-inventory-system/blob/main/Evidence.docx
 
📊 Requirement Response Sheet
 – Excel tracker mapping each requirement to implementation details and test evidence. Request and response.xlsx https://github.com/isoftkarthikeyanBalachandran/order-inventory-system/blob/main/Request%20and%20response.xlsx
 
🧱 Architecture Diagram
 – Visual overview of the five-zone microservices layout and inter-service communication. Architecture-Diagram.png https://github.com/isoftkarthikeyanBalachandran/order-inventory-system/blob/main/Architecture-Diagram.png
 
🗂️ Folder Structure Diagram
 – Organized module representation showing service separation and dependencies. image.png https://github.com/isoftkarthikeyanBalachandran/order-inventory-system/blob/main/image.png
 
🧰 Build & Deployment Script
 – Automated batch file to build all modules, create Docker images, and deploy to Kubernetes.  built.bat https://github.com/isoftkarthikeyanBalachandran/order-inventory-system/blob/main/built.bat
 
🗄️ Database Schema Script
 – SQL DDL for orderdb and inventorydb creation, with proper indexes and constraints.  DB-Design.sql https://github.com/isoftkarthikeyanBalachandran/order-inventory-system/blob/main/DB-Design.sql


## Requirement Compliance Summary

All requirements from the original project specification have been implemented and verified.
   ## Functional Modules

- Order Service: Handles order creation, retrieval, and updates with transactional consistency.
- Inventory Service: Manages product stock, validates thresholds, and issues low-stock alerts via Kafka.
- API Gateway: Provides a unified entry point and routes traffic to backend microservices.
- IAM Service: Secures all APIs using JWT-based authentication integrated through the Common Library.

    ## Technical Implementation

- Spring Boot 3.3 + Spring Cloud 2023.x used for all microservices.
- Inter-service communication: REST APIs secured with JWT.
- Resilience & Data Consistency: Implemented via @Transactional, Retry, and Resilience4j Circuit Breaker.
- Dynamic Configurations: Managed centrally using Spring Cloud Config Server and 

    ## Kubernetes ConfigMaps.

- Docker & Kubernetes: All services containerized and deployed in the order-system namespace.
- Service Discovery: Enabled using Eureka Server; supports horizontal scaling and high availability.
- Logging & Observability: Achieved through Spring Boot Actuator endpoints and Kubernetes logs.

Deliverables
✅ Complete microservice source code
✅ Dockerfiles for all services
✅ Kubernetes manifests for deployment
✅ Architecture and system context diagrams
✅ Configuration via ConfigMaps and Secrets

Result

All project requirements have been fully met — delivering a modular, scalable, and resilient Order & Inventory Microservices System designed for real-world enterprise deployment.





## Author
**Karthik Balachandran**  
