"use client";

import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import api from "@/lib/api";
import { Header } from "@/components/layout/Header";
import { formatDateTime, cn } from "@/lib/utils";
import { Plus, CheckCircle, Clock, Phone, Mail, Video, MessageCircle } from "lucide-react";
import type { Activity, PageResponse } from "@/types";

const typeIcons: Record<string, React.ReactNode> = {
  TASK: <CheckCircle size={15} />,
  CALL: <Phone size={15} />,
  MEETING: <Video size={15} />,
  EMAIL: <Mail size={15} />,
  WHATSAPP: <MessageCircle size={15} />,
};

const typeColors: Record<string, string> = {
  TASK: "text-indigo-600 bg-indigo-50",
  CALL: "text-green-600 bg-green-50",
  MEETING: "text-yellow-600 bg-yellow-50",
  EMAIL: "text-blue-600 bg-blue-50",
  WHATSAPP: "text-emerald-600 bg-emerald-50",
};

export default function ActivitiesPage() {
  const [statusFilter, setStatusFilter] = useState("PENDING");
  const qc = useQueryClient();

  const { data } = useQuery<PageResponse<Activity>>({
    queryKey: ["activities", statusFilter],
    queryFn: () => api.get("/activities/my", { params: { status: statusFilter, size: 30 } }).then((r) => r.data),
  });

  const completeMutation = useMutation({
    mutationFn: (id: string) => api.patch(`/activities/${id}/complete`),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["activities"] }),
  });

  return (
    <div>
      <Header title="Atividades" />
      <div className="p-6">
        <div className="flex items-center gap-3 mb-6">
          <div className="flex gap-1 p-1 bg-gray-100 rounded-lg">
            {["PENDING", "IN_PROGRESS", "COMPLETED"].map((s) => (
              <button key={s} onClick={() => setStatusFilter(s)}
                className={cn("px-3 py-1.5 text-sm rounded-md font-medium transition",
                  statusFilter === s ? "bg-white shadow-sm text-gray-900" : "text-gray-500")}>
                {s === "PENDING" ? "Pendentes" : s === "IN_PROGRESS" ? "Em andamento" : "Concluídas"}
              </button>
            ))}
          </div>
          <button className="ml-auto flex items-center gap-2 bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2.5 rounded-lg text-sm font-medium transition">
            <Plus size={16} /> Nova Atividade
          </button>
        </div>

        <div className="space-y-3">
          {data?.content.map((activity) => (
            <div key={activity.id} className="bg-white rounded-xl border border-gray-200 p-4 flex items-center gap-4">
              <div className={cn("w-9 h-9 rounded-lg flex items-center justify-center flex-shrink-0", typeColors[activity.type])}>
                {typeIcons[activity.type]}
              </div>
              <div className="flex-1 min-w-0">
                <p className="font-medium text-gray-900">{activity.title}</p>
                {activity.dueDate && (
                  <p className="text-sm text-gray-500 flex items-center gap-1 mt-0.5">
                    <Clock size={12} />{formatDateTime(activity.dueDate)}
                  </p>
                )}
              </div>
              <span className={cn("text-xs font-medium px-2 py-1 rounded-full",
                activity.priority === "HIGH" ? "bg-red-100 text-red-700" :
                activity.priority === "MEDIUM" ? "bg-yellow-100 text-yellow-700" :
                "bg-gray-100 text-gray-700")}>
                {activity.priority}
              </span>
              {activity.status === "PENDING" && (
                <button onClick={() => completeMutation.mutate(activity.id)}
                  className="p-2 text-green-600 hover:bg-green-50 rounded-lg transition" title="Concluir">
                  <CheckCircle size={18} />
                </button>
              )}
            </div>
          ))}
          {data?.content.length === 0 && (
            <div className="text-center py-16 text-gray-400">
              <CheckCircle size={48} className="mx-auto mb-3 opacity-20" />
              <p>Nenhuma atividade {statusFilter === "PENDING" ? "pendente" : "encontrada"}</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
