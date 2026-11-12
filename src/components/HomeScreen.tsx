import React from 'react';
import { ProjectItem } from '../types';
import { Folder, Package, Binary, TerminalSquare, ArrowRight, CheckCircle2, ChevronRight, Sparkles } from 'lucide-react';

interface HomeScreenProps {
  onNavigateToTab: (tab: string) => void;
  onOpenTool: (toolId: string) => void;
  projects: ProjectItem[];
}

export const HomeScreen: React.FC<HomeScreenProps> = ({ onNavigateToTab, onOpenTool, projects }) => {
  const quickActions = [
    { id: 'files', name: 'File Manager', desc: 'Kelola berkas', icon: Folder, color: 'text-amber-500 bg-amber-50', isTab: true },
    { id: 'apktool', name: 'APKTool', desc: 'Bongkar & rakit', icon: Package, color: 'text-emerald-600 bg-emerald-50', isTab: false },
    { id: 'dex-explorer', name: 'DEX Explorer', desc: 'Bytecode Dalvik', icon: Binary, color: 'text-blue-600 bg-blue-50', isTab: false },
    { id: 'smali-editor', name: 'Smali Editor', desc: 'Edit instruksi', icon: TerminalSquare, color: 'text-rose-500 bg-rose-50', isTab: false }
  ];

  return (
    <div className="space-y-5">
      {/* Hero Banner */}
      <div className="relative overflow-hidden rounded-3xl bg-gradient-to-tr from-[#24116C] via-[#673DE6] to-[#168BFF] p-6 text-white shadow-xl shadow-indigo-500/10">
        <div className="relative z-10 space-y-3">
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-white/15 backdrop-blur-md text-[11px] font-semibold text-white/90">
            <Sparkles className="w-3.5 h-3.5 text-amber-300" />
            <span>Developer Suite v1.0.0</span>
          </div>
          <h2 className="text-xl sm:text-2xl font-black tracking-tight leading-tight">
            Jelajahi Potensi Tanpa Batas
          </h2>
          <p className="text-xs sm:text-sm text-white/80 leading-relaxed max-w-md">
            Toolkit profesional dan File Manager untuk modifikasi, reverse engineering, dan pengembangan Android dalam satu workspace.
          </p>
          <div className="pt-1">
            <button
              onClick={() => onNavigateToTab('tools')}
              className="inline-flex items-center gap-2 bg-white text-[#673DE6] px-4 py-2 rounded-2xl text-xs font-bold shadow-md hover:bg-slate-50 transition-all hover:scale-105 active:scale-95"
            >
              <span>Mulai Jelajahi Tools</span>
              <ArrowRight className="w-3.5 h-3.5" />
            </button>
          </div>
        </div>
        <div className="absolute -right-8 -bottom-8 w-40 h-40 bg-white/10 rounded-full blur-2xl pointer-events-none" />
      </div>

      {/* Quick Actions Grid */}
      <div>
        <div className="flex items-center justify-between mb-3 px-1">
          <h3 className="text-sm font-black text-slate-800">Aksi Cepat</h3>
          <button
            onClick={() => onNavigateToTab('tools')}
            className="text-xs font-bold text-[#673DE6] hover:underline"
          >
            Semua Tools
          </button>
        </div>
        <div className="grid grid-cols-4 gap-2.5">
          {quickActions.map(action => {
            const Icon = action.icon;
            return (
              <button
                key={action.id}
                onClick={() => {
                  if (action.isTab) {
                    onNavigateToTab(action.id);
                  } else {
                    onOpenTool(action.id);
                  }
                }}
                className="flex flex-col items-center justify-center p-3 rounded-2xl bg-white border border-slate-200/80 shadow-sm hover:border-violet-300 hover:shadow-md transition-all active:scale-95 group"
              >
                <div className={`w-10 h-10 rounded-xl ${action.color} flex items-center justify-center mb-2 group-hover:scale-110 transition-transform`}>
                  <Icon className="w-5 h-5" />
                </div>
                <span className="text-[11px] font-bold text-slate-800 text-center truncate w-full">
                  {action.name}
                </span>
                <span className="text-[9px] text-slate-400 text-center truncate w-full">
                  {action.desc}
                </span>
              </button>
            );
          })}
        </div>
      </div>

      {/* Recent Projects */}
      <div>
        <div className="flex items-center justify-between mb-3 px-1">
          <h3 className="text-sm font-black text-slate-800">Proyek Terakhir</h3>
          <button
            onClick={() => onNavigateToTab('projects')}
            className="text-xs font-bold text-[#673DE6] hover:underline"
          >
            Lihat Semua
          </button>
        </div>
        <div className="space-y-2.5">
          {projects.slice(0, 3).map(p => (
            <div
              key={p.id}
              onClick={() => onNavigateToTab('projects')}
              className="flex items-center justify-between p-3.5 rounded-2xl bg-white border border-slate-200/80 shadow-sm hover:border-violet-200 cursor-pointer transition-all hover:shadow"
            >
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-xl bg-violet-50 text-[#673DE6] flex items-center justify-center font-bold text-sm">
                  {p.type.toUpperCase()}
                </div>
                <div>
                  <h4 className="text-xs font-black text-slate-800">{p.name}</h4>
                  <p className="text-[10px] text-slate-400">{p.detail} • {p.lastModified}</p>
                </div>
              </div>
              <ChevronRight className="w-4 h-4 text-slate-400" />
            </div>
          ))}
        </div>
      </div>

      {/* System Status Banner */}
      <div className="flex items-center gap-3 p-4 rounded-2xl bg-emerald-50 border border-emerald-200/80">
        <CheckCircle2 className="w-6 h-6 text-emerald-600 shrink-0" />
        <div>
          <h4 className="text-xs font-extrabold text-emerald-900">Semua sistem berjalan normal</h4>
          <p className="text-[11px] text-emerald-700">Engine lokal siap, SAF storage aktif, dan 0 dependensi eksternal.</p>
        </div>
      </div>
    </div>
  );
};
