import { DevelopmentPhase } from '../types';
import { X, CheckCircle2, FileCode, Layers, ShieldAlert, Cpu } from 'lucide-react';

interface Props {
  phase: DevelopmentPhase | null;
  onClose: () => void;
}

export function PhaseInspectorModal({ phase, onClose }: Props) {
  if (!phase) return null;

  return (
    <div
      id="phase-inspector-modal"
      className="fixed inset-0 z-50 bg-black/80 backdrop-blur-sm flex items-center justify-center p-4"
    >
      <div className="bg-slate-950 border border-slate-700 rounded-lg max-w-xl w-full max-h-[85vh] flex flex-col shadow-2xl overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between px-4 py-3 bg-slate-900 border-b border-slate-800">
          <div className="flex items-center gap-2">
            <span className="w-6 h-6 rounded-full bg-emerald-500/20 text-emerald-400 border border-emerald-500/40 flex items-center justify-center text-xs font-mono font-bold">
              {phase.number}
            </span>
            <div>
              <div className="text-sm font-bold text-slate-100 font-mono">
                PHASE {phase.number}: {phase.title}
              </div>
              <span className="text-[10px] text-slate-500 font-mono uppercase tracking-wider">
                Category: {phase.category}
              </span>
            </div>
          </div>
          <button
            id="btn-close-phase-modal"
            onClick={onClose}
            className="p-1 rounded bg-slate-800 hover:bg-slate-700 text-slate-400 hover:text-white"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Content */}
        <div className="p-4 overflow-y-auto custom-scrollbar flex flex-col gap-3 text-xs">
          {/* Objective */}
          <div className="bg-slate-900/50 p-2.5 rounded border border-slate-800">
            <div className="text-[10px] font-bold uppercase text-slate-400 font-mono mb-1 flex items-center gap-1.5">
              <Cpu className="w-3.5 h-3.5 text-blue-400" />
              Objective
            </div>
            <p className="text-slate-300 leading-relaxed">{phase.objective}</p>
          </div>

          {/* Features Completed */}
          <div className="bg-slate-900/50 p-2.5 rounded border border-slate-800">
            <div className="text-[10px] font-bold uppercase text-slate-400 font-mono mb-1.5 flex items-center gap-1.5">
              <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400" />
              Features Completed
            </div>
            <ul className="space-y-1 text-slate-300">
              {phase.featuresCompleted.map((f, i) => (
                <li key={i} className="flex items-start gap-1.5">
                  <span className="text-emerald-500 font-bold">•</span>
                  <span>{f}</span>
                </li>
              ))}
            </ul>
          </div>

          {/* Files Affected */}
          <div className="bg-slate-900/50 p-2.5 rounded border border-slate-800">
            <div className="text-[10px] font-bold uppercase text-slate-400 font-mono mb-1.5 flex items-center gap-1.5">
              <FileCode className="w-3.5 h-3.5 text-purple-400" />
              Files Affected
            </div>
            <div className="flex flex-wrap gap-1 font-mono text-[10px]">
              {phase.filesAffected.map((file, i) => (
                <span key={i} className="px-2 py-0.5 bg-slate-950 border border-slate-800 text-purple-300 rounded">
                  {file}
                </span>
              ))}
            </div>
          </div>

          {/* Dependencies & Database Changes */}
          <div className="grid grid-cols-2 gap-2">
            <div className="bg-slate-900/50 p-2 rounded border border-slate-800">
              <div className="text-[9px] font-bold uppercase text-slate-400 font-mono mb-1 flex items-center gap-1">
                <Layers className="w-3 h-3 text-amber-400" />
                Dependencies
              </div>
              <ul className="text-[10px] font-mono text-slate-400 space-y-0.5">
                {phase.dependencies.map((d, i) => (
                  <li key={i} className="truncate">• {d}</li>
                ))}
              </ul>
            </div>
            <div className="bg-slate-900/50 p-2 rounded border border-slate-800">
              <div className="text-[9px] font-bold uppercase text-slate-400 font-mono mb-1 flex items-center gap-1">
                <ShieldAlert className="w-3 h-3 text-orange-400" />
                Database Schema
              </div>
              <p className="text-[10px] text-slate-400 leading-snug">{phase.databaseChanges}</p>
            </div>
          </div>

          {/* Testing Requirements */}
          <div className="bg-slate-900/30 p-2.5 rounded border border-slate-800">
            <div className="text-[10px] font-bold uppercase text-slate-400 font-mono mb-1">
              Testing Requirements
            </div>
            <p className="text-slate-400 leading-relaxed">{phase.testingRequirements}</p>
          </div>

          {/* Exit Criteria */}
          <div className="bg-emerald-950/20 p-2.5 rounded border border-emerald-800/60">
            <div className="text-[10px] font-bold uppercase text-emerald-400 font-mono mb-1">
              Strict Exit Criteria
            </div>
            <p className="text-emerald-200 font-mono leading-relaxed">{phase.exitCriteria}</p>
          </div>
        </div>

        {/* Footer */}
        <div className="px-4 py-2 bg-slate-900 border-t border-slate-800 flex justify-end">
          <button
            id="btn-dismiss-phase-modal"
            onClick={onClose}
            className="px-3 py-1 bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs rounded"
          >
            Close Details
          </button>
        </div>
      </div>
    </div>
  );
}
