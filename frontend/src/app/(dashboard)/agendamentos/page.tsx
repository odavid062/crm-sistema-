"use client";

import { useMemo, useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { DragDropContext, Droppable, Draggable, DropResult } from "@hello-pangea/dnd";
import {
  startOfMonth, endOfMonth, startOfWeek, endOfWeek, eachDayOfInterval,
  format, isSameMonth, isSameDay, addMonths, subMonths, parseISO,
} from "date-fns";
import { ptBR } from "date-fns/locale";
import api from "@/lib/api";
import { Header } from "@/components/layout/Header";
import { Appointment, AppointmentStatus } from "@/types";
import { Plus, ChevronLeft, ChevronRight, Kanban, CalendarDays, X, Clock, MapPin } from "lucide-react";
import toast from "react-hot-toast";

const COLUMNS: { status: AppointmentStatus; label: string; color: string }[] = [
  { status: "SCHEDULED", label: "Agendado", color: "#6366f1" },
  { status: "CONFIRMED", label: "Confirmado", color: "#0ea5e9" },
  { status: "IN_PROGRESS", label: "Em andamento", color: "#f59e0b" },
  { status: "DONE", label: "Concluído", color: "#10b981" },
  { status: "CANCELLED", label: "Cancelado", color: "#ef4444" },
  { status: "NO_SHOW", label: "Não compareceu", color: "#6b7280" },
];

const TYPE_LABEL: Record<string, string> = {
  MEETING: "Reunião", CALL: "Ligação", VISIT: "Visita", DEMO: "Demonstração", FOLLOWUP: "Follow-up", OTHER: "Outro",
};

export default function AgendamentosPage() {
  const [view, setView] = useState<"kanban" | "calendar">("kanban");
  const [modalOpen, setModalOpen] = useState(false);
  const [preset, setPreset] = useState<string | undefined>();

  return (
    <div>
      <Header title="Agendamentos" />
      <div className="p-6">
        <div className="flex items-center justify-between mb-6">
          <div className="flex gap-1 bg-gray-100 p-1 rounded-lg">
            <button onClick={() => setView("kanban")}
              className={`flex items-center gap-2 px-4 py-2 rounded-md text-sm font-medium transition ${view === "kanban" ? "bg-white shadow text-indigo-600" : "text-gray-500"}`}>
              <Kanban size={16} /> Kanban
            </button>
            <button onClick={() => setView("calendar")}
              className={`flex items-center gap-2 px-4 py-2 rounded-md text-sm font-medium transition ${view === "calendar" ? "bg-white shadow text-indigo-600" : "text-gray-500"}`}>
              <CalendarDays size={16} /> Calendário
            </button>
          </div>
          <button onClick={() => { setPreset(undefined); setModalOpen(true); }}
            className="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2.5 rounded-lg text-sm font-medium">
            <Plus size={16} /> Novo agendamento
          </button>
        </div>

        {view === "kanban" ? <KanbanView /> : <CalendarView onNewAt={(d) => { setPreset(d); setModalOpen(true); }} />}
      </div>

      {modalOpen && <AppointmentModal presetDate={preset} onClose={() => setModalOpen(false)} />}
    </div>
  );
}

function KanbanView() {
  const qc = useQueryClient();
  const { data: items = [] } = useQuery<Appointment[]>({
    queryKey: ["appointments-board"],
    queryFn: () => api.get("/appointments").then((r) => r.data),
  });

  const move = useMutation({
    mutationFn: ({ id, status }: { id: string; status: string }) =>
      api.patch(`/appointments/${id}/status`, { status }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["appointments-board"] }),
    onError: () => toast.error("Erro ao mover"),
  });

  const grouped = useMemo(() => {
    const g: Record<string, Appointment[]> = {};
    COLUMNS.forEach((c) => (g[c.status] = []));
    items.forEach((a) => { (g[a.status] ??= []).push(a); });
    return g;
  }, [items]);

  function onDragEnd(result: DropResult) {
    if (!result.destination) return;
    const status = result.destination.droppableId;
    if (status !== result.source.droppableId) {
      move.mutate({ id: result.draggableId, status });
    }
  }

  return (
    <DragDropContext onDragEnd={onDragEnd}>
      <div className="flex gap-4 overflow-x-auto pb-4">
        {COLUMNS.map((col) => (
          <div key={col.status} className="flex-shrink-0 w-72">
            <div className="flex items-center gap-2 mb-3">
              <span className="w-2.5 h-2.5 rounded-full" style={{ background: col.color }} />
              <h3 className="font-semibold text-sm">{col.label}</h3>
              <span className="text-xs text-gray-400">({grouped[col.status]?.length || 0})</span>
            </div>
            <Droppable droppableId={col.status}>
              {(provided, snapshot) => (
                <div ref={provided.innerRef} {...provided.droppableProps}
                  className={`space-y-2 min-h-[120px] rounded-lg p-2 transition ${snapshot.isDraggingOver ? "bg-indigo-50" : "bg-gray-50"}`}>
                  {grouped[col.status]?.map((a, i) => (
                    <Draggable key={a.id} draggableId={a.id} index={i}>
                      {(prov) => (
                        <div ref={prov.innerRef} {...prov.draggableProps} {...prov.dragHandleProps}
                          className="bg-white rounded-lg border border-gray-200 p-3 shadow-sm hover:shadow transition">
                          <div className="flex items-start gap-2">
                            <span className="w-1 h-full min-h-[36px] rounded" style={{ background: a.color }} />
                            <div className="flex-1 min-w-0">
                              <p className="font-medium text-sm truncate">{a.title}</p>
                              <p className="text-xs text-gray-500 flex items-center gap-1 mt-1">
                                <Clock size={11} /> {format(parseISO(a.startAt), "dd/MM HH:mm")}
                              </p>
                              {a.contactName && <p className="text-xs text-gray-400 truncate mt-0.5">{a.contactName}</p>}
                              <span className="inline-block mt-1.5 text-[10px] px-1.5 py-0.5 rounded bg-gray-100 text-gray-600">
                                {TYPE_LABEL[a.type]}
                              </span>
                            </div>
                          </div>
                        </div>
                      )}
                    </Draggable>
                  ))}
                  {provided.placeholder}
                </div>
              )}
            </Droppable>
          </div>
        ))}
      </div>
    </DragDropContext>
  );
}

function CalendarView({ onNewAt }: { onNewAt: (iso: string) => void }) {
  const [cursor, setCursor] = useState(new Date());
  const monthStart = startOfMonth(cursor);
  const monthEnd = endOfMonth(cursor);
  const gridStart = startOfWeek(monthStart, { weekStartsOn: 0 });
  const gridEnd = endOfWeek(monthEnd, { weekStartsOn: 0 });
  const days = eachDayOfInterval({ start: gridStart, end: gridEnd });

  const { data: items = [] } = useQuery<Appointment[]>({
    queryKey: ["appointments-cal", cursor.getFullYear(), cursor.getMonth()],
    queryFn: () => api.get("/appointments/calendar", {
      params: { from: gridStart.toISOString().slice(0, 19), to: gridEnd.toISOString().slice(0, 19) },
    }).then((r) => r.data),
  });

  const byDay = useMemo(() => {
    const m: Record<string, Appointment[]> = {};
    items.forEach((a) => {
      const k = format(parseISO(a.startAt), "yyyy-MM-dd");
      (m[k] ??= []).push(a);
    });
    return m;
  }, [items]);

  return (
    <div className="bg-white rounded-xl border border-gray-200 overflow-hidden">
      <div className="flex items-center justify-between p-4 border-b border-gray-100">
        <h2 className="text-lg font-semibold capitalize">{format(cursor, "MMMM yyyy", { locale: ptBR })}</h2>
        <div className="flex gap-1">
          <button onClick={() => setCursor(subMonths(cursor, 1))} className="p-2 hover:bg-gray-100 rounded-lg"><ChevronLeft size={18} /></button>
          <button onClick={() => setCursor(new Date())} className="px-3 py-2 text-sm hover:bg-gray-100 rounded-lg">Hoje</button>
          <button onClick={() => setCursor(addMonths(cursor, 1))} className="p-2 hover:bg-gray-100 rounded-lg"><ChevronRight size={18} /></button>
        </div>
      </div>
      <div className="grid grid-cols-7 text-center text-xs font-medium text-gray-400 border-b border-gray-100">
        {["Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb"].map((d) => <div key={d} className="py-2">{d}</div>)}
      </div>
      <div className="grid grid-cols-7">
        {days.map((day) => {
          const key = format(day, "yyyy-MM-dd");
          const dayItems = byDay[key] || [];
          const inMonth = isSameMonth(day, cursor);
          const today = isSameDay(day, new Date());
          return (
            <div key={key}
              onClick={() => onNewAt(format(day, "yyyy-MM-dd'T'09:00"))}
              className={`min-h-[96px] border-b border-r border-gray-100 p-1.5 cursor-pointer hover:bg-indigo-50/50 transition ${inMonth ? "" : "bg-gray-50/50"}`}>
              <div className={`text-xs mb-1 w-6 h-6 flex items-center justify-center rounded-full ${today ? "bg-indigo-600 text-white" : inMonth ? "text-gray-700" : "text-gray-300"}`}>
                {format(day, "d")}
              </div>
              <div className="space-y-1">
                {dayItems.slice(0, 3).map((a) => (
                  <div key={a.id} className="text-[10px] px-1.5 py-0.5 rounded truncate text-white" style={{ background: a.color }} title={a.title}>
                    {format(parseISO(a.startAt), "HH:mm")} {a.title}
                  </div>
                ))}
                {dayItems.length > 3 && <div className="text-[10px] text-gray-400 px-1">+{dayItems.length - 3} mais</div>}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

function AppointmentModal({ presetDate, onClose }: { presetDate?: string; onClose: () => void }) {
  const qc = useQueryClient();
  const [form, setForm] = useState({
    title: "", type: "MEETING", status: "SCHEDULED",
    startAt: presetDate || format(new Date(), "yyyy-MM-dd'T'HH:mm"),
    endAt: "", location: "", meetingUrl: "", color: "#6366f1", description: "",
  });

  const create = useMutation({
    mutationFn: () => api.post("/appointments", {
      ...form,
      endAt: form.endAt || null,
    }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["appointments-board"] });
      qc.invalidateQueries({ queryKey: ["appointments-cal"] });
      toast.success("Agendamento criado!");
      onClose();
    },
    onError: (e: any) => toast.error(e?.response?.data?.message || "Erro ao criar"),
  });

  const set = (k: string) => (e: any) => setForm({ ...form, [k]: e.target.value });
  const input = "w-full px-3 py-2.5 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none";

  return (
    <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-2xl p-6 w-full max-w-lg max-h-[90vh] overflow-y-auto">
        <div className="flex items-center justify-between mb-5">
          <h2 className="text-lg font-bold">Novo agendamento</h2>
          <button onClick={onClose} className="p-1 hover:bg-gray-100 rounded-lg"><X size={20} /></button>
        </div>
        <div className="space-y-3">
          <input className={input} placeholder="Título *" value={form.title} onChange={set("title")} />
          <div className="grid grid-cols-2 gap-3">
            <select className={input} value={form.type} onChange={set("type")}>
              {Object.entries(TYPE_LABEL).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
            </select>
            <select className={input} value={form.status} onChange={set("status")}>
              {COLUMNS.map((c) => <option key={c.status} value={c.status}>{c.label}</option>)}
            </select>
          </div>
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="text-xs text-gray-500">Início *</label>
              <input type="datetime-local" className={input} value={form.startAt} onChange={set("startAt")} />
            </div>
            <div>
              <label className="text-xs text-gray-500">Fim</label>
              <input type="datetime-local" className={input} value={form.endAt} onChange={set("endAt")} />
            </div>
          </div>
          <input className={input} placeholder="Local" value={form.location} onChange={set("location")} />
          <input className={input} placeholder="Link da reunião (Meet/Zoom)" value={form.meetingUrl} onChange={set("meetingUrl")} />
          <div className="flex items-center gap-3">
            <label className="text-sm text-gray-600">Cor:</label>
            <input type="color" value={form.color} onChange={set("color")} className="w-10 h-10 rounded border border-gray-200" />
          </div>
          <textarea className={input} rows={3} placeholder="Descrição" value={form.description} onChange={set("description")} />
        </div>
        <div className="flex gap-3 mt-5">
          <button onClick={onClose} className="flex-1 border border-gray-200 py-2.5 rounded-lg text-sm font-medium hover:bg-gray-50">Cancelar</button>
          <button onClick={() => create.mutate()} disabled={!form.title || !form.startAt}
            className="flex-1 bg-indigo-600 hover:bg-indigo-700 disabled:opacity-40 text-white py-2.5 rounded-lg text-sm font-medium">
            Criar
          </button>
        </div>
      </div>
    </div>
  );
}
