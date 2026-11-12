import React, { useState } from 'react';
import { ThemeMode } from '../types';
import { Terminal, Award, ShieldCheck, Cpu, HardDrive, Info, ExternalLink, ChevronRight, Moon, Palette } from 'lucide-react';

interface MoreScreenProps {
  currentTheme: ThemeMode;
  onSelectTheme: (th: ThemeMode) => void;
  onOpenTerminal: () => void;
  onOpenRoadmap: () => void;
  onOpenChecklist: () => void;
}

export const MoreScreen: React.FC<MoreScreenProps> = ({
  currentTheme,
  onSelectTheme,
  onOpenTerminal,
  onOpenRoadmap,
  onOpenChecklist
}) => {
  const [showAbout, setShowAbout] = useState(false);

  const menuItems = [
    { title: 'Engine Terminal CLI', desc: 'Jalankan perintah apktool, jadx, smali', icon: Terminal, action: onOpenTerminal, tint: 'text-emerald-600 bg-emerald-50' },
    { title: 'Verifikasi Hardening', desc: 'Audit keamanan, SAF, dan kesiapan rilis', icon: ShieldCheck, action: onOpenChecklist, tint: 'text-blue-600 bg-blue-50' },
    { title: 'Roadmap Pengembangan (100%)', desc: 'Status 6 tahapan arsitektur aplikasi', icon: Award, action: onOpenRoadmap, tint: 'text-amber-600 bg-amber-50' },
    { title: 'Tentang OmniFile', desc: 'Versi 1.0.0 Final, lisensi & engine info', icon: Info, action: () => setShowAbout(true), tint: 'text-purple-600 bg-purple-50' }
  ];

  return (
    <div className="space-y-4">
      {/* Privacy Notice Banner */}
      <div className="p-4 rounded-3xl bg-slate-900 text-white shadow-md">
        <h3 className="text-xs font-black mb-1">OmniFile Developer Suite</h3>
        <p className="text-[11px] text-slate-300">
          Aplikasi 100% gratis, lokal, dan privat. Tanpa akun, tanpa analitik eksternal, dan tanpa login.
        </p>
      </div>

      {/* Theme Selector */}
      <div className="p-4 rounded-3xl bg-white border border-slate-200/80 shadow-sm space-y-3">
        <div className="flex items-center gap-2">
          <Palette className="w-4 h-4 text-[#673DE6]" />
          <h4 className="text-xs font-black text-slate-800">Tema &amp; Tampilan</h4>
        </div>
        <div className="grid grid-cols-3 gap-2">
          {[
            { id: 'violet' as ThemeMode, label: 'Violet', bg: 'bg-[#F7F7FC] text-[#17143A]' },
            { id: 'dark' as ThemeMode, label: 'Dark Night', bg: 'bg-[#090810] text-white' },
            { id: 'indigo' as ThemeMode, label: 'Cyber Indigo', bg: 'bg-[#0B0F19] text-white' }
          ].map(theme => (
            <button
              key={theme.id}
              onClick={() => onSelectTheme(theme.id)}
              className={`p-2.5 rounded-2xl border text-xs font-bold transition-all ${
                currentTheme === theme.id
                  ? 'border-[#673DE6] ring-2 ring-[#673DE6]/20 bg-violet-50 text-[#673DE6]'
                  : 'border-slate-200 bg-white text-slate-600 hover:bg-slate-50'
              }`}
            >
              {theme.label}
            </button>
          ))}
        </div>
      </div>

      {/* Settings Menu List */}
      <div className="p-2 rounded-3xl bg-white border border-slate-200/80 shadow-sm space-y-1">
        {menuItems.map((item, idx) => {
          const Icon = item.icon;
          return (
            <div
              key={idx}
              onClick={item.action}
              className="flex items-center justify-between p-3 rounded-2xl hover:bg-slate-50 cursor-pointer transition-all"
            >
              <div className="flex items-center gap-3">
                <div className={`w-9 h-9 rounded-xl flex items-center justify-center ${item.tint}`}>
                  <Icon className="w-4 h-4" />
                </div>
                <div>
                  <h4 className="text-xs font-bold text-slate-800">{item.title}</h4>
                  <p className="text-[10px] text-slate-400">{item.desc}</p>
                </div>
              </div>
              <ChevronRight className="w-4 h-4 text-slate-300" />
            </div>
          );
        })}
      </div>

      {/* About Modal */}
      {showAbout && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40 backdrop-blur-sm">
          <div className="bg-white w-full max-w-sm rounded-3xl p-5 shadow-2xl space-y-4">
            <h3 className="text-sm font-black text-slate-900">Tentang OmniFile</h3>
            <p className="text-xs text-slate-600 leading-relaxed">
              OmniFile v1.0.0 Final dirancang khusus sebagai toolkit pengembangan dan reverse engineering Android modern dengan modular clean architecture.
            </p>
            <div className="space-y-1.5 text-[11px] bg-slate-50 p-3 rounded-2xl border border-slate-100 font-mono text-slate-600">
              <p>• Engine: APKTool 2.9.3 &amp; JADX 1.5.0</p>
              <p>• Bytecode: Smali / Baksmali 3.0.8</p>
              <p>• Keamanan: Android 14/15 Scoped Storage (SAF)</p>
              <p>• Lisensi: Apache 2.0 Open Source</p>
            </div>
            <button
              onClick={() => setShowAbout(false)}
              className="w-full py-2.5 rounded-2xl bg-[#673DE6] text-white text-xs font-bold"
            >
              Tutup
            </button>
          </div>
        </div>
      )}
    </div>
  );
};
