# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot 4.1 microservices e-commerce backend using Java 21. Each service is an independent Maven project — there is no parent POM aggregating them.

## Services and Ports

| Service | Port | Database |
|---|---|---|
| discovery-server | 8761 | — (Eureka Server) |
| config-server | 8088 | — (Spring Cloud Config, native/filesystem) |
| api-gateway | 9000 | — (Spring Cloud Gateway WebFlux) |
| product-service | 8081 | MongoDB (port 27017, db: product-db) |
| order-service | 8083 | PostgreSQL (port 5432, db: orderdb) |
| stock-service | 8082 | MySQL (port 3306, db: stockdb) |
| notification-service | — | — (stub, no config yet) |

## Startup Order

Services must start in this order (each depends on the previous):

```
1. docker-compose up -d       # databases
2. discovery-server            # Eureka (8761)
3. config-server               # Config Server (8088) — optional; services use optional:configserver
4. product-service, order-service, stock-service   # any order
5. api-gateway                 # last; depends on all services being registered in Eureka
```

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

## Spring Cloud Config

Configuration is centralized in `config-data/` at the repo root. The config-server reads this directory with `spring.profiles.active: native` and serves it to all services.

**File layout:**
- `config-data/application.yml` — shared config for all services (Eureka URL, virtual threads, actuator)
- `config-data/<service-name>.yml` — per-service config (port, database, service-specific properties)

Each service has `spring.config.import: "optional:configserver:http://localhost:8088"` in its local `application.yaml`. The `optional:` prefix means services start normally without config-server, falling back to their local `application.yaml`.

**Config priority** (highest to lowest):
1. Environment variables (e.g. `SERVER_PORT`) — override everything
2. Config Server (`config-data/<name>.yml`)
3. Local `application.yaml`

> IntelliJ run configurations can have environment variables that silently override ports. Check **Run > Edit Configurations > Environment variables** if a service starts on an unexpected port.

## Architecture

Each service follows the same layered structure:
- `controller/` — REST controllers with `@RequestMapping("/api/v1/<resource>")`
- `service/` + `service/impl/` — interface + implementation pattern
- `dto/` — request/response DTOs
- `mapper/` — MapStruct mappers between model and DTOs
- `model/` — JPA/MongoDB domain entities
- `repository/` — Spring Data repositories
- `exception/` — `ResourceNotFoundException` + `GlobalControllerAdvice` for error handling

## API Gateway

All external requests go through the gateway at port 9000. Routes are defined in `GatewayConfig.java`:

| Gateway path | Target service |
|---|---|
| `/api/v1/product/**` | `lb://PRODUCT-SERVICE` |
| `/api/v1/orders/**` | `lb://ORDER-SERVICE` |
| `/api/v1/stock/**` | `lb://STOCK-SERVICE` |

**Spring Boot 4.x trailing slash:** Spring MVC 7 removed automatic trailing slash matching. The gateway has a `TrailingSlashFilter` (`GlobalFilter` with `HIGHEST_PRECEDENCE`) that strips trailing slashes from all incoming requests before routing, so controllers never receive paths ending in `/`.

## Inter-service Communication

Order-service calls stock-service via `StockClient` — a declarative HTTP interface using `@PutExchange`:

```
OrderServiceImpl → StockClient → PUT http://STOCK-SERVICE/api/v1/stock/reduce/{sku}?quantity={n}
```

Configured in `WebClientConfig.java`:
- `WebClient.Builder` bean with `@LoadBalanced` — Spring Cloud injects the `ReactorLoadBalancerExchangeFilterFunction`, resolving `STOCK-SERVICE` via Eureka
- `StockClient` bean created via `HttpServiceProxyFactory` wrapping the load-balanced `WebClient`

## Security

**CVE-2020-13956** (Apache HttpClient path URI bypass) — fixed in all services that use Spring Cloud Netflix Eureka. `org.apache.httpcomponents:httpclient` is pinned to `4.5.14` via `<dependencyManagement>` in each pom.xml.

## Key Libraries

- **Lombok** — used on all models and services (`@Data`, `@Builder`, `@RequiredArgsConstructor`, `@Slf4j`)
- **MapStruct 1.6.3** — annotation processor for DTO mapping; Lombok must be listed before MapStruct in annotation processor paths (product-service uses `lombok-mapstruct-binding` for ordering)
- **Spring Validation** — `@Valid` on controller request bodies; `@NotNull`/`@NotBlank` on DTOs
- **Virtual threads** — enabled globally in all services via `config-data/application.yml`
- **Spring Cloud 2025.1.2** — Eureka, Config Client, Gateway, LoadBalancer

## Package Naming Inconsistency

There is a typo inconsistency in base packages across services — some use `com.ecomerce` (one 'm') and others use `com.ecommerce` (two 'm's). Do not "fix" this without understanding which services are affected:

- `com.ecomerce.*` — product-service, order-service, notification-service
- `com.ecommerce.*` — stock-service, discovery-server, config-server, api-gateway
