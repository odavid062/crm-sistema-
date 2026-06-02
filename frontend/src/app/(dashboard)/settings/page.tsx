"use client";

import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import api from "@/lib/api";
import { Header } from "@/components/layout/Header";
import { useAuthStore } from "@/store/authStore";
import { formatDate } from "@/lib/utils";
import { Plus, Trash2, CheckCircle, XCircle, Zap, MessageCircle, CreditCard, Key, Radio, Copy } from "lucide-react";
import toast from "react-hot-toast";

type TabKey = "tokens" | "channels" | "webhooks" | "uazapi" | "asaas" | "n8n";

export default function SettingsPage() {
  const [tab, setTab] = useState<TabKey>("tokens");

  return (
    <div>
      <Header title="Configurações e Integrações" />
      <div className="p-6">
        <div className="flex gap-2 mb-6 border-b border-gray-200 flex-wrap">
          {[
            { key: "tokens", label: "API Tokens", icon: <Key size={15} /> },
            { key: "channels", label: "Canais", icon: <Radio size={15} /> },
            { key: "webhooks", label: "Webhooks", icon: <Zap size={15} /> },
            { key: "uazapi", label: "WhatsApp (UazAPI)", icon: <MessageCircle size={15} /> },
            { key: "asaas", label: "Asaas", icon: <CreditCard size={15} /> },
            { key: "n8n", label: "n8n", icon: <Zap size={15} /> },
          ].map(({ key, label, icon }) => (
            <button
              key={key}
              onClick={() => setTab(key as TabKey)}
              className={`flex items-center gap-2 px-4 py-2.5 text-sm font-medium border-b-2 transition ${
                tab === key ? "border-indigo-600 text-indigo-600" : "border-transparent text-gray-500 hover:text-gray-700"
              }`}
            >
              {icon}{label}
            </button>
          ))}
        </div>

        {tab === "tokens" && <TokensTab />}
        {tab === "channels" && <ChannelsTab />}
        {tab === "webhooks" && <WebhooksTab />}
        {tab === "uazapi" && <UazApiTab />}
        {tab === "asaas" && <AsaasTab />}
        {tab === "n8n" && <N8nTab />}
      </div>
    </div>
  );
}

function TokensTab() {
  const qc = useQueryClient();
  const [name, setName] = useState("");
  const [newToken, setNewToken] = useState<string | null>(null);

  const { data: tokens = [] } = useQuery({
    queryKey: ["api-tokens"],
    queryFn: () => api.get("/api-tokens").then((r) => r.data),
  });

  const createMutation = useMutation({
    mutationFn: () => api.post("/api-tokens", { name }).then((r) => r.data),
    onSuccess: (data) => {
      qc.invalidateQueries({ queryKey: ["api-tokens"] });
      setNewToken(data.plainToken);
      setName("");
      toast.success("Token gerado!");
    },
  });

  const revokeMutation = useMutation({
    mutationFn: (id: string) => api.delete(`/api-tokens/${id}`),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ["api-tokens"] }); toast.success("Token revogado"); },
  });

  return (
    <div className="space-y-6 max-w-3xl">
      <div className="bg-white rounded-xl border border-gray-200 p-6">
        <h3 className="font-semibold mb-1">Tokens de API (Bearer)</h3>
        <p className="text-sm text-gray-500 mb-4">
          Use para autenticar sistemas externos (n8n, ERPs). Envie no header
          <code className="bg-gray-100 px-1 rounded mx-1">Authorization: Bearer crm_live_...</code>
        </p>
        <div className="flex gap-3">
          <input value={name} onChange={(e) => setName(e.target.value)} placeholder="Nome do token (ex: Integração n8n)"
            className="flex-1 px-3 py-2.5 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none" />
          <button onClick={() => createMutation.mutate()} disabled={!name}
            className="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-700 disabled:opacity-40 text-white px-4 py-2 rounded-lg text-sm font-medium">
            <Plus size={15} /> Gerar
          </button>
        </div>

        {newToken && (
          <div className="mt-4 p-4 bg-amber-50 border border-amber-200 rounded-lg">
            <p className="text-sm text-amber-800 font-medium mb-2">⚠️ Copie agora — este token não será mostrado novamente:</p>
            <div className="flex items-center gap-2">
              <code className="flex-1 text-xs bg-white p-2 rounded border border-amber-200 break-all">{newToken}</code>
              <button onClick={() => { navigator.clipboard.writeText(newToken); toast.success("Copiado!"); }}
                className="p-2 bg-amber-600 text-white rounded-lg"><Copy size={14} /></button>
            </div>
          </div>
        )}
      </div>

      <div className="bg-white rounded-xl border border-gray-200 overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 border-b border-gray-200">
            <tr>
              <th className="text-left px-4 py-3 text-gray-500 font-medium">Nome</th>
              <th className="text-left px-4 py-3 text-gray-500 font-medium">Token</th>
              <th className="text-left px-4 py-3 text-gray-500 font-medium">Último uso</th>
              <th className="text-left px-4 py-3 text-gray-500 font-medium">Status</th>
              <th className="px-4 py-3" />
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {tokens.map((t: any) => (
              <tr key={t.id} className="hover:bg-gray-50">
                <td className="px-4 py-3 font-medium">{t.name}</td>
                <td className="px-4 py-3 text-gray-500 font-mono text-xs">{t.tokenPrefix}</td>
                <td className="px-4 py-3 text-gray-500">{t.lastUsedAt ? formatDate(t.lastUsedAt) : "nunca"}</td>
                <td className="px-4 py-3">
                  {t.active
                    ? <span className="flex items-center gap-1 text-green-600 text-xs"><CheckCircle size={13} /> Ativo</span>
                    : <span className="flex items-center gap-1 text-gray-400 text-xs"><XCircle size={13} /> Revogado</span>}
                </td>
                <td className="px-4 py-3 text-right">
                  {t.active && (
                    <button onClick={() => revokeMutation.mutate(t.id)}
                      className="p-1.5 text-red-500 hover:bg-red-50 rounded-lg"><Trash2 size={14} /></button>
                  )}
                </td>
              </tr>
            ))}
            {tokens.length === 0 && (
              <tr><td colSpan={5} className="px-4 py-8 text-center text-gray-400">Nenhum token criado ainda</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}

const CHANNEL_TYPES = ["WHATSAPP", "INSTAGRAM", "FACEBOOK", "TELEGRAM", "EMAIL", "SMS", "WEBCHAT"];
const CHANNEL_STATUS_COLORS: Record<string, string> = {
  CONNECTED: "text-green-600", CONNECTING: "text-blue-600", DISCONNECTED: "text-gray-400", ERROR: "text-red-500",
};

function ChannelsTab() {
  const qc = useQueryClient();
  const [name, setName] = useState("");
  const [type, setType] = useState("WHATSAPP");

  const { data: channels = [] } = useQuery({
    queryKey: ["channels"],
    queryFn: () => api.get("/channels").then((r) => r.data),
  });

  const createMutation = useMutation({
    mutationFn: () => api.post("/channels", { name, type }),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ["channels"] }); toast.success("Canal criado!"); setName(""); },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => api.delete(`/channels/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["channels"] }),
  });

  return (
    <div className="space-y-6 max-w-3xl">
      <div className="bg-white rounded-xl border border-gray-200 p-6">
        <h3 className="font-semibold mb-4">Novo Canal de Atendimento</h3>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <input value={name} onChange={(e) => setName(e.target.value)} placeholder="Nome do canal"
            className="px-3 py-2.5 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none" />
          <select value={type} onChange={(e) => setType(e.target.value)}
            className="px-3 py-2.5 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none">
            {CHANNEL_TYPES.map((t) => <option key={t} value={t}>{t}</option>)}
          </select>
          <button onClick={() => createMutation.mutate()} disabled={!name}
            className="flex items-center justify-center gap-2 bg-indigo-600 hover:bg-indigo-700 disabled:opacity-40 text-white px-4 py-2 rounded-lg text-sm font-medium">
            <Plus size={15} /> Adicionar
          </button>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {channels.map((c: any) => (
          <div key={c.id} className="bg-white rounded-xl border border-gray-200 p-5 flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-lg bg-indigo-50 flex items-center justify-center">
                <Radio size={18} className="text-indigo-600" />
              </div>
              <div>
                <p className="font-medium">{c.name}</p>
                <p className="text-xs text-gray-500">{c.type}</p>
              </div>
            </div>
            <div className="flex items-center gap-3">
              <span className={`text-xs font-medium ${CHANNEL_STATUS_COLORS[c.status]}`}>{c.status}</span>
              <button onClick={() => deleteMutation.mutate(c.id)}
                className="p-1.5 text-red-500 hover:bg-red-50 rounded-lg"><Trash2 size={14} /></button>
            </div>
          </div>
        ))}
        {channels.length === 0 && (
          <p className="text-sm text-gray-400 col-span-2 text-center py-8">Nenhum canal configurado</p>
        )}
      </div>
    </div>
  );
}

function WebhooksTab() {
  const qc = useQueryClient();
  const { data: webhooks = [] } = useQuery({
    queryKey: ["webhooks"],
    queryFn: () => api.get("/webhooks").then((r) => r.data),
  });
  const [name, setName] = useState("");
  const [url, setUrl] = useState("");
  const [events, setEvents] = useState("contact.created,deal.created");

  const createMutation = useMutation({
    mutationFn: () => api.post("/webhooks", { name, url, events: events.split(","), active: true }),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ["webhooks"] }); toast.success("Webhook criado!"); setName(""); setUrl(""); },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => api.delete(`/webhooks/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["webhooks"] }),
  });

  const testMutation = useMutation({
    mutationFn: (id: string) => api.post(`/webhooks/test/${id}`),
    onSuccess: () => toast.success("Webhook testado!"),
  });

  return (
    <div className="space-y-6">
      <div className="bg-white rounded-xl border border-gray-200 p-6">
        <h3 className="font-semibold mb-4">Novo Webhook</h3>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <input value={name} onChange={(e) => setName(e.target.value)} placeholder="Nome do webhook"
            className="px-3 py-2.5 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none" />
          <input value={url} onChange={(e) => setUrl(e.target.value)} placeholder="URL do endpoint"
            className="px-3 py-2.5 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none" />
          <input value={events} onChange={(e) => setEvents(e.target.value)} placeholder="eventos (vírgula)"
            className="px-3 py-2.5 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none" />
        </div>
        <button onClick={() => createMutation.mutate()} disabled={!name || !url}
          className="mt-4 flex items-center gap-2 bg-indigo-600 hover:bg-indigo-700 disabled:opacity-40 text-white px-4 py-2 rounded-lg text-sm font-medium transition">
          <Plus size={15} /> Criar Webhook
        </button>
      </div>

      <div className="bg-white rounded-xl border border-gray-200 overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 border-b border-gray-200">
            <tr>
              <th className="text-left px-4 py-3 text-gray-500 font-medium">Nome</th>
              <th className="text-left px-4 py-3 text-gray-500 font-medium">URL</th>
              <th className="text-left px-4 py-3 text-gray-500 font-medium">Status</th>
              <th className="px-4 py-3" />
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {webhooks.map((wh: any) => (
              <tr key={wh.id} className="hover:bg-gray-50">
                <td className="px-4 py-3 font-medium">{wh.name}</td>
                <td className="px-4 py-3 text-gray-500 text-xs truncate max-w-xs">{wh.url}</td>
                <td className="px-4 py-3">
                  {wh.active
                    ? <span className="flex items-center gap-1 text-green-600 text-xs"><CheckCircle size={13} /> Ativo</span>
                    : <span className="flex items-center gap-1 text-gray-400 text-xs"><XCircle size={13} /> Inativo</span>}
                </td>
                <td className="px-4 py-3">
                  <div className="flex items-center gap-2 justify-end">
                    <button onClick={() => testMutation.mutate(wh.id)}
                      className="text-xs text-indigo-600 hover:underline">Testar</button>
                    <button onClick={() => deleteMutation.mutate(wh.id)}
                      className="p-1.5 text-red-500 hover:bg-red-50 rounded-lg"><Trash2 size={14} /></button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

function UazApiTab() {
  const tenantId = useAuthStore((s) => s.user?.tenantId);
  const { data: status } = useQuery({
    queryKey: ["uazapi-status"],
    queryFn: () => api.get("/whatsapp/status").then((r) => r.data),
  });

  const { data: qrCode, refetch } = useQuery({
    queryKey: ["uazapi-qr"],
    queryFn: () => api.get("/whatsapp/qrcode").then((r) => r.data),
    enabled: false,
  });

  return (
    <div className="bg-white rounded-xl border border-gray-200 p-6 max-w-lg">
      <h3 className="font-semibold mb-4">WhatsApp via UazAPI</h3>
      <div className="space-y-4">
        <div className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
          <span className="text-sm font-medium">Status da Instância</span>
          <span className={`text-sm font-medium ${status?.connected ? "text-green-600" : "text-red-500"}`}>
            {status?.connected ? "Conectado" : "Desconectado"}
          </span>
        </div>
        <button onClick={() => refetch()}
          className="w-full border border-gray-200 hover:bg-gray-50 py-2.5 rounded-lg text-sm font-medium transition">
          Gerar QR Code
        </button>
        {qrCode?.qrcode && (
          <div className="flex flex-col items-center p-4 border border-gray-200 rounded-lg">
            <img src={qrCode.qrcode} alt="QR Code WhatsApp" className="w-48 h-48" />
            <p className="text-sm text-gray-500 mt-2">Escaneie com o WhatsApp</p>
          </div>
        )}
        <div className="p-4 bg-blue-50 rounded-lg">
          <p className="text-sm text-blue-800 font-medium">Webhook URL para UazAPI (exclusiva da sua empresa):</p>
          <code className="text-xs text-blue-700 break-all mt-1 block">
            {process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api"}/whatsapp/webhook/receive/{tenantId ?? "{tenantId}"}
          </code>
        </div>
      </div>
    </div>
  );
}

function AsaasTab() {
  return (
    <div className="bg-white rounded-xl border border-gray-200 p-6 max-w-lg">
      <h3 className="font-semibold mb-4">Asaas - Pagamentos</h3>
      <div className="space-y-4">
        <div className="p-4 bg-green-50 rounded-lg">
          <p className="text-sm text-green-800 font-medium">Integração configurada via variáveis de ambiente</p>
          <code className="text-xs text-green-700 mt-1 block">ASAAS_API_KEY=sua-chave-asaas</code>
        </div>
        <p className="text-sm text-gray-600">
          Configure seu webhook no painel do Asaas para:
        </p>
        <code className="text-xs bg-gray-100 p-3 rounded-lg block break-all">
          {process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api"}/payments/webhook
        </code>
        <p className="text-xs text-gray-500">Eventos: PAYMENT_RECEIVED, PAYMENT_OVERDUE, PAYMENT_CONFIRMED</p>
      </div>
    </div>
  );
}

function N8nTab() {
  return (
    <div className="bg-white rounded-xl border border-gray-200 p-6 max-w-lg">
      <h3 className="font-semibold mb-4">n8n - Automação de Fluxos</h3>
      <div className="space-y-4">
        <div className="p-4 bg-orange-50 rounded-lg">
          <p className="text-sm text-orange-800 font-medium">Endpoints disponíveis para n8n:</p>
        </div>
        {[
          { method: "POST", path: "/webhooks/receive/n8n", desc: "Receber dados do n8n" },
          { method: "GET", path: "/contacts?size=100", desc: "Listar contatos" },
          { method: "POST", path: "/contacts", desc: "Criar contato" },
          { method: "POST", path: "/deals", desc: "Criar deal" },
          { method: "POST", path: "/whatsapp/conversations/{id}/send", desc: "Enviar WhatsApp" },
          { method: "POST", path: "/payments", desc: "Criar cobrança" },
        ].map(({ method, path, desc }) => (
          <div key={path} className="flex items-center gap-3 p-3 border border-gray-100 rounded-lg">
            <span className={`text-xs font-bold px-2 py-0.5 rounded ${method === "GET" ? "bg-blue-100 text-blue-700" : "bg-green-100 text-green-700"}`}>
              {method}
            </span>
            <div>
              <code className="text-xs text-gray-700">{path}</code>
              <p className="text-xs text-gray-500">{desc}</p>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
