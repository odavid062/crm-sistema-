"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { cn } from "@/lib/utils";
import {
  LayoutDashboard, Users, Building2, Kanban, TrendingUp,
  Headphones, CreditCard, CheckSquare, BarChart2,
  Settings, LogOut, Zap, Shield, CalendarDays, BookOpen,
} from "lucide-react";
import { useAuthStore } from "@/store/authStore";

const nav = [
  { href: "/dashboard", icon: LayoutDashboard, label: "Dashboard" },
  { href: "/contacts", icon: Users, label: "Contatos" },
  { href: "/companies", icon: Building2, label: "Empresas" },
  { href: "/pipeline", icon: Kanban, label: "Pipeline" },
  { href: "/deals", icon: TrendingUp, label: "Deals" },
  { href: "/atendimentos", icon: Headphones, label: "Atendimentos" },
  { href: "/agendamentos", icon: CalendarDays, label: "Agendamentos" },
  { href: "/payments", icon: CreditCard, label: "Pagamentos" },
  { href: "/activities", icon: CheckSquare, label: "Atividades" },
  { href: "/reports", icon: BarChart2, label: "Relatórios" },
  { href: "/documentacao", icon: BookOpen, label: "API & Docs" },
  { href: "/settings", icon: Settings, label: "Configurações" },
];

export function Sidebar() {
  const pathname = usePathname();
  const { user, logout, isSuperAdmin } = useAuthStore();

  return (
    <aside className="w-64 bg-gray-900 text-white flex flex-col h-screen sticky top-0">
      <div className="p-6 border-b border-gray-700">
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 bg-indigo-500 rounded-lg flex items-center justify-center">
            <Zap size={18} className="text-white" />
          </div>
          <div>
            <p className="font-bold text-sm">CRM Sistema</p>
            <p className="text-xs text-gray-400">v1.0</p>
          </div>
        </div>
      </div>

      <nav className="flex-1 overflow-y-auto py-4 px-3 space-y-1">
        {isSuperAdmin() && (
          <Link
            href="/admin"
            className={cn(
              "flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors mb-2",
              pathname.startsWith("/admin")
                ? "bg-amber-500 text-white"
                : "bg-amber-500/10 text-amber-400 hover:bg-amber-500/20"
            )}
          >
            <Shield size={18} />
            Super Admin
          </Link>
        )}
        {nav.map(({ href, icon: Icon, label }) => (
          <Link
            key={href}
            href={href}
            className={cn(
              "flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors",
              pathname === href || pathname.startsWith(href + "/")
                ? "bg-indigo-600 text-white"
                : "text-gray-400 hover:bg-gray-800 hover:text-white"
            )}
          >
            <Icon size={18} />
            {label}
          </Link>
        ))}
      </nav>

      <div className="p-4 border-t border-gray-700">
        <div className="flex items-center gap-3 mb-3">
          <div className="w-8 h-8 bg-indigo-500 rounded-full flex items-center justify-center text-xs font-bold">
            {user?.name?.charAt(0) ?? "U"}
          </div>
          <div className="flex-1 min-w-0">
            <p className="text-sm font-medium truncate">{user?.name}</p>
            <p className="text-xs text-gray-400 truncate">{user?.role}</p>
          </div>
        </div>
        <button
          onClick={logout}
          className="flex items-center gap-2 text-gray-400 hover:text-red-400 text-sm w-full transition-colors"
        >
          <LogOut size={16} />
          Sair
        </button>
      </div>
    </aside>
  );
}
