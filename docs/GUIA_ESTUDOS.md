# 📚 Guia de Estudos - Arquitetura de Microservices com Spring Boot

## 📌 Índice
1. [Visão Geral da Arquitetura](#visão-geral-da-arquitetura)
2. [O que é Config Server](#o-que-é-config-server)
3. [O que é Service Discovery (Eureka)](#o-que-é-service-discovery-eureka)
4. [Estrutura de Configurações](#estrutura-de-configurações)
5. [Como Subir Tudo Localmente](#como-subir-tudo-localmente)
6. [Entendendo as Portas e Serviços](#entendendo-as-portas-e-serviços)
7. [Fluxo de Requisições](#fluxo-de-requisições)
8. [Erros Comuns e Soluções](#erros-comuns-e-soluções)
9. [Conceitos Importantes](#conceitos-importantes)

---

## 🏗️ Visão Geral da Arquitetura

### O que é um Microservice?
Um **microservice** é um serviço independente que executa uma função específica do negócio. Diferente de uma aplicação monolítica (tudo em um único projeto), os microservices são separados em pequenos projetos que se comunicam entre si.

```
┌─────────────────────────────────────────────────────────┐
│                    ARQUITETURA DO PROJETO               │
├─────────────────────────────────────────────────────────┤
│                                                           │
│  ┌──────────────────────────────────────────────────┐   │
│  │  CLIENTE (seu computador/navegador)              │   │
│  └────────────────────┬─────────────────────────────┘   │
│                       │ HTTP Requests                   │
│  ┌────────────────────▼─────────────────────────────┐   │
│  │ DISCOVERY SERVER (Eureka) - Porta 8761          │   │
│  │ Gerencia o registro de todos os serviços         │   │
│  └────────────────────┬─────────────────────────────┘   │
│                       │                                 │
│       ┌───────────────┼───────────────┐                │
│       │               │               │                │
│  ┌────▼────┐     ┌────▼────┐    ┌────▼────┐          │
│  │ CONFIG   │     │PRODUCT  │    │ STOCK   │          │
│  │ SERVER   │     │ SERVICE │    │ SERVICE │          │
│  │ 8088     │     │ 8081    │    │ 8082    │          │
│  └────┬────┘     └────┬────┘    └────┬────┘          │
│       │               │               │                │
│       │          ┌────▼────┐         │                │
│       │          │ORDER    │         │                │
│       │          │ SERVICE │         │                │
│       │          │ 8083    │         │                │
│       │          └────┬────┘         │                │
│       │               │               │                │
│  ┌────▼─────────────▼─────────────▼────┐             │
│  │  BANCOS DE DADOS                     │             │
│  │  ├─ MongoDB    (Porta 27017)         │             │
│  │  ├─ PostgreSQL (Porta 5432)          │             │
│  │  └─ MySQL      (Porta 3306)          │             │
│  └──────────────────────────────────────┘             │
│                                                           │
└─────────────────────────────────────────────────────────┘
```

### Serviços no Projeto

| Serviço | Porta | Banco | Função |
|---------|-------|-------|--------|
| **Config Server** | 8088 | Nenhum | Centraliza todas as configurações dos microservices |
| **Discovery Server (Eureka)** | 8761 | Nenhum | Registra todos os microservices (DNS dos serviços) |
| **Product Service** | 8081 | MongoDB | Gerencia produtos do e-commerce |
| **Stock Service** | 8082 | MySQL | Controla estoque dos produtos |
| **Order Service** | 8083 | PostgreSQL | Gerencia pedidos |
| **Notification Service** | 8085 | Nenhum | Envia notificações (em desenvolvimento) |

---

## 🔧 O que é Config Server?

### Por que precisa de Config Server?

Imagine que você tem 5 microservices rodando em produção. Se precisar mudar uma propriedade (ex: URL de um banco de dados), você teria que:

❌ **SEM Config Server:**
1. Parar cada serviço
2. Editar o arquivo de configuração
3. Recompilar
4. Fazer deploy novamente
5. Repetir 5 vezes

✅ **COM Config Server:**
1. Editar o arquivo de configuração centralizado
2. Todos os serviços já leem a nova configuração (sem restart)
3. Pronto!

### Como o Config Server funciona

```
FLUXO DE INICIALIZAÇÃO DE UM MICROSERVICE
═════════════════════════════════════════

┌─────────────────────────────┐
│ 1. Microservice inicia      │
│    (ex: product-service)    │
└────────────────┬────────────┘
                 │
                 ▼
┌─────────────────────────────────────────┐
│ 2. Lê o arquivo application.yaml        │
│    (busca por propriedades de config)   │
└────────────────┬────────────────────────┘
                 │
         ┌───────┴────────┐
         │                │
    ┌────▼────┐      ┌───▼──────┐
    │ Encontrar├─────►│ Não      │
    │ Config   │      │ existe?  │
    │ Server?  │      └───┬──────┘
    └────┬────┘           │
         │            ┌───▼─────────────┐
    ┌────▼────┐       │ Usa fallback    │
    │ Sim      │       │ local do        │
    │ conectar │       │ application.yaml│
    └────┬────┘       └────────┬────────┘
         │                     │
    ┌────▼────────────────────┐│
    │ Busca arquivo no        ││
    │ config-data/            ││
    │ (ex: product-service.yml)││
    │                         ││
    │ Carrega propriedades:   ││
    │ - spring.datasource.url ││
    │ - server.port           ││
    │ - logging.level         ││
    └────┬────────────────────┘│
         │                     │
         └──────┬──────────────┘
                │
         ┌──────▼──────────┐
         │ Microservice    │
         │ inicia com      │
         │ as propriedades │
         │ corretas! ✓     │
         └─────────────────┘
```

### Estrutura de Arquivos do Config Server

```
config-data/
├── application.yml              (configurações globais)
├── product-service.yml          (config do product-service)
├── order-service.yml            (config do order-service)
├── stock-service.yml            (config do stock-service)
└── notification-service.yml     (config do notification-service)
```

### Exemplo de arquivo: `config-data/product-service.yml`

```yaml
spring:
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

logging:
  level:
    root: INFO
    com.eccomerce.product_service: INFO
```

**O que significa cada propriedade:**
- `spring.threads.virtual.enabled: true` → Usa virtual threads (mais eficiente)
- `spring.data.mongodb.*` → Configurações do MongoDB
- `server.port: 8081` → Porta que o serviço roda
- `logging.level.*` → Quantidade de logs que aparecem no console

---

## 🔍 O que é Service Discovery (Eureka)?

### Por que precisa de Service Discovery?

Em microservices, um serviço precisa comunicar com outro. Mas como sabe qual é o endereço (IP:porta) do outro serviço?

```
PROBLEMA SEM SERVICE DISCOVERY
──────────────────────────────

order-service precisa chamar stock-service
Mas qual é o IP e porta do stock-service?

❌ Se fizesse hardcoded:
   url: http://192.168.1.5:8082

Problema: E se o stock-service for para outro servidor?
Código fica inválido!
```

### Como o Eureka funciona

```
FLUXO COM EUREKA (SERVICE DISCOVERY)
═══════════════════════════════════════

┌──────────────────────┐
│ DISCOVERY SERVER     │
│ (Eureka) - 8761      │
│                      │
│ Registro de serviços:│
│ ├─ product-service   │
│ │  URL: localhost:8081
│ ├─ stock-service     │
│ │  URL: localhost:8082
│ ├─ order-service     │
│ │  URL: localhost:8083
│ └─ ...               │
└──────────┬───────────┘
           │
     ┌─────┴─────┐
     │           │
┌────▼────┐ ┌───▼─────┐
│ Product │ │  Order  │
│ Service │ │ Service │
│ 8081    │ │ 8083    │
└────┬────┘ └───┬─────┘
     │         │
     │ 1. Preciso chamar
     │    stock-service
     │         │
     │    2. Pergunta ao Eureka:
     │       "Onde fica stock-service?"
     │         │
     └─────────┼──────────────┐
               │              │
         ┌─────▼────┐  ┌──────▼─────┐
         │ Eureka   │  │ Eureka     │
         │ responde │  │ retorna:   │
         │          │  │ localhost  │
         │          │  │ :8082      │
         └──────────┘  └──────┬─────┘
                              │
                         ┌────▼──────┐
                         │ Order      │
                         │ Service    │
                         │ conecta em │
                         │ localhost: │
                         │ 8082 ✓     │
                         └────────────┘
```

### O que acontece quando cada serviço sobe?

1. **Product Service** inicia
   - Se registra no Eureka: "Eu sou product-service, estou em localhost:8081"

2. **Stock Service** inicia
   - Se registra no Eureka: "Eu sou stock-service, estou em localhost:8082"

3. **Order Service** inicia
   - Se registra no Eureka: "Eu sou order-service, estou em localhost:8083"
   - Quando precisa chamar stock-service, consulta o Eureka e obtém o endereço

---

## 📋 Estrutura de Configurações

### Arquivos de Configuração

Cada microservice tem **3 camadas de configuração**:

```
CAMADAS DE CONFIGURAÇÃO (ordem de precedência)
═════════════════════════════════════════════════

┌────────────────────────────────────────┐  ◄─── MAIOR
│ 1. application.yaml                    │      PRIORIDADE
│ (arquivo local do microservice)         │
│ CAMINHO: product-service/src/main/     │
│ resources/application.yaml              │
└────────────────────────────────────────┘
            ▲
            │ sobrescreve
            ▼
┌────────────────────────────────────────┐
│ 2. Propriedades do Config Server       │
│ CAMINHO: config-data/product-service.  │
│ yml                                     │
│ (quando Config Server está disponível)  │
└────────────────────────────────────────┘
            ▲
            │ sobrescreve
            ▼
┌────────────────────────────────────────┐  ◄─── MENOR
│ 3. Valores padrão do Spring Boot       │      PRIORIDADE
│ (baked-in, nunca muda)                 │
└────────────────────────────────────────┘

RESUMO:
──────
- Se propriedade está em application.yaml → usa essa
- Se não está, mas existe em config-data/ → usa essa
- Se não existe em nenhum lugar → erro!
```

### Exemplo Prático

**Arquivo: `product-service/src/main/resources/application.yaml`**

```yaml
spring:
  application:
    name: product-service
  config:
    import: "optional:configserver:http://localhost:8088"
  # Fallback local (se Config Server não estiver disponível)
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

**Explicação de cada linha:**
- `spring.application.name` → Nome do serviço (importante para Eureka e Config Server)
- `spring.config.import: "optional:configserver:..."` → Tenta pegar config do servidor remoto (se falhar, continua mesmo assim por causa de "optional:")
- `spring.data.mongodb.*` → Configurações do banco Mongo **para usar quando Config Server não estiver disponível**
- `server.port: 8081` → Porta que o serviço roda

---

## 🚀 Como Subir Tudo Localmente

### Pré-requisitos

✅ Você tem instalado:
- Java 21
- Maven
- Docker (para rodar os bancos de dados)

### Passo 1: Subir os Bancos de Dados

Abra um terminal no **repositório raiz** e execute:

```bash
cd /Users/geraldoluiz/Development/backend/microservices-ecommerce

# Inicia os containers do Docker com os bancos
docker-compose up -d mongodb order-db stock-db
```

**Verificar que estão rodando:**
```bash
docker ps

# Deve listar os 3 containers:
# - mongodb (porta 27017)
# - order-db (postgres, porta 5432)
# - stock-db (mysql, porta 3306)
```

### Passo 2: Rodar Config Server

Abra uma aba de terminal e execute:

```bash
cd /Users/geraldoluiz/Development/backend/microservices-ecommerce/config-server

# Rodar o Config Server
./mvnw spring-boot:run
```

**Esperado:**
```
...
2026-07-23T16:46:42... INFO ... Starting ConfigServerApplication
2026-07-23T16:46:43... INFO ... Tomcat started on port 8088
```

**Testar que está funcionando:**
```bash
# Em outro terminal
curl http://localhost:8088/product-service/default

# Deve retornar JSON com as configurações do product-service
```

### Passo 3: Rodar Discovery Server (Eureka)

Abra outra aba de terminal:

```bash
cd /Users/geraldoluiz/Development/backend/microservices-ecommerce/discovery-server

./mvnw spring-boot:run
```

**Esperado:**
```
...
2026-07-23... INFO ... Tomcat started on port 8761
```

**Acessar no navegador:**
```
http://localhost:8761
```

Você verá um dashboard mostrando os serviços registrados.

### Passo 4: Rodar Microservices

Abra abas de terminal para cada um:

#### 4.1 Product Service

```bash
cd /Users/geraldoluiz/Development/backend/microservices-ecommerce/product-service
./mvnw spring-boot:run
```

#### 4.2 Stock Service

```bash
cd /Users/geraldoluiz/Development/backend/microservices-ecommerce/stock-service
./mvnw spring-boot:run
```

#### 4.3 Order Service

```bash
cd /Users/geraldoluiz/Development/backend/microservices-ecommerce/order-service
./mvnw spring-boot:run
```

#### 4.4 Notification Service (opcional)

```bash
cd /Users/geraldoluiz/Development/backend/microservices-ecommerce/notification-service
./mvnw spring-boot:run
```

### Resumo Visual

```
TERMINAL 1: Config Server (8088)
TERMINAL 2: Discovery Server (8761)
TERMINAL 3: Product Service (8081)
TERMINAL 4: Stock Service (8082)
TERMINAL 5: Order Service (8083)
TERMINAL 6: Notification Service (8085)
```

---

## 🔌 Entendendo as Portas e Serviços

### Qual é a função de cada porta?

| Porta | Serviço | O que faz | Necessário? |
|-------|---------|----------|-----------|
| 8088 | Config Server | Distribui configurações | ⚠️ Importante (funciona sem se necessário) |
| 8761 | Discovery Server (Eureka) | Registra serviços | ⚠️ Importante (para comunicação entre serviços) |
| 8081 | Product Service | API de produtos | ✅ Sim |
| 8082 | Stock Service | API de estoque | ✅ Sim |
| 8083 | Order Service | API de pedidos | ✅ Sim |
| 8085 | Notification Service | API de notificações | ⚠️ Opcional (em desenvolvimento) |

### Como testar cada um?

```bash
# Config Server está rodando?
curl http://localhost:8088/product-service/default

# Discovery Server está rodando?
curl http://localhost:8761/eureka/apps/product-service

# Product Service está rodando?
curl http://localhost:8081/api/v1/products

# Stock Service está rodando?
curl http://localhost:8082/api/v1/stock

# Order Service está rodando?
curl http://localhost:8083/api/v1/orders
```

---

## 📬 Fluxo de Requisições

### Exemplo: Criar um pedido (Order)

```
FLUXO COMPLETO DE UMA REQUISIÇÃO
═════════════════════════════════

1. Cliente faz requisição
   └─► POST http://localhost:8083/api/v1/orders
       Corpo: { productId: "123", quantity: 5 }

2. Order Service recebe
   └─► Valida os dados
   └─► Precisa verificar estoque
   
3. Order Service consulta Eureka
   └─► "Onde está o stock-service?"
   └─► Eureka responde: "localhost:8082"

4. Order Service chama Stock Service
   └─► PUT http://localhost:8082/api/v1/stock/reduce/SKU123?quantity=5

5. Stock Service verifica e reduz estoque
   └─► Se há estoque:
       ├─ Reduz quantidade
       └─ Retorna sucesso (200 OK)
   └─ Se não há estoque:
       └─ Retorna erro (400 Bad Request)

6. Order Service recebe resposta
   └─ Se sucesso:
      ├─ Cria o pedido
      ├─ Salva no banco (PostgreSQL)
      └─ Retorna ao cliente (201 Created)
   └─ Se erro:
      └─ Cancela operação
      └─ Retorna erro ao cliente

7. Cliente recebe resposta
   └─► { orderId: "456", status: "created" }

RESUMO:
──────
Client → Order Service (8083)
    ↓
Order Service → Eureka (8761)
    ↓ "Onde é stock-service?"
Eureka responde: "localhost:8082"
    ↓
Order Service → Stock Service (8082)
    ↓
Stock Service → Banco MySQL
    ↓
Stock Service → Order Service
    ↓
Order Service → Banco PostgreSQL
    ↓
Order Service → Client
```

---

## ⚠️ Erros Comuns e Soluções

### Erro 1: "Invalid config server configuration"

```
***************************
APPLICATION FAILED TO START
***************************

Description:
Invalid config server configuration.

Action:
If you are using the git profile, you need to set a Git URI...
```

**Causa:** Config Server não consegue determinar qual backend usar (nativo ou git)

**Solução:**
1. Abra `config-server/src/main/resources/application.yaml`
2. Certifique-se que tem:
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
3. Reconstrua: `./mvnw clean package`
4. Rodar novamente

### Erro 2: "Failed to configure a DataSource"

```
Failed to configure a DataSource: 'url' attribute is not specified 
and no embedded datasource could be configured.
```

**Causa:** Propriedades de banco de dados não foram encontradas (nem em Config Server, nem em application.yaml local)

**Solução:**
1. Verifique se o arquivo `config-data/order-service.yml` existe e tem:
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/orderdb
       username: admin
       password: admin
   ```
2. Verifique se `order-service/src/main/resources/application.yaml` tem o fallback:
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/orderdb
       username: admin
       password: admin
   ```

### Erro 3: "Could not resolve placeholder"

```
Could not resolve placeholder 'spring.data.mongodb.host' in value "${spring.data.mongodb.host}"
```

**Causa:** Código está tentando usar uma propriedade que não foi configurada em nenhum lugar

**Solução:**
1. Verifique se `config-data/product-service.yml` tem:
   ```yaml
   spring:
     data:
       mongodb:
         host: localhost
         port: 27017
         database: product-db
         username: root
         password: password
         authentication-database: admin
   ```
2. E `product-service/src/main/resources/application.yaml` tem o fallback
3. Reinicie o serviço

### Erro 4: "Connection refused" ao chamar outro serviço

```
java.net.ConnectException: Connection refused
```

**Causa:** 
- O serviço chamado não está rodando
- Eureka não consegue resolver o nome do serviço
- Firewall bloqueando porta

**Solução:**
1. Verifique que o serviço desejado está rodando:
   ```bash
   nc -z localhost 8082  # Stock Service
   # Deve responder: Connection succeeded
   ```
2. Verifique que Eureka está rodando:
   ```bash
   curl http://localhost:8761
   ```
3. Se o serviço está registrado:
   ```bash
   curl http://localhost:8761/eureka/apps/stock-service
   ```

---

## 💡 Conceitos Importantes

### Virtual Threads (Spring Boot 4.x)

```yaml
spring:
  threads:
    virtual:
      enabled: true
```

**O que são?**
- Threads muito leves que rodam na JVM
- Podem rodar milhões delas (em vez de centenas com threads normais)
- Ideal para aplicações com muito I/O (bancos de dados, chamadas HTTP)

**Analogia:**
```
Threads Normais: 1 atendente por cliente (caro!)
Virtual Threads: 1 atendente atende vários clientes na vez (eficiente!)
```

### Import "optional:" vs sem prefix

```yaml
# COM "optional:" - RECOMENDADO
spring:
  config:
    import: "optional:configserver:http://localhost:8088"

# Se Config Server não estiver disponível:
# ✓ Aplicação continua iniciando (usa fallback local)
# ✓ Melhor para desenvolvimento

# SEM "optional:"
spring:
  config:
    import: "configserver:http://localhost:8088"

# Se Config Server não estiver disponível:
# ✗ Aplicação falha na inicialização
# ✗ Melhor para produção (quando Config Server sempre existe)
```

### Profile "native" vs "git"

```yaml
# PROFILE NATIVE (para desenvolvimento local)
spring:
  profiles:
    active: native
  cloud:
    config:
      server:
        native:
          search-locations: file:../config-data

# Lê configurações de arquivos no diretório local (config-data/)
# ✓ Rápido
# ✓ Não precisa de Internet
# ✓ Ideal para local development


# PROFILE GIT (para produção)
spring:
  profiles:
    active: git
  cloud:
    config:
      server:
        git:
          uri: https://github.com/seu-repo/microservices-config.git
          username: seu-usuario
          password: seu-token

# Lê configurações de um repositório Git
# ✓ Centralizado
# ✓ Versionado
# ✓ Colaborativo
# ✗ Requer Internet e credenciais
```

### Estrutura de Pastas Importante

```
microservices-ecommerce/
│
├── config-server/                    ◄─── Config Server (distribui configs)
│   ├── src/main/resources/
│   │   └── application.yaml          ◄─── Config: usa native profile
│   └── pom.xml
│
├── discovery-server/                 ◄─── Eureka (Service Discovery)
│   ├── src/main/resources/
│   │   └── application.yaml          ◄─── Config: register-with-eureka: false
│   └── pom.xml
│
├── config-data/                      ◄─── PASTA MAIS IMPORTANTE
│   ├── application.yml               ◄─── Configs globais
│   ├── product-service.yml           ◄─── Configs do product-service
│   ├── order-service.yml             ◄─── Configs do order-service
│   ├── stock-service.yml             ◄─── Configs do stock-service
│   └── notification-service.yml      ◄─── Configs do notification-service
│
├── product-service/                  ◄─── Microservice 1
│   ├── src/main/resources/
│   │   └── application.yaml          ◄─── Config local + fallback
│   └── pom.xml
│
├── order-service/                    ◄─── Microservice 2
│   ├── src/main/resources/
│   │   └── application.yaml          ◄─── Config local + fallback
│   └── pom.xml
│
├── stock-service/                    ◄─── Microservice 3
│   ├── src/main/resources/
│   │   └── application.yaml          ◄─── Config local + fallback
│   └── pom.xml
│
├── notification-service/             ◄─── Microservice 4
│   ├── src/main/resources/
│   │   └── application.yaml          ◄─── Config local + fallback
│   └── pom.xml
│
└── docker-compose.yml                ◄─── Define os bancos de dados
```

### Qual arquivo editar para mudar uma configuração?

```
PRECISO MUDAR A PORTA DO PRODUCT SERVICE
═════════════════════════════════════════

OPÇÃO 1: Mudar em config-data/product-service.yml (RECOMENDADO)
server:
  port: 9000  # ← Mude aqui

Resultado: Quando Config Server distribui, todos pegam porta 9000
          (se application.yaml local não sobrescrever)

OPÇÃO 2: Mudar em product-service/src/main/resources/application.yaml
server:
  port: 9000  # ← Mude aqui

Resultado: Apenas esta instância usa porta 9000
          (quando Config Server não está disponível)

OPÇÃO 3: Mudar em tempo de execução
./mvnw spring-boot:run -Dspring-boot.run.arguments="--server.port=9000"

Resultado: Apenas esta execução usa porta 9000
          (não muda os arquivos)

ORDEM DE PRECEDÊNCIA:
────────────────────
Argumento CLI > application.yaml local > config-data/ > default

Então se você passar --server.port=9000 na linha de comando,
isso sobrescreve tudo! Útil para testes.
```

---

## 📖 Dicas para o Seu Curso Udemy

### Conceitos que você vai ver

✅ **Configuração Centralizada**
- Config Server distribui propriedades para todos os serviços
- Mudança de configuração sem restart (em alguns casos)

✅ **Service Discovery (Eureka)**
- Um serviço descobre outro automaticamente
- Registro e deregistro dinâmico

✅ **Load Balancing**
- Quando tem múltiplas instâncias de um serviço
- Requisições são distribuídas entre elas

✅ **Circuit Breaker (Resilience4j)**
- Se um serviço falha, não "quebra" o outro
- Tenta novamente ou usa fallback

✅ **API Gateway**
- Porta única para acessar todos os serviços
- Autenticação centralizada
- Rate limiting

### Exercícios Práticos Recomendados

1. **Mude a porta do product-service de 8081 para 9001**
   - Altere em `config-data/product-service.yml`
   - Reinicie
   - Verifique que a porta mudou

2. **Chame o product-service do order-service**
   - Use `@FeignClient` ou `WebClient`
   - Veja como Eureka resolve o nome automaticamente

3. **Desative o Config Server**
   - Parar o Config Server
   - Inicie um microservice
   - Veja que ele usa o fallback local (application.yaml)

4. **Registre um novo endpoint em um microservice**
   - Crie um novo controller
   - Veja ele registrado no Eureka
   - Acesse via http://localhost:8761

---

## 🔗 Recursos Úteis

### Documentação Oficial
- [Spring Cloud Config Server](https://spring.io/projects/spring-cloud-config)
- [Spring Cloud Netflix Eureka](https://spring.io/projects/spring-cloud-netflix)
- [Spring Boot Reference Guide](https://spring.io/projects/spring-boot)

### Seus Arquivos
- **Documentação desta arquitetura:** Este arquivo (GUIA_ESTUDOS.md)
- **Configurações gerais:** `config-data/`
- **Código dos serviços:** Cada pasta de microservice

---

## ✅ Checklist para Iniciante

- [ ] Entendi o que é um microservice
- [ ] Entendi a diferença entre Config Server e bancos de dados
- [ ] Entendi o que é Eureka (Service Discovery)
- [ ] Consegui subir tudo localmente
- [ ] Consegui acessar http://localhost:8761 (Eureka Dashboard)
- [ ] Consegui fazer uma requisição a cada microservice
- [ ] Entendi a ordem de precedência de configurações
- [ ] Consegui mudar uma propriedade e ver o efeito
- [ ] Entendi por que virtual threads são importantes
- [ ] Consegui debugar um erro de configuração

---

**Última atualização:** 2026-07-23
**Versão do Spring Boot:** 4.1.0
**Java:** 21
**Maven:** 3.x

```
Se tiver dúvidas, leia de novo a seção:
"Fluxo de Requisições" e "Erros Comuns e Soluções"
```

