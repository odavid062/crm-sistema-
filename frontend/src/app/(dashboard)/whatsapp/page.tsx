"use client";

import { useState, useEffect, useRef } from "react";
import { useQuery, useMutation } from "@tanstack/react-query";
import api from "@/lib/api";
import { Header } from "@/components/layout/Header";
import { formatDateTime, cn, getInitials } from "@/lib/utils";
import { Send, Search, MessageCircle, CheckCheck } from "lucide-react";
import type { WhatsAppConversation, WhatsAppMessage } from "@/types";

export default function WhatsAppPage() {
  const [selectedConv, setSelectedConv] = useState<WhatsAppConversation | null>(null);
  const [message, setMessage] = useState("");
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const { data: convData } = useQuery<{ content: WhatsAppConversation[] }>({
    queryKey: ["conversations"],
    queryFn: () => api.get("/whatsapp/conversations", { params: { size: 50 } }).then((r) => r.data),
    refetchInterval: 5000,
  });

  const { data: messagesData, refetch: refetchMessages } = useQuery<{ content: WhatsAppMessage[] }>({
    queryKey: ["messages", selectedConv?.id],
    queryFn: () => api.get(`/whatsapp/conversations/${selectedConv!.id}/messages`, { params: { size: 100 } }).then((r) => r.data),
    enabled: !!selectedConv?.id,
    refetchInterval: 3000,
  });

  const sendMutation = useMutation({
    mutationFn: (text: string) => api.post(`/whatsapp/conversations/${selectedConv!.id}/send`, { to: selectedConv!.remoteJid, text }),
    onSuccess: () => { refetchMessages(); setMessage(""); },
  });

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messagesData]);

  const conversations = convData?.content ?? [];
  const messages = messagesData?.content ?? [];

  return (
    <div className="flex flex-col h-full">
      <Header title="WhatsApp" />
      <div className="flex flex-1 overflow-hidden">
        {/* Conversations list */}
        <div className="w-80 border-r border-gray-200 bg-white flex flex-col">
          <div className="p-4 border-b border-gray-200">
            <div className="relative">
              <Search size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
              <input placeholder="Buscar conversa..." className="w-full pl-8 pr-3 py-2 text-sm border border-gray-200 rounded-lg outline-none focus:ring-2 focus:ring-indigo-500" />
            </div>
          </div>
          <div className="flex-1 overflow-y-auto">
            {conversations.length === 0 ? (
              <div className="flex flex-col items-center justify-center h-full text-gray-400 p-6 text-center">
                <MessageCircle size={40} className="mb-3 opacity-40" />
                <p className="text-sm">Nenhuma conversa ainda</p>
                <p className="text-xs mt-1">Configure o webhook UazAPI para receber mensagens</p>
              </div>
            ) : (
              conversations.map((conv) => (
                <button
                  key={conv.id}
                  onClick={() => setSelectedConv(conv)}
                  className={cn(
                    "w-full flex items-center gap-3 p-4 hover:bg-gray-50 transition text-left border-b border-gray-100",
                    selectedConv?.id === conv.id && "bg-indigo-50 border-indigo-100"
                  )}
                >
                  <div className="w-10 h-10 bg-green-100 rounded-full flex items-center justify-center text-green-700 font-semibold text-sm flex-shrink-0">
                    {getInitials(conv.contactName || conv.remoteJid)}
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center justify-between">
                      <p className="font-medium text-sm text-gray-900 truncate">{conv.contactName || conv.remoteJid}</p>
                      {conv.unreadCount > 0 && (
                        <span className="bg-green-500 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center ml-2">
                          {conv.unreadCount}
                        </span>
                      )}
                    </div>
                    <p className="text-xs text-gray-400 mt-0.5">
                      {conv.lastMessageAt ? formatDateTime(conv.lastMessageAt) : "—"}
                    </p>
                  </div>
                </button>
              ))
            )}
          </div>
        </div>

        {/* Chat area */}
        {selectedConv ? (
          <div className="flex-1 flex flex-col bg-gray-50">
            {/* Chat header */}
            <div className="bg-white border-b border-gray-200 p-4 flex items-center gap-3">
              <div className="w-10 h-10 bg-green-100 rounded-full flex items-center justify-center text-green-700 font-semibold text-sm">
                {getInitials(selectedConv.contactName || selectedConv.remoteJid)}
              </div>
              <div>
                <p className="font-semibold text-gray-900">{selectedConv.contactName || selectedConv.remoteJid}</p>
                <p className="text-xs text-green-500">Online</p>
              </div>
            </div>

            {/* Messages */}
            <div className="flex-1 overflow-y-auto p-4 space-y-3">
              {messages.map((msg) => (
                <div key={msg.id} className={cn("flex", msg.direction === "OUT" ? "justify-end" : "justify-start")}>
                  <div
                    className={cn(
                      "max-w-xs lg:max-w-md px-4 py-2.5 rounded-2xl text-sm",
                      msg.direction === "OUT"
                        ? "bg-indigo-600 text-white rounded-br-sm"
                        : "bg-white text-gray-900 shadow-sm rounded-bl-sm"
                    )}
                  >
                    <p>{msg.content}</p>
                    <p className={cn("text-xs mt-1 flex items-center gap-1 justify-end", msg.direction === "OUT" ? "text-indigo-200" : "text-gray-400")}>
                      {formatDateTime(msg.timestamp)}
                      {msg.direction === "OUT" && <CheckCheck size={12} />}
                    </p>
                  </div>
                </div>
              ))}
              <div ref={messagesEndRef} />
            </div>

            {/* Input */}
            <div className="bg-white border-t border-gray-200 p-4 flex items-center gap-3">
              <input
                value={message}
                onChange={(e) => setMessage(e.target.value)}
                onKeyDown={(e) => { if (e.key === "Enter" && message.trim()) { sendMutation.mutate(message.trim()); } }}
                placeholder="Digite uma mensagem..."
                className="flex-1 px-4 py-2.5 border border-gray-200 rounded-xl text-sm focus:ring-2 focus:ring-indigo-500 outline-none"
              />
              <button
                onClick={() => { if (message.trim()) sendMutation.mutate(message.trim()); }}
                disabled={!message.trim() || sendMutation.isPending}
                className="w-10 h-10 bg-indigo-600 hover:bg-indigo-700 disabled:opacity-40 text-white rounded-xl flex items-center justify-center transition"
              >
                <Send size={16} />
              </button>
            </div>
          </div>
        ) : (
          <div className="flex-1 flex items-center justify-center bg-gray-50">
            <div className="text-center text-gray-400">
              <MessageCircle size={48} className="mx-auto mb-3 opacity-30" />
              <p>Selecione uma conversa</p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
