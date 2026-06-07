"use client";

import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import api from "@/lib/api";
import { Header } from "@/components/layout/Header";
import { formatDate, getInitials } from "@/lib/utils";
import { Plus, Search, Trash2, Eye, Globe, Pencil } from "lucide-react";
import type { Company, PageResponse } from "@/types";
import toast from "react-hot-toast";
import { CompanyModal } from "@/components/companies/CompanyModal";

export default function CompaniesPage() {
  const [search, setSearch] = useState("");
  const [page, setPage] = useState(0);
  const [modalOpen, setModalOpen] = useState(false);
  const [editCompany, setEditCompany] = useState<Company | undefined>(undefined);
  const qc = useQueryClient();

  const { data, isLoading } = useQuery<PageResponse<Company>>({
    queryKey: ["companies", search, page],
    queryFn: () => api.get("/companies", { params: { search: search || undefined, page, size: 20 } }).then((r) => r.data),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => api.delete(`/companies/${id}`),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ["companies"] }); toast.success("Empresa excluída"); },
    onError: () => toast.error("Erro ao excluir empresa"),
  });

  function openCreate() { setEditCompany(undefined); setModalOpen(true); }
  function openEdit(company: Company) { setEditCompany(company); setModalOpen(true); }
  function handleClose() { setModalOpen(false); setEditCompany(undefined); }

  return (
    <div>
      <Header title="Empresas" />
      <div className="p-6">
        <div className="flex gap-3 mb-6">
          <div className="relative flex-1">
            <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
            <input placeholder="Buscar empresa..." value={search} onChange={(e) => { setSearch(e.target.value); setPage(0); }}
              className="w-full pl-9 pr-4 py-2.5 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none" />
          </div>
          <button onClick={openCreate}
            className="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2.5 rounded-lg text-sm font-medium transition">
            <Plus size={16} /> Nova Empresa
          </button>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {isLoading ? (
            Array.from({ length: 6 }).map((_, i) => (
              <div key={i} className="bg-white rounded-xl border border-gray-200 p-5 animate-pulse">
                <div className="h-4 bg-gray-200 rounded w-3/4 mb-3" />
                <div className="h-3 bg-gray-100 rounded w-1/2" />
              </div>
            ))
          ) : data?.content.length === 0 ? (
            <div className="col-span-full text-center py-16 text-gray-400">Nenhuma empresa encontrada</div>
          ) : (
            data?.content.map((company) => (
              <div key={company.id} className="bg-white rounded-xl border border-gray-200 p-5 hover:shadow-md transition">
                <div className="flex items-center gap-3 mb-3">
                  <div className="w-10 h-10 bg-blue-100 rounded-xl flex items-center justify-center text-blue-700 font-bold text-sm">
                    {getInitials(company.name)}
                  </div>
                  <div>
                    <p className="font-semibold text-gray-900">{company.name}</p>
                    {company.industry && <p className="text-xs text-gray-500">{company.industry}</p>}
                  </div>
                </div>
                <div className="space-y-1 text-sm text-gray-500">
                  {company.email && <p>{company.email}</p>}
                  {company.phone && <p>{company.phone}</p>}
                  {company.website && (
                    <a href={company.website} target="_blank" rel="noopener"
                      className="flex items-center gap-1 text-indigo-500 hover:underline">
                      <Globe size={12} />{company.website}
                    </a>
                  )}
                </div>
                <div className="flex items-center justify-between mt-4 pt-3 border-t border-gray-100">
                  <span className="text-xs text-gray-400">{formatDate(company.createdAt)}</span>
                  <div className="flex gap-1">
                    <button onClick={() => openEdit(company)} className="p-1.5 text-indigo-500 hover:bg-indigo-50 rounded-lg transition" title="Editar">
                      <Pencil size={14} />
                    </button>
                    <button onClick={() => { if (confirm("Excluir empresa?")) deleteMutation.mutate(company.id); }}
                      className="p-1.5 text-red-500 hover:bg-red-50 rounded-lg transition">
                      <Trash2 size={14} />
                    </button>
                  </div>
                </div>
              </div>
            ))
          )}
        </div>

        {/* Pagination */}
        {data && data.totalPages > 1 && (
          <div className="flex items-center justify-between mt-6">
            <p className="text-sm text-gray-500">{data.totalElements} empresas · Página {data.number + 1} de {data.totalPages}</p>
            <div className="flex gap-2">
              <button disabled={page === 0} onClick={() => setPage(p => p - 1)}
                className="px-3 py-1.5 text-sm border rounded-lg disabled:opacity-40 hover:bg-gray-50">Anterior</button>
              <button disabled={page >= data.totalPages - 1} onClick={() => setPage(p => p + 1)}
                className="px-3 py-1.5 text-sm border rounded-lg disabled:opacity-40 hover:bg-gray-50">Próxima</button>
            </div>
          </div>
        )}
      </div>

      <CompanyModal open={modalOpen} onClose={handleClose} company={editCompany} />
    </div>
  );
}
