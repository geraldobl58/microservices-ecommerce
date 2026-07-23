# 📋 Referência de Configurações

## Visão Geral dos Arquivos YAML

```
Há 2 TIPOS de configuração no projeto:

1️⃣ CONFIGURAÇÃO DISTRIBUÍDA (Config Server)
   └─ Pasta: config-data/
   └─ Lido por: Config Server
   └─ Propósito: Centralizar configurações de todos os serviços
   └─ Quando muda: Config Server distribui automaticamente

2️⃣ CONFIGURAÇÃO LOCAL (cada microservice)
   └─ Caminho: [service]/src/main/resources/application.yaml
   └─ Lido por: O próprio microservice
   └─ Propósito: Fallback quando Config Server não está disponível
   └─ Quando muda: Precisa reiniciar o serviço
```

---

## 📁 Arquivos Config Server

### `config-server/src/main/resources/application.yaml`

```yaml
spring:
  application:
    name: config-server                    # Nome único do serviço

  profiles:
    active: native                         # Ativa profile "native" para ler local

  cloud:
    config:
      server:
        native:
          # Onde está a pasta com as configurações dos microservices
          search-locations: file:../config-data
          # ../config-data = pasta config-data no RAIZ do projeto

server:
  port: 8088                               # Porta do Config Server
```

**Explicação:**
- `spring.profiles.active: native` → Diz que está no modo "development local" (lê de arquivos)
- `search-locations: file:../config-data` → Caminho relativo para onde Config Server busca os YAMLs
- `server.port: 8088` → Todos os microservices acessam Config Server nesta porta

---

## 📁 Config Data (Configurações dos Microservices)

### `config-data/product-service.yml`

```yaml
spring:
  # Virtual threads (Java 21) - mais eficiente que threads normais
  threads:
    virtual:
      enabled: true

  # Configuração do banco MongoDB
  data:
    mongodb:
      host: localhost            # Endereço do servidor MongoDB
      port: 27017               # Porta padrão do MongoDB
      database: product-db      # Nome do banco de dados
      username: root            # Usuário para conectar
      password: password        # Senha
      authentication-database: admin  # Banco onde a autenticação acontece

# Configuração do servidor web (Tomcat)
server:
  port: 8081                    # Porta que o serviço roda

# Configuração de logs
logging:
  level:
    root: INFO                  # Loglevel padrão (INFO, DEBUG, WARN, ERROR)
    com.eccomerce.product_service: INFO  # Loglevel específico do package

  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level %logger{36} - %msg%n"
```

**Cada propriedade:**

| Propriedade | Exemplo | Significado |
|-------------|---------|-------------|
| `spring.threads.virtual.enabled` | true | Usar virtual threads (mais rápido) |
| `spring.data.mongodb.host` | localhost | IP/hostname do MongoDB |
| `spring.data.mongodb.port` | 27017 | Porta do MongoDB |
| `spring.data.mongodb.database` | product-db | Nome do banco no MongoDB |
| `spring.data.mongodb.username` | root | Usuário MongoDB |
| `spring.data.mongodb.password` | password | Senha do usuário |
| `spring.data.mongodb.authentication-database` | admin | Database que guarda usuários |
| `server.port` | 8081 | Porta HTTP do microservice |
| `logging.level.root` | INFO | Quantidade de logs (DEBUG > INFO > WARN > ERROR) |

### `config-data/order-service.yml`

```yaml
spring:
  threads:
    virtual:
      enabled: true

  # Configuração do banco PostgreSQL (SQL)
  datasource:
    url: jdbc:postgresql://localhost:5432/orderdb    # Conexão JDBC
    username: admin
    password: admin
    driver-class-name: org.postgresql.Driver         # Driver PostgreSQL

  # Configuração do Hibernate/JPA
  jpa:
    hibernate:
      ddl-auto: update          # AUTO: update - cria/atualiza tabelas automaticamente
                               # Outras opções: create, validate, create-drop

    show-sql: true             # Mostra SQL executado no log (útil para debug)

    properties:
      hibernate:
        format_sql: true       # Formata SQL no log (deixa mais legível)
        dialect: org.hibernate.dialect.PostgreSQLDialect  # Dialeto SQL específico

server:
  port: 8083

# URL de outro microservice (para chamadas inter-serviço)
stock:
  service:
    url: http://localhost:8082   # Endereço do stock-service
```

**Cada propriedade:**

| Propriedade | Exemplo | Significado |
|-------------|---------|-------------|
| `spring.datasource.url` | jdbc:postgresql://localhost:5432/orderdb | String JDBC de conexão |
| `spring.datasource.username` | admin | Usuário do banco |
| `spring.datasource.password` | admin | Senha do banco |
| `spring.datasource.driver-class-name` | org.postgresql.Driver | Driver JDBC |
| `spring.jpa.hibernate.ddl-auto` | update | Criar/atualizar schema automaticamente |
| `spring.jpa.show-sql` | true | Mostrar SQL executado |
| `spring.jpa.properties.hibernate.dialect` | PostgreSQLDialect | Tipo de banco SQL |
| `stock.service.url` | http://localhost:8082 | Propriedade customizada |

### `config-data/stock-service.yml`

```yaml
spring:
  threads:
    virtual:
      enabled: true

  # Configuração do banco MySQL
  datasource:
    url: jdbc:mysql://localhost:3306/stockdb    # Conexão JDBC MySQL
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver  # Driver MySQL

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect

server:
  port: 8082
```

### `config-data/notification-service.yml`

```yaml
# Ainda em desenvolvimento - configuração mínima

spring:
  threads:
    virtual:
      enabled: true

server:
  port: 8085
```

---

## 📁 Configurações Locais (Fallback)

### `product-service/src/main/resources/application.yaml`

```yaml
spring:
  application:
    name: product-service                  # OBRIGATÓRIO - nome único

  # Tenta buscar config do Config Server (se não conseguir, continua)
  config:
    import: "optional:configserver:http://localhost:8088"

  # Configuração LOCAL de fallback (quando Config Server não funciona)
  threads:
    virtual:
      enabled: true

  data:
    mongodb:
      host: localhost
      port: 27017
      database: product-db
      username: root
      password: password
      authentication-database: admin

server:
  port: 8081
```

**Por que `optional:`?**
- `optional:configserver:` → Se não conseguir conectar, não falha
- `configserver:` → Se não conseguir conectar, falha na inicialização

### `order-service/src/main/resources/application.yaml`

```yaml
spring:
  application:
    name: order-service

  config:
    import: "optional:configserver:http://localhost:8088"

  # Fallback local
  threads:
    virtual:
      enabled: true

  datasource:
    url: jdbc:postgresql://localhost:5432/orderdb
    username: admin
    password: admin
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.PostgreSQLDialect

server:
  port: 8083

stock:
  service:
    url: http://localhost:8082
```

### `stock-service/src/main/resources/application.yaml`

```yaml
spring:
  application:
    name: stock-service

  config:
    import: "optional:configserver:http://localhost:8088"

  threads:
    virtual:
      enabled: true

  datasource:
    url: jdbc:mysql://localhost:3306/stockdb
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect

server:
  port: 8082
```

### `discovery-server/src/main/resources/application.yaml`

```yaml
spring:
  application:
    name: discovery-server

server:
  port: 8761

eureka:
  client:
    register-with-eureka: false    # Não se registra a si mesmo
    fetch-registry: false          # Não busca registry de si mesmo
```

### `notification-service/src/main/resources/application.yaml`

```yaml
spring:
  application:
    name: notification-service

  config:
    import: "optional:configserver:http://localhost:8088"

server:
  port: 8085
```

---

## 🔄 Ordem de Leitura de Propriedades

Quando um microservice inicia, ele lê propriedades nesta ORDEM:

```
1️⃣ ARGUMENTOS DA LINHA DE COMANDO (maior prioridade)
   Exemplo: ./mvnw spring-boot:run --server.port=9000

2️⃣ ARQUIVO: application.yaml LOCAL
   Caminho: [service]/src/main/resources/application.yaml

3️⃣ CONFIG SERVER (se disponível)
   URL: http://localhost:8088/[service-name]/default

4️⃣ VALORES PADRÃO DO SPRING BOOT
   (menor prioridade)
```

**Exemplo Prático:**

```yaml
# Suponha que você tenha:

# config-data/product-service.yml
server:
  port: 8081

# product-service/src/main/resources/application.yaml
server:
  port: 8081

# Linha de comando
./mvnw spring-boot:run --server.port=9000

# Qual porta será usada?
# ✓ RESPOSTA: 9000 (argumento CLI tem maior prioridade)
```

---

## 🗂️ Arquivo Docker Compose

### `docker-compose.yml`

```yaml
services:
  # MONGODB para Product Service
  mongodb:
    image: mongo:7.0.4
    container_name: mongodb
    restart: always
    ports:
      - "27017:27017"              # Porta host:porta container
    environment:
      - MONGO_INITDB_ROOT_USERNAME=root
      - MONGO_INITDB_ROOT_PASSWORD=password
    volumes:
      - mongo_data:/data/db         # Persistência de dados

  # MYSQL para Stock Service
  stock-db:
    image: mysql:8
    container_name: stock-db
    ports:
      - "3306:3306"
    environment:
      - MYSQL_ROOT_PASSWORD=root
      - MYSQL_DATABASE=stockdb
    volumes:
      - stock_db_data:/var/lib/mysql

  # POSTGRESQL para Order Service
  order-db:
    image: postgres:16-alpine
    container_name: order-db
    ports:
      - "5432:5432"
    environment:
      - POSTGRES_USER=admin
      - POSTGRES_PASSWORD=admin
      - POSTGRES_DB=orderdb
    volumes:
      - order_db_data:/var/lib/postgresql/data

volumes:
  mongo_data:
  stock_db_data:
  order_db_data:
```

**Explicação:**

| Campo | Significado |
|-------|-------------|
| `image` | Qual imagem Docker usar |
| `container_name` | Nome do container (para identificar) |
| `ports` | Mapear portas (host:container) |
| `environment` | Variáveis de ambiente do container |
| `volumes` | Persistência de dados entre container restarts |

---

## 🔍 Como Descobrir Qual Propriedade Precisa?

### Método 1: Documentação Oficial
```
spring-boot-starter-data-mongodb → https://docs.spring.io/spring-data/mongodb/
spring-boot-starter-data-jpa → https://docs.spring.io/spring-boot/docs/current/reference/
```

### Método 2: IDE (IntelliJ)
1. Abra `application.yaml` no IDE
2. Comece a digitar `spring.data.`
3. IDE autocompleta todas as propriedades disponíveis

### Método 3: Vendo o Erro
```
Error: Could not resolve placeholder 'spring.data.mongodb.host'
^
Isso significa: Propriedade spring.data.mongodb.host não foi encontrada

Solução: Adicione em application.yaml ou config-data/
spring:
  data:
    mongodb:
      host: localhost
```

---

## ✅ Checklist: Quando Adicionar Nova Configuração

Preciso adicionar suporte a Redis (cache):

**1. Adicionar dependência no `pom.xml`:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

**2. Adicionar em `config-data/[service].yml`:**
```yaml
spring:
  redis:
    host: localhost
    port: 6379
```

**3. Adicionar fallback em `[service]/src/main/resources/application.yaml`:**
```yaml
spring:
  redis:
    host: localhost
    port: 6379
```

**4. Adicionar serviço no `docker-compose.yml`:**
```yaml
services:
  redis:
    image: redis:7-alpine
    container_name: redis
    ports:
      - "6379:6379"
```

**5. Subir o container:**
```bash
docker-compose up -d redis
```

---

**Última atualização:** 2026-07-23

