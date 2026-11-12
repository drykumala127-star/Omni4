import React, { useState } from 'react';
import { Folder, File, Search, ChevronRight, FolderPlus, RefreshCw, Star, Info, Trash2, FileArchive, Copy, Scissors } from 'lucide-react';

interface MockFile {
  id: string;
  name: string;
  size: string;
  date: string;
  isDir: boolean;
  type: string;
}

const INITIAL_FILES: MockFile[] = [
  { id: '1', name: 'app-release-unsigned.apk', size: '24.2 MB', date: 'Hari ini, 11:20', isDir: false, type: 'apk' },
  { id: '2', name: 'classes.dex', size: '4.8 MB', date: 'Hari ini, 10:15', isDir: false, type: 'dex' },
  { id: '3', name: 'resources.arsc', size: '1.2 MB', date: 'Kemarin', isDir: false, type: 'arsc' },
  { id: '4', name: 'AndroidManifest.xml', size: '12 KB', date: 'Kemarin', isDir: false, type: 'xml' },
  { id: '5', name: 'smali_out', size: '42 item', date: '3 hari yang lalu', isDir: true, type: 'folder' },
  { id: '6', name: 'decompiled_java', size: '128 item', date: '4 hari yang lalu', isDir: true, type: 'folder' },
  { id: '7', name: 'assets_backup.zip', size: '8.4 MB', date: '5 hari yang lalu', isDir: false, type: 'zip' }
];

export const FileManagerScreen: React.FC = () => {
  const [files, setFiles] = useState<MockFile[]>(INITIAL_FILES);
  const [selectedIds, setSelectedIds] = useState<string[]>([]);
  const [search, setSearch] = useState('');
  const [currentPath, setCurrentPath] = useState<string[]>(['Internal Storage', 'OmniFile_Workspace']);
  const [activeInfoFile, setActiveInfoFile] = useState<MockFile | null>(null);

  const toggleSelect = (id: string) => {
    setSelectedIds(prev => prev.includes(id) ? prev.filter(x => x !== id) : [...prev, id]);
  };

  const filtered = files.filter(f => f.name.toLowerCase().includes(search.toLowerCase()));

  return (
    <div className="space-y-4">
      {/* Storage Header */}
      <div className="p-4 rounded-3xl bg-white border border-slate-200/80 shadow-sm flex items-center justify-between">
        <div>
          <h3 className="text-xs font-black text-slate-800">Penyimpanan Perangkat (SAF)</h3>
          <p className="text-[10px] text-emerald-600 font-bold">Akses Scoped Storage Terverifikasi</p>
        </div>
        <button className="px-3 py-1.5 rounded-xl bg-[#673DE6] text-white text-xs font-bold shadow-sm hover:bg-[#572ec7]">
          Pilih Folder
        </button>
      </div>

      {/* Breadcrumb Path */}
      <div className="flex items-center gap-1.5 text-xs text-slate-500 overflow-x-auto py-1 px-1">
        {currentPath.map((crumb, idx) => (
          <React.Fragment key={idx}>
            <span className={idx === currentPath.length - 1 ? 'font-bold text-[#673DE6]' : 'hover:text-slate-800'}>
              {crumb}
            </span>
            {idx < currentPath.length - 1 && <ChevronRight className="w-3.5 h-3.5 text-slate-300" />}
          </React.Fragment>
        ))}
      </div>

      {/* Search & Actions Bar */}
      <div className="flex items-center gap-2">
        <div className="relative flex-1">
          <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Cari berkas..."
            className="w-full bg-white border border-slate-200/90 rounded-2xl pl-10 pr-4 py-2 text-xs text-slate-800 focus:outline-none focus:ring-2 focus:ring-[#673DE6]/20"
          />
        </div>
        <button
          onClick={() => {
            const newName = prompt('Nama folder baru:');
            if (newName) {
              setFiles([{ id: Date.now().toString(), name: newName, size: '0 item', date: 'Baru saja', isDir: true, type: 'folder' }, ...files]);
            }
          }}
          className="p-2.5 rounded-2xl bg-white border border-slate-200/90 text-slate-700 hover:text-[#673DE6]"
          title="Folder Baru"
        >
          <FolderPlus className="w-4 h-4" />
        </button>
      </div>

      {/* Selected Action Toolbar */}
      {selectedIds.length > 0 && (
        <div className="flex items-center justify-between p-3 rounded-2xl bg-violet-100/90 border border-violet-200 text-[#673DE6]">
          <span className="text-xs font-black">{selectedIds.length} item terpilih</span>
          <div className="flex items-center gap-2">
            <button
              onClick={() => {
                setFiles(prev => prev.filter(f => !selectedIds.includes(f.id)));
                setSelectedIds([]);
              }}
              className="p-1.5 rounded-xl bg-rose-500 text-white"
              title="Hapus"
            >
              <Trash2 className="w-4 h-4" />
            </button>
            <button
              onClick={() => setSelectedIds([])}
              className="text-xs font-bold text-slate-600 px-2"
            >
              Batal
            </button>
          </div>
        </div>
      )}

      {/* Files List */}
      <div className="space-y-2">
        {filtered.map(file => {
          const isSelected = selectedIds.includes(file.id);
          return (
            <div
              key={file.id}
              onClick={() => toggleSelect(file.id)}
              className={`flex items-center justify-between p-3.5 rounded-2xl bg-white border transition-all cursor-pointer ${
                isSelected ? 'border-[#673DE6] bg-violet-50/50 shadow-sm' : 'border-slate-200/80 hover:border-slate-300'
              }`}
            >
              <div className="flex items-center gap-3">
                <div className={`w-10 h-10 rounded-xl flex items-center justify-center ${file.isDir ? 'bg-amber-50 text-amber-500' : 'bg-violet-50 text-[#673DE6]'}`}>
                  {file.isDir ? <Folder className="w-5 h-5" /> : <File className="w-5 h-5" />}
                </div>
                <div>
                  <h4 className="text-xs font-bold text-slate-800">{file.name}</h4>
                  <p className="text-[10px] text-slate-400">{file.size} • {file.date}</p>
                </div>
              </div>

              <div className="flex items-center gap-1.5">
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    setActiveInfoFile(file);
                  }}
                  className="p-1.5 rounded-xl hover:bg-slate-100 text-slate-400 hover:text-slate-700"
                >
                  <Info className="w-4 h-4" />
                </button>
              </div>
            </div>
          );
        })}
      </div>

      {/* File Info Modal */}
      {activeInfoFile && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40 backdrop-blur-sm">
          <div className="bg-white w-full max-w-sm rounded-3xl p-5 shadow-2xl space-y-4">
            <h3 className="text-sm font-black text-slate-900 flex items-center gap-2">
              <Info className="w-4 h-4 text-[#673DE6]" />
              Detail Berkas &amp; Integritas
            </h3>
            <div className="space-y-2 text-xs">
              <div className="flex justify-between py-1 border-b border-slate-100">
                <span className="text-slate-400">Nama:</span>
                <span className="font-bold text-slate-800">{activeInfoFile.name}</span>
              </div>
              <div className="flex justify-between py-1 border-b border-slate-100">
                <span className="text-slate-400">Ukuran:</span>
                <span className="font-bold text-slate-800">{activeInfoFile.size}</span>
              </div>
              <div className="flex justify-between py-1 border-b border-slate-100">
                <span className="text-slate-400">Tipe:</span>
                <span className="font-bold text-slate-800">{activeInfoFile.type.toUpperCase()}</span>
              </div>
              <div className="pt-2">
                <span className="text-slate-400 block mb-1">SHA-256 Checksum:</span>
                <code className="text-[10px] bg-slate-100 p-2 rounded-xl block break-all font-mono text-slate-700">
                  9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08
                </code>
              </div>
            </div>
            <button
              onClick={() => setActiveInfoFile(null)}
              className="w-full py-2.5 rounded-2xl bg-[#673DE6] text-white text-xs font-bold hover:bg-[#572ec7]"
            >
              Tutup
            </button>
          </div>
        </div>
      )}
    </div>
  );
};
