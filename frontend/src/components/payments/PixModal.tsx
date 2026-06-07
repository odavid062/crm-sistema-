"use client";

import { useQuery } from "@tanstack/react-query";
import api from "@/lib/api";
import { X, Copy } from "lucide-react";
import toast from "react-hot-toast";

interface Props {
  paymentId: string | null;
  onClose: () => void;
}

export function PixModal({ paymentId, onClose }: Props) {
  const { data, isLoading } = useQuery<{ qrCode: string; qrCodeUrl: string; expirationDate: string }>({
    queryKey: ["pix", paymentId],
    queryFn: () => api.get(`/payments/${paymentId}/pix`).then((r) => r.data),
    enabled: !!paymentId,
  });

  function copyCode() {
    if (data?.qrCode) {
      navigator.clipboard.writeText(data.qrCode);
      toast.success("Código Pix copiado!");
    }
  }

  if (!paymentId) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm p-4">
      <div className="bg-white rounded-2xl shadow-2xl w-full max-w-sm">
        <div className="flex items-center justify-between p-5 border-b border-gray-200">
          <h2 className="text-lg font-semibold">QR Code Pix</h2>
          <button onClick={onClose} className="p-1 hover:bg-gray-100 rounded-lg transition"><X size={20} /></button>
        </div>

        <div className="p-6 flex flex-col items-center gap-4">
          {isLoading ? (
            <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-indigo-600" />
          ) : data ? (
            <>
              {data.qrCodeUrl && (
                <img src={data.qrCodeUrl} alt="QR Code Pix" className="w-48 h-48 rounded-lg" />
              )}
              {data.qrCode && (
                <div className="w-full">
                  <p className="text-xs text-gray-500 mb-1">Código Copia e Cola</p>
                  <div className="flex gap-2">
                    <input readOnly value={data.qrCode}
                      className="flex-1 px-3 py-2 border border-gray-200 rounded-lg text-xs bg-gray-50 outline-none" />
                    <button onClick={copyCode}
                      className="p-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg transition">
                      <Copy size={15} />
                    </button>
                  </div>
                </div>
              )}
              {data.expirationDate && (
                <p className="text-xs text-gray-400">
                  Expira em: {new Date(data.expirationDate).toLocaleString("pt-BR")}
                </p>
              )}
            </>
          ) : (
            <p className="text-gray-500 text-sm">QR Code indisponível</p>
          )}
        </div>
      </div>
    </div>
  );
}
