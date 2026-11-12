import React from 'react';
import { X, ShieldCheck, CheckCircle2 } from 'lucide-react';

interface ProductionChecklistModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const ProductionChecklistModal: React.FC<ProductionChecklistModalProps> = ({ isOpen, onClose }) => {
  if (!isOpen) return null;

  const items = [
    { title: 'Android 14/15 Scoped Storage (SAF)', desc: 'Akses berkas aman tanpa izin broad storage yang dilarang Google Play.' },
    { title: 'Engine Terisolasi & Sandboxed', desc: 'APKTool, JADX, dan Smali berjalan pada layer aplikasi lokal yang aman.' },
    { title: 'Zero Account & No Login', desc: '100% bebas dari flow login, pendaftaran, langganan, atau pengumpulan data pribadi.' },
    { title: 'Proteksi Keamanan ZIP Slip', desc: 'Ekstraksi arsip diverifikasi terhadap serangan traversal path.' },
    { title: 'Integritas Hash Biner', desc: 'Kalkulasi MD5, SHA-1, dan SHA-256 berjalan pada thread latar belakang.' },
    { title: 'Keamanan Workspace Terenkapsulasi', desc: 'Setiap proyek memiliki folder terisolasi dan validasi path.' }
  ];

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-3 bg-black/50 backdrop-blur-sm">
      <div className="bg-white w-full max-w-lg rounded-3xl overflow-hidden shadow-2xl flex flex-col max-h-[85vh]">
        {/* Header */}
        <div className="flex items-center justify-between p-4 border-b border-slate-100 bg-emerald-50/50">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-2xl bg-emerald-100 text-emerald-700 flex items-center justify-center">
              <ShieldCheck className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-sm font-black text-slate-800">Verifikasi Hardening &amp; Rilis Final</h3>
              <p className="text-[11px] text-slate-400">Semua 6/6 kriteria terverifikasi untuk rilis v1.0.0</p>
            </div>
          </div>
          <button onClick={onClose} className="p-1.5 rounded-full hover:bg-slate-200 text-slate-500">
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content */}
        <div className="p-4 overflow-y-auto flex-1 space-y-2.5">
          {items.map((item, idx) => (
            <div key={idx} className="p-3.5 rounded-2xl bg-slate-50 border border-slate-200/80 flex items-start gap-3">
              <CheckCircle2 className="w-5 h-5 text-emerald-600 shrink-0 mt-0.5" />
              <div>
                <h4 className="text-xs font-black text-slate-800">{item.title}</h4>
                <p className="text-[11px] text-slate-500 mt-0.5">{item.desc}</p>
              </div>
            </div>
          ))}
        </div>

        {/* Footer */}
        <div className="p-3 border-t border-slate-100 flex justify-end">
          <button onClick={onClose} className="px-4 py-2 rounded-xl bg-[#673DE6] text-white text-xs font-bold hover:bg-[#572ec7]">
            Tutup Verifikasi
          </button>
        </div>
      </div>
    </div>
  );
};
