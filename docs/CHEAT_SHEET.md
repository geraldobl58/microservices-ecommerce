# ⚡ CHEAT SHEET - Referência Rápida

> Imprima este arquivo! 🖨️

---

## 🚀 RODAR TUDO

```bash
# Terminal 1: Bancos
cd /Users/geraldoluiz/Development/backend/microservices-ecommerce
docker-compose up -d mongodb order-db stock-db

# Terminal 2: Config Server
cd config-server
./mvnw spring-boot:run

# Terminal 3: Discovery Server
cd ../discovery-server
./mvnw spring-boot:run

# Terminal 4: Product Service
cd ../product-service
./mvnw spring-boot:run

# Terminal 5: Stock Service
cd ../stock-service
./mvnw spring-boot:run

# Terminal 6: Order Service
cd ../order-service
./mvnw spring-boot:run
```

---

## 🔗 URLs

| Serviço | URL | Testa com |
|---------|-----|-----------|
| Eureka | http://localhost:8761 | Navegador |
| Config Server | http://localhost:8088/product-service/default | curl |
| Product API | http://localhost:8081/api/v1/products | curl |
| Stock API | http://localhost:8082/api/v1/stock | curl |
| Order API | http://localhost:8083/api/v1/orders | curl |

---

## 🧪 Testes Rápidos

```bash
# 1. Eureka Dashboard
open http://localhost:8761

# 2. Config Server serve product config?
curl http://localhost:8088/product-service/default

# 3. Product Service responde?
curl http://localhost:8081/api/v1/products

# 4. Todos os serviços estão em UP no Eureka?
curl http://localhost:8761/eureka/apps/

# 5. Checar porta em uso
lsof -iTCP:8081 -sTCP:LISTEN -Pn

# 6. Verificar containers Docker
docker ps

# 7. Ver logs de um container
docker logs mongodb
docker logs order-db
docker logs stock-db
```

---

## 📁 Arquivos Importantes

```
Editar configurações:
├─ config-data/product-service.yml
├─ config-data/order-service.yml
├─ config-data/stock-service.yml
└─ config-data/notification-service.yml

Fallback local (quando Config Server offline):
├─ product-service/src/main/resources/application.yaml
├─ order-service/src/main/resources/application.yaml
├─ stock-service/src/main/resources/application.yaml
└─ notification-service/src/main/resources/application.yaml

Configuração de repositório:
├─ config-server/src/main/resources/application.yaml
├─ discovery-server/src/main/resources/application.yaml
└─ docker-compose.yml
```

---

## ⚙️ Propriedades Importantes

### Config Server
```yaml
spring:
  profiles:
    active: native
  cloud:
    config:
      server:
        native:
          search-locations: file:../config-data

server:
  port: 8088
```

### Microservice (Exemplo: Product)
```yaml
spring:
  application:
    name: product-service
  config:
    import: "optional:configserver:http://localhost:8088"
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

### Microservice (Exemplo: Order)
```yaml
spring:
  application:
    name: order-service
  config:
    import: "optional:configserver:http://localhost:8088"
  datasource:
    url: jdbc:postgresql://localhost:5432/orderdb
    username: admin
    password: admin
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update

server:
  port: 8083

stock:
  service:
    url: http://localhost:8082
```

---

## 🔴 Erros Comuns & Fixes

| Erro | Causa | Fix |
|------|-------|-----|
| `Invalid config server configuration` | Profile nativo não ativo | Verifique `config-server/application.yaml` tem `profiles.active: native` |
| `Failed to configure DataSource` | Propriedades de banco faltam | Adicione em `config-data/[service].yml` e `application.yaml` local |
| `Could not resolve placeholder` | Propriedade não existe | Grep a propriedade em config-data, se não achar, adicione |
| `Connection refused` | Serviço não está rodando | Rode o serviço ou descubra qual porta está em uso |
| `Port already in use` | Porta ocupada | Kill o processo: `kill -9 [PID]` |

---

## 🛠️ Troubleshooting Rápido

```bash
# 1. Nenhuma resposta do serviço?
nc -z localhost 8081  # Mostra se porta está aberta
# Se "Connection refused" → serviço não está rodando

# 2. Config Server não distribui configs?
curl http://localhost:8088/product-service/default
# Deve retornar JSON com propertySources

# 3. Eureka não vê serviços?
curl http://localhost:8761/eureka/apps/
# Deve listar product-service, stock-service, etc.

# 4. Aplicação não sobe?
./mvnw spring-boot:run -Dspring-boot.run.arguments="--debug"
# Procure por ConfigPropertySourceLoader, DataSource, etc.

# 5. Porta em uso?
lsof -iTCP:8081 -sTCP:LISTEN
kill -9 [PID]

# 6. Docker containers com problema?
docker-compose down
docker-compose up -d
```

---

## 🎯 Portas por Serviço

```
Config Server        → 8088
Discovery (Eureka)   → 8761
Product Service      → 8081
Stock Service        → 8082
Order Service        → 8083
Notification Service → 8085

Bancos (Docker)
MongoDB              → 27017
PostgreSQL           → 5432
MySQL                → 3306
```

---

## 🔨 Operações Comuns

### Mudar porta de um serviço
```bash
# Edite config-data/product-service.yml
server:
  port: 9000  # mude aqui

# Ou via CLI
./mvnw spring-boot:run --server.port=9000
```

### Adicionar nova propriedade
```bash
# 1. Edite config-data/[service].yml
spring:
  nova-propriedade: valor

# 2. Edite [service]/src/main/resources/application.yaml (fallback)
spring:
  nova-propriedade: valor

# 3. Restart serviço
```

### Rodar com logs verbosos
```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--debug"
```

### Limpar cache Maven
```bash
./mvnw clean
```

### Recompilar sem testes
```bash
./mvnw clean package -DskipTests
```

---

## 📚 Documentação Completa

| Tempo | Documento | Conteúdo |
|-------|-----------|----------|
| 5 min | QUICK_START.md | Rodar tudo agora |
| 45 min | GUIA_ESTUDOS.md | Entender completo |
| 30 min | REFERENCIA_CONFIGURACOES.md | Consultar YAMLs |
| 20 min | TROUBLESHOOTING.md | Debugar problemas |
| 5 min | INDEX.md | Índice navegável |
| ⏱️ | CHEAT_SHEET.md | Este arquivo! |

---

## 🎓 Ordem de Aprendizado

1. **Dia 1:** QUICK_START.md (rodar tudo)
2. **Dia 1:** GUIA_ESTUDOS.md (entender)
3. **Dia 2:** REFERENCIA_CONFIGURACOES.md (estudar YAMLs)
4. **Dia 2+:** TROUBLESHOOTING.md (conforme precisa)

---

## ✨ Pro Tips

```bash
# 1. Salvar saída em arquivo de log
./mvnw spring-boot:run > service.log 2>&1 &

# 2. Rodar em background e pegar PID
./mvnw spring-boot:run &
BG_PID=$!
echo $BG_PID  # Salve para depois: kill $BG_PID

# 3. Ver última linha do log
tail -f service.log

# 4. Grep de erro rápido
grep "ERROR\|WARN" service.log

# 5. Matador de todos os Java
killall java
# ⚠️ Cuidado! Mata TUDO

# 6. Health check rápido
for i in 8761 8088 8081 8082 8083; do
  nc -z localhost $i && echo "Port $i UP" || echo "Port $i DOWN"
done
```

---

## 🖼️ Diagrama Mental

```
Client Request
      │
      ├─→ Product (8081)
      │   ├─ Lê Config (8088)
      │   ├─ MongoDB (27017)
      │   └─ Registra em Eureka (8761)
      │
      ├─→ Order (8083)
      │   ├─ Lê Config (8088)
      │   ├─ PostgreSQL (5432)
      │   ├─ Chama Stock via Eureka
      │   └─ Registra em Eureka (8761)
      │
      └─→ Stock (8082)
          ├─ Lê Config (8088)
          ├─ MySQL (3306)
          └─ Registra em Eureka (8761)
```

---

## 🎯 Se Estou Preso

**Cenário 1: "Config Server não sobe"**
```
→ Verifique: config-server/src/main/resources/application.yaml
→ Procure por: spring.profiles.active: native
→ Se não tem, ADICIONE
→ Reconstrua: ./mvnw clean package
→ Rode novamente
```

**Cenário 2: "Serviço não consegue conectar no banco"**
```
→ Verifique: Banco está em docker (docker ps)
→ Verifique: Propriedades de conexão em config-data/[service].yml
→ Verifique: Propriedades de fallback em [service]/application.yaml
→ Se não tem, COPIE de config-data/
```

**Cenário 3: "Algo não está respondendo"**
```
→ Teste: curl http://localhost:[PORTA]
→ Se "Connection refused" → serviço não rodando
→ Se timeout → servidor preso/lento
→ Se erro 500 → aplicação crashou
→ Veja logs na aba do terminal
```

**Cenário 4: "Eureka vazio"**
```
→ Aguarde 30s (Eureka leva tempo)
→ Verifique: Cada serviço tem spring.application.name
→ Verifique: Discovery Server está rodando (8761)
→ Refresh: http://localhost:8761 no navegador
```

---

## 🚀 Atalho para Rodar Tudo (Script)

```bash
#!/bin/bash

echo "1. Subindo bancos..."
cd /Users/geraldoluiz/Development/backend/microservices-ecommerce
docker-compose up -d

echo "2. Aguardando bancos ficarem prontos..."
sleep 5

echo "3. Subindo serviços em background..."
cd config-server && ./mvnw spring-boot:run > /tmp/config.log 2>&1 &
sleep 5

cd ../discovery-server && ./mvnw spring-boot:run > /tmp/discovery.log 2>&1 &
sleep 5

cd ../product-service && ./mvnw spring-boot:run > /tmp/product.log 2>&1 &
cd ../stock-service && ./mvnw spring-boot:run > /tmp/stock.log 2>&1 &
cd ../order-service && ./mvnw spring-boot:run > /tmp/order.log 2>&1 &

echo "Tudo rodando! Visite: http://localhost:8761"
echo ""
echo "Logs em: /tmp/[service].log"
```

Salve como `run_all.sh` e execute:
```bash
chmod +x run_all.sh
./run_all.sh
```

---

**Guarde este arquivo! 💾**

---

**Última atualização:** 2026-07-23

**Versão:** 1.0

**Útil para:** Desenvolvimento local + Estudando o curso Udemy

