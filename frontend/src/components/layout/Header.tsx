"use client";

import { Bell, Search } from "lucide-react";
import { useQuery } from "@tanstack/react-query";
import api from "@/lib/api";

export function Header({ title }: { title: string }) {
  const { data } = useQuery({
    queryKey: ["notifications-count"],
    queryFn: () => api.get("/notifications/unread-count").then((r) => r.data),
    refetchInterval: 30000,
  });

  return (
    <header className="h-16 bg-white border-b border-gray-200 flex items-center justify-between px-6 sticky top-0 z-10">
      <h1 className="text-xl font-semibold text-gray-900">{title}</h1>
      <div className="flex items-center gap-4">
        <div className="relative hidden md:block">
          <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
          <input
            placeholder="Buscar..."
            className="pl-9 pr-4 py-2 text-sm border border-gray-200 rounded-lg bg-gray-50 focus:ring-2 focus:ring-indigo-500 focus:border-transparent outline-none w-64"
          />
        </div>
        <button className="relative p-2 text-gray-500 hover:bg-gray-100 rounded-lg transition">
          <Bell size={20} />
          {data?.count > 0 && (
            <span className="absolute top-1 right-1 w-4 h-4 bg-red-500 rounded-full text-white text-[10px] flex items-center justify-center">
              {data.count > 9 ? "9+" : data.count}
            </span>
          )}
        </button>
      </div>
    </header>
  );
}
