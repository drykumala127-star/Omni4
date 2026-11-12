import React, { useState } from 'react';
import { TabType, ToolDef, ProjectItem, ThemeMode } from './types';
import { TOOLS_LIST, INITIAL_PROJECTS } from './data/toolsData';
import { translations, Language } from './data/i18n';
import { HomeScreen } from './components/HomeScreen';
import { ToolsScreen } from './components/ToolsScreen';
import { FileManagerScreen } from './components/FileManagerScreen';
import { ProjectsScreen } from './components/ProjectsScreen';
import { MoreScreen } from './components/MoreScreen';
import { BottomNavBar } from './components/BottomNavBar';
import { ToolModal } from './components/tools/ToolModals';
import { EngineTerminalModal } from './components/EngineTerminalModal';
import { RoadmapModal } from './components/RoadmapModal';
import { ProductionChecklistModal } from './components/ProductionChecklistModal';
import { Terminal, Award, ShieldCheck } from 'lucide-react';

export default function App() {
  const [currentTab, setCurrentTab] = useState<TabType>('home');
  const [selectedToolId, setSelectedToolId] = useState<string | null>(null);
  const [isTerminalOpen, setIsTerminalOpen] = useState(false);
  const [isRoadmapOpen, setIsRoadmapOpen] = useState(false);
  const [isProductionOpen, setIsProductionOpen] = useState(false);
  const [projects, setProjects] = useState<ProjectItem[]>(INITIAL_PROJECTS);
  const [lang, setLang] = useState<Language>('id');
  const [currentTheme, setCurrentTheme] = useState<ThemeMode>('violet');

  const t = translations[lang];
  const selectedTool = selectedToolId ? TOOLS_LIST.find(t => t.id === selectedToolId) : null;

  const themeClasses = {
    violet: 'bg-[#F7F7FC] text-[#17143A]',
    dark: 'bg-[#090810] text-[#EBEBF5]',
    indigo: 'bg-[#0B0F19] text-[#F1F5F9]'
  };

  return (
    <div className={`min-h-screen ${themeClasses[currentTheme]} flex flex-col font-sans transition-colors duration-300`}>
      {/* Top Studio Control Bar */}
      <header className="sticky top-0 z-30 backdrop-blur-md bg-white/90 border-b border-slate-200/90 px-4 py-2.5">
        <div className="max-w-xl mx-auto flex items-center justify-between gap-3">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-xl bg-gradient-to-tr from-[#673DE6] to-[#168BFF] flex items-center justify-center text-white font-extrabold text-sm shadow-sm">
              O
            </div>
            <div>
              <div className="flex items-center gap-2">
                <span className="font-extrabold text-sm tracking-tight">{t.appName}</span>
                <span className="text-[10px] bg-emerald-100 text-emerald-800 font-bold px-2 py-0.5 rounded-full">
                  v1.0.0 Final
                </span>
              </div>
              <span className="text-[10px] text-slate-400 hidden sm:inline">
                {t.appSubtitle}
              </span>
            </div>
          </div>

          {/* Quick Header Actions */}
          <div className="flex items-center gap-1.5">
            <button
              onClick={() => setIsTerminalOpen(true)}
              className="p-1.5 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700"
              title="Buka Terminal CLI"
            >
              <Terminal className="w-4 h-4 text-emerald-600" />
            </button>
            <button
              onClick={() => setIsRoadmapOpen(true)}
              className="p-1.5 rounded-xl bg-amber-50 hover:bg-amber-100 text-amber-800"
              title="Tahapan Roadmap"
            >
              <Award className="w-4 h-4 text-amber-600" />
            </button>
            <button
              onClick={() => setIsProductionOpen(true)}
              className="p-1.5 rounded-xl bg-emerald-50 hover:bg-emerald-100 text-emerald-800"
              title="Verifikasi Hardening"
            >
              <ShieldCheck className="w-4 h-4 text-emerald-600" />
            </button>
            <button
              onClick={() => setLang(prev => (prev === 'id' ? 'en' : 'id'))}
              className="px-2 py-1 rounded-xl bg-violet-50 text-violet-700 font-bold text-xs"
              title="Ganti Bahasa"
            >
              {lang.toUpperCase()}
            </button>
          </div>
        </div>
      </header>

      {/* Main Content Area */}
      <main className="flex-1 max-w-xl mx-auto w-full p-4">
        {currentTab === 'home' && (
          <HomeScreen
            onNavigateToTab={(tab) => setCurrentTab(tab as TabType)}
            onOpenTool={(toolId) => setSelectedToolId(toolId)}
            projects={projects}
          />
        )}
        {currentTab === 'tools' && (
          <ToolsScreen
            onOpenTool={(toolId) => setSelectedToolId(toolId)}
          />
        )}
        {currentTab === 'files' && (
          <FileManagerScreen />
        )}
        {currentTab === 'projects' && (
          <ProjectsScreen
            projects={projects}
            onAddProject={(p) => setProjects([p, ...projects])}
            onOpenProject={(id) => setSelectedToolId('apktool')}
          />
        )}
        {currentTab === 'more' && (
          <MoreScreen
            currentTheme={currentTheme}
            onSelectTheme={(th) => setCurrentTheme(th)}
            onOpenTerminal={() => setIsTerminalOpen(true)}
            onOpenRoadmap={() => setIsRoadmapOpen(true)}
            onOpenChecklist={() => setIsProductionOpen(true)}
          />
        )}
      </main>

      {/* Bottom Navigation */}
      <BottomNavBar
        currentTab={currentTab}
        onSelectTab={(tab) => setCurrentTab(tab)}
        labels={t.tabs}
      />

      {/* Interactive Tool Modal */}
      {selectedTool && (
        <ToolModal
          tool={selectedTool}
          onClose={() => setSelectedToolId(null)}
        />
      )}

      {/* Engine Terminal Modal */}
      <EngineTerminalModal
        isOpen={isTerminalOpen}
        onClose={() => setIsTerminalOpen(false)}
      />

      {/* Roadmap Modal */}
      <RoadmapModal
        isOpen={isRoadmapOpen}
        onClose={() => setIsRoadmapOpen(false)}
      />

      {/* Production Checklist Modal */}
      <ProductionChecklistModal
        isOpen={isProductionOpen}
        onClose={() => setIsProductionOpen(false)}
      />
    </div>
  );
}
