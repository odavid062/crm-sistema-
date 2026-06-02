"use client";

import { useState } from "react";
import { Header } from "@/components/layout/Header";
import { Copy, Check, ExternalLink, Shield, Key, Webhook, Code2, Lock } from "lucide-react";
import toast from "react-hot-toast";

const API_BASE = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api";

type Endpoint = {
  method: "GET" | "POST" | "PUT" | "PATCH" | "DELETE";
  path: string;
  desc: string;
  body?: string;
};

const GROUPS: { name: string; endpoints: Endpoint[] }[] = [
  {
    name: "Contatos",
    endpoints: [
      { method: "GET", path: "/contacts?search=&status=&page=0&size=20", desc: "Listar/buscar contatos (paginado)" },
      { method: "GET", path: "/contacts/{id}", desc: "Buscar contato por ID" },
      { method: "POST", path: "/contacts", desc: "Criar contato", body: `{
  "name": "João Silva",
  "email": "joao@email.com",
  "phone": "5511999998888",
  "whatsapp": "5511999998888",
  "status": "LEAD",
  "source": "site"
}` },
      { method: "PUT", path: "/contacts/{id}", desc: "Atualizar contato" },
      { method: "DELETE", path: "/contacts/{id}", desc: "Excluir contato" },
    ],
  },
  {
    name: "Negócios (Deals)",
    endpoints: [
      { method: "GET", path: "/deals?pipelineId=&status=", desc: "Listar deals" },
      { method: "POST", path: "/deals", desc: "Criar deal", body: `{
  "title": "Proposta XYZ",
  "value": 4500.00,
  "pipelineId": "uuid-do-pipeline",
  "stageId": "uuid-da-etapa",
  "contactId": "uuid-do-contato"
}` },
      { method: "PATCH", path: "/deals/{id}/stage", desc: "Mover deal de etapa", body: `{ "stageId": "uuid" }` },
      { method: "PATCH", path: "/deals/{id}/status", desc: "Ganhar/perder deal", body: `{ "status": "WON" }` },
    ],
  },
  {
    name: "Agendamentos",
    endpoints: [
      { method: "GET", path: "/appointments", desc: "Listar (visão Kanban)" },
      { method: "GET", path: "/appointments/calendar?from=&to=", desc: "Listar por intervalo (calendário)" },
      { method: "POST", path: "/appointments", desc: "Criar agendamento", body: `{
  "title": "Reunião de apresentação",
  "type": "MEETING",
  "startAt": "2026-06-10T14:00",
  "endAt": "2026-06-10T15:00",
  "contactId": "uuid-do-contato",
  "meetingUrl": "https://meet.google.com/xxx"
}` },
      { method: "PATCH", path: "/appointments/{id}/status", desc: "Mover no Kanban", body: `{ "status": "CONFIRMED" }` },
    ],
  },
  {
    name: "WhatsApp",
    endpoints: [
      { method: "GET", path: "/whatsapp/conversations", desc: "Listar conversas" },
      { method: "POST", path: "/whatsapp/conversations/{id}/send", desc: "Enviar mensagem", body: `{ "text": "Olá! Como posso ajudar?" }` },
    ],
  },
  {
    name: "Pagamentos (Asaas)",
    endpoints: [
      { method: "GET", path: "/payments?status=", desc: "Listar cobranças" },
      { method: "POST", path: "/payments", desc: "Criar cobrança", body: `{
  "contactId": "uuid-do-contato",
  "description": "Mensalidade",
  "value": 199.90,
  "billingType": "PIX",
  "dueDate": "2026-06-15"
}` },
    ],
  },
  {
    name: "Automação & Relatório de Valor",
    endpoints: [
      { method: "POST", path: "/automation-logs", desc: "Registrar ação automatizada (para o ROI)", body: `{
  "type": "MESSAGE_SENT",
  "channel": "WHATSAPP",
  "success": true
}` },
      { method: "GET", path: "/value-reports/preview?periodType=MONTHLY", desc: "Prévia do relatório de valor" },
      { method: "GET", path: "/value-reports", desc: "Histórico de relatórios" },
    ],
  },
];

const WEBHOOK_EVENTS = [
  "contact.created", "contact.updated", "contact.status_changed",
  "deal.created", "deal.stage_changed", "deal.won", "deal.lost",
  "appointment.created", "appointment.status_changed", "appointment.cancelled",
  "payment.received", "payment.overdue",
];

const METHOD_COLORS: Record<string, string> = {
  GET: "bg-blue-100 text-blue-700", POST: "bg-green-100 text-green-700",
  PUT: "bg-amber-100 text-amber-700", PATCH: "bg-purple-100 text-purple-700",
  DELETE: "bg-red-100 text-red-700",
};

function CodeBlock({ code }: { code: string }) {
  const [copied, setCopied] = useState(false);
  return (
    <div className="relative group">
      <pre className="bg-gray-900 text-gray-100 text-xs p-3 rounded-lg overflow-x-auto"><code>{code}</code></pre>
      <button
        onClick={() => { navigator.clipboard.writeText(code); setCopied(true); toast.success("Copiado!"); setTimeout(() => setCopied(false), 1500); }}
        className="absolute top-2 right-2 p-1.5 bg-gray-800 hover:bg-gray-700 rounded text-gray-300 opacity-0 group-hover:opacity-100 transition">
        {copied ? <Check size={13} /> : <Copy size={13} />}
      </button>
    </div>
  );
}

export default function DocumentacaoPage() {
  const [token, setToken] = useState("");
  const tk = token || "SEU_TOKEN_AQUI";

  const curlExample = `curl -X POST "${API_BASE}/contacts" \\
  -H "Authorization: Bearer ${tk}" \\
  -H "Content-Type: application/json" \\
  -d '{"name":"João Silva","email":"joao@email.com","status":"LEAD"}'`;

  const fetchExample = `const res = await fetch("${API_BASE}/contacts", {
  method: "POST",
  headers: {
    "Authorization": "Bearer ${tk}",
    "Content-Type": "application/json"
  },
  body: JSON.stringify({ name: "João Silva", email: "joao@email.com", status: "LEAD" })
});
const data = await res.json();`;

  return (
    <div>
      <Header title="Documentação da API" />
      <div className="p-6 max-w-4xl space-y-8">

        {/* Segurança */}
        <section className="bg-white rounded-xl border border-gray-200 p-6">
          <h2 className="flex items-center gap-2 text-lg font-bold mb-3"><Shield size={20} className="text-indigo-600" /> Autenticação & Segurança</h2>
          <p className="text-sm text-gray-600 mb-4">
            Toda requisição externa é autenticada por um <strong>API Token</strong> (Bearer). O token identifica sua empresa
            e <strong>só enxerga os dados dela</strong> — o isolamento multi-tenant é automático e obrigatório.
          </p>
          <div className="grid md:grid-cols-3 gap-3 text-sm">
            <div className="flex items-start gap-2 p-3 bg-gray-50 rounded-lg">
              <Key size={16} className="text-indigo-600 mt-0.5" />
              <div><strong>1. Gere o token</strong><br /><span className="text-gray-500">Configurações → API Tokens → Gerar</span></div>
            </div>
            <div className="flex items-start gap-2 p-3 bg-gray-50 rounded-lg">
              <Code2 size={16} className="text-indigo-600 mt-0.5" />
              <div><strong>2. Envie no header</strong><br /><span className="text-gray-500">Authorization: Bearer crm_live_...</span></div>
            </div>
            <div className="flex items-start gap-2 p-3 bg-gray-50 rounded-lg">
              <Lock size={16} className="text-indigo-600 mt-0.5" />
              <div><strong>3. Mantenha em segredo</strong><br /><span className="text-gray-500">Nunca exponha no frontend público</span></div>
            </div>
          </div>
          <div className="mt-4 p-3 bg-amber-50 border border-amber-200 rounded-lg text-sm text-amber-800">
            🔒 O token é mostrado <strong>uma única vez</strong> na criação. Se perder, revogue e gere outro. Cada token pode ser revogado individualmente sem afetar os demais.
          </div>
        </section>

        {/* Testar com token */}
        <section className="bg-white rounded-xl border border-gray-200 p-6">
          <h2 className="text-lg font-bold mb-2">Base URL & Exemplos prontos</h2>
          <div className="flex items-center gap-2 mb-4">
            <code className="text-sm bg-gray-100 px-3 py-1.5 rounded">{API_BASE}</code>
          </div>
          <p className="text-sm text-gray-600 mb-2">Cole seu token abaixo para gerar exemplos prontos pra copiar:</p>
          <input value={token} onChange={(e) => setToken(e.target.value)} placeholder="crm_live_..."
            className="w-full px-3 py-2.5 border border-gray-200 rounded-lg text-sm font-mono focus:ring-2 focus:ring-indigo-500 outline-none mb-4" />
          <div className="grid md:grid-cols-2 gap-4">
            <div><p className="text-xs font-semibold text-gray-400 uppercase mb-1">cURL</p><CodeBlock code={curlExample} /></div>
            <div><p className="text-xs font-semibold text-gray-400 uppercase mb-1">JavaScript (fetch)</p><CodeBlock code={fetchExample} /></div>
          </div>
        </section>

        {/* Endpoints */}
        <section>
          <h2 className="text-lg font-bold mb-4">Endpoints disponíveis</h2>
          <div className="space-y-6">
            {GROUPS.map((g) => (
              <div key={g.name} className="bg-white rounded-xl border border-gray-200 overflow-hidden">
                <div className="px-4 py-3 bg-gray-50 border-b border-gray-100 font-semibold text-sm">{g.name}</div>
                <div className="divide-y divide-gray-100">
                  {g.endpoints.map((e, i) => (
                    <div key={i} className="px-4 py-3">
                      <div className="flex items-center gap-3 flex-wrap">
                        <span className={`text-xs font-bold px-2 py-0.5 rounded ${METHOD_COLORS[e.method]}`}>{e.method}</span>
                        <code className="text-sm text-gray-700">{e.path}</code>
                      </div>
                      <p className="text-xs text-gray-500 mt-1">{e.desc}</p>
                      {e.body && <div className="mt-2"><CodeBlock code={e.body} /></div>}
                    </div>
                  ))}
                </div>
              </div>
            ))}
          </div>
        </section>

        {/* Webhooks */}
        <section className="bg-white rounded-xl border border-gray-200 p-6">
          <h2 className="flex items-center gap-2 text-lg font-bold mb-3"><Webhook size={20} className="text-indigo-600" /> Webhooks (eventos disparados)</h2>
          <p className="text-sm text-gray-600 mb-4">
            Configure uma URL em <strong>Configurações → Webhooks</strong> e o CRM faz POST automático nesses eventos
            (com assinatura HMAC no header <code className="bg-gray-100 px-1 rounded">X-CRM-Signature</code>). Ideal para n8n, Zapier ou seu app.
          </p>
          <div className="flex flex-wrap gap-2">
            {WEBHOOK_EVENTS.map((ev) => (
              <span key={ev} className="text-xs font-mono px-2 py-1 bg-indigo-50 text-indigo-700 rounded">{ev}</span>
            ))}
          </div>
        </section>

        {/* Swagger */}
        <section className="bg-gradient-to-r from-indigo-600 to-purple-600 rounded-xl p-6 text-white">
          <h2 className="text-lg font-bold mb-2">Documentação interativa completa (Swagger)</h2>
          <p className="text-sm text-indigo-100 mb-4">Explore e teste todos os endpoints direto no navegador, com schemas detalhados.</p>
          <a href={`${API_BASE.replace("/api", "")}/swagger-ui.html`} target="_blank" rel="noreferrer"
            className="inline-flex items-center gap-2 bg-white text-indigo-700 px-4 py-2.5 rounded-lg text-sm font-semibold hover:bg-indigo-50 transition">
            Abrir Swagger UI <ExternalLink size={15} />
          </a>
        </section>

      </div>
    </div>
  );
}
