import { create } from "zustand";
import { persist } from "zustand/middleware";
import api from "@/lib/api";

interface User {
  id: string;
  name: string;
  email: string;
  role: string;
  tenantId: string | null;
}

interface RegisterPayload {
  name: string;
  email: string;
  password: string;
  companyName?: string;
}

interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isSuperAdmin: () => boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (payload: RegisterPayload) => Promise<void>;
  logout: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      token: null,
      isAuthenticated: false,

      isSuperAdmin: () => get().user?.role === "SUPER_ADMIN",

      login: async (email, password) => {
        const { data } = await api.post("/auth/login", { email, password });
        localStorage.setItem("access_token", data.accessToken);
        set({
          user: { id: data.userId, name: data.name, email: data.email, role: data.role, tenantId: data.tenantId },
          token: data.accessToken,
          isAuthenticated: true,
        });
      },

      register: async (payload) => {
        const { data } = await api.post("/auth/register", payload);
        localStorage.setItem("access_token", data.accessToken);
        set({
          user: { id: data.userId, name: data.name, email: data.email, role: data.role, tenantId: data.tenantId },
          token: data.accessToken,
          isAuthenticated: true,
        });
      },

      logout: () => {
        localStorage.removeItem("access_token");
        set({ user: null, token: null, isAuthenticated: false });
        window.location.href = "/login";
      },
    }),
    { name: "crm-auth" }
  )
);
