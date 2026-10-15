# CRM Sistema

CRM completo com integrações WhatsApp (UazAPI), Pagamentos (Asaas) e Automação (n8n).

## Stack

- **Backend**: Java 17 + Spring Boot 3.3.2 + Maven + PostgreSQL 16 + Flyway + WebSocket
- **Frontend**: Next.js 15 + React 19 + TypeScript + Tailwind CSS + TanStack Query + Zustand
- **Integrações**: UazAPI (WhatsApp), Asaas (Pagamentos), n8n (Automação)
- **Docs API**: Swagger UI em `http://localhost:8080/swagger-ui.html`

## Módulos

| Módulo | Descrição |
|--------|-----------|
| Dashboard | KPIs, gráficos e visão geral |
| Contatos | Leads, clientes, segmentação e histórico |
| Empresas | Cadastro e gestão de empresas |
| Pipeline | Funil de vendas com Kanban drag-and-drop |
| Deals | Negociações com status won/lost |
| Agendamentos | Agenda com visão Kanban e Calendário de compromissos |
| Atendimentos | Central de atendimento e conversas via WhatsApp |
| WhatsApp | Inbox via UazAPI, chat em tempo real |
| Pagamentos | Boleto, Pix e cartão via Asaas |
| Atividades | Tarefas, ligações e reuniões |
| Relatórios | Analytics e funil de conversão |
| Relatório de Valor | Métricas de ROI, tempo economizado e impacto de automações |
| API Tokens | Gestão de tokens Bearer com isolamento multi-tenant para integrações |
| Documentação API | Interface interna com schemas e exemplos cURL/JavaScript |
| Super Admin | Gestão de tenants e estatísticas globais do SaaS (perfil SUPER_ADMIN) |
| Configurações | Webhooks, UazAPI, Asaas e n8n |

## Pré-requisitos

- **Docker** e **Docker Compose** (recomendado para subir ambiente completo)
- Ou para execução manual:
  - **Java 17** (JDK) e **Maven 3.9+**
  - **Node.js 20+** ou **22+** e **npm**
  - **PostgreSQL 16**

## Setup Rápido com Docker

```bash
# Clone e entre na pasta
cd crm-sistema

# Copie o .env e configure suas chaves
cp backend/.env.example backend/.env
cp frontend/.env.local.example frontend/.env.local

# Suba tudo
docker-compose up -d
```

Acesse:
- **Frontend**: http://localhost:3000
- **API Swagger**: http://localhost:8080/swagger-ui.html
- **n8n**: http://localhost:5678

**Login padrão**: `admin@crm.com` / `Admin@123`

## Setup Manual

### Backend

```bash
cd backend
cp .env.example .env
# Edite o .env com suas configurações

# Executar em desenvolvimento
mvn spring-boot:run

# Executar testes
mvn test

# Gerar JAR de produção
mvn clean package -DskipTests
```

### Frontend

```bash
cd frontend
cp .env.local.example .env.local
npm install

# Executar em desenvolvimento (com Turbopack)
npm run dev

# Gerar build de produção
npm run build

# Iniciar servidor em produção
npm run start

# Executar verificação de linter
npm run lint
```

## Configuração das Integrações

### UazAPI (WhatsApp)
1. Crie uma conta em uazapi.com
2. Configure `UAZAPI_TOKEN` e `UAZAPI_INSTANCE` no `.env`
3. No painel UazAPI, configure o webhook para: `http://seu-backend/api/whatsapp/webhook/receive`
4. Acesse Settings > WhatsApp para conectar escaneando o QR Code

### Asaas (Pagamentos)
1. Crie uma conta em asaas.com
2. Gere sua API Key no painel
3. Configure `ASAAS_API_KEY` no `.env`
4. Configure o webhook no Asaas para: `http://seu-backend/api/payments/webhook`

### n8n (Automação)
1. Acesse http://localhost:5678 (usuário: admin / senha: admin123)
2. Crie workflows usando HTTP Request para chamar a API do CRM
3. Use o endpoint `POST /api/webhooks/receive/n8n` para receber dados do n8n
4. Configure webhooks no CRM (Settings > Webhooks) para disparar eventos para o n8n

## Endpoints da API

A documentação completa está no Swagger: `http://localhost:8080/swagger-ui.html`

Principais grupos:
- `/api/auth` - Autenticação
- `/api/contacts` - Contatos
- `/api/companies` - Empresas
- `/api/pipelines` - Pipelines
- `/api/deals` - Deals
- `/api/appointments` - Agendamentos (Kanban e visão calendário)
- `/api/whatsapp` - WhatsApp
- `/api/payments` - Pagamentos
- `/api/activities` - Atividades
- `/api/webhooks` - Webhooks
- `/api/reports` - Relatórios
- `/api/api-tokens` - Tokens Bearer para integrações externas (multi-tenant)
- `/api/automation-logs` - Registro de ações automatizadas (alimenta o ROI)
- `/api/value-reports` - Relatórios de valor gerado (ROI e automações)
- `/api/admin/tenants` - Gestão de tenants (Super Admin)

## Variáveis de Ambiente

### Backend (`backend/.env`)

| Variável | Descrição |
|----------|-----------|
| `DATABASE_URL` | URL JDBC de conexão com o PostgreSQL |
| `DATABASE_USER` | Usuário de autenticação do PostgreSQL |
| `DATABASE_PASSWORD` | Senha de autenticação do PostgreSQL |
| `JWT_SECRET` | Chave secreta para assinatura dos tokens JWT |
| `JWT_EXPIRATION` | Tempo de expiração do access token em milissegundos |
| `JWT_REFRESH_EXPIRATION` | Tempo de expiração do refresh token em milissegundos |
| `CORS_ORIGINS` | Origens autorizadas para CORS (separadas por vírgula) |
| `UAZAPI_BASE_URL` | URL base da API da UazAPI |
| `UAZAPI_TOKEN` | Token de autenticação da UazAPI |
| `UAZAPI_INSTANCE` | Nome da instância do WhatsApp na UazAPI |
| `ASAAS_BASE_URL` | URL base da API do Asaas |
| `ASAAS_API_KEY` | Chave de API para integração com o Asaas |
| `N8N_BASE_URL` | URL base do n8n |
| `N8N_API_KEY` | Chave de API para autenticação no n8n |
| `N8N_WEBHOOK_URL` | URL de webhook do n8n para disparo de eventos |
| `MAIL_HOST` | Host do servidor SMTP para envio de emails |
| `MAIL_PORT` | Porta do servidor SMTP |
| `MAIL_USERNAME` | Usuário/email do servidor SMTP |
| `MAIL_PASSWORD` | Senha ou app password do servidor SMTP |

### Frontend (`frontend/.env.local`)

| Variável | Descrição |
|----------|-----------|
| `NEXT_PUBLIC_API_URL` | URL base da API REST do backend |
| `NEXT_PUBLIC_WS_URL` | URL base para conexões WebSocket / STOMP |

## Estrutura de Pastas

```text
crm-sistema/
├── backend/                  # API Spring Boot (Java 17, Maven)
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/crm/ # Código-fonte dividido por domínio
│   │   │   └── resources/    # Configurações e migrations Flyway
│   │   └── test/             # Testes unitários e de integração
│   ├── Dockerfile            # Imagem Docker do backend
│   └── pom.xml               # Dependências e build Maven
├── frontend/                 # Frontend Next.js 15 (TypeScript, Tailwind CSS)
│   ├── src/
│   │   ├── app/              # Rotas e páginas (App Router)
│   │   ├── components/       # Componentes visuais e de layout
│   │   ├── lib/              # Utilitários e configurações HTTP
│   │   └── store/            # Gerenciamento de estado (Zustand)
│   ├── Dockerfile            # Imagem Docker do frontend
│   └── package.json          # Dependências Node.js e scripts
└── docker-compose.yml        # Orquestração local dos serviços
```

## Testes

### Backend
Para executar os testes unitários e de integração:
```bash
cd backend
mvn test
```

### Frontend
Para executar a verificação estática de lint:
```bash
cd frontend
npm run lint
```
