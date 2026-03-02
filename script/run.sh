#!/bin/bash
# Script to run the service in Docker container using docker-compose

set -e

echo " Starting Cart Service with Docker Compose..."

# Stop any running containers
docker-compose down

# Build and start services
docker-compose up --build -d

echo "Waiting for services to be healthy..."

# Wait for database to be ready
until docker-compose exec -T db pg_isready -U cartuser -d cartdb > /dev/null 2>&1; do
  echo "Waiting for PostgreSQL..."
  sleep 2
done

echo "PostgreSQL is ready!"

# Wait for the application to be healthy
MAX_RETRIES=30
RETRY_COUNT=0

while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
  if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "Cart Service is healthy and ready!"
    echo ""
    echo " Service Information:"
    echo "   - API: http://localhost:8080"
    echo "   - Health: http://localhost:8080/actuator/health"
    echo "   - Swagger UI: http://localhost:8080/swagger-ui.html"
    echo "   - Database: localhost:5432 (cartdb/cartuser)"
    echo ""
    echo " View logs with: docker-compose logs -f cart-service"
    echo " Stop services with: docker-compose down"
    exit 0
  fi

  RETRY_COUNT=$((RETRY_COUNT + 1))
  echo "  Waiting for Cart Service... ($RETRY_COUNT/$MAX_RETRIES)"
  sleep 2
done

echo " Cart Service failed to start in time. Check logs:"
echo "   docker-compose logs cart-service"
exit 1

