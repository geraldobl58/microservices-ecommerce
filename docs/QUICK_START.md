# 🚀 Quick Start - Como Rodar Tudo

## ⚡ 3 Passos para Rodar Tudo

### PASSO 1: Subir os Bancos de Dados (1 comando)

```bash
cd /Users/geraldoluiz/Development/backend/microservices-ecommerce
docker-compose up -d mongodb order-db stock-db
```

### PASSO 2: Abrir 6 Terminais e Rodar Cada Serviço

Abra **6 abas de terminal** (uma para cada serviço). Execute em ordem:

#### Terminal 1️⃣ - Config Server
```bash
cd /Users/geraldoluiz/Development/backend/microservices-ecommerce/config-server
./mvnw spring-boot:run
```
Aguarde até ver:
```
Tomcat started on port 8088 (http)
```

#### Terminal 2️⃣ - Discovery Server (Eureka)
```bash
cd /Users/geraldoluiz/Development/backend/microservices-ecommerce/discovery-server
./mvnw spring-boot:run
```
Aguarde até ver:
```
Tomcat started on port 8761 (http)
```

#### Terminal 3️⃣ - Product Service
```bash
cd /Users/geraldoluiz/Development/backend/microservices-ecommerce/product-service
./mvnw spring-boot:run
```
Aguarde até ver:
```
Tomcat started on port 8081 (http)
```

#### Terminal 4️⃣ - Stock Service
```bash
cd /Users/geraldoluiz/Development/backend/microservices-ecommerce/stock-service
./mvnw spring-boot:run
```
Aguarde até ver:
```
Tomcat started on port 8082 (http)
```

#### Terminal 5️⃣ - Order Service
```bash
cd /Users/geraldoluiz/Development/backend/microservices-ecommerce/order-service
./mvnw spring-boot:run
```
Aguarde até ver:
```
Tomcat started on port 8083 (http)
```

#### Terminal 6️⃣ - Notification Service (opcional)
```bash
cd /Users/geraldoluiz/Development/backend/microservices-ecommerce/notification-service
./mvnw spring-boot:run
```

### PASSO 3: Acessar e Testar

**Eureka Dashboard (veja todos os serviços registrados):**
```
http://localhost:8761
```

**Testar serviços:**
```bash
# Product Service
curl http://localhost:8081/api/v1/products

# Stock Service
curl http://localhost:8082/api/v1/stock

# Order Service
curl http://localhost:8083/api/v1/orders
```

---

## 🔴 Algo Não Funcionou?

### Config Server não sobe
```
Error: Invalid config server configuration
```
**Solução:** Verifique `config-server/src/main/resources/application.yaml`:
```yaml
spring:
  profiles:
    active: native
  cloud:
    config:
      server:
        native:
          search-locations: file:../config-data
```

### Serviço não consegue conectar ao banco
```
Error: Failed to configure a DataSource
```
**Solução:** Verifique se:
1. Bancos estão rodando: `docker ps`
2. Arquivo `config-data/[service-name].yml` tem as credenciais corretas
3. Arquivo `[service-name]/src/main/resources/application.yaml` tem fallback

### Eureka não enxerga os serviços
```
Error: Unable to connect to Eureka
```
**Solução:** 
1. Verifique se Discovery Server está rodando (porta 8761)
2. Verifique se cada microservice tem:
   ```yaml
   spring:
     application:
       name: product-service  # Nome correto!
   ```

---

## 📊 Checklist de Sucesso

- [ ] Docker rodando: `docker ps` mostra 3 containers (mongodb, order-db, stock-db)
- [ ] Config Server: `curl http://localhost:8088/product-service/default` retorna JSON
- [ ] Eureka: `http://localhost:8761` abre no navegador
- [ ] 5+ serviços aparecem no dashboard do Eureka
- [ ] Consegui fazer `curl http://localhost:8081/api/v1/products`

---

## 📚 Ler Documentação Completa

Abra: `GUIA_ESTUDOS.md` neste mesmo diretório

---

## 🎯 Próximos Passos do Curso

1. Implementar chamadas entre microservices (order → stock)
2. Adicionar circuit breaker (resilience4j)
3. Implementar API Gateway
4. Adicionar cache com Redis
5. Implementar logging distribuído

---

**Última atualização:** 2026-07-23

