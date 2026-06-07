"use client";

import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import api from "@/lib/api";
import toast from "react-hot-toast";
import { X, Mail, Phone, MessageCircle, Building2, Calendar, Edit2, Check } from "lucide-react";
import { useState } from "react";
import { cn, STATUS_COLORS, STATUS_LABELS, formatDate, getInitials } from "@/lib/utils";
import type { Contact } from "@/types";

interface Props {
  contactId: string | null;
  onClose: () => void;
}

export function ContactDrawer({ contactId, onClose }: Props) {
  const [editing, setEditing] = useState(false);
  const qc = useQueryClient();

  const { data: contact, isLoading } = useQuery<Contact>({
    queryKey: ["contact", contactId],
    queryFn: () => api.get(`/contacts/${contactId}`).then((r) => r.data),
    enabled: !!contactId,
  });

  const { register, handleSubmit, reset } = useForm<Partial<Contact>>();

  const updateMutation = useMutation({
    mutationFn: (data: Partial<Contact>) => api.put(`/contacts/${contactId}`, data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["contacts"] });
      qc.invalidateQueries({ queryKey: ["contact", contactId] });
      toast.success("Contato atualizado!");
      setEditing(false);
    },
    onError: () => toast.error("Erro ao atualizar"),
  });

  if (!contactId) return null;

  return (
    <>
      <div className="fixed inset-0 z-40 bg-black/30" onClick={onClose} />
      <div className="fixed right-0 top-0 bottom-0 z-50 w-full max-w-md bg-white shadow-2xl flex flex-col">
        {/* Header */}
        <div className="flex items-center justify-between p-5 border-b border-gray-200">
          <h2 className="font-semibold text-gray-900">Detalhes do Contato</h2>
          <div className="flex items-center gap-2">
            {!editing && (
              <button onClick={() => { reset(); setEditing(true); }}
                className="p-1.5 text-gray-500 hover:bg-gray-100 rounded-lg transition">
                <Edit2 size={16} />
              </button>
            )}
            <button onClick={onClose} className="p-1.5 hover:bg-gray-100 rounded-lg transition">
              <X size={18} />
            </button>
          </div>
        </div>

        {/* Content */}
        <div className="flex-1 overflow-y-auto p-5">
          {isLoading ? (
            <div className="animate-pulse space-y-4">
              <div className="h-16 w-16 bg-gray-200 rounded-full mx-auto" />
              <div className="h-4 bg-gray-200 rounded w-3/4 mx-auto" />
            </div>
          ) : contact ? (
            editing ? (
              <form id="contact-edit-form" onSubmit={handleSubmit((d) => updateMutation.mutate(d))} className="space-y-4">
                <div>
                  <label className="block text-xs font-medium text-gray-500 mb-1">Nome</label>
                  <input defaultValue={contact.name} {...register("name")}
                    className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none" />
                </div>
                <div>
                  <label className="block text-xs font-medium text-gray-500 mb-1">Email</label>
                  <input type="email" defaultValue={contact.email} {...register("email")}
                    className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none" />
                </div>
                <div>
                  <label className="block text-xs font-medium text-gray-500 mb-1">Telefone</label>
                  <input defaultValue={contact.phone} {...register("phone")}
                    className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none" />
                </div>
                <div>
                  <label className="block text-xs font-medium text-gray-500 mb-1">WhatsApp</label>
                  <input defaultValue={contact.whatsapp} {...register("whatsapp")}
                    className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none" />
                </div>
                <div>
                  <label className="block text-xs font-medium text-gray-500 mb-1">Status</label>
                  <select defaultValue={contact.status} {...register("status")}
                    className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none">
                    <option value="LEAD">Lead</option>
                    <option value="PROSPECT">Prospect</option>
                    <option value="CUSTOMER">Cliente</option>
                    <option value="CHURNED">Perdido</option>
                  </select>
                </div>
              </form>
            ) : (
              <div className="space-y-6">
                {/* Avatar & Name */}
                <div className="flex flex-col items-center text-center gap-2 pt-2">
                  <div className="w-16 h-16 rounded-full bg-indigo-100 flex items-center justify-center text-indigo-700 font-bold text-xl">
                    {getInitials(contact.name)}
                  </div>
                  <h3 className="text-lg font-semibold text-gray-900">{contact.name}</h3>
                  <span className={cn("px-3 py-1 rounded-full text-xs font-medium", STATUS_COLORS[contact.status])}>
                    {STATUS_LABELS[contact.status]}
                  </span>
                </div>

                {/* Info */}
                <div className="space-y-3">
                  {contact.email && (
                    <div className="flex items-center gap-3 text-sm">
                      <Mail size={15} className="text-gray-400 flex-shrink-0" />
                      <a href={`mailto:${contact.email}`} className="text-indigo-600 hover:underline">{contact.email}</a>
                    </div>
                  )}
                  {contact.phone && (
                    <div className="flex items-center gap-3 text-sm">
                      <Phone size={15} className="text-gray-400 flex-shrink-0" />
                      <span className="text-gray-700">{contact.phone}</span>
                    </div>
                  )}
                  {contact.whatsapp && (
                    <div className="flex items-center gap-3 text-sm">
                      <MessageCircle size={15} className="text-green-500 flex-shrink-0" />
                      <a href={`https://wa.me/${contact.whatsapp}`} target="_blank" rel="noopener"
                        className="text-green-600 hover:underline">{contact.whatsapp}</a>
                    </div>
                  )}
                  {contact.companyName && (
                    <div className="flex items-center gap-3 text-sm">
                      <Building2 size={15} className="text-gray-400 flex-shrink-0" />
                      <span className="text-gray-700">{contact.companyName}</span>
                    </div>
                  )}
                  <div className="flex items-center gap-3 text-sm">
                    <Calendar size={15} className="text-gray-400 flex-shrink-0" />
                    <span className="text-gray-500">Criado em {formatDate(contact.createdAt)}</span>
                  </div>
                </div>

                {/* Tags */}
                {contact.tags?.length > 0 && (
                  <div>
                    <p className="text-xs font-medium text-gray-500 mb-2">Tags</p>
                    <div className="flex flex-wrap gap-1">
                      {contact.tags.map((tag) => (
                        <span key={tag.id}
                          className="px-2 py-0.5 rounded-full text-xs font-medium"
                          style={{ backgroundColor: tag.color + "20", color: tag.color }}>
                          {tag.name}
                        </span>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            )
          ) : null}
        </div>

        {/* Footer */}
        {editing && (
          <div className="p-5 border-t border-gray-200 flex gap-3">
            <button onClick={() => setEditing(false)}
              className="flex-1 px-4 py-2.5 border border-gray-200 rounded-lg text-sm font-medium hover:bg-gray-50 transition">
              Cancelar
            </button>
            <button form="contact-edit-form" type="submit" disabled={updateMutation.isPending}
              className="flex-1 bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2.5 rounded-lg text-sm font-medium transition disabled:opacity-60 flex items-center justify-center gap-2">
              <Check size={15} />
              {updateMutation.isPending ? "Salvando..." : "Salvar"}
            </button>
          </div>
        )}
      </div>
    </>
  );
}
