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
echo 🐳 Building Docker images...
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
