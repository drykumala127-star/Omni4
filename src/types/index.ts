export type TabType = 'home' | 'tools' | 'files' | 'projects' | 'more';
export type ThemeMode = 'violet' | 'dark' | 'indigo';

export interface ToolDef {
  id: string;
  name: string;
  desc: string;
  category: 'reversing' | 'editors' | 'system' | 'utilities';
  icon: string;
  tint: string;
  badge?: string;
}

export interface ProjectItem {
  id: string;
  name: string;
  type: 'apk' | 'workspace' | 'dex' | 'resource';
  detail: string;
  size: string;
  lastModified: string;
  status: 'Ready' | 'Compiling' | 'Signed' | 'Decompiled';
  packageName?: string;
  targetSdk?: number;
  minSdk?: number;
}

export interface FileItem {
  name: string;
  path: string;
  size: number;
  isDir: boolean;
  type: string;
  lastModified?: string;
  content?: string;
}

export interface SourceFile {
  path: string;
  name: string;
  size: number;
  ext: string;
  content: string;
}

export interface RoadmapStage {
  id: number;
  title: string;
  status: 'completed' | 'in-progress' | 'pending';
  progress: number;
  description: string;
  deliverables: string[];
}
