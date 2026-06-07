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
  defaultContactId?: string;
  defaultDealId?: string;
}

interface FormData {
  title: string;
  type: string;
  description: string;
  priority: string;
  dueDate: string;
  durationMinutes: string;
  contactId: string;
  dealId: string;
}

export function ActivityModal({ open, onClose, defaultContactId, defaultDealId }: Props) {
  const qc = useQueryClient();

  const { register, handleSubmit, reset, formState: { errors } } = useForm<FormData>({
    defaultValues: { type: "TASK", priority: "MEDIUM", contactId: defaultContactId ?? "", dealId: defaultDealId ?? "" },
  });

  const { data: contacts } = useQuery<{ content: Contact[] }>({
    queryKey: ["contacts-simple"],
    queryFn: () => api.get("/contacts", { params: { size: 100 } }).then((r) => r.data),
    enabled: open,
  });

  const mutation = useMutation({
    mutationFn: (data: FormData) =>
      api.post("/activities", {
        ...data,
        durationMinutes: data.durationMinutes ? Number(data.durationMinutes) : undefined,
        contactId: data.contactId || undefined,
        dealId: data.dealId || undefined,
        dueDate: data.dueDate || undefined,
      }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["activities"] });
      toast.success("Atividade criada!");
      reset();
      onClose();
    },
    onError: () => toast.error("Erro ao criar atividade"),
  });

  if (!open) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm p-4">
      <div className="bg-white rounded-2xl shadow-2xl w-full max-w-lg max-h-[90vh] overflow-y-auto">
        <div className="flex items-center justify-between p-6 border-b border-gray-200 sticky top-0 bg-white">
          <h2 className="text-lg font-semibold">Nova Atividade</h2>
          <button onClick={onClose} className="p-1 hover:bg-gray-100 rounded-lg transition"><X size={20} /></button>
        </div>

        <form onSubmit={handleSubmit((d) => mutation.mutate(d))} className="p-6 space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div className="col-span-2">
              <label className="block text-sm font-medium text-gray-700 mb-1">Título *</label>
              <input {...register("title", { required: true })}
                className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none" />
              {errors.title && <p className="text-red-500 text-xs mt-1">Campo obrigatório</p>}
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Tipo *</label>
              <select {...register("type", { required: true })}
                className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none">
                <option value="TASK">Tarefa</option>
                <option value="CALL">Ligação</option>
                <option value="MEETING">Reunião</option>
                <option value="EMAIL">Email</option>
                <option value="WHATSAPP">WhatsApp</option>
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Prioridade</label>
              <select {...register("priority")}
                className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none">
                <option value="LOW">Baixa</option>
                <option value="MEDIUM">Média</option>
                <option value="HIGH">Alta</option>
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Data/Hora</label>
              <input type="datetime-local" {...register("dueDate")}
                className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Duração (min)</label>
              <input type="number" {...register("durationMinutes")} placeholder="30"
                className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none" />
            </div>
            <div className="col-span-2">
              <label className="block text-sm font-medium text-gray-700 mb-1">Contato</label>
              <select {...register("contactId")}
                className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none">
                <option value="">Nenhum</option>
                {contacts?.content.map((c) => (
                  <option key={c.id} value={c.id}>{c.name}</option>
                ))}
              </select>
            </div>
            <div className="col-span-2">
              <label className="block text-sm font-medium text-gray-700 mb-1">Descrição</label>
              <textarea {...register("description")} rows={3}
                className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none resize-none" />
            </div>
          </div>

          <div className="flex gap-3 pt-2">
            <button type="button" onClick={() => { reset(); onClose(); }}
              className="flex-1 px-4 py-2.5 border border-gray-200 rounded-lg text-sm font-medium hover:bg-gray-50 transition">
              Cancelar
            </button>
            <button type="submit" disabled={mutation.isPending}
              className="flex-1 bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2.5 rounded-lg text-sm font-medium transition disabled:opacity-60">
              {mutation.isPending ? "Salvando..." : "Criar Atividade"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
