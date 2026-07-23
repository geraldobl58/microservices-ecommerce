# 🔧 Troubleshooting & Debugging

## 🌳 Árvore de Decisão - Quando Algo Não Funciona

```
┌─ APLICAÇÃO NÃO SOBE ─────────────────────────────┐
│                                                    │
├─► Erro: "Invalid config server configuration"     │
│   └─► VER: Seção "Erro 1" abaixo                 │
│                                                    │
├─► Erro: "Could not resolve placeholder..."        │
│   └─► VER: Seção "Erro 3" abaixo                 │
│                                                    │
├─► Erro: "Failed to configure a DataSource..."     │
│   └─► VER: Seção "Erro 2" abaixo                 │
│                                                    │
├─► Erro: "Connection refused"                      │
│   └─► VER: Seção "Erro 4" abaixo                 │
│                                                    │
└─► Erro: "Port is already in use"                 │
    └─► VER: Seção "Erro 5" abaixo
```

---

## 🔴 Erro 1: Invalid Config Server Configuration

### Sintoma
```
***************************
APPLICATION FAILED TO START
***************************

Description:
Invalid config server configuration.

Action:
If you are using the git profile, you need to set a Git URI in your configuration.
```

### Causas Possíveis
1. ❌ Não está no profile "native"
2. ❌ Arquivo `application.yaml` não tem configuração de servidor
3. ❌ Path para `config-data` está errado

### Solução Step-by-Step

**Passo 1:** Abra `config-server/src/main/resources/application.yaml`

**Passo 2:** Verifique se tem:
```yaml
spring:
  profiles:
    active: native          # ◄─── OBRIGATÓRIO

  cloud:
    config:
      server:
        native:             # ◄─── OBRIGATÓRIO
          search-locations: file:../config-data  # ◄─── OBRIGATÓRIO
```

**Passo 3:** Se não tem, ADICIONE essas linhas

**Passo 4:** Salve o arquivo e reconstrua:
```bash
cd config-server
./mvnw clean package -DskipTests
./mvnw spring-boot:run
```

**Passo 5:** Verifique:
```bash
curl http://localhost:8088/product-service/default

# Deve retornar JSON com propriedades
```

### Verificação Extra

Cheque se o caminho está correto:
```bash
# Você DEVE estar na pasta config-server quando rodar
cd config-server

# Daqui, o caminho ../config-data aponta para:
# /Users/geraldoluiz/Development/backend/microservices-ecommerce/config-data

# Verifique que existe:
ls ../config-data

# Deve listar:
# product-service.yml
# order-service.yml
# stock-service.yml
# notification-service.yml
```

---

## 🔴 Erro 2: Failed to Configure a DataSource

### Sintoma
```
Failed to configure a DataSource: 'url' attribute is not specified 
and no embedded datasource could be configured.

Reason: Failed to determine a suitable driver class
```

### Causas Possíveis
1. ❌ Config Server está offline e não distribuiu as props do banco
2. ❌ Arquivo local (`application.yaml`) não tem propriedades do banco
3. ❌ Arquivo `config-data/[service].yml` não existe ou está incompleto
4. ❌ Propriedades têm typo (ex: `datasource` vs `dataSource`)

### Solução Step-by-Step

**Passo 1:** Identifique qual serviço está falhando
```
Procure no erro por qual classe está falhando:
ERROR ... order_service ... Application run failed
         ^^^^^^
         É o order-service que está falhando
```

**Passo 2:** Verifique o arquivo de config desse serviço:
```bash
# Para order-service, cheque:
cat config-data/order-service.yml

# Deve ter:
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/orderdb
    username: admin
    password: admin
```

**Passo 3:** Verifique o fallback local:
```bash
# Para order-service, cheque:
cat order-service/src/main/resources/application.yaml

# Deve ter (igual ou parecido):
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/orderdb
    username: admin
    password: admin
```

**Passo 4:** Se faltam propriedades, ADICIONE e salve

**Passo 5:** Reconstrua e rode:
```bash
cd order-service
./mvnw clean package -DskipTests
./mvnw spring-boot:run
```

### Debug: Qual Propriedade está Faltando?

Rode com debug ativado:
```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--debug"

# Procure por linhas como:
# PropertySourceLoader ... Loaded config for 'order-service'
# PropertySourceDescriptor ... source 'file:../config-data/order-service.yml'
```

---

## 🔴 Erro 3: Could Not Resolve Placeholder

### Sintoma
```
Could not resolve placeholder 'spring.data.mongodb.host' in value "${spring.data.mongodb.host}"

Caused by: java.lang.IllegalArgumentException: Could not resolve placeholder 'spring.data.mongodb.host'
```

### Causas Possíveis
1. ❌ A propriedade não está em nenhum lugar (nem em Config Server, nem local)
2. ❌ TYPO no nome da propriedade
3. ❌ Código Java está procurando propriedade que não foi definida

### Solução Step-by-Step

**Passo 1:** Copie o nome da propriedade que está faltando
```
Procure por: "spring.data.mongodb.host"
```

**Passo 2:** Procure em `config-data/product-service.yml`
```bash
grep "spring.data.mongodb.host" config-data/product-service.yml

# Se não encontrar, a propriedade não está lá
```

**Passo 3:** Adicione a propriedade em `config-data/product-service.yml`:
```yaml
spring:
  data:
    mongodb:
      host: localhost    # ◄─── ADICIONE ESSA LINHA
```

**Passo 4:** Adicione a mesma em `product-service/src/main/resources/application.yaml` (fallback):
```yaml
spring:
  data:
    mongodb:
      host: localhost    # ◄─── ADICIONE ESSA LINHA
```

**Passo 5:** Reconstrua e rode:
```bash
./mvnw clean package -DskipTests
./mvnw spring-boot:run
```

### Dica: Encontre o Código Java que Usa essa Propriedade

```bash
# Procure nos arquivos Java do projeto:
grep -r "spring.data.mongodb.host" product-service/src/

# Vai aparecer algo como:
# product-service/src/main/java/com/ecomerce/product_service/config/MongoConfig.java:16:
# @Value("${spring.data.mongodb.host}")
# private String host;

# Isso significa: O arquivo MongoConfig.java está procurando essa propriedade
```

---

## 🔴 Erro 4: Connection Refused

### Sintoma
```
java.net.ConnectException: Connection refused
java.io.IOException: Failed to connect to server
```

### Causas Possíveis (em ordem de probabilidade)
1. ❌ Serviço chamado não está rodando
2. ❌ Eureka não conseguiu resolver o nome
3. ❌ Firewall/porta bloqueada
4. ❌ Configuração de URL está errada

### Solução Step-by-Step

**Passo 1:** Identifique qual serviço está tentando conectar
```
Procure no stacktrace por:
"Failed to connect to: localhost:8082"
              ^^^^^^ serviço que quer conectar
```

**Passo 2:** Verifique se o serviço está rodando:
```bash
# Se quer conectar em stock-service (8082)
nc -z localhost 8082

# Se retornar "Connection succeeded" → está rodando
# Se retornar "Connection refused" → NÃO está rodando
```

**Passo 3:** Se não está rodando, rode:
```bash
cd stock-service
./mvnw spring-boot:run
```

**Passo 4:** Verifique se Eureka consegue encontrar:
```bash
curl http://localhost:8761/eureka/apps/stock-service

# Deve retornar JSON com informações do serviço
```

**Passo 5:** Se o serviço está em outro IP/porta:
```bash
# Edite o arquivo de configuração:
# config-data/order-service.yml

stock:
  service:
    url: http://seu-ip-correto:sua-porta
```

### Teste de Conectividade

```bash
# Teste direto com curl
curl http://localhost:8082/api/v1/stock

# Se retornar resposta (mesmo erro 404, 500, etc)
# → Serviço está rodando ✓

# Se "Connection refused"
# → Serviço NÃO está rodando ✗
```

---

## 🔴 Erro 5: Port Already in Use

### Sintoma
```
Port 8081 is already in use

Caused by: java.net.BindException: Address already in use
```

### Causas Possíveis
1. ❌ Serviço anteriormente iniciado ainda está rodando
2. ❌ Outro programa usando essa porta
3. ❌ Dois serviços com mesma porta configurada

### Solução Step-by-Step

**Passo 1:** Encontre qual processo está usando a porta:
```bash
# Para macOS/Linux:
lsof -iTCP:8081 -sTCP:LISTEN -Pn

# Deve mostrar algo como:
# COMMAND   PID        USER   FD   TYPE ... NODE NAME
# java    53325 geraldoluiz   73u  IPv6 ... TCP *:8081 (LISTEN)
```

**Passo 2:** Mate o processo:
```bash
kill 53325

# Se não morrer, force:
kill -9 53325
```

**Passo 3:** Aguarde 5 segundos e rode novamente:
```bash
./mvnw spring-boot:run
```

### Alternativa: Use Porta Diferente

```bash
# Rode em outra porta:
./mvnw spring-boot:run \
  -Dspring-boot.run.arguments="--server.port=9000"
```

---

## 🟡 Aviso: Services Não Aparecem em Eureka

### Sintoma
```
Acessa http://localhost:8761
Eureka está vazio ou não mostra alguns serviços
```

### Causas Possíveis
1. ⚠️ Serviço ainda está inicializando
2. ⚠️ Serviço falhou na inicialização (silenciosamente)
3. ⚠️ `spring.application.name` não está configurado
4. ⚠️ Discovery Server não está rodando

### Solução

**Passo 1:** Aguarde 30 segundos
```
Eureka leva tempo para registrar serviços (até 30s)
```

**Passo 2:** Verifique se Discovery Server está rodando:
```bash
curl http://localhost:8761

# Deve retornar HTML (página do Eureka)
```

**Passo 3:** Verifique se cada serviço tem `spring.application.name`:
```yaml
# Deve estar em TODOS os application.yaml:
spring:
  application:
    name: product-service  # Nome único!
```

**Passo 4:** Veja o log do serviço procurando por:
```
Registered instance ... with status UP
# ou
Heartbeat sent successfully
```

**Passo 5:** Se não registrou, reinicie:
```bash
# Matar processo
kill [PID]

# Aguardar 5s
sleep 5

# Rodar novamente
./mvnw spring-boot:run
```

---

## 🟢 Modo Debug - Como Rodar com Logs Detalhados

### Config Server com Debug
```bash
cd config-server
./mvnw spring-boot:run -Dspring-boot.run.arguments="--debug" 2>&1 | tee debug.log
```

### Microservice com Debug Específico
```bash
cd product-service
./mvnw spring-boot:run -Dspring-boot.run.arguments="--debug" \
  --logging.level.org.springframework=DEBUG \
  --logging.level.com.ecomerce=DEBUG
```

### Saída Esperada
```
...
2026-07-23 16:46:42... DEBUG ... Fetching config from server at: http://localhost:8088
2026-07-23 16:46:42... DEBUG ... Located environment...
2026-07-23 16:46:43... DEBUG ... HikariPool-1 - Starting...
2026-07-23 16:46:43... INFO  ... Tomcat started on port 8081
2026-07-23 16:46:44... INFO  ... Started ProductServiceApplication
```

---

## 📊 Checklist de Saúde

Rode esses comandos para verificar tudo:

```bash
#!/bin/bash

echo "1. Verificando Docker Containers..."
docker ps | grep -E "mongodb|order-db|stock-db"

echo -e "\n2. Testando Config Server..."
curl -s http://localhost:8088/product-service/default | grep -q "mongodb" && echo "✓ OK" || echo "✗ FALHA"

echo -e "\n3. Testando Eureka..."
curl -s http://localhost:8761 | grep -q "Eureka" && echo "✓ OK" || echo "✗ FALHA"

echo -e "\n4. Verificando serviços registrados..."
for service in product-service stock-service order-service; do
  curl -s http://localhost:8761/eureka/apps/$service | grep -q "UP" && echo "✓ $service" || echo "✗ $service"
done

echo -e "\n5. Testando portas..."
for port in 8761 8088 8081 8082 8083 8085; do
  nc -z localhost $port && echo "✓ Porta $port" || echo "✗ Porta $port"
done
```

Salve como `health-check.sh` e rode:
```bash
chmod +x health-check.sh
./health-check.sh
```

---

## 🔬 Inspecionando o Config Server

### Ver todos os serviços conhecidos:
```bash
curl http://localhost:8088/

# Retorna lista de endpoints disponíveis
```

### Ver configuração de um serviço específico:
```bash
curl http://localhost:8088/product-service/default

# Retorna JSON com todas as propriedades
```

### Ver em formato YAML:
```bash
curl -H "Accept: application/x-yaml" \
  http://localhost:8088/product-service/default
```

### Ver propriedades específicas:
```bash
curl http://localhost:8088/product-service/default | jq '.propertySources[0].source.server'

# Mostra apenas configurações do servidor (porta, etc)
```

---

## 🧩 Fluxo de Início de um Microservice (Com Diagrama)

```
MICROSERVICE INICIA
════════════════════

1. JVM inicia processo Java
   └─ Carrega classes compiladas

2. Spring Boot processa application.yaml
   └─ Encontra: spring.config.import: "optional:configserver:..."

3. Tenta conectar ao Config Server
   ├─ SUCESSO:
   │  └─ Obtém propSources[0].name = "file:../config-data/product-service.yml"
   │  └─ Carrega propriedades de lá
   │
   └─ FALHA (porque "optional:"):
      └─ Pula para propriedades locais
      └─ Usa application.yaml local como fallback

4. Spring Data resolve datasources/bancos
   └─ MongoDB: Cria conexão com localhost:27017

5. Spring registra ApplicationContext
   └─ Cria beans (componentes da aplicação)

6. Tomcat inicia e abre porta 8081
   └─ Começa a escutar requisições HTTP

7. Microservice envia heartbeat para Eureka
   ├─ "Eu sou product-service"
   ├─ "Estou em http://localhost:8081"
   ├─ "Meu status é UP"
   └─ Eureka registra

8. Aplicação pronta!
   └─ Pode receber requisições
```

---

**Última atualização:** 2026-07-23

**Se nada acima funcionou:**
1. Veja todos os logs (copy-paste completo)
2. Abra issue no curso Udemy
3. Procure pela mensagem de erro completa neste arquivo

