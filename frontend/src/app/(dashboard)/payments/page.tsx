"use client";

import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import api from "@/lib/api";
import { Header } from "@/components/layout/Header";
import { formatCurrency, formatDate, STATUS_LABELS, STATUS_COLORS, cn } from "@/lib/utils";
import { Plus, ExternalLink, QrCode, Trash2 } from "lucide-react";
import type { Payment, PageResponse } from "@/types";
import toast from "react-hot-toast";
import { PaymentModal } from "@/components/payments/PaymentModal";
import { PixModal } from "@/components/payments/PixModal";

export default function PaymentsPage() {
  const [status, setStatus] = useState("");
  const [modalOpen, setModalOpen] = useState(false);
  const [pixPaymentId, setPixPaymentId] = useState<string | null>(null);
  const qc = useQueryClient();

  const { data, isLoading } = useQuery<PageResponse<Payment>>({
    queryKey: ["payments", status],
    queryFn: () => api.get("/payments", { params: { status: status || undefined, size: 20 } }).then((r) => r.data),
  });

  const cancelMutation = useMutation({
    mutationFn: (id: string) => api.delete(`/payments/${id}`),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ["payments"] }); toast.success("Cobrança cancelada"); },
    onError: () => toast.error("Erro ao cancelar cobrança"),
  });

  const billingLabels: Record<string, string> = {
    BOLETO: "Boleto", PIX: "Pix", CREDIT_CARD: "Cartão", UNDEFINED: "Indefinido",
  };

  const billingColors: Record<string, string> = {
    BOLETO: "bg-yellow-100 text-yellow-700",
    PIX: "bg-blue-100 text-blue-700",
    CREDIT_CARD: "bg-purple-100 text-purple-700",
  };

  return (
    <div>
      <Header title="Pagamentos (Asaas)" />
      <div className="p-6">
        <div className="flex gap-3 mb-6">
          <select value={status} onChange={(e) => setStatus(e.target.value)}
            className="px-3 py-2.5 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none">
            <option value="">Todos os status</option>
            <option value="PENDING">Pendente</option>
            <option value="RECEIVED">Recebido</option>
            <option value="OVERDUE">Vencido</option>
            <option value="CANCELLED">Cancelado</option>
          </select>
          <button onClick={() => setModalOpen(true)}
            className="ml-auto flex items-center gap-2 bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2.5 rounded-lg text-sm font-medium transition">
            <Plus size={16} /> Nova Cobrança
          </button>
        </div>

        <div className="bg-white rounded-xl border border-gray-200 overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 border-b border-gray-200">
              <tr>
                <th className="text-left px-4 py-3 text-gray-500 font-medium">Descrição</th>
                <th className="text-left px-4 py-3 text-gray-500 font-medium">Forma</th>
                <th className="text-right px-4 py-3 text-gray-500 font-medium">Valor</th>
                <th className="text-left px-4 py-3 text-gray-500 font-medium">Vencimento</th>
                <th className="text-left px-4 py-3 text-gray-500 font-medium">Status</th>
                <th className="px-4 py-3" />
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {isLoading ? (
                <tr><td colSpan={6} className="text-center py-12 text-gray-500">Carregando...</td></tr>
              ) : data?.content.length === 0 ? (
                <tr><td colSpan={6} className="text-center py-12 text-gray-500">Nenhum pagamento encontrado</td></tr>
              ) : (
                data?.content.map((payment) => (
                  <tr key={payment.id} className="hover:bg-gray-50">
                    <td className="px-4 py-3 font-medium text-gray-900">{payment.description}</td>
                    <td className="px-4 py-3">
                      <span className={cn("px-2 py-1 rounded-full text-xs font-medium", billingColors[payment.billingType] ?? "bg-gray-100 text-gray-700")}>
                        {billingLabels[payment.billingType] ?? payment.billingType}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-right font-semibold text-gray-900">{formatCurrency(payment.value)}</td>
                    <td className="px-4 py-3 text-gray-500">{formatDate(payment.dueDate)}</td>
                    <td className="px-4 py-3">
                      <span className={cn("px-2 py-1 rounded-full text-xs font-medium", STATUS_COLORS[payment.status] ?? "bg-gray-100 text-gray-700")}>
                        {STATUS_LABELS[payment.status] ?? payment.status}
                      </span>
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex items-center gap-1 justify-end">
                        {payment.invoiceUrl && (
                          <a href={payment.invoiceUrl} target="_blank" rel="noopener"
                            className="p-1.5 text-indigo-500 hover:bg-indigo-50 rounded-lg transition" title="Ver fatura">
                            <ExternalLink size={15} />
                          </a>
                        )}
                        {payment.billingType === "PIX" && payment.status === "PENDING" && (
                          <button onClick={() => setPixPaymentId(payment.id)}
                            className="p-1.5 text-blue-500 hover:bg-blue-50 rounded-lg transition" title="QR Code Pix">
                            <QrCode size={15} />
                          </button>
                        )}
                        {payment.status === "PENDING" && (
                          <button onClick={() => { if (confirm("Cancelar cobrança?")) cancelMutation.mutate(payment.id); }}
                            className="p-1.5 text-red-500 hover:bg-red-50 rounded-lg transition">
                            <Trash2 size={15} />
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      <PaymentModal open={modalOpen} onClose={() => setModalOpen(false)} />
      <PixModal paymentId={pixPaymentId} onClose={() => setPixPaymentId(null)} />
    </div>
  );
}
