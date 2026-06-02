"use client";

import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import api from "@/lib/api";
import { Header } from "@/components/layout/Header";
import { formatDate } from "@/lib/utils";
import { Plus, Trash2, Building2, Users, Ban, CheckCircle2, X } from "lucide-react";
import toast from "react-hot-toast";

const STATUS_COLORS: Record<string, string> = {
  ACTIVE: "bg-green-100 text-green-700",
  TRIAL: "bg-blue-100 text-blue-700",
  SUSPENDED: "bg-amber-100 text-amber-700",
  CANCELLED: "bg-red-100 text-red-700",
};

export default function AdminPage() {
  const qc = useQueryClient();
  const [modalOpen, setModalOpen] = useState(false);

  const { data: stats } = useQuery({
    queryKey: ["admin-stats"],
    queryFn: () => api.get("/admin/tenants/stats").then((r) => r.data),
  });

  const { data: tenants } = useQuery({
    queryKey: ["admin-tenants"],
    queryFn: () => api.get("/admin/tenants?size=100").then((r) => r.data),
  });

  const statusMutation = useMutation({
    mutationFn: ({ id, status }: { id: string; status: string }) =>
      api.patch(`/admin/tenants/${id}/status`, { status }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["admin-tenants"] });
      qc.invalidateQueries({ queryKey: ["admin-stats"] });
      toast.success("Status atualizado");
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => api.delete(`/admin/tenants/${id}`),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["admin-tenants"] });
      qc.invalidateQueries({ queryKey: ["admin-stats"] });
      toast.success("Tenant excluído");
    },
  });

  const cards = [
    { label: "Total de Empresas", value: stats?.total ?? 0, icon: Building2, color: "text-indigo-600" },
    { label: "Ativas", value: stats?.active ?? 0, icon: CheckCircle2, color: "text-green-600" },
    { label: "Em Trial", value: stats?.trial ?? 0, icon: Users, color: "text-blue-600" },
    { label: "Suspensas", value: stats?.suspended ?? 0, icon: Ban, color: "text-amber-600" },
  ];

  return (
    <div>
      <Header title="Super Admin — Gestão de Empresas" />
      <div className="p-6 space-y-6">
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
          {cards.map(({ label, value, icon: Icon, color }) => (
            <div key={label} className="bg-white rounded-xl border border-gray-200 p-5">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-500">{label}</p>
                  <p className="text-2xl font-bold mt-1">{value}</p>
                </div>
                <Icon className={color} size={28} />
              </div>
            </div>
          ))}
        </div>

        <div className="flex justify-end">
          <button onClick={() => setModalOpen(true)}
            className="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2.5 rounded-lg text-sm font-medium transition">
            <Plus size={16} /> Nova Empresa
          </button>
        </div>

        <div className="bg-white rounded-xl border border-gray-200 overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 border-b border-gray-200">
              <tr>
                <th className="text-left px-4 py-3 text-gray-500 font-medium">Empresa</th>
                <th className="text-left px-4 py-3 text-gray-500 font-medium">Slug</th>
                <th className="text-left px-4 py-3 text-gray-500 font-medium">Plano</th>
                <th className="text-left px-4 py-3 text-gray-500 font-medium">Usuários</th>
                <th className="text-left px-4 py-3 text-gray-500 font-medium">Status</th>
                <th className="text-left px-4 py-3 text-gray-500 font-medium">Criada</th>
                <th className="px-4 py-3" />
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {tenants?.content?.map((t: any) => (
                <tr key={t.id} className="hover:bg-gray-50">
                  <td className="px-4 py-3 font-medium">{t.name}</td>
                  <td className="px-4 py-3 text-gray-500">{t.slug}</td>
                  <td className="px-4 py-3 text-gray-500">{t.planName ?? "-"}</td>
                  <td className="px-4 py-3 text-gray-500">{t.userCount}</td>
                  <td className="px-4 py-3">
                    <span className={`text-xs px-2 py-1 rounded-full font-medium ${STATUS_COLORS[t.status]}`}>
                      {t.status}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-gray-500">{formatDate(t.createdAt)}</td>
                  <td className="px-4 py-3">
                    <div className="flex items-center gap-2 justify-end">
                      {t.status !== "ACTIVE" && (
                        <button onClick={() => statusMutation.mutate({ id: t.id, status: "ACTIVE" })}
                          className="text-xs text-green-600 hover:underline">Ativar</button>
                      )}
                      {t.status !== "SUSPENDED" && (
                        <button onClick={() => statusMutation.mutate({ id: t.id, status: "SUSPENDED" })}
                          className="text-xs text-amber-600 hover:underline">Suspender</button>
                      )}
                      <button onClick={() => { if (confirm("Excluir empresa e TODOS os dados?")) deleteMutation.mutate(t.id); }}
                        className="p-1.5 text-red-500 hover:bg-red-50 rounded-lg"><Trash2 size={14} /></button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {modalOpen && <CreateTenantModal onClose={() => setModalOpen(false)} />}
    </div>
  );
}

function CreateTenantModal({ onClose }: { onClose: () => void }) {
  const qc = useQueryClient();
  const [form, setForm] = useState({
    name: "", slug: "", document: "", email: "", phone: "",
    adminName: "", adminEmail: "", adminPassword: "",
  });

  const { data: plans = [] } = useQuery({
    queryKey: ["plans"],
    queryFn: () => api.get("/plans").then((r) => r.data),
  });
  const [planId, setPlanId] = useState("");

  const createMutation = useMutation({
    mutationFn: () => api.post("/admin/tenants", { ...form, planId: planId || undefined }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["admin-tenants"] });
      qc.invalidateQueries({ queryKey: ["admin-stats"] });
      toast.success("Empresa provisionada com sucesso!");
      onClose();
    },
    onError: (e: any) => toast.error(e?.response?.data?.message || "Erro ao criar"),
  });

  const set = (k: string) => (e: React.ChangeEvent<HTMLInputElement>) => setForm({ ...form, [k]: e.target.value });
  const input = "px-3 py-2.5 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none w-full";

  return (
    <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-2xl p-6 w-full max-w-lg max-h-[90vh] overflow-y-auto">
        <div className="flex items-center justify-between mb-5">
          <h2 className="text-lg font-bold">Nova Empresa (Tenant)</h2>
          <button onClick={onClose} className="p-1 hover:bg-gray-100 rounded-lg"><X size={20} /></button>
        </div>

        <p className="text-xs font-semibold text-gray-400 uppercase mb-2">Dados da empresa</p>
        <div className="grid grid-cols-2 gap-3 mb-4">
          <input className={input} placeholder="Nome da empresa *" value={form.name} onChange={set("name")} />
          <input className={input} placeholder="Slug (ex: minha-empresa) *" value={form.slug} onChange={set("slug")} />
          <input className={input} placeholder="CNPJ" value={form.document} onChange={set("document")} />
          <input className={input} placeholder="Telefone" value={form.phone} onChange={set("phone")} />
          <input className={input + " col-span-2"} placeholder="Email da empresa" value={form.email} onChange={set("email")} />
          <select className={input + " col-span-2"} value={planId} onChange={(e) => setPlanId(e.target.value)}>
            <option value="">Selecione um plano</option>
            {plans.map((p: any) => (
              <option key={p.id} value={p.id}>{p.name} — R$ {p.price}</option>
            ))}
          </select>
        </div>

        <p className="text-xs font-semibold text-gray-400 uppercase mb-2">Administrador inicial</p>
        <div className="grid grid-cols-2 gap-3 mb-5">
          <input className={input} placeholder="Nome do admin *" value={form.adminName} onChange={set("adminName")} />
          <input className={input} placeholder="Email do admin *" value={form.adminEmail} onChange={set("adminEmail")} />
          <input className={input + " col-span-2"} type="password" placeholder="Senha do admin *" value={form.adminPassword} onChange={set("adminPassword")} />
        </div>

        <div className="flex gap-3">
          <button onClick={onClose} className="flex-1 border border-gray-200 py-2.5 rounded-lg text-sm font-medium hover:bg-gray-50">Cancelar</button>
          <button onClick={() => createMutation.mutate()}
            disabled={!form.name || !form.slug || !form.adminName || !form.adminEmail || !form.adminPassword}
            className="flex-1 bg-indigo-600 hover:bg-indigo-700 disabled:opacity-40 text-white py-2.5 rounded-lg text-sm font-medium">
            Criar Empresa
          </button>
        </div>
      </div>
    </div>
  );
}
