# OmniFile — Android File Manager & Reverse Engineering Toolkit

OmniFile is a modern, modular Android application built with **Kotlin** and **Jetpack Compose (Material Design 3)**. It provides a complete developer workspace for Android file management, APK analysis, bytecode inspection, signature verification, and reverse engineering.

## Key Features

- **File Manager**:
  - Full Android 14/15 Storage Access Framework (SAF) integration
  - Real-time file hashing (MD5, SHA-1, SHA-256)
  - Breadcrumb navigation, category filtering, and sorting
  - Multi-select actions (Copy, Cut, Delete, Rename)
  - ZIP compression and extraction with Zip Slip security validation

- **12 Developer & Reverse Engineering Tools**:
  - **APKTool**: Decode resources (AAPT2) & disassemble Dalvik bytecode to smali, with APK rebuilding
  - **JADX**: DEX to Java source code decompiler with syntax view and clipboard copy
  - **APK Signer**: Sign APKs with debug keystore or custom keys supporting V1, V2, and V3 signature schemes
  - **Hash Tools**: Fast multi-algorithm checksum calculation and integrity comparison
  - **ZIP Tools**: Fast archive extraction, creation, and integrity verification
  - **Hex Editor**: Hexadecimal dump with 64-bit ELF inspection and byte editing
  - **Manifest Editor**: Inspect and edit package names, permissions, and debuggable attributes
  - **SQLite Browser**: Query and view internal application database schemas and tables
  - **Smali Editor**: Dalvik bytecode viewer with quick patches (Root detection bypass, SSL pinning bypass)
  - **XML Editor**: Formatted AXML/XML syntax validation and pretty-printing
  - **DEX Explorer**: Inspect classes.dex header checksums, class definitions, and method IDs
  - **APK Editor**: Tree viewer for APK internals, resources, and compiled assets

- **Workspace Management**:
  - Encapsulated project workspaces for APK modification and analysis
  - Revision history and crash recovery
  - Safe offline local backups

- **System & Security**:
  - Interactive Engine Terminal (CLI)
  - Production Hardening verification
  - Multi-theme support: Violet (default), Dark Night, and Cyber Indigo
  - 100% Free, offline, local-first, with zero login or account requirements

## Architecture & Modules

The application is modularized into distinct, testable layers:
- `:app` — Main UI, navigation, Compose screens, and application shell
- `:core:engine-api` — Engine contracts, execution requests, and lifecycle
- `:core:workspace` — Project models, security validator, and backup managers
- `:feature:filemanager` — SAF storage provider, file repository, and file manager UI
- `:feature:toolkit` — Editor models (Smali, Manifest, Hex, XML, SQLite) and task management
- `:engines:apktool` — APK decoding and packaging engine
- `:engines:jadx` — DEX-to-Java decompilation adapter
- `:engines:smali` — Dalvik bytecode assembler and disassembler

## Build & Requirements

- **Language**: Kotlin 2.0+
- **UI Framework**: Jetpack Compose (Material Design 3)
- **Minimum SDK**: Android API 26 (Android 8.0 Oreo)
- **Target SDK**: Android API 35/36
- **JDK Target**: Java 17 / 21
