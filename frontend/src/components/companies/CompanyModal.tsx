"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import api from "@/lib/api";
import toast from "react-hot-toast";
import { X } from "lucide-react";
import type { Company } from "@/types";

interface Props {
  open: boolean;
  onClose: () => void;
  company?: Company;
}

interface FormData {
  name: string;
  cnpj: string;
  email: string;
  phone: string;
  website: string;
  industry: string;
  size: string;
  address: string;
  city: string;
  state: string;
  notes: string;
}

export function CompanyModal({ open, onClose, company }: Props) {
  const qc = useQueryClient();
  const isEdit = !!company;

  const { register, handleSubmit, reset, formState: { errors } } = useForm<FormData>({
    defaultValues: company
      ? { name: company.name, email: company.email, phone: company.phone, website: company.website, industry: company.industry }
      : {},
  });

  const mutation = useMutation({
    mutationFn: (data: FormData) =>
      isEdit
        ? api.put(`/companies/${company!.id}`, data)
        : api.post("/companies", data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["companies"] });
      toast.success(isEdit ? "Empresa atualizada!" : "Empresa criada!");
      reset();
      onClose();
    },
    onError: () => toast.error("Erro ao salvar empresa"),
  });

  if (!open) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm p-4">
      <div className="bg-white rounded-2xl shadow-2xl w-full max-w-lg max-h-[90vh] overflow-y-auto">
        <div className="flex items-center justify-between p-6 border-b border-gray-200 sticky top-0 bg-white">
          <h2 className="text-lg font-semibold">{isEdit ? "Editar Empresa" : "Nova Empresa"}</h2>
          <button onClick={onClose} className="p-1 hover:bg-gray-100 rounded-lg transition"><X size={20} /></button>
        </div>

        <form onSubmit={handleSubmit((d) => mutation.mutate(d))} className="p-6 space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div className="col-span-2">
              <label className="block text-sm font-medium text-gray-700 mb-1">Razão Social / Nome *</label>
              <input {...register("name", { required: true })}
                className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none" />
              {errors.name && <p className="text-red-500 text-xs mt-1">Campo obrigatório</p>}
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">CNPJ</label>
              <input {...register("cnpj")} placeholder="00.000.000/0001-00"
                className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Segmento</label>
              <select {...register("industry")}
                className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none">
                <option value="">Selecionar...</option>
                <option value="Tecnologia">Tecnologia</option>
                <option value="Saúde">Saúde</option>
                <option value="Educação">Educação</option>
                <option value="Varejo">Varejo</option>
                <option value="Serviços">Serviços</option>
                <option value="Indústria">Indústria</option>
                <option value="Financeiro">Financeiro</option>
                <option value="Agronegócio">Agronegócio</option>
                <option value="Outro">Outro</option>
              </select>
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
              <label className="block text-sm font-medium text-gray-700 mb-1">Website</label>
              <input {...register("website")} placeholder="https://"
                className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Porte</label>
              <select {...register("size")}
                className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none">
                <option value="">Selecionar...</option>
                <option value="MEI">MEI</option>
                <option value="ME">Micro empresa</option>
                <option value="EPP">Pequeno porte</option>
                <option value="MEDIO">Médio porte</option>
                <option value="GRANDE">Grande porte</option>
              </select>
            </div>
            <div className="col-span-2">
              <label className="block text-sm font-medium text-gray-700 mb-1">Endereço</label>
              <input {...register("address")}
                className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Cidade</label>
              <input {...register("city")}
                className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Estado</label>
              <input {...register("state")} placeholder="SP"
                className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none" />
            </div>
            <div className="col-span-2">
              <label className="block text-sm font-medium text-gray-700 mb-1">Observações</label>
              <textarea {...register("notes")} rows={3}
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
              {mutation.isPending ? "Salvando..." : isEdit ? "Salvar" : "Criar Empresa"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
