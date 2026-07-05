#!/bin/bash
# Starts all services in the correct order: infra first, then business services.
# Logs go to logs/<service>.log
# Press Ctrl+C to stop all services cleanly.

PIDS=()

cleanup() {
    echo ""
    echo "Shutting down all services..."
    kill "${PIDS[@]}" 2>/dev/null
    wait "${PIDS[@]}" 2>/dev/null
    echo "All services stopped."
    exit 0
}
trap cleanup SIGINT SIGTERM

mkdir -p logs

echo "Building all modules..."
./mvnw -DskipTests clean install -q || { echo "Build failed. Aborting."; exit 1; }

echo "Starting Eureka Server (port 8761)..."
java -jar infra-eureka-server/target/infra-eureka-server-0.1.0-SNAPSHOT.jar > logs/eureka.log 2>&1 &
PIDS+=($!)

echo "Waiting for Eureka to be ready..."
until curl -s -o /dev/null -w "%{http_code}" http://localhost:8761/actuator/health | grep -q "200"; do sleep 2; done
echo "Eureka is up."

echo "Starting Config Server (port 8888)..."
java -jar infra-config-server/target/infra-config-server-0.1.0-SNAPSHOT.jar > logs/config-server.log 2>&1 &
PIDS+=($!)

echo "Waiting for Config Server to be ready..."
until curl -s -o /dev/null -w "%{http_code}" http://localhost:8888/application/default | grep -q "200"; do sleep 2; done
echo "Config Server is up."

echo "Starting business services..."
java -jar bc01-identity-access/target/bc01-identity-access-0.1.0-SNAPSHOT.jar   > logs/bc01.log 2>&1 &
PIDS+=($!)
java -jar bc02-fleet-management/target/bc02-fleet-management-0.1.0-SNAPSHOT.jar > logs/bc02.log 2>&1 &
PIDS+=($!)
java -jar bc03-booking/target/bc03-booking-0.1.0-SNAPSHOT.jar                   > logs/bc03.log 2>&1 &
PIDS+=($!)
java -jar bc04-payment/target/bc04-payment-0.1.0-SNAPSHOT.jar                   > logs/bc04.log 2>&1 &
PIDS+=($!)
java -jar bc05-rating/target/bc05-rating-0.1.0-SNAPSHOT.jar                     > logs/bc05.log 2>&1 &
PIDS+=($!)

echo "Starting API Gateway (port 8080)..."
java -jar infra-api-gateway/target/infra-api-gateway-0.1.0-SNAPSHOT.jar > logs/gateway.log 2>&1 &
PIDS+=($!)

echo ""
echo "========================================"
echo "  All services are up!"
echo "========================================"
echo ""
echo "INFRASTRUCTURE"
echo "  Eureka dashboard   http://localhost:8761"
echo "  Config Server      http://localhost:8888"
echo "  API Gateway        http://localhost:8080"
echo ""
echo "BC-01  Identity & Access    (port 8081)"
echo "  Home        http://localhost:8081/"
echo "  Login       http://localhost:8081/ui/login"
echo "  Register    http://localhost:8081/ui/register"
echo "  Dashboard   http://localhost:8081/ui/dashboard"
echo "  Swagger     http://localhost:8081/swagger-ui/index.html"
echo "  H2 console  http://localhost:8081/h2-console  (JDBC: jdbc:h2:mem:identity, user: sa)"
echo ""
echo "BC-02  Fleet Management     (port 8082)"
echo "  Vehicles    http://localhost:8082/list.html"
echo "  Add vehicle http://localhost:8082/add.html"
echo "  Search near http://localhost:8082/search.html"
echo "  Swagger     http://localhost:8082/swagger-ui/index.html"
echo "  H2 console  http://localhost:8082/h2-console  (JDBC: jdbc:h2:mem:fleet, user: sa)"
echo ""
echo "BC-03  Booking              (port 8083)"
echo "  Search      http://localhost:8083/ui/search"
echo "  Bookings    http://localhost:8083/ui/bookings"
echo "  Swagger     http://localhost:8083/swagger-ui/index.html"
echo "  H2 console  http://localhost:8083/h2-console  (JDBC: jdbc:h2:mem:booking, user: sa)"
echo ""
echo "BC-04  Payment              (port 8084)"
echo "  Payments    http://localhost:8084/payments"
echo "  New payment http://localhost:8084/payments/new"
echo "  Swagger     http://localhost:8084/swagger-ui/index.html"
echo "  H2 console  http://localhost:8084/h2-console  (JDBC: jdbc:h2:mem:payment, user: sa)"
echo ""
echo "BC-05  Rating               (port 8085)"
echo "  Ratings     http://localhost:8085/ratings"
echo "  Submit      http://localhost:8085/ratings/submit"
echo "  Swagger     http://localhost:8085/swagger-ui/index.html"
echo "  H2 console  http://localhost:8085/h2-console  (JDBC: jdbc:h2:mem:rating, user: sa)"
echo ""
echo "Logs are in the logs/ folder. Press Ctrl+C to stop everything."

wait
