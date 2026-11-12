import React from 'react';
import { ROADMAP_STAGES } from '../data/roadmapData';
import { X, Award, CheckCircle2 } from 'lucide-react';

interface RoadmapModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const RoadmapModal: React.FC<RoadmapModalProps> = ({ isOpen, onClose }) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-3 bg-black/50 backdrop-blur-sm">
      <div className="bg-white w-full max-w-lg rounded-3xl overflow-hidden shadow-2xl flex flex-col max-h-[85vh]">
        {/* Header */}
        <div className="flex items-center justify-between p-4 border-b border-slate-100 bg-amber-50/50">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-2xl bg-amber-100 text-amber-700 flex items-center justify-center">
              <Award className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-sm font-black text-slate-800">Tahapan Pengembangan (100%)</h3>
              <p className="text-[11px] text-slate-400">Arsitektur 6 fase produksi selesai &amp; terverifikasi</p>
            </div>
          </div>
          <button onClick={onClose} className="p-1.5 rounded-full hover:bg-slate-200 text-slate-500">
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content */}
        <div className="p-4 overflow-y-auto flex-1 space-y-3">
          {ROADMAP_STAGES.map(stage => (
            <div key={stage.id} className="p-3.5 rounded-2xl bg-slate-50 border border-slate-200/80 space-y-2">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <span className="w-6 h-6 rounded-full bg-emerald-100 text-emerald-800 font-black text-[11px] flex items-center justify-center">
                    {stage.id}
                  </span>
                  <h4 className="text-xs font-black text-slate-800">{stage.title}</h4>
                </div>
                <span className="text-[10px] font-bold text-emerald-700 bg-emerald-100 px-2 py-0.5 rounded-full flex items-center gap-1">
                  <CheckCircle2 className="w-3 h-3" />
                  Selesai 100%
                </span>
              </div>
              <p className="text-[11px] text-slate-500">{stage.description}</p>
              <div className="grid grid-cols-2 gap-1.5 pt-1">
                {stage.deliverables.map((deliv, idx) => (
                  <div key={idx} className="text-[10px] bg-white p-1.5 rounded-xl border border-slate-200/60 text-slate-700 flex items-center gap-1.5 truncate">
                    <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 shrink-0" />
                    <span className="truncate">{deliv}</span>
                  </div>
                ))}
              </div>
            </div>
          ))}
        </div>

        {/* Footer */}
        <div className="p-3 border-t border-slate-100 flex justify-end">
          <button onClick={onClose} className="px-4 py-2 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-bold">
            Tutup
          </button>
        </div>
      </div>
    </div>
  );
};
