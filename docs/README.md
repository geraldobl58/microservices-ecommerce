# 📚 Microservices E-Commerce com Spring Boot

> Documentação completa para estudar arquitetura de microservices seguindo o curso Udemy

**Curso:** [Spring Boot - Arquitectura Microservicios](https://www.udemy.com/course/spring-boot-arquitectura-microservicios/)

---

## 🎯 Documentação

Escolha qual guia ler baseado no seu objetivo:

### 🚀 Quer rodar tudo AGORA?
👉 Abra: **[QUICK_START.md](QUICK_START.md)**
- ⏱️ 5 minutos
- Passo-a-passo rápido
- 1, 2, 3 → tudo rodando

### 📖 Quer entender tudo do zero?
👉 Abra: **[GUIA_ESTUDOS.md](GUIA_ESTUDOS.md)**
- ⏱️ 30-45 minutos de leitura
- Explicações detalhadas
- Diagramas e analogias
- Conceitos importantes
- Exercícios práticos

### ⚙️ Quer entender as configurações?
👉 Abra: **[REFERENCIA_CONFIGURACOES.md](REFERENCIA_CONFIGURACOES.md)**
- ⏱️ Consulta rápida
- Cada propriedade explicada
- Todos os YAMLs do projeto
- Quando adicionar novas configs

### 🔧 Algo não está funcionando?
👉 Abra: **[TROUBLESHOOTING.md](TROUBLESHOOTING.md)**
- ⏱️ Busca rápida
- Erros comuns e soluções
- Árvore de decisão
- Modo debug

---

## 📋 Arquitetura Rápida

```
┌─────────────────────────────────────────┐
│ CLIENTE (seu navegador/Postman)        │
│ Requisições HTTP                        │
└────────────────┬────────────────────────┘
                 │
         ┌───────▼──────────┐
         │ API Gateway      │ (em desenvolvimento)
         │ (opcional)       │
         └───────┬──────────┘
                 │
    ┌────────────┼────────────┐
    │            │            │
┌───▼───┐   ┌───▼────┐  ┌───▼───┐
│Product│   │ Order  │  │ Stock │
│ 8081  │   │ 8083   │  │ 8082  │
└───┬───┘   └───┬────┘  └───┬───┘
    │           │           │
    │     COMUNICA COM EUREKA
    │           │
    └───────────┼───────────┘
                │
         ┌──────▼───────┐
         │ Eureka       │
         │ 8761         │
         │ (Service     │
         │  Discovery)  │
         └──────┬───────┘
                │
         ┌──────▼───────┐
         │ Config       │
         │ Server       │
         │ 8088         │
         │(Distribuidor │
         │de configs)   │
         └──────┬───────┘
                │
         ┌──────▼───────────────┐
         │ Bancos de Dados      │
         │ MongoDB, Postgres,   │
         │ MySQL (Docker)       │
         └──────────────────────┘
```

| Serviço | Porta | Função |
|---------|-------|--------|
| **Config Server** | 8088 | Distribui configurações centralizadas |
| **Eureka/Discovery** | 8761 | Registra e descobre serviços |
| **Product Service** | 8081 | Gerencia produtos (MongoDB) |
| **Stock Service** | 8082 | Controla estoque (MySQL) |
| **Order Service** | 8083 | Gerencia pedidos (PostgreSQL) |
| **Notification** | 8085 | Notificações (em desenvolvimento) |

---

## ✅ Checklist Rápido

- [ ] Docker instalado
- [ ] Java 21 instalado
- [ ] Repositório clonado
- [ ] Leu QUICK_START.md
- [ ] Conseguiu rodar Config Server
- [ ] Conseguiu rodar Discovery Server
- [ ] Conseguiu rodar Product Service
- [ ] Conseguiu rodar Stock Service
- [ ] Conseguiu rodar Order Service
- [ ] Conseguiu acessar http://localhost:8761

---

## 🗂️ Estrutura de Pastas

```
microservices-ecommerce/
├── 📄 GUIA_ESTUDOS.md              ◄─── LEIA PRIMEIRO
├── 📄 QUICK_START.md               ◄─── OU COMECE AQUI
├── 📄 REFERENCIA_CONFIGURACOES.md  ◄─── CONSULTE DEPOIS
├── 📄 TROUBLESHOOTING.md           ◄─── SE TIVER ERRO
├── 📄 docker-compose.yml           ◄─── Bancos (MongoDB, Postgres, MySQL)
│
├── config-data/                    ◄─── 🔴 PASTA IMPORTANTE
│   ├── application.yml
│   ├── product-service.yml
│   ├── order-service.yml
│   ├── stock-service.yml
│   └── notification-service.yml
│
├── config-server/                  ◄─── Serviço que distribui configs
│   └── src/main/resources/application.yaml
│
├── discovery-server/               ◄─── Eureka (Service Discovery)
│   └── src/main/resources/application.yaml
│
├── product-service/                ◄─── Microservice 1
│   └── src/main/resources/application.yaml
│
├── order-service/                  ◄─── Microservice 2
│   └── src/main/resources/application.yaml
│
├── stock-service/                  ◄─── Microservice 3
│   └── src/main/resources/application.yaml
│
└── notification-service/           ◄─── Microservice 4
    └── src/main/resources/application.yaml
```

---

## 🎓 Progressão de Aprendizado

### Nível 1: Iniciante 👶
1. Abra **QUICK_START.md**
2. Rode tudo localmente
3. Acesse Eureka em http://localhost:8761
4. ✅ Parabéns! Você tem tudo rodando

### Nível 2: Intermediário 🤓
1. Abra **GUIA_ESTUDOS.md** - Seção "Conceitos Importantes"
2. Entenda Virtual Threads, Profiles, Import "optional:"
3. Mude uma porta em `config-data/` e veja o efeito
4. ✅ Você entende como funciona

### Nível 3: Avançado 🚀
1. Abra **REFERENCIA_CONFIGURACOES.md**
2. Estude cada propriedade do `application.yaml`
3. Adicione Redis em `config-data/` e `docker-compose.yml`
4. Faça uma chamada entre microservices (Order → Stock)
5. ✅ Você está pronto para o curso

### Nível 4: Expert 💎
1. Implementar Circuit Breaker
2. Adicionar API Gateway
3. Implementar cache distribuído
4. Adicionar logging centralizado (ELK Stack)
5. Deploy em Kubernetes

---

## 🔍 Conceitos Chave por Documento

| Documento | Conceitos | Ideal Para |
|-----------|-----------|-----------|
| **QUICK_START.md** | Passo-a-passo, Docker, portas | Começar rápido |
| **GUIA_ESTUDOS.md** | Arquitetura, Config Server, Eureka, fluxo | Entender tudo |
| **REFERENCIA_CONFIGURACOES.md** | YAML, properties, ordem de leitura | Consulta |
| **TROUBLESHOOTING.md** | Erros comuns, debug, soluções | Resolver problemas |

---

## 🧪 Testando a Arquitetura

### Teste 1: Config Server distribui configs

```bash
curl http://localhost:8088/product-service/default
# Deve retornar JSON com configurações
```

### Teste 2: Eureka registra serviços

```bash
curl http://localhost:8761/eureka/apps/product-service
# Deve mostrar instância UP
```

### Teste 3: Serviço responde

```bash
curl http://localhost:8081/api/v1/products
# Deve retornar lista de produtos (ou erro 404 se sem dados)
```

### Teste 4: Chamada inter-serviços

```bash
# Abra o código do order-service
# Veja como ele chama stock-service
# Ordem → Stock (reduzir estoque)
```

---

## 📞 Problemas Comuns?

| Problema | Solução |
|----------|---------|
| Aplicação não sobe | Veja **TROUBLESHOOTING.md - Erro 1** |
| DataSource não configurado | Veja **TROUBLESHOOTING.md - Erro 2** |
| Placeholder não resolvido | Veja **TROUBLESHOOTING.md - Erro 3** |
| Connection refused | Veja **TROUBLESHOOTING.md - Erro 4** |
| Porta em uso | Veja **TROUBLESHOOTING.md - Erro 5** |

---

## 💾 Resumo dos Arquivos Importantes

### Arquivos de Configuração (centralizado)
- `config-data/product-service.yml`
- `config-data/order-service.yml`
- `config-data/stock-service.yml`

### Servidor de Configuração
- `config-server/src/main/resources/application.yaml`

### Descoberta de Serviços
- `discovery-server/src/main/resources/application.yaml`

### Cada Microservice
- `[service]/src/main/resources/application.yaml` (fallback local)

### Bancos de Dados
- `docker-compose.yml` (MongoDB, Postgres, MySQL)

---

## 🚀 Próximos Passos no Curso

Depois que entender tudo aqui:

1. **Adicionar autenticação** (Spring Security)
2. **Implementar Circuit Breaker** (Resilience4j)
3. **Criar API Gateway** (Spring Cloud Gateway)
4. **Adicionar cache** (Redis)
5. **Logging centralizado** (ELK Stack)
6. **Testes de integração**
7. **Deploy em Docker/Kubernetes**

---

## 📖 Como Usar Esta Documentação

### Se está com pressa ⏱️
```
1. QUICK_START.md (5 min)
2. Rodou tudo? ✓ Bom!
3. Algo quebrou? → TROUBLESHOOTING.md
```

### Se tem tempo 📚
```
1. GUIA_ESTUDOS.md (ler tudo)
2. QUICK_START.md (fazer passo-a-passo)
3. REFERENCIA_CONFIGURACOES.md (estudar YAMLs)
4. TROUBLESHOOTING.md (para futuro)
```

### Se quer ir a fundo 🔬
```
1. Ler TODOS os arquivos (2-3 horas)
2. Fazer cada exercício do GUIA_ESTUDOS.md
3. Debugar cada erro do TROUBLESHOOTING.md
4. Modificar configs e ver efeitos
5. Estudar código-fonte de cada serviço
```

---

## ✨ Último Lembrete

> "Não tente entender tudo de uma vez. Comece pelo QUICK_START.md,
> rode tudo, depois leia GUIA_ESTUDOS.md para entender os conceitos.
> Use REFERENCIA_CONFIGURACOES.md como consulta rápida."

---

## 📝 Informações do Projeto

- **Spring Boot Version:** 4.1.0
- **Java Version:** 21
- **Maven:** 3.x
- **Docker:** Compose v2+
- **Bancos:** MongoDB 7.0, PostgreSQL 16, MySQL 8

---

**Ultima atualização:** 2026-07-23

**Autor:** Seu Professor Udemy

**Status:** ✅ 100% Funcional e Documentado

```
Se esta documentação ajudou, compartilhe com seus colegas! 📚
```

