import { ToolDef, ProjectItem } from '../types';

export const TOOLS_LIST: ToolDef[] = [
  {
    id: 'apktool',
    name: 'APKTool',
    desc: 'Decode & rebuild APK',
    category: 'reversing',
    icon: 'Package',
    tint: '#673DE6'
  },
  {
    id: 'jadx',
    name: 'JADX',
    desc: 'DEX ke Java source',
    category: 'reversing',
    icon: 'Code2',
    tint: '#168BFF'
  },
  {
    id: 'apk-editor',
    name: 'APK Editor',
    desc: 'Edit struktur APK',
    category: 'editors',
    icon: 'FileEdit',
    tint: '#E95A7B'
  },
  {
    id: 'dex-explorer',
    name: 'DEX Explorer',
    desc: 'Jelajahi classes.dex',
    category: 'reversing',
    icon: 'Binary',
    tint: '#673DE6'
  },
  {
    id: 'smali-editor',
    name: 'Smali Editor',
    desc: 'Lihat & edit Smali',
    category: 'editors',
    icon: 'TerminalSquare',
    tint: '#168BFF'
  },
  {
    id: 'xml-editor',
    name: 'XML Editor',
    desc: 'Edit format XML',
    category: 'editors',
    icon: 'FileCode',
    tint: '#13A67A'
  },
  {
    id: 'apk-signer',
    name: 'APK Signer',
    desc: 'Tanda tangani APK',
    category: 'utilities',
    icon: 'ShieldCheck',
    tint: '#EE9A2C'
  },
  {
    id: 'zip-tools',
    name: 'ZIP Tools',
    desc: 'Buat & ekstrak arsip',
    category: 'utilities',
    icon: 'Archive',
    tint: '#168BFF'
  },
  {
    id: 'hash-tools',
    name: 'Hash Tools',
    desc: 'MD5, SHA-1, SHA-256',
    category: 'utilities',
    icon: 'Hash',
    tint: '#17A77A'
  },
  {
    id: 'manifest-editor',
    name: 'Manifest Editor',
    desc: 'Kelola manifest',
    category: 'editors',
    icon: 'FileSpreadsheet',
    tint: '#673DE6'
  },
  {
    id: 'sqlite-browser',
    name: 'SQLite Browser',
    desc: 'Jelajahi database',
    category: 'system',
    icon: 'Database',
    tint: '#168BFF'
  },
  {
    id: 'hex-editor',
    name: 'Hex Editor',
    desc: 'Edit data hexadecimal',
    category: 'utilities',
    icon: 'Cpu',
    tint: '#E95A7B'
  }
];

export const INITIAL_PROJECTS: ProjectItem[] = [
  {
    id: 'p1',
    name: 'MyApp.apk',
    type: 'apk',
    detail: '2.4 MB · APK Project',
    size: '2.4 MB',
    lastModified: 'Hari ini, 10:45',
    status: 'Ready'
  },
  {
    id: 'p2',
    name: 'StudioProject',
    type: 'workspace',
    detail: '8.7 MB · Development Project',
    size: '8.7 MB',
    lastModified: 'Kemarin, 16:20',
    status: 'Ready'
  },
  {
    id: 'p3',
    name: 'Sample-Dex',
    type: 'dex',
    detail: '1.1 MB · Analisis bytecode',
    size: '1.1 MB',
    lastModified: '3 hari yang lalu',
    status: 'Decompiled'
  },
  {
    id: 'p4',
    name: 'Resource-Edit',
    type: 'resource',
    detail: '4.5 MB · XML dan gambar',
    size: '4.5 MB',
    lastModified: '5 hari yang lalu',
    status: 'Signed'
  }
];
