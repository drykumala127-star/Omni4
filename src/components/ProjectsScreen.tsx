import React, { useState } from 'react';
import { ProjectItem } from '../types';
import { Plus, Package, FolderGit2, Binary, FileSpreadsheet, Play, Trash2, ArrowUpRight } from 'lucide-react';

interface ProjectsScreenProps {
  projects: ProjectItem[];
  onAddProject: (p: ProjectItem) => void;
  onOpenProject: (id: string) => void;
}

export const ProjectsScreen: React.FC<ProjectsScreenProps> = ({ projects, onAddProject, onOpenProject }) => {
  const [filter, setFilter] = useState<string>('all');
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [newProjectName, setNewProjectName] = useState('');

  const filtered = projects.filter(p => filter === 'all' || p.type === filter);

  return (
    <div className="space-y-4">
      {/* Banner Create Project */}
      <div className="p-5 rounded-3xl bg-gradient-to-tr from-[#673DE6] to-[#168BFF] text-white shadow-lg shadow-indigo-500/10 flex items-center justify-between">
        <div>
          <h3 className="text-base font-black">Workspace Terpadu</h3>
          <p className="text-xs text-white/80">Kelola reverse engineering, decompile, dan patch APK.</p>
        </div>
        <button
          onClick={() => setShowCreateModal(true)}
          className="inline-flex items-center gap-1.5 px-3.5 py-2 rounded-2xl bg-white text-[#673DE6] text-xs font-black shadow-md hover:scale-105 active:scale-95 transition-all"
        >
          <Plus className="w-4 h-4" />
          <span>Proyek Baru</span>
        </button>
      </div>

      {/* Filter Tabs */}
      <div className="flex items-center gap-2 overflow-x-auto pb-1 scrollbar-none">
        {['all', 'apk', 'workspace', 'dex', 'resource'].map(cat => (
          <button
            key={cat}
            onClick={() => setFilter(cat)}
            className={`px-3 py-1.5 rounded-xl text-xs font-bold transition-all ${
              filter === cat
                ? 'bg-[#673DE6] text-white shadow-sm'
                : 'bg-white text-slate-600 hover:bg-slate-100 border border-slate-200/70'
            }`}
          >
            {cat.toUpperCase()}
          </button>
        ))}
      </div>

      {/* Project Cards */}
      <div className="space-y-3">
        {filtered.map(proj => (
          <div
            key={proj.id}
            className="p-4 rounded-3xl bg-white border border-slate-200/80 shadow-sm space-y-3 hover:border-violet-200 transition-all"
          >
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-2xl bg-violet-50 text-[#673DE6] flex items-center justify-center font-black text-sm">
                  {proj.type === 'apk' ? <Package className="w-5 h-5 text-emerald-600" /> : <FolderGit2 className="w-5 h-5 text-[#673DE6]" />}
                </div>
                <div>
                  <h4 className="text-xs font-black text-slate-800">{proj.name}</h4>
                  <p className="text-[10px] text-slate-400">{proj.detail}</p>
                </div>
              </div>
              <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-emerald-50 text-emerald-700">
                {proj.status}
              </span>
            </div>

            <div className="pt-2 border-t border-slate-100 flex items-center justify-between">
              <span className="text-[10px] text-slate-400">Dimodifikasi: {proj.lastModified}</span>
              <button
                onClick={() => onOpenProject(proj.id)}
                className="inline-flex items-center gap-1.5 px-3 py-1 rounded-xl bg-violet-50 hover:bg-violet-100 text-[#673DE6] text-[11px] font-bold transition-all"
              >
                <span>Buka Engine</span>
                <ArrowUpRight className="w-3.5 h-3.5" />
              </button>
            </div>
          </div>
        ))}
      </div>

      {/* Create Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40 backdrop-blur-sm">
          <div className="bg-white w-full max-w-sm rounded-3xl p-5 shadow-2xl space-y-4">
            <h3 className="text-sm font-black text-slate-900">Buat Proyek Baru</h3>
            <input
              type="text"
              value={newProjectName}
              onChange={(e) => setNewProjectName(e.target.value)}
              placeholder="Nama file / target (misal: MyApp.apk)"
              className="w-full bg-slate-50 border border-slate-200 rounded-2xl px-4 py-2.5 text-xs text-slate-800 focus:outline-none focus:ring-2 focus:ring-[#673DE6]"
            />
            <div className="flex gap-2">
              <button
                onClick={() => {
                  if (newProjectName) {
                    onAddProject({
                      id: `p-${Date.now()}`,
                      name: newProjectName,
                      type: 'apk',
                      detail: '0 B · Proyek baru dibuat',
                      size: '0 B',
                      lastModified: 'Baru saja',
                      status: 'Ready'
                    });
                    setNewProjectName('');
                    setShowCreateModal(false);
                  }
                }}
                className="flex-1 py-2.5 rounded-2xl bg-[#673DE6] text-white text-xs font-bold"
              >
                Buat
              </button>
              <button
                onClick={() => setShowCreateModal(false)}
                className="px-4 py-2.5 rounded-2xl bg-slate-100 text-slate-600 text-xs font-bold"
              >
                Batal
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
