# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot 4.1 microservices e-commerce backend using Java 21. Each service is an independent Maven project — there is no parent POM aggregating them.

## Services and Ports

| Service | Port | Database |
|---|---|---|
| discovery-server | 8761 | — (Eureka) |
| product-service | 8081 | MongoDB (port 27017) |
| order-service | 8081 | PostgreSQL (port 5432, db: orderdb) |
| stock-service | 8082 | MySQL (port 3306, db: stockdb) |
| notification-service | — | — (stub, no config yet) |

## Build and Run

All commands must be run from inside the individual service directory (no root-level build):

```bash
# Build a service (skip tests)
cd <service-name> && ./mvnw clean package -DskipTests

# Run tests for a service
cd <service-name> && ./mvnw test

# Run a single test class
cd <service-name> && ./mvnw test -Dtest=ClassName

# Start a service
cd <service-name> && ./mvnw spring-boot:run
```

## Infrastructure (Docker)

Start databases before running any service locally:

```bash
docker-compose up -d
```

This starts MongoDB, MySQL (stock-db), and PostgreSQL (order-db). The service containers are commented out — services are run locally via Maven.

## Architecture

Each service follows the same layered structure:
- `controller/` — REST controllers with `@RequestMapping("/api/v1/<resource>")`
- `service/` + `service/impl/` — interface + implementation pattern
- `dto/` — request/response DTOs
- `mapper/` — MapStruct mappers between model and DTOs
- `model/` — JPA/MongoDB domain entities
- `repository/` — Spring Data repositories
- `exception/` — `ResourceNotFoundException` + `GlobalControllerAdvice` for error handling

## Inter-service Communication

Order-service calls stock-service synchronously using `WebClient` (reactive, but `.block()`ed). The call goes to `PUT /api/v1/stock/reduce/{sku}?quantity={n}` on stock-service. The URL is configured via `stock.service.url` in `application.yaml` (defaults to `http://localhost:8082`).

A `StockClient` interface using `@PutExchange` exists in order-service (`service/client/StockClient.java`) but is not yet wired — the current implementation uses `WebClient.Builder` directly in `OrderServiceImpl`.

## Key Libraries

- **Lombok** — used on all models and services (`@Data`, `@Builder`, `@RequiredArgsConstructor`, `@Slf4j`)
- **MapStruct 1.6.3** — annotation processor for DTO mapping; Lombok must be listed before MapStruct in annotation processor paths (product-service uses `lombok-mapstruct-binding` for ordering)
- **Spring Validation** — `@Valid` on controller request bodies; `@NotNull`/`@NotBlank` on DTOs
- **Virtual threads** — enabled globally in all services (`spring.threads.virtual.enabled: true`)

## Package Naming Inconsistency

There is a typo inconsistency in base packages across services — some use `com.ecomerce` (one 'm') and others use `com.ecommerce` (two 'm's). Do not "fix" this without understanding which services are affected:

- `com.ecomerce.*` — product-service, order-service, notification-service
- `com.ecommerce.*` — stock-service, discovery-server
