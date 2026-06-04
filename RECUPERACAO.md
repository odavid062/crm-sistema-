# 🔑 Guia de Recuperação Pós-Format

Mapa do que cada projeto precisa de credenciais/config para voltar a rodar.
**Este arquivo NÃO contém segredos** — só diz *o que* é preciso e *onde obter*.
Guarde os **valores** num gerenciador de senhas (ex.: Bitwarden, grátis).

> Depois do format: clone os repos, abra este guia e me chame ("vamos restaurar").
> Com este guia eu sei exatamente o que pedir e onde colar — monto os `.env` e ligo tudo.

---

## ⚠️ ANTES de formatar — salvar no gerenciador de senhas (NÃO dá pra regenerar)
- [ ] **Finanças → `KEK_V1`, `KEK_V2`, `KEK_ACTIVE_VERSION`** (chaves de criptografia — se perder, dados criptografados viram lixo)
- [ ] Qualquer senha/segredo que você ainda use de verdade

## 🔄 Rotacionar (regenerar — foram expostas no chat OU por segurança)
- [ ] Supabase **service_role** (regenera no painel) — usada em CRM(n8n), xzone e finanças
- [ ] **n8n API key**
- [ ] Senha do **RDS** (lambda)
- [ ] **JWT_SECRET** do CRM (gera novo: `openssl rand -hex 32`)

---

## 📦 Repositórios (todos em github.com/odavid062)
| Projeto | Repo |
|---|---|
| CRM | `crm-sistema-` |
| Finanças | `controle-de-finan-as` |
| X-Zone | `x-zone` |
| Portfólio | `portifolio-david` |
| Lambda | `Lambda-AWS` (mudanças locais na branch `backup-pre-format`) |
| AdecGo | `adeco` |
| Jogos | `snake`, `snakeman`, `java-concorrencia` |

---

## 🟦 CRM (`crm-sistema-`)
`.env` na raiz (não versionado). O `docker-compose.yml` já traz defaults de Postgres.
| Variável | O que é | Onde obter |
|---|---|---|
| `JWT_SECRET` | assinatura dos tokens JWT | gerar novo: `openssl rand -hex 32` |
| `UAZAPI_BASE_URL` | URL da UazAPI | `https://free.uazapi.com` ou seu servidor |
| `UAZAPI_TOKEN` | token da instância WhatsApp | painel UazAPI |
| `UAZAPI_INSTANCE` | nome da instância | painel UazAPI |
| `ASAAS_API_KEY` | pagamentos | painel Asaas → Integrações → API |

**n8n ↔ CRM:** gerar **API Token** dentro do CRM (Configurações → API Tokens), formato `crm_live_...`.
**Supabase (via n8n):** no arquivo `n8n/workflow-supabase-PRONTO.json` trocar o placeholder `YOUR_SUPABASE_SERVICE_ROLE_KEY` pela service_role **nova** (Supabase → Project Settings → API).
**Subir:** `docker compose up -d --build postgres backend frontend` (excluir `n8n` se já tiver o seu rodando).

## 🟩 Finanças (`controle-de-finan-as`)
`.env` (servidor) + variáveis Vite. App React + Express + Supabase + Claude.
| Variável | O que é | Onde obter |
|---|---|---|
| `VITE_SUPABASE_URL` / `SUPABASE_URL` | projeto Supabase | Supabase → API |
| `VITE_SUPABASE_ANON_KEY` / `SUPABASE_ANON_KEY` | chave pública | Supabase → API |
| `SUPABASE_SERVICE_ROLE_KEY` | chave admin (server) | Supabase → API (**rotacionar**) |
| `ANTHROPIC_API_KEY` | Claude AI | console.anthropic.com → API Keys |
| `ANTHROPIC_MODEL` / `AI_PROVIDER` | config do modelo | ex.: claude-... / anthropic |
| `KEK_V1`, `KEK_V2`, `KEK_ACTIVE_VERSION` | **criptografia (NÃO regenerar)** | seu gerenciador de senhas |
| `CRON_SECRET` | segredo de cron | gerar novo |
| `PORT` / `NODE_ENV` | execução | 3000 / development |

## 🟪 X-Zone (`x-zone`)
`.env.local` (Next.js). Site de ingressos.
| Variável | O que é | Onde obter |
|---|---|---|
| `NEXT_PUBLIC_SUPABASE_URL` | projeto Supabase | Supabase → API |
| `NEXT_PUBLIC_SUPABASE_ANON_KEY` | chave pública | Supabase → API |
| `SUPABASE_SERVICE_ROLE_KEY` | chave admin | Supabase → API |
| `MERCADOPAGO_ACCESS_TOKEN` | pagamentos (`APP_USR-...`) | Mercado Pago → Suas integrações → Credenciais |
| `MERCADOPAGO_WEBHOOK_SECRET` | validação de webhook | Mercado Pago → Webhooks |
| `NEXT_PUBLIC_BASE_URL` | URL do site | ex.: http://localhost:3000 |

## 🟧 Lambda (`Lambda-AWS`)
`.env` com conexão ao RDS. Mudanças recentes na branch `backup-pre-format`.
| Variável | O que é | Onde obter |
|---|---|---|
| `PGHOST` | endpoint RDS | AWS RDS → seu banco |
| `PGUSER` / `PGDATABASE` / `PGPORT` | conexão | RDS (PGPORT=5432) |
| `PGPASSWORD` | senha do banco | **rotacionar no RDS** |
| AWS CLI | deploy/acesso | `aws configure` (Access Key/Secret do IAM) |

## ⬜ AdecGo (`adeco`)
**Sem segredos.** Formulário usa FormSubmit (e-mail público da ONG). Nada a configurar — só abrir/hospedar.

## 🎮 Jogos (`snake`, `snakeman`, `java-concorrencia`)
**Sem segredos.** Java 17 puro. Abrir no IntelliJ e rodar.

---

## 🧰 Stack a reinstalar pós-format
Git · Docker Desktop · Node.js · Java 17 (JDK) · VS Code / IntelliJ
**Lembrete:** desativar o **Inicialização Rápida** do Windows (Painel de Controle → Opções de Energia) — foi o que travou o WSL/Docker.
