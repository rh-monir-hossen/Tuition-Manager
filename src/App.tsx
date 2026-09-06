/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import { useState } from 'react';
import {
  ARCHITECTURE_LAYERS,
  DATABASE_ENTITIES,
  DEVELOPMENT_PHASES,
  FOLDER_TREE,
  SHEETS_TABS,
} from './data/architectureData';
import { DatabaseEntity, DevelopmentPhase } from './types';
import { ConflictSimulator } from './components/ConflictSimulator';
import { EntityInspectorModal } from './components/EntityInspectorModal';
import { PhaseInspectorModal } from './components/PhaseInspectorModal';
import { SheetsViewerModal } from './components/SheetsViewerModal';
import { AndroidStep4Preview } from './components/AndroidStep4Preview';
import {
  Layers,
  Database,
  RefreshCw,
  FolderTree,
  FileSpreadsheet,
  AlertTriangle,
  CheckCircle2,
  ExternalLink,
  Search,
  Filter,
  Code2,
  Cpu,
  ShieldCheck,
  ChevronRight,
  Smartphone,
} from 'lucide-react';

export default function App() {
  const [selectedEntity, setSelectedEntity] = useState<DatabaseEntity | null>(null);
  const [selectedPhase, setSelectedPhase] = useState<DevelopmentPhase | null>(null);
  const [isSheetsModalOpen, setIsSheetsModalOpen] = useState(false);
  const [isSimulatorOpen, setIsSimulatorOpen] = useState(false);
  const [activeViewMode, setActiveViewMode] = useState<'ARCHITECTURE' | 'APP_PREVIEW'>('APP_PREVIEW');
  const [entityCategoryFilter, setEntityCategoryFilter] = useState<string>('ALL');
  const [entitySearch, setEntitySearch] = useState<string>('');

  const filteredEntities = DATABASE_ENTITIES.filter((e) => {
    const matchesCategory =
      entityCategoryFilter === 'ALL' || e.category === entityCategoryFilter;
    const matchesSearch =
      e.name.toLowerCase().includes(entitySearch.toLowerCase()) ||
      e.tableName.toLowerCase().includes(entitySearch.toLowerCase()) ||
      e.description.toLowerCase().includes(entitySearch.toLowerCase());
    return matchesCategory && matchesSearch;
  });

  return (
    <div className="flex flex-col min-h-screen lg:h-screen w-full bg-slate-950 text-slate-300 font-sans p-3 lg:p-4 overflow-y-auto lg:overflow-hidden custom-scrollbar">
      {/* Top Header */}
      <header className="flex flex-col sm:flex-row justify-between items-start sm:items-center mb-3 border-b border-slate-800 pb-2.5 gap-2 shrink-0">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-lg lg:text-xl font-bold text-emerald-400 tracking-tight flex items-center gap-2">
              <span>TUITION MANAGER</span>
              <span className="text-slate-500 font-normal text-sm lg:text-base">
                | Technical Architecture Blueprint
              </span>
            </h1>
            <span className="text-[9px] px-1.5 py-0.5 bg-emerald-950 text-emerald-400 border border-emerald-800/80 rounded font-mono font-semibold">
              OFFLINE-FIRST
            </span>
          </div>
          <p className="text-[10px] text-slate-500 uppercase tracking-widest mt-0.5 font-mono">
            v1.0.0 Architecture Spec • Native Android • Room SQLite SSOT
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2 text-xs">
          {/* Mode Switcher */}
          <div className="bg-slate-900 p-0.5 border border-slate-700/90 rounded flex items-center gap-1 shadow-sm">
            <button
              onClick={() => setActiveViewMode('APP_PREVIEW')}
              className={`px-2.5 py-1 rounded text-[11px] font-semibold flex items-center gap-1.5 transition-colors cursor-pointer ${
                activeViewMode === 'APP_PREVIEW'
                  ? 'bg-emerald-600 text-white'
                  : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              <Smartphone className="w-3.5 h-3.5" />
              <span>Live Android UI (Step 4)</span>
            </button>
            <button
              onClick={() => setActiveViewMode('ARCHITECTURE')}
              className={`px-2.5 py-1 rounded text-[11px] font-semibold flex items-center gap-1.5 transition-colors cursor-pointer ${
                activeViewMode === 'ARCHITECTURE'
                  ? 'bg-emerald-600 text-white'
                  : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              <Layers className="w-3.5 h-3.5" />
              <span>Architecture Console</span>
            </button>
          </div>

          <button
            id="btn-open-sheets-schema"
            onClick={() => setIsSheetsModalOpen(true)}
            className="px-2.5 py-1 bg-slate-900 hover:bg-slate-800 border border-slate-700 rounded text-[11px] text-emerald-400 hover:text-emerald-300 flex items-center gap-1.5 transition-colors cursor-pointer"
          >
            <FileSpreadsheet className="w-3.5 h-3.5" />
            <span>G-Sheets Schema (14 Tabs)</span>
          </button>
          <button
            id="btn-toggle-conflict-sim"
            onClick={() => setIsSimulatorOpen(!isSimulatorOpen)}
            className={`px-2.5 py-1 border rounded text-[11px] flex items-center gap-1.5 transition-colors cursor-pointer ${
              isSimulatorOpen
                ? 'bg-amber-950/60 text-amber-300 border-amber-700'
                : 'bg-slate-900 hover:bg-slate-800 text-amber-400 border-slate-700'
            }`}
          >
            <AlertTriangle className="w-3.5 h-3.5" />
            <span>Conflict Engine</span>
          </button>
        </div>
      </header>

      {/* Main Viewport Content */}
      {activeViewMode === 'APP_PREVIEW' ? (
        <div className="flex-1 overflow-y-auto lg:overflow-hidden p-1 custom-scrollbar">
          <AndroidStep4Preview />
        </div>
      ) : (
        /* Main High-Density Grid */
        <main className="grid grid-cols-1 lg:grid-cols-12 lg:grid-rows-12 gap-3 flex-grow overflow-hidden">
        {/* Left Column: Core Architecture & Folder Structure (Col 1-3, Row 1-8) */}
        <section className="lg:col-span-3 lg:row-span-8 bg-slate-900/50 border border-slate-800 rounded-lg p-3 flex flex-col overflow-hidden shadow-sm">
          <div className="flex justify-between items-center border-b border-slate-800 pb-1.5 mb-2">
            <h2 className="text-[10px] font-bold text-slate-400 uppercase flex items-center gap-1.5 font-mono">
              <Layers className="w-3.5 h-3.5 text-emerald-400" />
              Core Architecture
            </h2>
            <span className="text-[9px] text-slate-500 font-mono">UDF / Reactive</span>
          </div>

          {/* Architecture Layers */}
          <div className="space-y-2 mb-3">
            {ARCHITECTURE_LAYERS.map((layer) => (
              <div
                key={layer.name}
                className={`p-2 rounded border transition-all ${layer.color}`}
              >
                <div className="flex justify-between items-center mb-0.5">
                  <p className={`text-[10px] font-mono font-semibold ${layer.textColor}`}>
                    // {layer.name}
                  </p>
                </div>
                <p className="text-[11px] font-bold text-slate-200">{layer.tech}</p>
                <p className="text-[9px] text-slate-400 leading-tight mt-0.5">
                  {layer.scope}
                </p>
              </div>
            ))}
          </div>

          {/* Folder Structure */}
          <div className="flex-1 flex flex-col min-h-[140px] overflow-hidden border-t border-slate-800/80 pt-2">
            <h3 className="text-[10px] font-bold text-slate-400 uppercase mb-1.5 flex items-center gap-1.5 font-mono">
              <FolderTree className="w-3 h-3 text-purple-400" />
              Folder Structure
            </h3>
            <div className="flex-1 bg-slate-950/80 p-2 rounded border border-slate-800 overflow-y-auto custom-scrollbar font-mono text-[9px] text-slate-400 leading-tight select-all">
              <pre className="whitespace-pre">{FOLDER_TREE}</pre>
            </div>
          </div>
        </section>

        {/* Center Column: Database ER Model / Interactive Conflict Engine (Col 4-9, Row 1-8) */}
        <section className="lg:col-span-6 lg:row-span-8 bg-slate-900/50 border border-slate-800 rounded-lg p-3 flex flex-col overflow-hidden shadow-sm">
          {/* Section Header with Search & Filter */}
          <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center border-b border-slate-800 pb-2 mb-2.5 gap-2">
            <div>
              <div className="flex items-center gap-2">
                <h2 className="text-[10px] font-bold text-slate-400 uppercase flex items-center gap-1.5 font-mono">
                  <Database className="w-3.5 h-3.5 text-emerald-400" />
                  Database Entity Relations (Room / SQLite)
                </h2>
                <span className="text-[9px] px-1.5 py-0.2 bg-slate-800 text-emerald-400 rounded font-mono">
                  15 Tables
                </span>
              </div>
              <p className="text-[9px] text-slate-500 italic mt-0.5">
                Strict Normalized Model • Soft Delete (isDeleted=1) • Cascade / Restrict Rules
              </p>
            </div>

            {/* Entity Filters */}
            <div className="flex items-center gap-1 text-[9px] font-mono">
              {(['ALL', 'Core', 'Schedule', 'Finance', 'Sync & System'] as const).map(
                (cat) => (
                  <button
                    key={cat}
                    id={`filter-${cat.toLowerCase().replace(/[^a-z]/g, '')}`}
                    onClick={() => setEntityCategoryFilter(cat)}
                    className={`px-1.5 py-0.5 rounded transition-colors ${
                      entityCategoryFilter === cat
                        ? 'bg-emerald-900/80 text-emerald-300 border border-emerald-700'
                        : 'bg-slate-900 text-slate-400 hover:text-slate-200 border border-slate-800'
                    }`}
                  >
                    {cat}
                  </button>
                ),
              )}
            </div>
          </div>

          {/* Conditional Simulator or Entity Grid */}
          {isSimulatorOpen ? (
            <div className="flex-1 overflow-y-auto custom-scrollbar">
              <ConflictSimulator />
            </div>
          ) : (
            <div className="flex-1 flex flex-col overflow-hidden">
              {/* Search Bar */}
              <div className="relative mb-2 shrink-0">
                <Search className="w-3.5 h-3.5 absolute left-2 top-2 text-slate-500" />
                <input
                  id="search-entities"
                  type="text"
                  placeholder="Filter entities by name, table, or field..."
                  value={entitySearch}
                  onChange={(e) => setEntitySearch(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded pl-7 pr-2 py-1 text-[10px] text-slate-200 placeholder-slate-600 focus:outline-none focus:border-emerald-500 font-mono"
                />
              </div>

              {/* Entity Cards Grid */}
              <div className="flex-1 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-2 overflow-y-auto pr-1 custom-scrollbar">
                {filteredEntities.map((entity) => (
                  <div
                    key={entity.id}
                    id={`entity-card-${entity.id}`}
                    onClick={() => setSelectedEntity(entity)}
                    className="border border-slate-800 hover:border-emerald-500/60 bg-slate-950/70 p-2 rounded shadow-sm hover:shadow-md transition-all cursor-pointer flex flex-col justify-between group"
                  >
                    <div>
                      <div className="flex justify-between items-start mb-1 border-b border-slate-800/80 pb-1">
                        <div>
                          <p className="text-[11px] font-bold text-slate-200 group-hover:text-emerald-400 font-mono transition-colors">
                            {entity.name}
                          </p>
                          <span className="text-[8px] text-slate-500 font-mono block">
                            {entity.tableName}
                          </span>
                        </div>
                        {entity.badge && (
                          <span className="text-[8px] px-1 py-0.2 bg-emerald-950 text-emerald-400 border border-emerald-800/60 rounded font-mono">
                            {entity.badge}
                          </span>
                        )}
                      </div>

                      {/* Snippet of key fields */}
                      <ul className="text-[9px] text-slate-400 font-mono space-y-0.5">
                        {entity.fields.slice(0, 3).map((f) => (
                          <li key={f.name} className="flex justify-between">
                            <span className={f.isPk ? 'text-emerald-400 font-semibold' : f.isFk ? 'text-blue-400' : ''}>
                              {f.name}
                            </span>
                            <span className="text-slate-600 text-[8px] uppercase">
                              {f.isPk ? 'PK' : f.isFk ? 'FK' : f.type.split(' ')[0]}
                            </span>
                          </li>
                        ))}
                      </ul>
                    </div>

                    <div className="mt-2 pt-1 border-t border-slate-900 flex justify-between items-center text-[8px] text-slate-500 font-mono">
                      <span>{entity.fields.length} columns</span>
                      <span className="text-emerald-500/80 group-hover:text-emerald-400 flex items-center gap-0.5">
                        Inspect <ChevronRight className="w-2.5 h-2.5" />
                      </span>
                    </div>
                  </div>
                ))}
              </div>

              {/* Bottom Quick Rule Banner */}
              <div className="mt-2 p-2 rounded bg-amber-950/20 border border-amber-900/40 flex justify-between items-center shrink-0">
                <div className="flex items-center gap-2">
                  <div className="w-1.5 h-1.5 rounded-full bg-amber-400"></div>
                  <span className="text-[10px] font-bold text-amber-400 font-mono uppercase">
                    Conflict Engine:
                  </span>
                  <span className="text-[9px] text-amber-500/90 font-mono">
                    newStart &lt; existingEnd ∧ newEnd &gt; existingStart
                  </span>
                </div>
                <button
                  id="btn-quick-open-sim"
                  onClick={() => setIsSimulatorOpen(true)}
                  className="text-[9px] px-2 py-0.5 bg-amber-950 text-amber-300 border border-amber-800 rounded font-mono hover:bg-amber-900/60"
                >
                  Open Simulator
                </button>
              </div>
            </div>
          )}
        </section>

        {/* Right Column: Cloud Sync Pipeline & Metadata (Col 10-12, Row 1-8) */}
        <section className="lg:col-span-3 lg:row-span-8 bg-slate-900/50 border border-slate-800 rounded-lg p-3 flex flex-col overflow-hidden shadow-sm">
          <div className="flex justify-between items-center border-b border-slate-800 pb-1.5 mb-2">
            <h2 className="text-[10px] font-bold text-slate-400 uppercase flex items-center gap-1.5 font-mono">
              <RefreshCw className="w-3.5 h-3.5 text-blue-400" />
              Cloud Sync Pipeline
            </h2>
            <span className="text-[9px] text-slate-500 font-mono">LMW Conflict</span>
          </div>

          {/* Sync Architecture Flow Diagram */}
          <div className="flex-1 flex flex-col items-center justify-center gap-2 py-2">
            {/* Step 1: Room Store */}
            <div className="w-full p-2 bg-slate-950 rounded border border-slate-800 text-center shadow-sm">
              <div className="flex justify-between items-center mb-0.5">
                <span className="text-[8px] text-slate-500 font-mono">// 01</span>
                <span className="text-[8px] px-1 bg-slate-800 text-slate-400 rounded font-mono">PRIMARY SSOT</span>
              </div>
              <span className="text-[11px] text-slate-200 font-mono font-bold block">
                Local Room SQLite DB
              </span>
              <span className="text-[9px] text-slate-500">100% Offline Operational</span>
            </div>

            {/* Down Arrow */}
            <div className="flex flex-col items-center">
              <div className="w-0.5 h-3 bg-slate-700"></div>
              <div className="w-1.5 h-1.5 border-b-2 border-r-2 border-slate-500 rotate-45"></div>
            </div>

            {/* Step 2: Sync Engine */}
            <div className="w-full p-2 bg-emerald-950/20 rounded border border-emerald-800/50 text-center relative shadow-sm">
              <div className="flex justify-between items-center mb-0.5">
                <span className="text-[8px] text-emerald-600 font-mono">// 02</span>
                <span className="text-[8px] px-1 bg-emerald-950 text-emerald-400 border border-emerald-800 rounded font-mono">
                  ENGINE
                </span>
              </div>
              <span className="text-[11px] text-emerald-400 font-mono font-bold block">
                Sync / Conflict Engine
              </span>
              <div className="text-[8px] text-emerald-600 font-mono mt-0.5">
                Last-Modified-Wins • Tombstone Tracking
              </div>
            </div>

            {/* Down Arrow */}
            <div className="flex flex-col items-center">
              <div className="w-0.5 h-3 bg-slate-700"></div>
              <div className="w-1.5 h-1.5 border-b-2 border-r-2 border-slate-500 rotate-45"></div>
            </div>

            {/* Step 3: Google Sheets API */}
            <div className="w-full p-2 bg-blue-950/20 rounded border border-blue-800/50 text-center flex flex-col shadow-sm">
              <div className="flex justify-between items-center mb-0.5">
                <span className="text-[8px] text-blue-600 font-mono">// 03</span>
                <span className="text-[8px] px-1 bg-blue-950 text-blue-400 border border-blue-800 rounded font-mono">
                  CLOUD SINK
                </span>
              </div>
              <span className="text-[11px] text-blue-400 font-mono font-bold">
                Google Sheets API v4
              </span>
              <span className="text-[9px] text-slate-500">1 Spreadsheet • 14 Relational Tabs</span>
            </div>
          </div>

          {/* Sync Metadata & Tabs Inspector */}
          <div className="mt-auto pt-2 border-t border-slate-800">
            <div className="flex justify-between items-center mb-1.5">
              <p className="text-[9px] font-bold text-slate-400 font-mono uppercase">
                14 Multi-Tab Worksheets
              </p>
              <button
                id="btn-inspect-all-sheets"
                onClick={() => setIsSheetsModalOpen(true)}
                className="text-[9px] text-emerald-400 hover:underline font-mono flex items-center gap-0.5"
              >
                View Details <ExternalLink className="w-2.5 h-2.5" />
              </button>
            </div>
            <div className="flex flex-wrap gap-1 max-h-24 overflow-y-auto custom-scrollbar">
              {SHEETS_TABS.map((tab) => (
                <span
                  key={tab.tabName}
                  onClick={() => setIsSheetsModalOpen(true)}
                  className="px-1.5 py-0.5 bg-slate-950 hover:bg-slate-800 border border-slate-800 text-[8px] text-slate-400 hover:text-slate-200 rounded font-mono cursor-pointer transition-colors"
                >
                  {tab.tabName}
                </span>
              ))}
            </div>
          </div>
        </section>

        {/* Bottom Section: Development Roadmap & Phases (Col 1-12, Row 9-12) */}
        <section className="lg:col-span-12 lg:row-span-4 bg-slate-900/30 border border-slate-800 rounded-lg p-3 overflow-hidden flex flex-col shadow-sm">
          <div className="flex justify-between items-center mb-2">
            <h2 className="text-[10px] font-bold text-slate-400 uppercase flex items-center gap-1.5 font-mono">
              <Cpu className="w-3.5 h-3.5 text-orange-400" />
              Development Roadmap & 20 Implementation Phases
            </h2>
            <span className="text-[9px] text-slate-500 font-mono">
              Click any phase for files, testing & exit criteria
            </span>
          </div>

          <div className="flex-1 flex flex-col lg:flex-row justify-between items-start gap-3 overflow-hidden">
            {/* Horizontal Timeline Scroll */}
            <div className="flex-1 w-full overflow-x-auto custom-scrollbar pb-1">
              <div className="flex items-center gap-2 min-w-[1200px] py-1">
                {DEVELOPMENT_PHASES.map((p) => (
                  <button
                    key={p.number}
                    id={`phase-card-${p.number}`}
                    onClick={() => setSelectedPhase(p)}
                    className="flex-1 bg-slate-950/80 hover:bg-slate-900 border border-slate-800 hover:border-emerald-500/50 p-2 rounded text-left transition-all group cursor-pointer flex flex-col justify-between h-20"
                  >
                    <div>
                      <div className="flex justify-between items-center mb-1">
                        <span className="text-[9px] font-mono font-bold text-emerald-400 group-hover:text-emerald-300">
                          P{p.number < 10 ? `0${p.number}` : p.number}
                        </span>
                        <span className="text-[8px] text-slate-500 font-mono uppercase">
                          {p.category}
                        </span>
                      </div>
                      <p className="text-[10px] font-bold text-slate-200 leading-tight truncate">
                        {p.shortTitle}
                      </p>
                    </div>
                    <p className="text-[8px] text-slate-500 truncate mt-1">
                      {p.exitCriteria}
                    </p>
                  </button>
                ))}
              </div>
            </div>

            {/* Dependencies Summary Box */}
            <div className="w-full lg:w-72 bg-slate-950/80 p-2.5 border border-slate-800 rounded shrink-0 flex flex-col justify-between">
              <div>
                <p className="text-[9px] font-bold text-slate-400 uppercase mb-1.5 tracking-wider font-mono">
                  Key Android / Gradle Dependencies
                </p>
                <div className="grid grid-cols-2 gap-x-2 gap-y-1 text-[8px] font-mono text-slate-500">
                  <span className="truncate">• androidx.room:room-ktx</span>
                  <span className="truncate">• compose-bom:2024.09</span>
                  <span className="truncate">• google-api-sheets:v4</span>
                  <span className="truncate">• dagger.hilt.android</span>
                  <span className="truncate">• kotlinx-coroutines</span>
                  <span className="truncate">• androidx.work:work-ktx</span>
                </div>
              </div>
              <div className="mt-2 pt-1.5 border-t border-slate-900 flex justify-between items-center text-[8px] text-slate-500 font-mono">
                <span>Architecture Target</span>
                <span className="text-emerald-400 font-semibold">Offline-First Native</span>
              </div>
            </div>
          </div>
        </section>
      </main>
      )}

      {/* High-Density Footer */}
      <footer className="mt-2.5 flex flex-col sm:flex-row justify-between items-center text-[10px] text-slate-500 border-t border-slate-900 pt-2 font-mono shrink-0 gap-1">
        <div className="flex flex-wrap gap-4">
          <span>
            ARCHITECTURE: <span className="text-slate-300 font-semibold">Clean Architecture + MVVM</span>
          </span>
          <span>
            PRIMARY DB: <span className="text-slate-300 font-semibold">Room / SQLite (UUID PK)</span>
          </span>
          <span>
            CLOUD: <span className="text-slate-300 font-semibold">Optional Google Sheets v4</span>
          </span>
          <span>
            TIMEZONE: <span className="text-slate-300 font-semibold">UTC Storage (Asia/Dhaka UI)</span>
          </span>
        </div>
        <div className="flex items-center gap-2">
          <span className="w-2 h-2 rounded-full bg-emerald-500 animate-ping"></span>
          <span className="text-emerald-400 font-bold">STATUS: ARCHITECTURE READY FOR IMPLEMENTATION</span>
        </div>
      </footer>

      {/* Modals & Inspectors */}
      <EntityInspectorModal
        entity={selectedEntity}
        onClose={() => setSelectedEntity(null)}
      />
      <PhaseInspectorModal
        phase={selectedPhase}
        onClose={() => setSelectedPhase(null)}
      />
      <SheetsViewerModal
        isOpen={isSheetsModalOpen}
        onClose={() => setIsSheetsModalOpen(false)}
      />
    </div>
  );
}
