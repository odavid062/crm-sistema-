# CRM Sistema

CRM completo com integrações WhatsApp (UazAPI), Pagamentos (Asaas) e Automação (n8n).

## Stack

- **Backend**: Java 21 + Spring Boot 3.3 + Maven + PostgreSQL + Flyway
- **Frontend**: Next.js 15 + TypeScript + Tailwind CSS + TanStack Query
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
| WhatsApp | Inbox via UazAPI, chat em tempo real |
| Pagamentos | Boleto, Pix e cartão via Asaas |
| Atividades | Tarefas, ligações e reuniões |
| Relatórios | Analytics e funil de conversão |
| Configurações | Webhooks, UazAPI, Asaas e n8n |

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
mvn spring-boot:run
```

### Frontend

```bash
cd frontend
cp .env.local.example .env.local
npm install
npm run dev
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
- `/api/whatsapp` - WhatsApp
- `/api/payments` - Pagamentos
- `/api/activities` - Atividades
- `/api/webhooks` - Webhooks
- `/api/reports` - Relatórios
