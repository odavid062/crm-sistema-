"use client";

import { useQuery } from "@tanstack/react-query";
import api from "@/lib/api";
import { formatCurrency } from "@/lib/utils";
import { Header } from "@/components/layout/Header";
import {
  Users, TrendingUp, CheckCircle, XCircle,
  DollarSign, Clock, MessageCircle,
} from "lucide-react";
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell } from "recharts";
import type { DashboardData } from "@/types";

const COLORS = ["#6366f1", "#10b981", "#f59e0b", "#ef4444"];

export default function DashboardPage() {
  const { data, isLoading } = useQuery<DashboardData>({
    queryKey: ["dashboard"],
    queryFn: () => api.get("/reports/dashboard").then((r) => r.data),
  });

  if (isLoading) return <div className="flex items-center justify-center h-full"><div className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600" /></div>;

  const funnelData = [
    { name: "Leads", value: data?.contacts.leads ?? 0 },
    { name: "Prospects", value: Math.floor((data?.contacts.leads ?? 0) * 0.6) },
    { name: "Clientes", value: data?.contacts.customers ?? 0 },
  ];

  const dealsData = [
    { name: "Abertos", value: data?.deals.open ?? 0, color: "#6366f1" },
    { name: "Ganhos", value: data?.deals.won ?? 0, color: "#10b981" },
    { name: "Perdidos", value: data?.deals.lost ?? 0, color: "#ef4444" },
  ];

  return (
    <div>
      <Header title="Dashboard" />
      <div className="p-6 space-y-6">
        {/* KPI Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          <KpiCard icon={<Users size={20} className="text-indigo-600" />} label="Total Contatos"
            value={data?.contacts.total ?? 0} sub={`${data?.contacts.leads ?? 0} leads`} bg="bg-indigo-50" />
          <KpiCard icon={<TrendingUp size={20} className="text-green-600" />} label="Deals Abertos"
            value={data?.deals.open ?? 0} sub={formatCurrency(data?.deals.openRevenue ?? 0)} bg="bg-green-50" />
          <KpiCard icon={<CheckCircle size={20} className="text-emerald-600" />} label="Deals Ganhos"
            value={data?.deals.won ?? 0} sub={formatCurrency(data?.deals.wonRevenue ?? 0)} bg="bg-emerald-50" />
          <KpiCard icon={<DollarSign size={20} className="text-yellow-600" />} label="Pagamentos Recebidos"
            value={formatCurrency(data?.payments.received ?? 0)} sub={`${formatCurrency(data?.payments.pending ?? 0)} pendente`} bg="bg-yellow-50" />
        </div>

        {/* Charts */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <div className="bg-white rounded-xl border border-gray-200 p-6">
            <h3 className="font-semibold text-gray-900 mb-4">Funil de Conversão</h3>
            <ResponsiveContainer width="100%" height={220}>
              <BarChart data={funnelData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis />
                <Tooltip />
                <Bar dataKey="value" fill="#6366f1" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>

          <div className="bg-white rounded-xl border border-gray-200 p-6">
            <h3 className="font-semibold text-gray-900 mb-4">Status dos Deals</h3>
            <ResponsiveContainer width="100%" height={220}>
              <PieChart>
                <Pie data={dealsData} cx="50%" cy="50%" innerRadius={60} outerRadius={90}
                  paddingAngle={4} dataKey="value" label={({ name, value }) => `${name}: ${value}`}>
                  {dealsData.map((entry, i) => (
                    <Cell key={i} fill={entry.color} />
                  ))}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>
    </div>
  );
}

function KpiCard({ icon, label, value, sub, bg }: { icon: React.ReactNode; label: string; value: string | number; sub: string; bg: string }) {
  return (
    <div className="bg-white rounded-xl border border-gray-200 p-5">
      <div className="flex items-center justify-between mb-3">
        <span className="text-sm text-gray-500">{label}</span>
        <div className={`w-9 h-9 ${bg} rounded-lg flex items-center justify-center`}>{icon}</div>
      </div>
      <p className="text-2xl font-bold text-gray-900">{value}</p>
      <p className="text-sm text-gray-500 mt-1">{sub}</p>
    </div>
  );
}
