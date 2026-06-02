"use client";

import { useState, useEffect, useRef } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import api from "@/lib/api";
import { Header } from "@/components/layout/Header";
import { formatDateTime, cn, getInitials } from "@/lib/utils";
import { Send, Search, MessageCircle, CheckCheck, Check, RotateCcw, User } from "lucide-react";

const FILTERS = [
  { key: "OPEN", label: "Abertos" },
  { key: "PENDING", label: "Pendentes" },
  { key: "RESOLVED", label: "Resolvidos" },
  { key: "ALL", label: "Todos" },
];

const STATUS_BADGE: Record<string, { label: string; cls: string }> = {
  OPEN: { label: "Aberto", cls: "bg-green-100 text-green-700" },
  PENDING: { label: "Pendente", cls: "bg-amber-100 text-amber-700" },
  RESOLVED: { label: "Resolvido", cls: "bg-gray-100 text-gray-600" },
  SPAM: { label: "Spam", cls: "bg-red-100 text-red-600" },
};

export default function AtendimentosPage() {
  const qc = useQueryClient();
  const [filter, setFilter] = useState("OPEN");
  const [selected, setSelected] = useState<any | null>(null);
  const [message, setMessage] = useState("");
  const [search, setSearch] = useState("");
  const endRef = useRef<HTMLDivElement>(null);

  const { data: convData } = useQuery<{ content: any[] }>({
    queryKey: ["atendimentos", filter],
    queryFn: () => api.get("/whatsapp/conversations", {
      params: { size: 50, ...(filter !== "ALL" ? { status: filter } : {}) },
    }).then((r) => r.data),
    refetchInterval: 5000,
  });

  const { data: msgData, refetch: refetchMsgs } = useQuery<{ content: any[] }>({
    queryKey: ["atendimento-msgs", selected?.id],
    queryFn: () => api.get(`/whatsapp/conversations/${selected!.id}/messages`, { params: { size: 100 } }).then((r) => r.data),
    enabled: !!selected?.id,
    refetchInterval: 3000,
  });

  const send = useMutation({
    mutationFn: (text: string) => api.post(`/whatsapp/conversations/${selected!.id}/send`, { to: selected!.remoteJid, text }),
    onSuccess: () => { refetchMsgs(); setMessage(""); },
  });

  const changeStatus = useMutation({
    mutationFn: ({ id, status }: { id: string; status: string }) =>
      api.patch(`/whatsapp/conversations/${id}/status`, { status }),
    onSuccess: (_d, v) => {
      qc.invalidateQueries({ queryKey: ["atendimentos"] });
      setSelected((s: any) => (s ? { ...s, status: v.status } : s));
    },
  });

  useEffect(() => { endRef.current?.scrollIntoView({ behavior: "smooth" }); }, [msgData]);

  const conversations = (convData?.content ?? []).filter((c) =>
    !search || (c.contactName || c.remoteJid || "").toLowerCase().includes(search.toLowerCase()));
  const messages = msgData?.content ?? [];

  return (
    <div className="flex flex-col h-full">
      <Header title="Atendimentos" />
      <div className="flex flex-1 overflow-hidden">
        {/* Lista de conversas */}
        <div className="w-80 border-r border-gray-200 bg-white flex flex-col">
          <div className="p-3 border-b border-gray-200 space-y-3">
            <div className="relative">
              <Search size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
              <input value={search} onChange={(e) => setSearch(e.target.value)} placeholder="Buscar atendimento..."
                className="w-full pl-8 pr-3 py-2 text-sm border border-gray-200 rounded-lg outline-none focus:ring-2 focus:ring-indigo-500" />
            </div>
            <div className="flex gap-1">
              {FILTERS.map((f) => (
                <button key={f.key} onClick={() => setFilter(f.key)}
                  className={cn("flex-1 text-xs py-1.5 rounded-md font-medium transition",
                    filter === f.key ? "bg-indigo-600 text-white" : "bg-gray-100 text-gray-500 hover:bg-gray-200")}>
                  {f.label}
                </button>
              ))}
            </div>
          </div>
          <div className="flex-1 overflow-y-auto">
            {conversations.length === 0 ? (
              <div className="flex flex-col items-center justify-center h-full text-gray-400 p-6 text-center">
                <MessageCircle size={40} className="mb-3 opacity-40" />
                <p className="text-sm">Nenhum atendimento {filter !== "ALL" ? `(${FILTERS.find(f => f.key === filter)?.label.toLowerCase()})` : ""}</p>
                <p className="text-xs mt-1">As conversas do WhatsApp aparecem aqui automaticamente</p>
              </div>
            ) : conversations.map((conv) => (
              <button key={conv.id} onClick={() => setSelected(conv)}
                className={cn("w-full flex items-center gap-3 p-3 hover:bg-gray-50 transition text-left border-b border-gray-100",
                  selected?.id === conv.id && "bg-indigo-50 border-indigo-100")}>
                <div className="relative flex-shrink-0">
                  <div className="w-10 h-10 bg-green-100 rounded-full flex items-center justify-center text-green-700 font-semibold text-sm">
                    {getInitials(conv.contactName || conv.remoteJid)}
                  </div>
                  <span className="absolute -bottom-0.5 -right-0.5 w-4 h-4 bg-green-500 rounded-full border-2 border-white flex items-center justify-center">
                    <MessageCircle size={8} className="text-white" />
                  </span>
                </div>
                <div className="flex-1 min-w-0">
                  <div className="flex items-center justify-between gap-2">
                    <p className="font-medium text-sm text-gray-900 truncate">{conv.contactName || conv.remoteJid}</p>
                    {conv.unreadCount > 0 && (
                      <span className="bg-green-500 text-white text-xs rounded-full min-w-5 h-5 px-1 flex items-center justify-center">{conv.unreadCount}</span>
                    )}
                  </div>
                  <div className="flex items-center justify-between mt-0.5">
                    <span className={cn("text-[10px] px-1.5 py-0.5 rounded", STATUS_BADGE[conv.status]?.cls)}>{STATUS_BADGE[conv.status]?.label ?? conv.status}</span>
                    <p className="text-xs text-gray-400">{conv.lastMessageAt ? formatDateTime(conv.lastMessageAt) : "—"}</p>
                  </div>
                </div>
              </button>
            ))}
          </div>
        </div>

        {/* Chat */}
        {selected ? (
          <div className="flex-1 flex flex-col bg-gray-50">
            <div className="bg-white border-b border-gray-200 p-3 flex items-center gap-3">
              <div className="w-10 h-10 bg-green-100 rounded-full flex items-center justify-center text-green-700 font-semibold text-sm">
                {getInitials(selected.contactName || selected.remoteJid)}
              </div>
              <div className="flex-1">
                <p className="font-semibold text-gray-900">{selected.contactName || selected.remoteJid}</p>
                <p className="text-xs text-gray-400 flex items-center gap-1">
                  <MessageCircle size={11} className="text-green-500" /> WhatsApp
                  {selected.assignedToName && <span className="ml-2 flex items-center gap-1"><User size={10} /> {selected.assignedToName}</span>}
                </p>
              </div>
              {selected.status !== "RESOLVED" ? (
                <button onClick={() => changeStatus.mutate({ id: selected.id, status: "RESOLVED" })}
                  className="flex items-center gap-1.5 text-sm bg-green-600 hover:bg-green-700 text-white px-3 py-1.5 rounded-lg">
                  <Check size={15} /> Resolver
                </button>
              ) : (
                <button onClick={() => changeStatus.mutate({ id: selected.id, status: "OPEN" })}
                  className="flex items-center gap-1.5 text-sm border border-gray-200 hover:bg-gray-50 px-3 py-1.5 rounded-lg">
                  <RotateCcw size={15} /> Reabrir
                </button>
              )}
            </div>

            <div className="flex-1 overflow-y-auto p-4 space-y-3">
              {messages.map((msg) => (
                <div key={msg.id} className={cn("flex", msg.direction === "OUT" ? "justify-end" : "justify-start")}>
                  <div className={cn("max-w-xs lg:max-w-md px-4 py-2.5 rounded-2xl text-sm",
                    msg.direction === "OUT" ? "bg-indigo-600 text-white rounded-br-sm" : "bg-white text-gray-900 shadow-sm rounded-bl-sm")}>
                    <p className="whitespace-pre-wrap">{msg.content}</p>
                    <p className={cn("text-xs mt-1 flex items-center gap-1 justify-end", msg.direction === "OUT" ? "text-indigo-200" : "text-gray-400")}>
                      {formatDateTime(msg.timestamp)}
                      {msg.direction === "OUT" && <CheckCheck size={12} />}
                    </p>
                  </div>
                </div>
              ))}
              <div ref={endRef} />
            </div>

            <div className="bg-white border-t border-gray-200 p-3 flex items-center gap-3">
              <input value={message} onChange={(e) => setMessage(e.target.value)}
                onKeyDown={(e) => { if (e.key === "Enter" && message.trim()) send.mutate(message.trim()); }}
                placeholder="Digite uma mensagem..."
                className="flex-1 px-4 py-2.5 border border-gray-200 rounded-xl text-sm focus:ring-2 focus:ring-indigo-500 outline-none" />
              <button onClick={() => message.trim() && send.mutate(message.trim())}
                disabled={!message.trim() || send.isPending}
                className="w-10 h-10 bg-indigo-600 hover:bg-indigo-700 disabled:opacity-40 text-white rounded-xl flex items-center justify-center transition">
                <Send size={16} />
              </button>
            </div>
          </div>
        ) : (
          <div className="flex-1 flex items-center justify-center bg-gray-50">
            <div className="text-center text-gray-400">
              <MessageCircle size={48} className="mx-auto mb-3 opacity-30" />
              <p>Selecione um atendimento</p>
              <p className="text-xs mt-1">As conversas do WhatsApp (e outros canais) aparecem à esquerda</p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
