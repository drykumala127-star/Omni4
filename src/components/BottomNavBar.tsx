import React from 'react';
import { TabType } from '../types';
import { Home, Wrench, Folder, FolderGit2, Grid } from 'lucide-react';

interface BottomNavBarProps {
  currentTab: TabType;
  onSelectTab: (tab: TabType) => void;
  labels: {
    home: string;
    tools: string;
    files: string;
    projects: string;
    more: string;
  };
}

export const BottomNavBar: React.FC<BottomNavBarProps> = ({ currentTab, onSelectTab, labels }) => {
  const items = [
    { id: 'home' as TabType, label: labels.home, icon: Home },
    { id: 'tools' as TabType, label: labels.tools, icon: Wrench },
    { id: 'files' as TabType, label: labels.files, icon: Folder },
    { id: 'projects' as TabType, label: labels.projects, icon: FolderGit2 },
    { id: 'more' as TabType, label: labels.more, icon: Grid }
  ];

  return (
    <nav className="sticky bottom-0 z-30 bg-white/95 backdrop-blur-md border-t border-slate-200/80 px-2 py-2">
      <div className="max-w-xl mx-auto flex items-center justify-around">
        {items.map(item => {
          const Icon = item.icon;
          const isActive = currentTab === item.id;
          return (
            <button
              key={item.id}
              onClick={() => onSelectTab(item.id)}
              className={`flex flex-col items-center justify-center py-1 px-3 rounded-2xl transition-all duration-200 ${
                isActive
                  ? 'text-[#673DE6] bg-violet-50 font-bold scale-105'
                  : 'text-slate-500 hover:text-slate-800'
              }`}
            >
              <Icon className={`w-5 h-5 ${isActive ? 'stroke-[2.5]' : 'stroke-[1.8]'}`} />
              <span className="text-[10px] mt-1">{item.label}</span>
            </button>
          );
        })}
      </div>
    </nav>
  );
};
