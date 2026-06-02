"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { useAuthStore } from "@/store/authStore";
import toast from "react-hot-toast";

export default function RegisterPage() {
  const [name, setName] = useState("");
  const [companyName, setCompanyName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const { register } = useAuthStore();
  const router = useRouter();

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    try {
      await register({ name, email, password, companyName });
      toast.success("Conta criada! Bem-vindo ao seu CRM.");
      router.push("/dashboard");
    } catch (err: any) {
      toast.error(err?.response?.data?.message || "Erro ao criar conta");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-indigo-50 to-purple-50 py-10">
      <div className="bg-white rounded-2xl shadow-xl p-8 w-full max-w-md">
        <div className="text-center mb-8">
          <div className="w-16 h-16 bg-indigo-600 rounded-2xl flex items-center justify-center mx-auto mb-4">
            <span className="text-white text-2xl font-bold">C</span>
          </div>
          <h1 className="text-2xl font-bold text-gray-900">Crie sua conta</h1>
          <p className="text-gray-500 mt-1">14 dias grátis. Sua empresa, seu CRM.</p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Seu nome</label>
            <input value={name} onChange={(e) => setName(e.target.value)} required
              className="w-full px-4 py-2.5 rounded-lg border border-gray-300 focus:ring-2 focus:ring-indigo-500 outline-none transition"
              placeholder="João Silva" />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Nome da empresa</label>
            <input value={companyName} onChange={(e) => setCompanyName(e.target.value)}
              className="w-full px-4 py-2.5 rounded-lg border border-gray-300 focus:ring-2 focus:ring-indigo-500 outline-none transition"
              placeholder="Minha Empresa LTDA" />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
            <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required
              className="w-full px-4 py-2.5 rounded-lg border border-gray-300 focus:ring-2 focus:ring-indigo-500 outline-none transition"
              placeholder="seu@email.com" />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Senha</label>
            <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required minLength={6}
              className="w-full px-4 py-2.5 rounded-lg border border-gray-300 focus:ring-2 focus:ring-indigo-500 outline-none transition"
              placeholder="mínimo 6 caracteres" />
          </div>
          <button type="submit" disabled={loading}
            className="w-full bg-indigo-600 hover:bg-indigo-700 text-white font-semibold py-2.5 rounded-lg transition disabled:opacity-60">
            {loading ? "Criando..." : "Criar conta grátis"}
          </button>
        </form>

        <p className="text-center text-sm text-gray-500 mt-6">
          Já tem conta?{" "}
          <Link href="/login" className="text-indigo-600 font-medium hover:underline">Entrar</Link>
        </p>
      </div>
    </div>
  );
}
