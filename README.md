# Cart Service (Purchase Cart Service)

Spring Boot service that exposes APIs to browse products and create purchase orders.

Orders use the **snapshot pattern**: product price/VAT are copied into `order_items` at order creation time, so historical orders remain correct even if product prices change later.

---

## Tech stack

- Java 21
- Spring Boot 4.x (WebMVC, Validation, Data JPA)
- PostgreSQL (runtime) + Flyway migrations
- H2 (unit/slice tests)
- OpenAPI/Swagger UI (springdoc)
- Docker + Docker Compose
- (Optional) Testcontainers (E2E integration tests)

---

## Architecture overview

- **Controllers**: REST endpoints, request validation, OpenAPI annotations.
- **Services**: business logic (pricing, VAT, duplicate checks, currency consistency).
- **Repositories**: Spring Data JPA.
- **Entities**: JPA entities for `products`, `orders`, `order_items` (order-item relationship is bidirectional).
- **Mappers**: map entities to DTOs (`ProductResponse`, `OrderResponse`, etc.).
- **Errors**: consistent `ApiErrorResponse` with optional field violations.

---

## Running locally (no Docker)

Prerequisites:
- Java 21
- Maven
- PostgreSQL running locally (or use Docker below)

Command:

    mvn clean spring-boot:run

Service:
- API base: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI: http://localhost:8080/api-docs
- Health: http://localhost:8080/actuator/health

---

## Running with Docker

Prerequisites:
- Docker + Docker Compose

Start the service + PostgreSQL:

    ./scripts/run.sh

Stop everything:

    docker-compose down

Logs:

    docker-compose logs -f cart-service

---

## Running tests

### Run tests locally

    mvn test

### Run tests in Docker container (as required by the assignment)

    ./scripts/tests.sh

### (Optional) Run integration tests with Testcontainers

If enabled, integration tests will start a real PostgreSQL container automatically:

    mvn test

(You can also isolate them via Maven profiles if you prefer.)

---

## API Summary

### Products

- GET /api/v1/products  
  Query params: page, size, sort (validated / clamped server-side)
- GET /api/v1/products/{id}
  Retrieves a single product by ID
### Orders

- POST /api/v1/orders  
  Creates an order from a list of product IDs + quantities
- GET /api/v1/orders/{orderId}  
  Retrieves a single order by ID

Notes:
- Duplicate products in the same order are rejected.
- Currency must be consistent across all order items.
- Totals are computed as sum of line net prices and line VAT amounts.

---

## Example usage

Get products (page 0, size 20):

    curl "http://localhost:8080/api/v1/products?page=0&size=20&sort=id,asc"

Get product by id:

    curl "http://localhost:8080/api/v1/products/1"

Create an order:

    curl -X POST "http://localhost:8080/api/v1/orders" \
      -H "Content-Type: application/json" \
      -d '{
        "items": [
          { "productId": 1, "quantity": 2 },
          { "productId": 3, "quantity": 2 }
        ]
      }'

Get an order by id:

    curl "http://localhost:8080/api/v1/orders/1"

---

## Data model

- products  
  Product catalog.
- orders  
  Order header (totals, currency, createdAt).
- order_items  
  Order lines with snapshot fields (productName, unitNetPrice, vatRate, etc.).

Flyway migrations are under:
- src/main/resources/db/migration

---

## Logging

Logs are emitted to console in JSON format with fields (in order):

1. timestamp
2. logLevel
3. className (logger)
4. message

This makes logs easy to parse in container environments.

---

## Docker files

- Dockerfile: multi-stage build to run the service
- docker-compose.yml: runs the service + PostgreSQL
- script/run.sh: wrapper script to run the service
- script/tests.sh: wrapper script to execute tests in Docker

---

## Potential evolutions:
- Add authentication/authorization (e.g. JWT, OAuth2)
- Implement order cancel endpoints
- Add pagination to list orders endpoint
- Cache product data in Redis for faster reads
- Add multiple currency support with exchange rates

## Author

Giovanni Troia
