# Project Guide: Microservices E-Commerce Backend

> **Last updated:** MMMM DD, YYYY

---

## Project Overview

This is a **Spring Boot 4.1 microservices-based e-commerce backend** built with **Java 21**. The system follows a microservice architecture pattern, where each service is independently deployable, owns its own database, and communicates synchronously via HTTP. The project is not a monolithic build — each service is an independent Maven project with no parent POM aggregation.

### Key Technologies

| Technology | Version / Config |
|---|---|
| **Java** | 21 |
| **Spring Boot** | 4.1.0 |
| **Spring Cloud** | 2025.1.2 |
| **Maven** | Wrapper (mvnw) used per service |
| **Databases** | MongoDB 7, PostgreSQL 16, MySQL 8 |
| **Build** | Maven (per service) |
| **Containerization** | Docker Compose (databases only) |
| **Object Mapping** | MapStruct 1.6.3 + Lombok |
| **Service Discovery** | Netflix Eureka |
| **Config Management** | Spring Cloud Config Server (Git-backed) |
| **Inter-service HTTP** | Spring WebClient (reactive, `.block()`ed) / `@HttpExchange` interface |
| **Validation** | `spring-boot-starter-validation` (`@Valid`, `@NotBlank`, etc.) |

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    discovery-server                      │
│                    (Eureka, port 8761)                    │
└──────┬──────────────────────┬─────────────────────┬──────┘
       │                      │                     │
       ▼                      ▼                     ▼
┌──────────────┐   ┌──────────────────┐   ┌───────────────┐
│ product-svc  │   │   order-svc      │   │  stock-svc    │
│ (port 8081)  │   │   (port 8081*)   │   │  (port 8082)  │
│ MongoDB      │   │   PostgreSQL     │   │  MySQL        │
└──────────────┘   └────────┬─────────┘   └───────────────┘
                            │
                            │ (HTTP call to reduce stock)
                            ▼
                    ┌──────────────────┐
                    │  stock-service   │
                    │  (port 8082)     │
                    └──────────────────┘
```

\* *Order-service and product-service currently both default to port 8081 — see **Troubleshooting** section.*

---

## Getting Started

### Prerequisites

- **Java 21** (Eclipse Temurin recommended)
- **Docker Desktop** (for local databases)
- **IntelliJ IDEA** or another Java IDE (with Lombok plugin enabled)
- **Postman** / `curl` / `httpie` for API testing

### Installation

#### 1. Clone the repository

```bash
git clone <repository-url>
cd <repository-name>
```

#### 2. Start local databases

```bash
docker-compose up -d
```

This starts:
- **MongoDB** on port `27017` (user: `root`, password: `password`)
- **MySQL** on port `3306` (database: `stockdb`, user: `root`, password: `root`)
- **PostgreSQL** on port `5432` (database: `orderdb`, user: `admin`, password: `admin`)

> **Note:** The service containers are commented out in `docker-compose.yml` — services run locally via Maven.

#### 3. Start infrastructure services

Start the **discovery-server** first (Eureka must be available before other services — though services currently don't register with it):

```bash
cd discovery-server && ./mvnw spring-boot:run
```

Then start the **config-server** (only needed if stock-service uses it — currently `optional:configserver:`):

```bash
cd config-server && ./mvnw spring-boot:run
```

#### 4. Start business services

In separate terminal windows/tabs:

```bash
cd product-service && ./mvnw spring-boot:run   # port 8081
cd stock-service   && ./mvnw spring-boot:run   # port 8082
cd order-service   && ./mvnw spring-boot:run   # port 8081 (conflict! — see Troubleshooting)
cd notification-service && ./mvnw spring-boot:run  # stub, no config yet
```

### Basic Usage Examples

**Create a product (MongoDB):**

```bash
curl -X POST http://localhost:8081/api/v1/product \
  -H "Content-Type: application/json" \
  -d '{"name":"iPhone 16","description":"Latest iPhone","price":1299.99}'
```

**Get all products:**

```bash
curl http://localhost:8081/api/v1/product
```

**Check stock availability:**

```bash
curl "http://localhost:8082/api/v1/stock/{sku}?quantity=5"
```

**Create an order (triggers stock reduction):**

```bash
curl -X POST http://localhost:8081/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{"orderItems":[{"sku":"IPHONE-16","quantity":2}]}'
```

**Add stock:**

```bash
curl -X POST http://localhost:8082/api/v1/stock \
  -H "Content-Type: application/json" \
  -d '{"sku":"IPHONE-16","quantity":100}'
```

### Running Tests

```bash
# Run all tests for a service
cd <service-name> && ./mvnw test

# Run a single test class
cd <service-name> && ./mvnw test -Dtest=ClassName

# Build without tests
cd <service-name> && ./mvnw clean package -DskipTests

# Build and run tests
cd <service-name> && ./mvnw clean test
```

---

## Project Structure

### Directory Layout

```
.
├── docker-compose.yml              # Database containers (MongoDB, MySQL, PostgreSQL)
├── CLAUDE.md                       # Context for AI coding assistants
├── config-data/                    # Git-backed config files for Spring Cloud Config
│   ├── application.yml             # (empty — placeholder)
│   └── stock-service.yml           # Stock service config overrides
│
├── config-server/                  # Spring Cloud Config Server (port 8088)
├── discovery-server/               # Netflix Eureka Server (port 8761)
│
├── product-service/                # Product catalog (MongoDB, port 8081)
├── order-service/                  # Order management (PostgreSQL, port 8081)
├── stock-service/                  # Inventory/stock (MySQL, port 8082)
└── notification-service/           # Notification stub (no config yet)
```

### Microservice Internal Structure

Every business service follows the same layered architecture:

```
<service-name>/
├── pom.xml                      # Maven build (independent per service)
├── Dockerfile                   # Only product-service has one currently
├── src/
│   ├── main/
│   │   ├── java/com/...
│   │   │   ├── <Service>Application.java    # Entry point
│   │   │   ├── controller/                  # REST endpoints
│   │   │   ├── service/                     # Business logic interface
│   │   │   ├── service/impl/               # Implementation
│   │   │   ├── service/client/             # HTTP clients (order-service only)
│   │   │   ├── dto/                        # Request/Response DTOs
│   │   │   ├── mapper/                     # MapStruct mappers
│   │   │   ├── model/                      # JPA/MongoDB entities
│   │   │   ├── repository/                 # Spring Data repositories
│   │   │   ├── exception/                  # Global exception handler
│   │   │   ├── config/                     # Configuration classes
│   │   │   └── dataloader/                 # Test data seeding
│   │   └── resources/
│   │       └── application.yaml            # Service configuration
│   └── test/
└── .mvn/                                   # Maven wrapper
```

### Key Configuration Files

| File | Purpose |
|---|---|
| `docker-compose.yml` | Database infrastructure (MongoDB, MySQL, PostgreSQL) |
| `product-service/src/main/resources/application.yaml` | MongoDB connection, port 8081, virtual threads |
| `stock-service/src/main/resources/application.yaml` | MySQL config, port 8082, imports config-server |
| `order-service/src/main/resources/application.yaml` | PostgreSQL config, stock service URL, port 8081 |
| `config-data/stock-service.yml` | Externalized stock-service config via Spring Cloud Config |
| `discovery-server/src/main/resources/application.yaml` | Eureka standalone mode, port 8761 |

---

## Development Workflow

### Coding Standards & Conventions

1. **Service Naming:** REST endpoints follow pattern `/api/v1/<resource>` (e.g., `/api/v1/product`, `/api/v1/orders`, `/api/v1/stock`)
2. **Layer Separation:** Keep controller → service (interface) → service/impl ← repository — no cross-layer shortcuts
3. **DTOs:** Always use `*Request` and `*Response` DTOs; never expose entities directly
4. **Mappers:** Use MapStruct interfaces with `componentModel = "spring"`; annotate with `@Mapper(componentModel = "spring")`
5. **Lombok:** Use on all models (`@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`); `@RequiredArgsConstructor` on services; `@Slf4j` for logging
6. **Validation:** Use `jakarta.validation` annotations on request DTOs; `@Valid` on controller method parameters
7. **Error Handling:** Throw `ResourceNotFoundException` with meaningful messages; use `GlobalControllerAdvice` for consistent error responses
8. **Logging:** Use SLF4J via Lombok's `@Slf4j`; log entry/exit for important operations
9. **Transactions:** Use `@Transactional` on service methods; read-only operations get `@Transactional(readOnly = true)`
10. **Virtual Threads:** Enabled in every service via `spring.threads.virtual.enabled: true`

### Testing Approach

- **Spring Boot Test** with `@SpringBootTest` for integration tests
- Services use `spring-boot-starter-data-*-test` dependencies for database testing
- order-service uses `reactor-test` for WebClient testing
- No unit tests with mocking frameworks (Mockito) currently present — tests are integration-focused

### Build & Deployment

**Local Development:**
```bash
# 1. Start databases
docker-compose up -d

# 2. Build & run each service (in order)
cd discovery-server && ./mvnw spring-boot:run
cd config-server   && ./mvnw spring-boot:run
cd product-service && ./mvnw spring-boot:run
cd stock-service   && ./mvnw spring-boot:run
cd order-service   && ./mvnw spring-boot:run
```

**Docker Build (product-service example):**
```bash
cd product-service && docker build -t product-service .
```

**CI/CD Considerations:**
- Each service is independently buildable — no root POM
- Services can be built in parallel (except infrastructure dependencies)
- JARs output to `<service>/target/*.jar`

### Contribution Guidelines

1. Create a feature branch from `main`
2. Follow the established package structure
3. Ensure all tests pass: `./mvnw test`
4. Keep service boundaries clean — no shared domain models
5. Update documentation (this file) for structural changes
6. Be aware of the **port conflict** between order-service and product-service (both default to 8081)

---

## Key Concepts

### Domain-Specific Terminology

| Term | Definition |
|---|---|
| **SKU** | Stock Keeping Unit — unique identifier for a product variant (e.g., `IPHONE-16`) |
| **Order Number** | UUID generated at order creation to identify an order externally |
| **Stock Reduction** | Decrementing inventory when an order is placed (synchronous HTTP call) |

### Core Abstractions

- **`OrderRequest` / `OrderResponse`** — DTOs for order creation/retrieval; validated with `@Valid` and `@NotEmpty` on items
- **`ProductRequestDTO` / `ProductResponseDTO`** — DTOs for product CRUD
- **`StockRequest` / `StockResponse`** — DTOs for inventory management
- **`StockClient`** — HTTP interface (`@PutExchange`) for inter-service communication from order to stock
- **`ResourceNotFoundException`** — Standard 404 exception used across services
- **`GlobalControllerAdvice`** — `@RestControllerAdvice` for consistent exception-to-response mapping

### Design Patterns Used

| Pattern | Where Used |
|---|---|
| **Service Layer Interface + Impl** | Every business service (`OrderService` + `OrderServiceImpl`) |
| **DTO Pattern** | All request/response data transfer |
| **Mapper Pattern** | MapStruct converts between entities and DTOs |
| **Repository Pattern** | Spring Data repositories abstract database access |
| **Exception Handling** | `@RestControllerAdvice` for global error handling |
| **Synchronous HTTP Client** | Order service calls stock service via WebClient (blocking) |
| **Builder Pattern** | Lombok's `@Builder` on all entities |
| **Interface HTTP Client** | `StockClient` using Spring's `@HttpExchange` (replaces manual WebClient calls) |
| **Configuration Server** | Spring Cloud Config for externalized configuration |
| **Service Discovery** | Eureka server for registration/discovery (not fully wired yet) |

### Inter-Service Communication Flow (Order Creation)

```
1. POST /api/v1/orders  (OrderController)
2.   → OrderServiceImpl.createOrder()
3.     → For each order item:
4.       → StockClient.reduceStock(sku, quantity)  [HTTP PUT]
5.         → StockController.reduceStock()
6.           → StockServiceImpl.reduceStock()
7.     → Generate order number (UUID)
8.     → OrderRepository.save(order)
9.     → Return OrderResponse
```

---

## Common Tasks

### Add a new endpoint to a service

1. Add DTO class(es) in `dto/` (e.g., `MyNewRequest.java`, `MyNewResponse.java`)
2. Add method to repository in `repository/` (if new query needed)
3. Add method to service interface in `service/` and implement in `service/impl/`
4. Add mapping method to mapper in `mapper/`
5. Add endpoint method to controller in `controller/`

### Add a new microservice

1. Create the service directory with POM (`spring-boot-starter-parent 4.1.0`)
2. Create the application entry point class
3. Create `application.yaml` with service name and port
4. Add database credentials and connection details
5. Create the layered structure (`controller/`, `service/`, `dto/`, `model/`, `repository/`, `mapper/`, `exception/`)
6. Add a database service to `docker-compose.yml` if needed
7. Update this guide with the new service's port and purpose

### Add validation to a DTO

```java
// Add to the relevant field
@NotBlank(message = "SKU is required")
private String sku;

@NotNull(message = "Quantity is required")
@Min(value = 1, message = "Quantity must be at least 1")
private Integer quantity;
```

### Wire the StockClient (replace WebClient manually)

The `StockClient` interface with `@PutExchange` exists but the order service implementation currently uses it directly. To verify it's working:
- Check that `WebClientConfig` creates the proxy and exposes the bean
- Confirm `StockClient` is injected in `OrderServiceImpl` (currently `private final StockClient stockClient` is uncommented)

### Deploy a new Config Server configuration

Add a `<service-name>.yml` file in `config-data/` directory. The config server reads from this Git-backed directory. The stock service already uses this pattern.

---

## Troubleshooting

### Port Conflicts

**Issue:** `product-service` and `order-service` both default to port `8081`.

- To change a service's port at runtime, use `--server.port=<port>`:
  ```bash
  cd order-service && ./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8083
  ```
- Or update `server.port` in the service's `application.yaml`.

### MongoDB Connection Errors

**Issue:** Product service fails to connect to MongoDB.

- Ensure Docker is running: `docker ps`
- Ensure MongoDB container is up: `docker-compose up -d`
- Check credentials in `application.yaml` match `docker-compose.yml` (root/password)
- The `MongoConfig` class uses a custom `MongoClient` bean — verify the connection string format

### MySQL / PostgreSQL Connection Errors

- Verify the database containers are running: `docker ps`
- Check port mappings: `3306` (MySQL), `5432` (PostgreSQL)
- For stock-service, verify the `config-server` is running (or set `spring.cloud.config.enabled=false`)

### Stock Service Unavailable When Creating Orders

**Issue:** `IllegalStateException("Could not verify stock for SKU...")` on order creation.

- Verify stock-service is running on port 8082
- Check `stock.service.url` in `order-service/application.yaml` (default: `http://localhost:8082`)
- If using `StockClient` via `@HttpExchange`, the base URL is hardcoded in `WebClientConfig` — update if needed
- If the stock SKU doesn't exist, the service returns an error — add stock first via `POST /api/v1/stock`

### Package Naming Inconsistency

There is a **known typo inconsistency** across services:

| Package | Services |
|---|---|
| `com.ecomerce` (one 'm') | product-service, order-service, notification-service |
| `com.ecommerce` (two 'm's) | stock-service, discovery-server, config-server |

> **Do not "fix" this** without understanding the impact — all imports and annotations reference these package names.

### Spring Cloud Config Issues

- Stock-service imports config from `configserver:http://localhost:8088`
- If config-server is not running, the service will fail to start
- Use `optional:configserver:http://localhost:8088` (already configured) to make it non-blocking
- Config data is stored in the local `config-data/` directory (Git-backed)

---

## References

### Internal Documentation

- `CLAUDE.md` — Project summary for AI coding assistants
- `.continue/rules/CONTINUE.md` — This file

### External Resources

- [Spring Boot 4.1 Reference](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- [Spring Cloud Config](https://spring.io/projects/spring-cloud-config)
- [Spring Cloud Netflix Eureka](https://spring.io/projects/spring-cloud-netflix)
- [MapStruct 1.6.3](https://mapstruct.org/documentation/stable/reference/html/)
- [Lombok](https://projectlombok.org/features/)
- [MongoDB Spring Data](https://spring.io/projects/spring-data-mongodb)
- [Docker Compose](https://docs.docker.com/compose/)

### Working with Continue

Continue automatically loads this file into context when you open this project in VS Code or JetBrains with the Continue extension. You can reference sections by name when chatting with Continue (e.g., "What's the troubleshooting guide for port conflicts?").

To create additional component-specific documentation, add more `*.md` files in subdirectories. For example:

- `.continue/rules/order-service/CONTINUE.md`
- `.continue/rules/product-service/CONTINUE.md`
- `.continue/rules/inter-service-communication/CONTINUE.md`