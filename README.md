# 🧩 Order & Inventory Microservices System

## 📘 Overview
A highly available and scalable microservices architecture for managing orders and inventory, designed for deployment on Kubernetes.

---

## 🏗️ Architecture
**Modules**
- **Order Service** — Handles order creation, queries, and updates.
- **Inventory Service** — Manages products and low-stock warnings.
- **API Gateway** — Unified entry point and routing.
- **Eureka Server** — Service discovery.
- **Config Server** — Centralized configuration.
- **Kafka + Zookeeper** — Event communication.
- **PostgreSQL** — Persistent data store.
- **Prometheus + Grafana** — Monitoring and visualization.

---

## ⚙️ Tech Stack
Spring Boot 3.3, Spring Cloud 2023.0.x, PostgreSQL, Kafka, Docker, Kubernetes, Prometheus, Grafana.

---

## 🚀 Deployment Steps

```bash
kubectl create namespace order-system
kubectl apply -f k8s/postgres-db.yaml
kubectl apply -f k8s/zookeeper.yaml
kubectl apply -f k8s/kafka.yaml
kubectl apply -f k8s/config-server.yaml
kubectl apply -f k8s/eureka-server.yaml
kubectl apply -f k8s/inventory-service.yaml
kubectl apply -f k8s/order-service.yaml
kubectl apply -f k8s/api-gateway.yaml
