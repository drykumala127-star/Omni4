import React, { useState } from 'react';
import { ToolDef } from '../../types';
import { X, Play, Copy, Check, Terminal, ShieldCheck, Download, Code } from 'lucide-react';

interface ToolModalProps {
  tool: ToolDef;
  onClose: () => void;
}

export const ToolModal: React.FC<ToolModalProps> = ({ tool, onClose }) => {
  const [activeTab, setActiveTab] = useState<'action' | 'log'>('action');
  const [logs, setLogs] = useState<string[]>(['[I] Inisialisasi ' + tool.name + ' engine...', '[I] Sistem siap memproses target.']);
  const [copied, setCopied] = useState(false);

  const runSimulation = () => {
    setLogs(prev => [
      ...prev,
      `[>] Menjalankan ${tool.name} pada target...`,
      `[I] Memeriksa header berkas & integritas struktur...`,
      `[I] Analisis selesai: 0 error ditemukan.`,
      `[✓] Status: SUKSES (Hasil siap di /workspaces/)`
    ]);
    setActiveTab('log');
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-3 bg-black/50 backdrop-blur-sm">
      <div className="bg-white w-full max-w-lg rounded-3xl overflow-hidden shadow-2xl flex flex-col max-h-[85vh]">
        {/* Modal Header */}
        <div className="flex items-center justify-between p-4 border-b border-slate-100 bg-slate-50/70">
          <div className="flex items-center gap-3">
            <div
              className="w-10 h-10 rounded-2xl flex items-center justify-center"
              style={{ backgroundColor: `${tool.tint}20`, color: tool.tint }}
            >
              <Terminal className="w-5 h-5" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h3 className="text-sm font-black text-slate-800">{tool.name}</h3>
                <span className="text-[10px] bg-violet-100 text-[#673DE6] font-bold px-2 py-0.5 rounded-full">
                  Lokal &amp; Bebas Akun
                </span>
              </div>
              <p className="text-[11px] text-slate-400">{tool.desc}</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-full hover:bg-slate-200 text-slate-500"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Modal Tabs */}
        <div className="flex border-b border-slate-100 px-4 pt-2 gap-4 text-xs font-bold">
          <button
            onClick={() => setActiveTab('action')}
            className={`pb-2 transition-all ${activeTab === 'action' ? 'text-[#673DE6] border-b-2 border-[#673DE6]' : 'text-slate-400'}`}
          >
            Konfigurasi &amp; Eksekusi
          </button>
          <button
            onClick={() => setActiveTab('log')}
            className={`pb-2 transition-all ${activeTab === 'log' ? 'text-[#673DE6] border-b-2 border-[#673DE6]' : 'text-slate-400'}`}
          >
            Terminal Output ({logs.length})
          </button>
        </div>

        {/* Modal Body */}
        <div className="p-4 overflow-y-auto flex-1 space-y-4">
          {activeTab === 'action' ? (
            <div className="space-y-4 text-xs">
              <div className="p-3.5 rounded-2xl bg-slate-50 border border-slate-200/80 space-y-1.5">
                <span className="font-bold text-slate-700 block">Target Berkas:</span>
                <span className="font-mono text-slate-800 bg-white px-2 py-1 rounded border border-slate-200 block truncate">
                  /workspaces/MyApp.apk (2.4 MB)
                </span>
              </div>

              {tool.id === 'apktool' && (
                <div className="space-y-2">
                  <span className="font-bold text-slate-700 block">Opsi Pembongkaran:</span>
                  <label className="flex items-center gap-2 cursor-pointer text-slate-600">
                    <input type="checkbox" defaultChecked className="rounded text-[#673DE6]" />
                    <span>Decode resource table (resources.arsc)</span>
                  </label>
                  <label className="flex items-center gap-2 cursor-pointer text-slate-600">
                    <input type="checkbox" defaultChecked className="rounded text-[#673DE6]" />
                    <span>Disassemble DEX bytecode ke format Smali</span>
                  </label>
                </div>
              )}

              {tool.id === 'jadx' && (
                <div className="space-y-2">
                  <span className="font-bold text-slate-700 block">Decompilation Level:</span>
                  <div className="flex gap-2">
                    <button className="flex-1 py-1.5 rounded-xl bg-violet-100 text-[#673DE6] font-bold text-center">
                      Java Source Code
                    </button>
                    <button className="flex-1 py-1.5 rounded-xl bg-slate-100 text-slate-600 text-center">
                      Smali / Bytecode
                    </button>
                  </div>
                </div>
              )}

              {tool.id === 'hash-tools' && (
                <div className="space-y-2">
                  <span className="font-bold text-slate-700 block">Kalkulasi Multi-Algoritma:</span>
                  <div className="space-y-1 font-mono text-[11px]">
                    <div className="p-2 bg-slate-50 rounded border">MD5: e10adc3949ba59abbe56e057f20f883e</div>
                    <div className="p-2 bg-slate-50 rounded border">SHA-1: 7c4a8d09ca3762af61e59520943dc26494f8941b</div>
                    <div className="p-2 bg-slate-50 rounded border truncate">SHA-256: 9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08</div>
                  </div>
                </div>
              )}

              <button
                onClick={runSimulation}
                className="w-full py-3 rounded-2xl bg-[#673DE6] text-white font-black flex items-center justify-center gap-2 shadow-lg shadow-violet-500/20 hover:bg-[#572ec7] active:scale-95 transition-all"
              >
                <Play className="w-4 h-4 fill-current" />
                <span>Jalankan {tool.name} Sekarang</span>
              </button>
            </div>
          ) : (
            <div className="bg-[#090D16] p-4 rounded-2xl font-mono text-xs text-emerald-400 space-y-1.5 overflow-x-auto min-h-[220px]">
              {logs.map((log, i) => (
                <div key={i} className="leading-relaxed">
                  {log}
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Modal Footer */}
        <div className="p-3 border-t border-slate-100 flex justify-end">
          <button
            onClick={onClose}
            className="px-4 py-2 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-bold"
          >
            Tutup
          </button>
        </div>
      </div>
    </div>
  );
};
