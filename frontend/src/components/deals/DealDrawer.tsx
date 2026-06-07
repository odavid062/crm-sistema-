"use client";

import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import api from "@/lib/api";
import toast from "react-hot-toast";
import { X, DollarSign, Calendar, TrendingUp, User, CheckCircle, XCircle } from "lucide-react";
import { cn, STATUS_COLORS, STATUS_LABELS, formatCurrency, formatDate, getInitials } from "@/lib/utils";
import type { Deal } from "@/types";

interface Props {
  dealId: string | null;
  onClose: () => void;
}

export function DealDrawer({ dealId, onClose }: Props) {
  const qc = useQueryClient();

  const { data: deal, isLoading } = useQuery<Deal>({
    queryKey: ["deal", dealId],
    queryFn: () => api.get(`/deals/${dealId}`).then((r) => r.data),
    enabled: !!dealId,
  });

  const statusMutation = useMutation({
    mutationFn: ({ status, lostReason }: { status: string; lostReason?: string }) =>
      api.patch(`/deals/${dealId}/status`, { status, lostReason }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["deals"] });
      qc.invalidateQueries({ queryKey: ["deals-list"] });
      qc.invalidateQueries({ queryKey: ["deal", dealId] });
      toast.success("Deal atualizado!");
    },
    onError: () => toast.error("Erro ao atualizar deal"),
  });

  const deleteMutation = useMutation({
    mutationFn: () => api.delete(`/deals/${dealId}`),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["deals"] });
      qc.invalidateQueries({ queryKey: ["deals-list"] });
      toast.success("Deal excluído!");
      onClose();
    },
    onError: () => toast.error("Erro ao excluir deal"),
  });

  if (!dealId) return null;

  const priorityColors: Record<string, string> = {
    HIGH: "text-red-600 bg-red-50",
    MEDIUM: "text-yellow-600 bg-yellow-50",
    LOW: "text-gray-600 bg-gray-100",
  };

  return (
    <>
      <div className="fixed inset-0 z-40 bg-black/30" onClick={onClose} />
      <div className="fixed right-0 top-0 bottom-0 z-50 w-full max-w-md bg-white shadow-2xl flex flex-col">
        <div className="flex items-center justify-between p-5 border-b border-gray-200">
          <h2 className="font-semibold text-gray-900">Detalhes do Deal</h2>
          <button onClick={onClose} className="p-1.5 hover:bg-gray-100 rounded-lg transition">
            <X size={18} />
          </button>
        </div>

        <div className="flex-1 overflow-y-auto p-5">
          {isLoading ? (
            <div className="animate-pulse space-y-4">
              <div className="h-6 bg-gray-200 rounded w-3/4" />
              <div className="h-4 bg-gray-100 rounded w-1/2" />
            </div>
          ) : deal ? (
            <div className="space-y-6">
              {/* Title & Status */}
              <div>
                <h3 className="text-xl font-bold text-gray-900 mb-2">{deal.title}</h3>
                <div className="flex items-center gap-2 flex-wrap">
                  <span className={cn("px-2.5 py-1 rounded-full text-xs font-medium", STATUS_COLORS[deal.status])}>
                    {STATUS_LABELS[deal.status]}
                  </span>
                  <span className={cn("px-2.5 py-1 rounded-full text-xs font-medium", priorityColors[deal.priority] ?? "bg-gray-100 text-gray-700")}>
                    {deal.priority}
                  </span>
                </div>
              </div>

              {/* Value */}
              <div className="bg-indigo-50 rounded-xl p-4 flex items-center gap-3">
                <div className="w-10 h-10 bg-indigo-100 rounded-lg flex items-center justify-center">
                  <DollarSign size={20} className="text-indigo-600" />
                </div>
                <div>
                  <p className="text-xs text-indigo-600 font-medium">Valor do Deal</p>
                  <p className="text-2xl font-bold text-indigo-700">{formatCurrency(deal.value)}</p>
                </div>
              </div>

              {/* Details */}
              <div className="space-y-3">
                <div className="flex items-center gap-3 text-sm">
                  <TrendingUp size={15} className="text-gray-400 flex-shrink-0" />
                  <div className="flex items-center gap-2">
                    <div className="w-2.5 h-2.5 rounded-full" style={{ backgroundColor: deal.stageColor }} />
                    <span className="text-gray-700">{deal.pipelineName} → {deal.stageName}</span>
                  </div>
                </div>
                {deal.contactName && (
                  <div className="flex items-center gap-3 text-sm">
                    <User size={15} className="text-gray-400 flex-shrink-0" />
                    <span className="text-gray-700">{deal.contactName}</span>
                  </div>
                )}
                {deal.companyName && (
                  <div className="flex items-center gap-3 text-sm">
                    <User size={15} className="text-gray-400 flex-shrink-0" />
                    <span className="text-gray-700">{deal.companyName}</span>
                  </div>
                )}
                {deal.expectedCloseDate && (
                  <div className="flex items-center gap-3 text-sm">
                    <Calendar size={15} className="text-gray-400 flex-shrink-0" />
                    <span className="text-gray-500">Previsto: {formatDate(deal.expectedCloseDate)}</span>
                  </div>
                )}
                {deal.closedAt && (
                  <div className="flex items-center gap-3 text-sm">
                    <Calendar size={15} className="text-gray-400 flex-shrink-0" />
                    <span className="text-gray-500">Fechado em: {formatDate(deal.closedAt)}</span>
                  </div>
                )}
                <div className="flex items-center gap-3 text-sm">
                  <Calendar size={15} className="text-gray-400 flex-shrink-0" />
                  <span className="text-gray-500">Criado em {formatDate(deal.createdAt)}</span>
                </div>
              </div>

              {/* Actions */}
              {deal.status === "OPEN" && (
                <div className="space-y-2">
                  <p className="text-xs font-medium text-gray-500 mb-2">Ações</p>
                  <button
                    onClick={() => statusMutation.mutate({ status: "WON" })}
                    disabled={statusMutation.isPending}
                    className="w-full flex items-center justify-center gap-2 bg-green-600 hover:bg-green-700 text-white px-4 py-2.5 rounded-lg text-sm font-medium transition disabled:opacity-60">
                    <CheckCircle size={16} /> Marcar como Ganho
                  </button>
                  <button
                    onClick={() => {
                      const reason = prompt("Motivo da perda (opcional):");
                      statusMutation.mutate({ status: "LOST", lostReason: reason ?? undefined });
                    }}
                    disabled={statusMutation.isPending}
                    className="w-full flex items-center justify-center gap-2 bg-red-50 hover:bg-red-100 text-red-600 px-4 py-2.5 rounded-lg text-sm font-medium transition disabled:opacity-60">
                    <XCircle size={16} /> Marcar como Perdido
                  </button>
                </div>
              )}

              {deal.lostReason && (
                <div className="bg-red-50 rounded-lg p-3">
                  <p className="text-xs font-medium text-red-600 mb-1">Motivo da perda</p>
                  <p className="text-sm text-red-700">{deal.lostReason}</p>
                </div>
              )}
            </div>
          ) : null}
        </div>

        <div className="p-5 border-t border-gray-200">
          <button
            onClick={() => {
              if (confirm("Excluir este deal?")) deleteMutation.mutate();
            }}
            disabled={deleteMutation.isPending}
            className="w-full px-4 py-2.5 border border-red-200 text-red-600 hover:bg-red-50 rounded-lg text-sm font-medium transition disabled:opacity-60">
            {deleteMutation.isPending ? "Excluindo..." : "Excluir Deal"}
          </button>
        </div>
      </div>
    </>
  );
}
