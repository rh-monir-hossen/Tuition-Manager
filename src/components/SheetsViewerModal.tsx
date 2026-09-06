import { SHEETS_TABS } from '../data/architectureData';
import { X, FileSpreadsheet, Check } from 'lucide-react';
import { useState } from 'react';

interface Props {
  isOpen: boolean;
  onClose: () => void;
}

export function SheetsViewerModal({ isOpen, onClose }: Props) {
  const [selectedTab, setSelectedTab] = useState(SHEETS_TABS[0]);

  if (!isOpen) return null;

  return (
    <div
      id="sheets-viewer-modal"
      className="fixed inset-0 z-50 bg-black/80 backdrop-blur-sm flex items-center justify-center p-4"
    >
      <div className="bg-slate-950 border border-slate-700 rounded-lg max-w-3xl w-full max-h-[85vh] flex flex-col shadow-2xl overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between px-4 py-2.5 bg-slate-900 border-b border-slate-800">
          <div className="flex items-center gap-2">
            <FileSpreadsheet className="w-4 h-4 text-emerald-400" />
            <div>
              <span className="text-sm font-bold text-slate-100 font-mono">
                Google Sheets Multi-Tab Schema (14 Worksheets)
              </span>
              <p className="text-[11px] text-slate-400">
                Single Spreadsheet • Normalized Relational Tabs • RFC 4180 / Sheets v4 REST API
              </p>
            </div>
          </div>
          <button
            id="btn-close-sheets-modal"
            onClick={onClose}
            className="p-1 rounded bg-slate-800 hover:bg-slate-700 text-slate-400 hover:text-white"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Content Layout */}
        <div className="flex-1 flex overflow-hidden">
          {/* Tabs Sidebar */}
          <div className="w-48 bg-slate-900/60 border-r border-slate-800 p-2 overflow-y-auto custom-scrollbar flex flex-col gap-1">
            <span className="text-[9px] font-mono text-slate-500 uppercase px-2 py-1">Worksheets</span>
            {SHEETS_TABS.map((tab) => (
              <button
                key={tab.tabName}
                id={`btn-tab-${tab.tabName}`}
                onClick={() => setSelectedTab(tab)}
                className={`text-left text-[11px] font-mono px-2.5 py-1.5 rounded transition-colors flex items-center justify-between ${
                  selectedTab.tabName === tab.tabName
                    ? 'bg-emerald-950/60 text-emerald-300 border border-emerald-800/80 font-semibold'
                    : 'text-slate-400 hover:bg-slate-800/60 hover:text-slate-200'
                }`}
              >
                <span>{tab.tabName}</span>
                <span className="text-[9px] opacity-60 font-sans">{tab.columns.length} col</span>
              </button>
            ))}
          </div>

          {/* Tab Details */}
          <div className="flex-1 p-4 overflow-y-auto custom-scrollbar flex flex-col gap-3">
            <div className="flex items-center justify-between border-b border-slate-800 pb-2">
              <div>
                <h3 className="text-sm font-bold text-slate-200 font-mono flex items-center gap-2">
                  <span>Tab:</span>
                  <span className="text-emerald-400">{selectedTab.tabName}</span>
                </h3>
                <p className="text-xs text-slate-400 mt-0.5">{selectedTab.purpose}</p>
              </div>
              <span className="text-[10px] px-2 py-1 bg-slate-900 text-slate-400 font-mono border border-slate-800 rounded">
                Total Columns: {selectedTab.columns.length}
              </span>
            </div>

            {/* Simulated Frozen Spreadsheet Header Row */}
            <div className="bg-slate-900/80 border border-slate-800 rounded p-2 overflow-x-auto custom-scrollbar">
              <div className="text-[9px] font-mono text-slate-500 uppercase mb-1.5 flex items-center gap-1">
                <Check className="w-3 h-3 text-emerald-400" />
                Row 1 (Frozen Column Headers)
              </div>
              <div className="flex gap-1.5">
                {selectedTab.columns.map((col, idx) => (
                  <div
                    key={col}
                    className="px-2 py-1 bg-slate-950 border border-slate-700/80 rounded text-[10px] font-mono text-emerald-300 whitespace-nowrap flex items-center gap-1.5"
                  >
                    <span className="text-slate-600 text-[8px]">{idx + 1}</span>
                    <span>{col}</span>
                  </div>
                ))}
              </div>
            </div>

            {/* Sync Guarantees */}
            <div className="bg-slate-900/40 p-3 rounded border border-slate-800/80 text-[11px] text-slate-300 flex flex-col gap-1.5">
              <span className="text-[10px] font-bold text-slate-400 font-mono uppercase">
                Validation & Ingestion Rules
              </span>
              <ul className="space-y-1 text-slate-400 text-[10px]">
                <li>• <strong className="text-slate-300">Deterministic ID:</strong> `id` matches local Room UUID string; stable across cloud and multiple devices.</li>
                <li>• <strong className="text-slate-300">ISO-8601 UTC:</strong> `created_at` and `updated_at` formatted as ISO-8601 strings in cloud.</li>
                <li>• <strong className="text-slate-300">Tombstone Support:</strong> Soft-deleted rows have `is_deleted = TRUE` and populated `deleted_at`.</li>
                <li>• <strong className="text-slate-300">Atomic Multi-Tab:</strong> Updates are batched across all worksheets in a single `spreadsheets.values.batchUpdate`.</li>
              </ul>
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className="px-4 py-2 bg-slate-900 border-t border-slate-800 flex justify-end">
          <button
            id="btn-dismiss-sheets-modal"
            onClick={onClose}
            className="px-3 py-1 bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs rounded"
          >
            Close Schema
          </button>
        </div>
      </div>
    </div>
  );
}
