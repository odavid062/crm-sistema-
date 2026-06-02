"use client";

import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { DragDropContext, Droppable, Draggable, DropResult } from "@hello-pangea/dnd";
import api from "@/lib/api";
import { Header } from "@/components/layout/Header";
import { formatCurrency, cn } from "@/lib/utils";
import { Plus, MoreHorizontal } from "lucide-react";
import type { Pipeline, Deal } from "@/types";
import toast from "react-hot-toast";

export default function PipelinePage() {
  const [selectedPipeline, setSelectedPipeline] = useState<string | null>(null);
  const qc = useQueryClient();

  const { data: pipelines } = useQuery<Pipeline[]>({
    queryKey: ["pipelines"],
    queryFn: () => api.get("/pipelines").then((r) => r.data),
  });

  const activePipeline = pipelines?.find((p) => p.id === selectedPipeline) ?? pipelines?.[0];

  const { data: dealsData } = useQuery<{ content: Deal[] }>({
    queryKey: ["deals", activePipeline?.id],
    queryFn: () => api.get("/deals", { params: { pipelineId: activePipeline?.id, size: 200 } }).then((r) => r.data),
    enabled: !!activePipeline?.id,
  });

  const deals = dealsData?.content ?? [];

  const moveMutation = useMutation({
    mutationFn: ({ dealId, stageId }: { dealId: string; stageId: string }) =>
      api.patch(`/deals/${dealId}/stage`, { stageId }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["deals"] }),
    onError: () => toast.error("Erro ao mover card"),
  });

  function onDragEnd(result: DropResult) {
    if (!result.destination) return;
    const { draggableId, destination } = result;
    moveMutation.mutate({ dealId: draggableId, stageId: destination.droppableId });
  }

  const dealsByStage = (stageId: string) =>
    deals.filter((d) => d.stageId === stageId && d.status === "OPEN");

  const totalByStage = (stageId: string) =>
    dealsByStage(stageId).reduce((acc, d) => acc + d.value, 0);

  return (
    <div className="flex flex-col h-full">
      <Header title="Pipeline de Vendas" />
      <div className="p-6 pb-0">
        {/* Pipeline selector */}
        <div className="flex items-center gap-3 mb-4 overflow-x-auto">
          {pipelines?.map((p) => (
            <button
              key={p.id}
              onClick={() => setSelectedPipeline(p.id)}
              className={cn(
                "px-4 py-2 rounded-lg text-sm font-medium whitespace-nowrap transition",
                (activePipeline?.id === p.id)
                  ? "bg-indigo-600 text-white"
                  : "bg-white border border-gray-200 text-gray-600 hover:bg-gray-50"
              )}
            >
              {p.name}
            </button>
          ))}
        </div>
      </div>

      {/* Kanban Board */}
      <div className="flex-1 overflow-x-auto px-6 pb-6">
        <DragDropContext onDragEnd={onDragEnd}>
          <div className="flex gap-4 h-full min-h-0" style={{ minWidth: "max-content" }}>
            {activePipeline?.stages.map((stage) => (
              <div key={stage.id} className="w-72 flex flex-col bg-gray-100 rounded-xl overflow-hidden">
                {/* Stage Header */}
                <div className="p-3 flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <div className="w-3 h-3 rounded-full" style={{ backgroundColor: stage.color }} />
                    <span className="font-semibold text-sm text-gray-800">{stage.name}</span>
                    <span className="bg-gray-200 text-gray-600 text-xs rounded-full px-2">
                      {dealsByStage(stage.id).length}
                    </span>
                  </div>
                  <span className="text-xs text-gray-500">{formatCurrency(totalByStage(stage.id))}</span>
                </div>

                {/* Cards */}
                <Droppable droppableId={stage.id}>
                  {(provided, snapshot) => (
                    <div
                      ref={provided.innerRef}
                      {...provided.droppableProps}
                      className={cn(
                        "flex-1 p-2 space-y-2 overflow-y-auto min-h-32",
                        snapshot.isDraggingOver && "bg-indigo-50"
                      )}
                    >
                      {dealsByStage(stage.id).map((deal, index) => (
                        <Draggable key={deal.id} draggableId={deal.id} index={index}>
                          {(provided, snapshot) => (
                            <div
                              ref={provided.innerRef}
                              {...provided.draggableProps}
                              {...provided.dragHandleProps}
                              className={cn(
                                "bg-white rounded-lg p-3 shadow-sm border border-gray-200 cursor-grab active:cursor-grabbing",
                                snapshot.isDragging && "shadow-lg rotate-1"
                              )}
                            >
                              <p className="font-medium text-sm text-gray-900 mb-1">{deal.title}</p>
                              <p className="text-indigo-600 font-semibold text-sm">{formatCurrency(deal.value)}</p>
                              {deal.contactName && (
                                <p className="text-gray-400 text-xs mt-1">{deal.contactName}</p>
                              )}
                              {deal.expectedCloseDate && (
                                <p className="text-gray-400 text-xs mt-1">
                                  Fecha: {new Date(deal.expectedCloseDate).toLocaleDateString("pt-BR")}
                                </p>
                              )}
                            </div>
                          )}
                        </Draggable>
                      ))}
                      {provided.placeholder}
                    </div>
                  )}
                </Droppable>

                <button className="flex items-center gap-2 p-3 text-gray-500 hover:bg-gray-200 transition text-sm">
                  <Plus size={15} />
                  Adicionar Deal
                </button>
              </div>
            ))}
          </div>
        </DragDropContext>
      </div>
    </div>
  );
}
