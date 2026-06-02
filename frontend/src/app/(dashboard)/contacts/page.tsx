"use client";

import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import api from "@/lib/api";
import { Header } from "@/components/layout/Header";
import { formatDate, STATUS_LABELS, STATUS_COLORS, cn, getInitials } from "@/lib/utils";
import { Plus, Search, Filter, Trash2, Eye, MessageCircle } from "lucide-react";
import type { Contact, PageResponse } from "@/types";
import toast from "react-hot-toast";
import { ContactModal } from "@/components/contacts/ContactModal";

export default function ContactsPage() {
  const [search, setSearch] = useState("");
  const [status, setStatus] = useState("");
  const [page, setPage] = useState(0);
  const [modalOpen, setModalOpen] = useState(false);
  const qc = useQueryClient();

  const { data, isLoading } = useQuery<PageResponse<Contact>>({
    queryKey: ["contacts", search, status, page],
    queryFn: () =>
      api.get("/contacts", { params: { search: search || undefined, status: status || undefined, page, size: 20 } })
        .then((r) => r.data),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => api.delete(`/contacts/${id}`),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ["contacts"] }); toast.success("Contato excluído"); },
    onError: () => toast.error("Erro ao excluir contato"),
  });

  return (
    <div>
      <Header title="Contatos" />
      <div className="p-6">
        {/* Toolbar */}
        <div className="flex flex-col sm:flex-row gap-3 mb-6">
          <div className="relative flex-1">
            <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
            <input
              placeholder="Buscar por nome, email ou telefone..."
              value={search}
              onChange={(e) => { setSearch(e.target.value); setPage(0); }}
              className="w-full pl-9 pr-4 py-2.5 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none"
            />
          </div>
          <select
            value={status}
            onChange={(e) => { setStatus(e.target.value); setPage(0); }}
            className="px-3 py-2.5 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none"
          >
            <option value="">Todos os status</option>
            <option value="LEAD">Lead</option>
            <option value="PROSPECT">Prospect</option>
            <option value="CUSTOMER">Cliente</option>
            <option value="CHURNED">Perdido</option>
          </select>
          <button
            onClick={() => setModalOpen(true)}
            className="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2.5 rounded-lg text-sm font-medium transition"
          >
            <Plus size={16} />
            Novo Contato
          </button>
        </div>

        {/* Table */}
        <div className="bg-white rounded-xl border border-gray-200 overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 border-b border-gray-200">
              <tr>
                <th className="text-left px-4 py-3 text-gray-500 font-medium">Contato</th>
                <th className="text-left px-4 py-3 text-gray-500 font-medium hidden md:table-cell">Email / Telefone</th>
                <th className="text-left px-4 py-3 text-gray-500 font-medium">Status</th>
                <th className="text-left px-4 py-3 text-gray-500 font-medium hidden lg:table-cell">Empresa</th>
                <th className="text-left px-4 py-3 text-gray-500 font-medium hidden lg:table-cell">Criado em</th>
                <th className="px-4 py-3" />
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {isLoading ? (
                <tr><td colSpan={6} className="text-center py-12 text-gray-500">Carregando...</td></tr>
              ) : data?.content.length === 0 ? (
                <tr><td colSpan={6} className="text-center py-12 text-gray-500">Nenhum contato encontrado</td></tr>
              ) : (
                data?.content.map((contact) => (
                  <tr key={contact.id} className="hover:bg-gray-50">
                    <td className="px-4 py-3">
                      <div className="flex items-center gap-3">
                        <div className="w-9 h-9 rounded-full bg-indigo-100 flex items-center justify-center text-indigo-700 font-semibold text-sm">
                          {getInitials(contact.name)}
                        </div>
                        <span className="font-medium text-gray-900">{contact.name}</span>
                      </div>
                    </td>
                    <td className="px-4 py-3 hidden md:table-cell">
                      <p className="text-gray-700">{contact.email}</p>
                      <p className="text-gray-400 text-xs">{contact.phone}</p>
                    </td>
                    <td className="px-4 py-3">
                      <span className={cn("px-2 py-1 rounded-full text-xs font-medium", STATUS_COLORS[contact.status])}>
                        {STATUS_LABELS[contact.status]}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-gray-500 hidden lg:table-cell">{contact.companyName ?? "—"}</td>
                    <td className="px-4 py-3 text-gray-400 hidden lg:table-cell">{formatDate(contact.createdAt)}</td>
                    <td className="px-4 py-3">
                      <div className="flex items-center gap-1 justify-end">
                        {contact.whatsapp && (
                          <button className="p-1.5 text-green-600 hover:bg-green-50 rounded-lg transition" title="WhatsApp">
                            <MessageCircle size={16} />
                          </button>
                        )}
                        <button className="p-1.5 text-gray-500 hover:bg-gray-100 rounded-lg transition">
                          <Eye size={16} />
                        </button>
                        <button
                          onClick={() => deleteMutation.mutate(contact.id)}
                          className="p-1.5 text-red-500 hover:bg-red-50 rounded-lg transition"
                        >
                          <Trash2 size={16} />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>

          {/* Pagination */}
          {data && data.totalPages > 1 && (
            <div className="flex items-center justify-between px-4 py-3 border-t border-gray-200">
              <p className="text-sm text-gray-500">
                {data.totalElements} contatos · Página {data.number + 1} de {data.totalPages}
              </p>
              <div className="flex gap-2">
                <button disabled={page === 0} onClick={() => setPage(p => p - 1)}
                  className="px-3 py-1.5 text-sm border rounded-lg disabled:opacity-40 hover:bg-gray-50">
                  Anterior
                </button>
                <button disabled={page >= data.totalPages - 1} onClick={() => setPage(p => p + 1)}
                  className="px-3 py-1.5 text-sm border rounded-lg disabled:opacity-40 hover:bg-gray-50">
                  Próxima
                </button>
              </div>
            </div>
          )}
        </div>
      </div>

      <ContactModal open={modalOpen} onClose={() => setModalOpen(false)} />
    </div>
  );
}
