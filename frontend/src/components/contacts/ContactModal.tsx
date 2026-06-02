"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import api from "@/lib/api";
import toast from "react-hot-toast";
import { X } from "lucide-react";

interface Props { open: boolean; onClose: () => void; }

interface FormData {
  name: string;
  email: string;
  phone: string;
  whatsapp: string;
  status: string;
  source: string;
  notes: string;
}

export function ContactModal({ open, onClose }: Props) {
  const qc = useQueryClient();
  const { register, handleSubmit, reset, formState: { errors } } = useForm<FormData>();

  const mutation = useMutation({
    mutationFn: (data: FormData) => api.post("/contacts", data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["contacts"] });
      toast.success("Contato criado com sucesso!");
      reset(); onClose();
    },
    onError: () => toast.error("Erro ao criar contato"),
  });

  if (!open) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm p-4">
      <div className="bg-white rounded-2xl shadow-2xl w-full max-w-lg">
        <div className="flex items-center justify-between p-6 border-b border-gray-200">
          <h2 className="text-lg font-semibold">Novo Contato</h2>
          <button onClick={onClose} className="p-1 hover:bg-gray-100 rounded-lg transition"><X size={20} /></button>
        </div>

        <form onSubmit={handleSubmit((d) => mutation.mutate(d))} className="p-6 space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div className="col-span-2">
              <label className="block text-sm font-medium text-gray-700 mb-1">Nome *</label>
              <input {...register("name", { required: true })}
                className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
              <input type="email" {...register("email")}
                className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Telefone</label>
              <input {...register("phone")}
                className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">WhatsApp</label>
              <input {...register("whatsapp")}
                className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none"
                placeholder="55119..." />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Status</label>
              <select {...register("status")}
                className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none">
                <option value="LEAD">Lead</option>
                <option value="PROSPECT">Prospect</option>
                <option value="CUSTOMER">Cliente</option>
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Origem</label>
              <select {...register("source")}
                className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none">
                <option value="">Selecionar...</option>
                <option value="whatsapp">WhatsApp</option>
                <option value="instagram">Instagram</option>
                <option value="site">Site</option>
                <option value="indicacao">Indicação</option>
                <option value="email">Email</option>
              </select>
            </div>
            <div className="col-span-2">
              <label className="block text-sm font-medium text-gray-700 mb-1">Observações</label>
              <textarea {...register("notes")} rows={3}
                className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none resize-none" />
            </div>
          </div>

          <div className="flex gap-3 pt-2">
            <button type="button" onClick={onClose}
              className="flex-1 px-4 py-2.5 border border-gray-200 rounded-lg text-sm font-medium hover:bg-gray-50 transition">
              Cancelar
            </button>
            <button type="submit" disabled={mutation.isPending}
              className="flex-1 bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2.5 rounded-lg text-sm font-medium transition disabled:opacity-60">
              {mutation.isPending ? "Salvando..." : "Criar Contato"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
