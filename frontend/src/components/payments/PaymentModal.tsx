"use client";

import { useMutation, useQueryClient, useQuery } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import api from "@/lib/api";
import toast from "react-hot-toast";
import { X } from "lucide-react";
import type { Contact } from "@/types";

interface Props {
  open: boolean;
  onClose: () => void;
}

interface FormData {
  contactId: string;
  description: string;
  value: string;
  billingType: string;
  dueDate: string;
}

export function PaymentModal({ open, onClose }: Props) {
  const qc = useQueryClient();

  const { register, handleSubmit, reset, formState: { errors } } = useForm<FormData>({
    defaultValues: { billingType: "PIX" },
  });

  const { data: contacts } = useQuery<{ content: Contact[] }>({
    queryKey: ["contacts-simple"],
    queryFn: () => api.get("/contacts", { params: { size: 100 } }).then((r) => r.data),
    enabled: open,
  });

  const mutation = useMutation({
    mutationFn: (data: FormData) =>
      api.post("/payments", {
        contactId: data.contactId,
        description: data.description,
        value: Number(data.value),
        billingType: data.billingType,
        dueDate: data.dueDate,
      }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["payments"] });
      toast.success("Cobrança criada!");
      reset();
      onClose();
    },
    onError: () => toast.error("Erro ao criar cobrança. Verifique se o Asaas está configurado."),
  });

  if (!open) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm p-4">
      <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md">
        <div className="flex items-center justify-between p-6 border-b border-gray-200">
          <h2 className="text-lg font-semibold">Nova Cobrança</h2>
          <button onClick={onClose} className="p-1 hover:bg-gray-100 rounded-lg transition"><X size={20} /></button>
        </div>

        <form onSubmit={handleSubmit((d) => mutation.mutate(d))} className="p-6 space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Contato *</label>
            <select {...register("contactId", { required: true })}
              className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none">
              <option value="">Selecionar contato...</option>
              {contacts?.content.map((c) => (
                <option key={c.id} value={c.id}>{c.name}</option>
              ))}
            </select>
            {errors.contactId && <p className="text-red-500 text-xs mt-1">Campo obrigatório</p>}
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Descrição *</label>
            <input {...register("description", { required: true })}
              className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none" />
            {errors.description && <p className="text-red-500 text-xs mt-1">Campo obrigatório</p>}
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Valor (R$) *</label>
              <input type="number" step="0.01" {...register("value", { required: true, min: 0.01 })} placeholder="0,00"
                className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none" />
              {errors.value && <p className="text-red-500 text-xs mt-1">Valor inválido</p>}
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Forma</label>
              <select {...register("billingType")}
                className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none">
                <option value="PIX">Pix</option>
                <option value="BOLETO">Boleto</option>
                <option value="CREDIT_CARD">Cartão</option>
              </select>
            </div>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Vencimento *</label>
            <input type="date" {...register("dueDate", { required: true })}
              className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none" />
            {errors.dueDate && <p className="text-red-500 text-xs mt-1">Campo obrigatório</p>}
          </div>

          <div className="flex gap-3 pt-2">
            <button type="button" onClick={() => { reset(); onClose(); }}
              className="flex-1 px-4 py-2.5 border border-gray-200 rounded-lg text-sm font-medium hover:bg-gray-50 transition">
              Cancelar
            </button>
            <button type="submit" disabled={mutation.isPending}
              className="flex-1 bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2.5 rounded-lg text-sm font-medium transition disabled:opacity-60">
              {mutation.isPending ? "Criando..." : "Criar Cobrança"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
