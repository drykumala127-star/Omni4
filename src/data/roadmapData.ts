import { RoadmapStage } from '../types';

export const ROADMAP_STAGES: RoadmapStage[] = [
  {
    id: 1,
    title: 'File Manager & SAF',
    status: 'completed',
    progress: 100,
    description: 'Implementasi browser file, multi-select, sort, filter, dan perhitungan hash kriptografis.',
    deliverables: [
      'Scoped Storage & SAF Provider',
      'Multi-select Copy/Cut/Delete',
      'Kalkulasi Hash MD5, SHA-1, SHA-256',
      'Kompresi & Ekstraksi ZIP Aman'
    ]
  },
  {
    id: 2,
    title: 'Workspace Architecture',
    status: 'completed',
    progress: 100,
    description: 'Struktur folder proyek, metadata, backup, dan security validator terisolasi.',
    deliverables: [
      'Workspace Directory Isolation',
      'Validasi Keamanan Path Traversal',
      'Project Backup & Snapshot Export',
      'Crash Recovery & Autosave'
    ]
  },
  {
    id: 3,
    title: 'Engine Integration',
    status: 'completed',
    progress: 100,
    description: 'Integrasi APKTool, JADX, dan Smali engine dengan streaming output logging.',
    deliverables: [
      'APKTool Decode & Rebuild Engine',
      'JADX DEX-to-Java Decompiler',
      'Smali Assembler & Disassembler',
      'Engine Terminal CLI Interface'
    ]
  },
  {
    id: 4,
    title: 'Developer Tools Suite',
    status: 'completed',
    progress: 100,
    description: '12 tool lengkap: Manifest, Hex, XML, SQLite, Signer, dan DEX tools.',
    deliverables: [
      'Manifest & Permission Editor',
      'Hex Editor & ELF Analyzer',
      'Interactive SQLite DB Browser',
      'APK Signer v1/v2/v3 Schemes'
    ]
  },
  {
    id: 5,
    title: 'APK Build & Packaging',
    status: 'completed',
    progress: 100,
    description: 'Kompilasi dan penandatanganan APK biner dengan keystore AOSP terverifikasi.',
    deliverables: [
      'AAPT2 Resource Compilation',
      'DEX Bytecode Packaging',
      'Alignment & V1/V2 APK Signing',
      'Export Installeable APK'
    ]
  },
  {
    id: 6,
    title: 'Hardening & Final Release',
    status: 'completed',
    progress: 100,
    description: 'Verifikasi performa, audit lisensi, dan kesiapan rilis final v1.0.0.',
    deliverables: [
      'Zip Slip Path Validation Audit',
      'Zero Account & 100% Offline Privacy',
      'Google Play Compliance (Scoped Storage)',
      'Production Release Build Verification'
    ]
  }
];
