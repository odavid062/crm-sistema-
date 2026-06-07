"use client";

import { useMutation, useQueryClient, useQuery } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import api from "@/lib/api";
import toast from "react-hot-toast";
import { X } from "lucide-react";
import type { Pipeline, Contact } from "@/types";

interface Props {
  open: boolean;
  onClose: () => void;
  defaultPipelineId?: string;
  defaultStageId?: string;
}

interface FormData {
  title: string;
  value: string;
  pipelineId: string;
  stageId: string;
  contactId: string;
  priority: string;
  expectedCloseDate: string;
  description: string;
}

export function DealModal({ open, onClose, defaultPipelineId, defaultStageId }: Props) {
  const qc = useQueryClient();

  const { register, handleSubmit, watch, reset, formState: { errors } } = useForm<FormData>({
    defaultValues: {
      priority: "MEDIUM",
      pipelineId: defaultPipelineId ?? "",
      stageId: defaultStageId ?? "",
    },
  });

  const watchPipeline = watch("pipelineId");

  const { data: pipelines } = useQuery<Pipeline[]>({
    queryKey: ["pipelines"],
    queryFn: () => api.get("/pipelines").then((r) => r.data),
    enabled: open,
  });

  const { data: contacts } = useQuery<{ content: Contact[] }>({
    queryKey: ["contacts-simple"],
    queryFn: () => api.get("/contacts", { params: { size: 100 } }).then((r) => r.data),
    enabled: open,
  });

  const activePipeline = pipelines?.find((p) => p.id === watchPipeline) ?? pipelines?.[0];

  const mutation = useMutation({
    mutationFn: (data: FormData) =>
      api.post("/deals", {
        title: data.title,
        value: data.value ? Number(data.value) : undefined,
        pipelineId: data.pipelineId || activePipeline?.id,
        stageId: data.stageId || activePipeline?.stages[0]?.id,
        contactId: data.contactId || undefined,
        priority: data.priority,
        expectedCloseDate: data.expectedCloseDate || undefined,
        description: data.description || undefined,
        status: "OPEN",
      }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["deals"] });
      qc.invalidateQueries({ queryKey: ["deals-list"] });
      toast.success("Deal criado!");
      reset();
      onClose();
    },
    onError: () => toast.error("Erro ao criar deal"),
  });

  if (!open) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm p-4">
      <div className="bg-white rounded-2xl shadow-2xl w-full max-w-lg max-h-[90vh] overflow-y-auto">
        <div className="flex items-center justify-between p-6 border-b border-gray-200 sticky top-0 bg-white">
          <h2 className="text-lg font-semibold">Novo Deal</h2>
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
              <label className="block text-sm font-medium text-gray-700 mb-1">Valor (R$)</label>
              <input type="number" step="0.01" {...register("value")} placeholder="0,00"
                className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none" />
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
              <label className="block text-sm font-medium text-gray-700 mb-1">Pipeline</label>
              <select {...register("pipelineId")}
                className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none">
                {pipelines?.map((p) => (
                  <option key={p.id} value={p.id}>{p.name}</option>
                ))}
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Etapa</label>
              <select {...register("stageId")}
                className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none">
                {activePipeline?.stages.map((s) => (
                  <option key={s.id} value={s.id}>{s.name}</option>
                ))}
              </select>
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
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Previsão de fechamento</label>
              <input type="date" {...register("expectedCloseDate")}
                className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none" />
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
              {mutation.isPending ? "Salvando..." : "Criar Deal"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
