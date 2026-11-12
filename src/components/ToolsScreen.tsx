import React, { useState } from 'react';
import { TOOLS_LIST } from '../data/toolsData';
import { ToolDef } from '../types';
import { Search, Package, Code2, FileEdit, Binary, TerminalSquare, FileCode, ShieldCheck, Archive, Hash, FileSpreadsheet, Database, Cpu } from 'lucide-react';

interface ToolsScreenProps {
  onOpenTool: (toolId: string) => void;
}

export const ToolsScreen: React.FC<ToolsScreenProps> = ({ onOpenTool }) => {
  const [searchQuery, setSearchQuery] = useState('');
  const [activeCategory, setActiveCategory] = useState<string>('all');

  const iconMap: Record<string, React.ElementType> = {
    Package,
    Code2,
    FileEdit,
    Binary,
    TerminalSquare,
    FileCode,
    ShieldCheck,
    Archive,
    Hash,
    FileSpreadsheet,
    Database,
    Cpu
  };

  const categories = [
    { id: 'all', label: 'Semua' },
    { id: 'reversing', label: 'Reversing' },
    { id: 'editors', label: 'Editors' },
    { id: 'system', label: 'System' },
    { id: 'utilities', label: 'Utilities' }
  ];

  const filteredTools = TOOLS_LIST.filter(tool => {
    const matchesSearch = tool.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
                          tool.desc.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesCategory = activeCategory === 'all' || tool.category === activeCategory;
    return matchesSearch && matchesCategory;
  });

  return (
    <div className="space-y-4">
      {/* Search Input */}
      <div className="relative">
        <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
        <input
          type="text"
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          placeholder="Cari alat (APKTool, JADX, Hex, Signer...)"
          className="w-full bg-white border border-slate-200/90 rounded-2xl pl-10 pr-4 py-2.5 text-xs text-slate-800 focus:outline-none focus:ring-2 focus:ring-[#673DE6]/20 focus:border-[#673DE6] shadow-sm"
        />
      </div>

      {/* Category Pills */}
      <div className="flex items-center gap-2 overflow-x-auto pb-1 scrollbar-none">
        {categories.map(cat => (
          <button
            key={cat.id}
            onClick={() => setActiveCategory(cat.id)}
            className={`px-3 py-1.5 rounded-xl text-xs font-bold transition-all shrink-0 ${
              activeCategory === cat.id
                ? 'bg-[#673DE6] text-white shadow-sm'
                : 'bg-white text-slate-600 hover:bg-slate-100 border border-slate-200/70'
            }`}
          >
            {cat.label}
          </button>
        ))}
      </div>

      {/* 2-Column Tools Grid */}
      <div className="grid grid-cols-2 gap-3">
        {filteredTools.map(tool => {
          const Icon = iconMap[tool.icon] || Package;
          return (
            <button
              key={tool.id}
              onClick={() => onOpenTool(tool.id)}
              className="flex flex-col items-center justify-center p-4 rounded-3xl bg-white border border-slate-200/80 shadow-sm hover:border-violet-300 hover:shadow-md transition-all active:scale-95 group text-center"
            >
              <div
                className="w-12 h-12 rounded-2xl flex items-center justify-center mb-2.5 group-hover:scale-110 transition-transform shadow-inner"
                style={{ backgroundColor: `${tool.tint}18`, color: tool.tint }}
              >
                <Icon className="w-6 h-6" />
              </div>
              <h4 className="text-xs font-black text-slate-800 w-full truncate mb-0.5">
                {tool.name}
              </h4>
              <p className="text-[10px] text-slate-400 w-full truncate">
                {tool.desc}
              </p>
            </button>
          );
        })}
      </div>
    </div>
  );
};
