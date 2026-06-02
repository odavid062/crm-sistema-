"use client";

import { useQuery } from "@tanstack/react-query";
import api from "@/lib/api";
import { Header } from "@/components/layout/Header";
import { formatCurrency } from "@/lib/utils";
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell, LineChart, Line } from "recharts";
import type { DashboardData } from "@/types";

export default function ReportsPage() {
  const { data } = useQuery<DashboardData>({
    queryKey: ["dashboard"],
    queryFn: () => api.get("/reports/dashboard").then((r) => r.data),
  });

  const dealsChart = [
    { name: "Abertos", value: data?.deals.open ?? 0, fill: "#6366f1" },
    { name: "Ganhos", value: data?.deals.won ?? 0, fill: "#10b981" },
    { name: "Perdidos", value: data?.deals.lost ?? 0, fill: "#ef4444" },
  ];

  const paymentsChart = [
    { name: "Recebido", value: data?.payments.received ?? 0 },
    { name: "Pendente", value: data?.payments.pending ?? 0 },
  ];

  return (
    <div>
      <Header title="Relatórios" />
      <div className="p-6 space-y-6">
        {/* Summary cards */}
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
          {[
            { label: "Total de Contatos", value: data?.contacts.total ?? 0, color: "text-indigo-600" },
            { label: "Leads Ativos", value: data?.contacts.leads ?? 0, color: "text-blue-600" },
            { label: "Receita Realizada", value: formatCurrency(data?.deals.wonRevenue ?? 0), color: "text-green-600" },
            { label: "Pagamentos Recebidos", value: formatCurrency(data?.payments.received ?? 0), color: "text-emerald-600" },
          ].map(({ label, value, color }) => (
            <div key={label} className="bg-white rounded-xl border border-gray-200 p-5">
              <p className="text-sm text-gray-500 mb-1">{label}</p>
              <p className={`text-2xl font-bold ${color}`}>{value}</p>
            </div>
          ))}
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <div className="bg-white rounded-xl border border-gray-200 p-6">
            <h3 className="font-semibold text-gray-900 mb-4">Deals por Status</h3>
            <ResponsiveContainer width="100%" height={250}>
              <BarChart data={dealsChart}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis />
                <Tooltip />
                <Bar dataKey="value" radius={[4, 4, 0, 0]}>
                  {dealsChart.map((entry, i) => (
                    <Cell key={i} fill={entry.fill} />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          </div>

          <div className="bg-white rounded-xl border border-gray-200 p-6">
            <h3 className="font-semibold text-gray-900 mb-4">Pagamentos (R$)</h3>
            <ResponsiveContainer width="100%" height={250}>
              <PieChart>
                <Pie data={paymentsChart} cx="50%" cy="50%" innerRadius={70} outerRadius={100}
                  paddingAngle={4} dataKey="value" label={({ name, value }) => `${name}: ${formatCurrency(value)}`}>
                  <Cell fill="#10b981" />
                  <Cell fill="#f59e0b" />
                </Pie>
                <Tooltip formatter={(v: number) => formatCurrency(v)} />
              </PieChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="bg-white rounded-xl border border-gray-200 p-6">
          <h3 className="font-semibold text-gray-900 mb-4">Visão Geral do Funil</h3>
          <div className="flex items-center gap-4">
            {[
              { label: "Leads", count: data?.contacts.leads ?? 0, color: "bg-indigo-500" },
              { label: "Clientes", count: data?.contacts.customers ?? 0, color: "bg-green-500" },
              { label: "Deals Ganhos", count: data?.deals.won ?? 0, color: "bg-emerald-500" },
            ].map(({ label, count, color }, i) => (
              <div key={i} className="flex-1 text-center">
                <div className={`h-24 ${color} rounded-xl flex items-center justify-center text-white text-3xl font-bold`}>
                  {count}
                </div>
                <p className="text-sm text-gray-600 mt-2">{label}</p>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
