import { clsx, type ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export function formatCurrency(value: number) {
  return new Intl.NumberFormat("pt-BR", {
    style: "currency",
    currency: "BRL",
  }).format(value);
}

export function formatDate(date: string | Date) {
  return new Intl.DateTimeFormat("pt-BR").format(new Date(date));
}

export function formatDateTime(date: string | Date) {
  return new Intl.DateTimeFormat("pt-BR", {
    dateStyle: "short",
    timeStyle: "short",
  }).format(new Date(date));
}

export function getInitials(name: string) {
  return name
    .split(" ")
    .slice(0, 2)
    .map((n) => n[0])
    .join("")
    .toUpperCase();
}

export const STATUS_LABELS: Record<string, string> = {
  LEAD: "Lead",
  PROSPECT: "Prospect",
  CUSTOMER: "Cliente",
  CHURNED: "Perdido",
  INACTIVE: "Inativo",
  OPEN: "Aberto",
  WON: "Ganho",
  LOST: "Perdido",
  PENDING: "Pendente",
  RECEIVED: "Recebido",
  OVERDUE: "Vencido",
  CANCELLED: "Cancelado",
};

export const STATUS_COLORS: Record<string, string> = {
  LEAD: "bg-blue-100 text-blue-700",
  PROSPECT: "bg-purple-100 text-purple-700",
  CUSTOMER: "bg-green-100 text-green-700",
  CHURNED: "bg-red-100 text-red-700",
  INACTIVE: "bg-gray-100 text-gray-700",
  OPEN: "bg-blue-100 text-blue-700",
  WON: "bg-green-100 text-green-700",
  LOST: "bg-red-100 text-red-700",
  PENDING: "bg-yellow-100 text-yellow-700",
  RECEIVED: "bg-green-100 text-green-700",
  OVERDUE: "bg-red-100 text-red-700",
  CANCELLED: "bg-gray-100 text-gray-700",
};
