"use client";

import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import api from "@/lib/api";
import { Header } from "@/components/layout/Header";
import { formatCurrency, formatDate, STATUS_LABELS, STATUS_COLORS, cn } from "@/lib/utils";
import { Plus, Search, TrendingUp, Eye, Trash2 } from "lucide-react";
import type { Deal, PageResponse } from "@/types";
import toast from "react-hot-toast";
import { DealModal } from "@/components/deals/DealModal";
import { DealDrawer } from "@/components/deals/DealDrawer";

export default function DealsPage() {
  const [search, setSearch] = useState("");
  const [status, setStatus] = useState("");
  const [page, setPage] = useState(0);
  const [modalOpen, setModalOpen] = useState(false);
  const [selectedDealId, setSelectedDealId] = useState<string | null>(null);
  const qc = useQueryClient();

  const { data, isLoading } = useQuery<PageResponse<Deal>>({
    queryKey: ["deals-list", search, status, page],
    queryFn: () => api.get("/deals", { params: { search: search || undefined, status: status || undefined, page, size: 20 } }).then((r) => r.data),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => api.delete(`/deals/${id}`),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ["deals-list"] }); toast.success("Deal excluído"); },
    onError: () => toast.error("Erro ao excluir deal"),
  });

  const priorityColors: Record<string, string> = {
    HIGH: "text-red-600 bg-red-50",
    MEDIUM: "text-yellow-600 bg-yellow-50",
    LOW: "text-gray-600 bg-gray-100",
  };

  const priorityLabels: Record<string, string> = { HIGH: "Alta", MEDIUM: "Média", LOW: "Baixa" };

  return (
    <div>
      <Header title="Deals" />
      <div className="p-6">
        <div className="flex gap-3 mb-6">
          <div className="relative flex-1">
            <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
            <input placeholder="Buscar deal..." value={search} onChange={(e) => { setSearch(e.target.value); setPage(0); }}
              className="w-full pl-9 pr-4 py-2.5 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none" />
          </div>
          <select value={status} onChange={(e) => { setStatus(e.target.value); setPage(0); }}
            className="px-3 py-2.5 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none">
            <option value="">Todos os status</option>
            <option value="OPEN">Abertos</option>
            <option value="WON">Ganhos</option>
            <option value="LOST">Perdidos</option>
          </select>
          <button onClick={() => setModalOpen(true)}
            className="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2.5 rounded-lg text-sm font-medium transition">
            <Plus size={16} /> Novo Deal
          </button>
        </div>

        <div className="bg-white rounded-xl border border-gray-200 overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 border-b border-gray-200">
              <tr>
                <th className="text-left px-4 py-3 text-gray-500 font-medium">Deal</th>
                <th className="text-left px-4 py-3 text-gray-500 font-medium hidden md:table-cell">Pipeline</th>
                <th className="text-right px-4 py-3 text-gray-500 font-medium">Valor</th>
                <th className="text-left px-4 py-3 text-gray-500 font-medium">Status</th>
                <th className="text-left px-4 py-3 text-gray-500 font-medium hidden lg:table-cell">Prioridade</th>
                <th className="text-left px-4 py-3 text-gray-500 font-medium hidden lg:table-cell">Fechamento</th>
                <th className="px-4 py-3" />
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {isLoading ? (
                <tr><td colSpan={7} className="text-center py-12 text-gray-500">Carregando...</td></tr>
              ) : data?.content.length === 0 ? (
                <tr>
                  <td colSpan={7} className="text-center py-16">
                    <TrendingUp size={40} className="mx-auto text-gray-200 mb-3" />
                    <p className="text-gray-500">Nenhum deal encontrado</p>
                  </td>
                </tr>
              ) : (
                data?.content.map((deal) => (
                  <tr key={deal.id} className="hover:bg-gray-50">
                    <td className="px-4 py-3">
                      <p className="font-medium text-gray-900">{deal.title}</p>
                      {deal.contactName && <p className="text-xs text-gray-400 mt-0.5">{deal.contactName}</p>}
                    </td>
                    <td className="px-4 py-3 hidden md:table-cell">
                      <div className="flex items-center gap-2">
                        <div className="w-2 h-2 rounded-full" style={{ backgroundColor: deal.stageColor }} />
                        <span className="text-gray-600">{deal.stageName}</span>
                      </div>
                    </td>
                    <td className="px-4 py-3 text-right font-semibold text-gray-900">{formatCurrency(deal.value)}</td>
                    <td className="px-4 py-3">
                      <span className={cn("px-2 py-1 rounded-full text-xs font-medium", STATUS_COLORS[deal.status])}>
                        {STATUS_LABELS[deal.status]}
                      </span>
                    </td>
                    <td className="px-4 py-3 hidden lg:table-cell">
                      <span className={cn("px-2 py-1 rounded-full text-xs font-medium", priorityColors[deal.priority] ?? "bg-gray-100 text-gray-700")}>
                        {priorityLabels[deal.priority] ?? deal.priority}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-gray-500 hidden lg:table-cell">
                      {deal.expectedCloseDate ? formatDate(deal.expectedCloseDate) : "—"}
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex items-center gap-1 justify-end">
                        <button onClick={() => setSelectedDealId(deal.id)}
                          className="p-1.5 text-gray-500 hover:bg-gray-100 rounded-lg transition" title="Ver detalhes">
                          <Eye size={15} />
                        </button>
                        <button onClick={() => { if (confirm("Excluir deal?")) deleteMutation.mutate(deal.id); }}
                          className="p-1.5 text-red-500 hover:bg-red-50 rounded-lg transition">
                          <Trash2 size={15} />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>

          {data && data.totalPages > 1 && (
            <div className="flex items-center justify-between px-4 py-3 border-t border-gray-200">
              <p className="text-sm text-gray-500">{data.totalElements} deals · Página {data.number + 1} de {data.totalPages}</p>
              <div className="flex gap-2">
                <button disabled={page === 0} onClick={() => setPage(p => p - 1)}
                  className="px-3 py-1.5 text-sm border rounded-lg disabled:opacity-40 hover:bg-gray-50">Anterior</button>
                <button disabled={page >= data.totalPages - 1} onClick={() => setPage(p => p + 1)}
                  className="px-3 py-1.5 text-sm border rounded-lg disabled:opacity-40 hover:bg-gray-50">Próxima</button>
              </div>
            </div>
          )}
        </div>
      </div>

      <DealModal open={modalOpen} onClose={() => setModalOpen(false)} />
      <DealDrawer dealId={selectedDealId} onClose={() => setSelectedDealId(null)} />
    </div>
  );
}
