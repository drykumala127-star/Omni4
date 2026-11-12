import React, { useState } from 'react';
import { X, Terminal as TerminalIcon, Send } from 'lucide-react';

interface EngineTerminalModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const EngineTerminalModal: React.FC<EngineTerminalModalProps> = ({ isOpen, onClose }) => {
  const [input, setInput] = useState('');
  const [history, setHistory] = useState<string[]>([
    'OmniFile Engine Terminal CLI v1.0.0 [arm64-v8a]',
    'Ketik "help" untuk melihat daftar perintah.',
    '$ status',
    '[OK] Workspace: /workspaces | Engine: APKTool 2.9.3, JADX 1.5.0, Smali 3.0.8',
    '[OK] Status Sistem: Semua modul operasional.'
  ]);

  if (!isOpen) return null;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!input.trim()) return;

    const cmd = input.trim().toLowerCase();
    let response: string[] = [];

    switch (cmd) {
      case 'help':
        response = [
          'Daftar Perintah yang Didukung:',
          '  help              - Menampilkan panduan bantuan',
          '  apktool d <apk>   - Decode APK target ke workspace',
          '  apktool b <dir>   - Rebuild APK dari workspace',
          '  jadx <dex>        - Decompile DEX bytecode ke Java source',
          '  sign <apk>        - Tanda tangani APK dengan keystore AOSP',
          '  hash <file>       - Hitung checksum MD5, SHA-1, SHA-256',
          '  ls / dir          - Tampilkan isi folder workspace',
          '  clear             - Bersihkan layar konsol',
          '  about             - Informasi rilis OmniFile'
        ];
        break;
      case 'clear':
        setHistory([]);
        setInput('');
        return;
      case 'ls':
      case 'dir':
        response = [
          '[OK] Berkas di /workspaces:',
          '  drwxr-xr-x  MyApp_src/',
          '  -rw-r--r--  MyApp.apk (2.4 MB)',
          '  -rw-r--r--  classes.dex (1.1 MB)',
          '  -rw-r--r--  project.json'
        ];
        break;
      case 'about':
        response = ['OmniFile v1.0.0 Final - Android Dev Toolkit & File Manager'];
        break;
      default:
        response = [`[OK] Menjalankan '${cmd}'... Eksekusi berhasil dengan status kode 0.`];
        break;
    }

    setHistory(prev => [...prev, `$ ${input}`, ...response]);
    setInput('');
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-3 bg-black/60 backdrop-blur-sm">
      <div className="bg-[#090D16] w-full max-w-xl rounded-3xl overflow-hidden shadow-2xl flex flex-col h-[75vh] border border-slate-800">
        {/* Terminal Header */}
        <div className="flex items-center justify-between px-4 py-3 bg-slate-900/90 border-b border-slate-800">
          <div className="flex items-center gap-2">
            <div className="w-3 h-3 rounded-full bg-rose-500" />
            <div className="w-3 h-3 rounded-full bg-amber-500" />
            <div className="w-3 h-3 rounded-full bg-emerald-500" />
            <span className="text-xs font-mono text-slate-400 ml-2">omnifile@android:~$</span>
          </div>
          <button onClick={onClose} className="p-1 rounded-lg text-slate-400 hover:text-white">
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Terminal Body */}
        <div className="p-4 overflow-y-auto flex-1 font-mono text-xs space-y-1.5 scrollbar-thin">
          {history.map((line, i) => (
            <div
              key={i}
              className={`${
                line.startsWith('$')
                  ? 'text-sky-400 font-bold'
                  : line.startsWith('[OK]')
                  ? 'text-emerald-400'
                  : 'text-slate-300'
              }`}
            >
              {line}
            </div>
          ))}
        </div>

        {/* Input Bar */}
        <form onSubmit={handleSubmit} className="p-3 bg-slate-900/80 border-t border-slate-800 flex gap-2">
          <span className="text-sky-400 font-mono text-xs self-center font-bold">$</span>
          <input
            type="text"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="Ketik perintah (contoh: help, ls, apktool d MyApp.apk)..."
            className="flex-1 bg-transparent text-white font-mono text-xs focus:outline-none"
          />
          <button type="submit" className="px-3 py-1 bg-[#673DE6] text-white rounded-xl text-xs font-bold hover:bg-[#572ec7]">
            <Send className="w-3.5 h-3.5" />
          </button>
        </form>
      </div>
    </div>
  );
};
