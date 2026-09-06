import { useState } from 'react';
import { AlertTriangle, CheckCircle2, RefreshCw } from 'lucide-react';

interface Preset {
  name: string;
  s1: number; // minutes from midnight
  e1: number;
  s2: number;
  e2: number;
  expectedConflict: boolean;
  description: string;
}

const PRESETS: Preset[] = [
  {
    name: 'Partial Overlap',
    s1: 1020, // 17:00
    e1: 1080, // 18:00
    s2: 1050, // 17:30
    e2: 1110, // 18:30
    expectedConflict: true,
    description: 'Candidate starts at 5:30 PM before existing ends at 6:00 PM',
  },
  {
    name: 'Boundary Touch (Safe)',
    s1: 1020, // 17:00
    e1: 1080, // 18:00
    s2: 1080, // 18:00
    e2: 1140, // 19:00
    expectedConflict: false,
    description: 'Candidate starts exactly when existing ends. Valid back-to-back class.',
  },
  {
    name: 'Exact Collision',
    s1: 1020, // 17:00
    e1: 1080, // 18:00
    s2: 1020, // 17:00
    e2: 1080, // 18:00
    expectedConflict: true,
    description: 'Both sessions occupy the identical 5:00 PM - 6:00 PM slot.',
  },
  {
    name: 'Contained Inside',
    s1: 960,  // 16:00
    e1: 1140, // 19:00
    s2: 1020, // 17:00
    e2: 1080, // 18:00
    expectedConflict: true,
    description: 'Candidate session falls completely inside existing 3-hour batch.',
  },
  {
    name: 'Disjoint / Clear',
    s1: 900,  // 15:00
    e1: 960,  // 16:00
    s2: 1020, // 17:00
    e2: 1080, // 18:00
    expectedConflict: false,
    description: 'Separate sessions with 1 hour buffer between them.',
  },
];

function formatTime(mins: number): string {
  const h = Math.floor(mins / 60);
  const m = mins % 60;
  const ampm = h >= 12 ? 'PM' : 'AM';
  const h12 = h % 12 === 0 ? 12 : h % 12;
  const mStr = m < 10 ? `0${m}` : `${m}`;
  return `${h12}:${mStr} ${ampm}`;
}

export function ConflictSimulator() {
  const [s1, setS1] = useState(1020); // 17:00 (5:00 PM)
  const [e1, setE1] = useState(1080); // 18:00 (6:00 PM)
  const [s2, setS2] = useState(1050); // 17:30 (5:30 PM)
  const [e2, setE2] = useState(1110); // 18:30 (6:30 PM)

  // Math rule: newStart < existingEnd AND newEnd > existingStart
  const condition1 = s2 < e1;
  const condition2 = e2 > s1;
  const isConflict = condition1 && condition2;

  const handleApplyPreset = (p: Preset) => {
    setS1(p.s1);
    setE1(p.e1);
    setS2(p.s2);
    setE2(p.e2);
  };

  // Timeline representation range: 14:00 (840) to 21:00 (1260)
  const viewMin = 840;
  const viewMax = 1260;
  const totalSpan = viewMax - viewMin;

  const getPercent = (val: number) => {
    const clamped = Math.max(viewMin, Math.min(viewMax, val));
    return ((clamped - viewMin) / totalSpan) * 100;
  };

  return (
    <div id="conflict-simulator" className="p-3 bg-slate-950 border border-amber-900/60 rounded-lg flex flex-col gap-2.5">
      <div className="flex items-center justify-between border-b border-slate-800 pb-1.5">
        <div className="flex items-center gap-2">
          <div className="w-2 h-2 rounded-full bg-amber-400 animate-pulse"></div>
          <span className="text-[11px] font-bold text-amber-400 font-mono tracking-wide uppercase">
            Conflict Engine Simulation
          </span>
        </div>
        <span className="text-[9px] font-mono text-slate-500">
          Rule: newStart &lt; extEnd ∧ newEnd &gt; extStart
        </span>
      </div>

      {/* Preset Buttons */}
      <div className="flex flex-wrap gap-1">
        {PRESETS.map((p, idx) => (
          <button
            key={p.name}
            id={`conflict-preset-${idx}`}
            onClick={() => handleApplyPreset(p)}
            className="text-[9px] px-2 py-0.5 rounded bg-slate-900 hover:bg-slate-800 text-slate-300 border border-slate-800 transition-colors"
          >
            {p.name}
          </button>
        ))}
      </div>

      {/* Visual Timeline Bar */}
      <div className="relative h-14 bg-slate-900/80 rounded border border-slate-800 p-1.5 flex flex-col justify-between overflow-hidden">
        {/* Scale hours markers */}
        <div className="flex justify-between text-[8px] font-mono text-slate-600 px-1 border-b border-slate-800/60 pb-0.5">
          <span>2:00 PM</span>
          <span>4:00 PM</span>
          <span>6:00 PM</span>
          <span>8:00 PM</span>
        </div>

        {/* Existing Slot Bar */}
        <div className="relative h-3 w-full bg-slate-950 rounded overflow-hidden">
          <div
            className="absolute top-0 bottom-0 bg-blue-500/80 border border-blue-400 rounded text-[7px] text-white flex items-center justify-center font-mono"
            style={{
              left: `${getPercent(s1)}%`,
              width: `${Math.max(4, getPercent(e1) - getPercent(s1))}%`,
            }}
          >
            Existing: {formatTime(s1)}–{formatTime(e1)}
          </div>
        </div>

        {/* Candidate Slot Bar */}
        <div className="relative h-3 w-full bg-slate-950 rounded overflow-hidden">
          <div
            className={`absolute top-0 bottom-0 rounded text-[7px] text-white flex items-center justify-center font-mono border ${
              isConflict
                ? 'bg-rose-600/90 border-rose-400 animate-pulse'
                : 'bg-emerald-600/90 border-emerald-400'
            }`}
            style={{
              left: `${getPercent(s2)}%`,
              width: `${Math.max(4, getPercent(e2) - getPercent(s2))}%`,
            }}
          >
            Candidate: {formatTime(s2)}–{formatTime(e2)}
          </div>
        </div>
      </div>

      {/* Numerical Inputs and Mathematical Evaluation */}
      <div className="grid grid-cols-2 gap-2 text-[10px] font-mono">
        <div className="bg-slate-900/60 p-2 rounded border border-slate-800">
          <div className="text-blue-400 font-semibold mb-1 flex items-center justify-between">
            <span>[Slot 1] Existing Class</span>
            <span className="text-[9px] text-slate-500">{formatTime(s1)} - {formatTime(e1)}</span>
          </div>
          <div className="flex items-center gap-1 mt-1">
            <span className="text-[8px] text-slate-500 w-8">Start</span>
            <input
              id="input-s1"
              type="range"
              min={840}
              max={1200}
              step={15}
              value={s1}
              onChange={(e) => {
                const val = Number(e.target.value);
                setS1(val);
                if (val >= e1) setE1(val + 30);
              }}
              className="flex-1 accent-blue-500 h-1 bg-slate-800 rounded cursor-pointer"
            />
          </div>
          <div className="flex items-center gap-1 mt-1">
            <span className="text-[8px] text-slate-500 w-8">End</span>
            <input
              id="input-e1"
              type="range"
              min={s1 + 15}
              max={1260}
              step={15}
              value={e1}
              onChange={(e) => setE1(Number(e.target.value))}
              className="flex-1 accent-blue-500 h-1 bg-slate-800 rounded cursor-pointer"
            />
          </div>
        </div>

        <div className="bg-slate-900/60 p-2 rounded border border-slate-800">
          <div className="text-emerald-400 font-semibold mb-1 flex items-center justify-between">
            <span>[Slot 2] New Candidate</span>
            <span className="text-[9px] text-slate-500">{formatTime(s2)} - {formatTime(e2)}</span>
          </div>
          <div className="flex items-center gap-1 mt-1">
            <span className="text-[8px] text-slate-500 w-8">Start</span>
            <input
              id="input-s2"
              type="range"
              min={840}
              max={1200}
              step={15}
              value={s2}
              onChange={(e) => {
                const val = Number(e.target.value);
                setS2(val);
                if (val >= e2) setE2(val + 30);
              }}
              className="flex-1 accent-emerald-500 h-1 bg-slate-800 rounded cursor-pointer"
            />
          </div>
          <div className="flex items-center gap-1 mt-1">
            <span className="text-[8px] text-slate-500 w-8">End</span>
            <input
              id="input-e2"
              type="range"
              min={s2 + 15}
              max={1260}
              step={15}
              value={e2}
              onChange={(e) => setE2(Number(e.target.value))}
              className="flex-1 accent-emerald-500 h-1 bg-slate-800 rounded cursor-pointer"
            />
          </div>
        </div>
      </div>

      {/* Result Card */}
      <div
        className={`p-2 rounded border flex items-center justify-between ${
          isConflict
            ? 'bg-rose-950/30 border-rose-800/80 text-rose-300'
            : 'bg-emerald-950/30 border-emerald-800/80 text-emerald-300'
        }`}
      >
        <div className="flex items-center gap-2">
          {isConflict ? (
            <AlertTriangle className="w-4 h-4 text-rose-400 shrink-0" />
          ) : (
            <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
          )}
          <div>
            <div className="text-[10px] font-bold uppercase tracking-wider">
              {isConflict ? 'CONFLICT DETECTED (BLOCK WRITE)' : 'NO CONFLICT (SAFE TO SCHEDULE)'}
            </div>
            <div className="text-[9px] font-mono opacity-80">
              ({s2} &lt; {e1} [{condition1 ? 'TRUE' : 'FALSE'}]) ∧ ({e2} &gt; {s1} [{condition2 ? 'TRUE' : 'FALSE'}])
            </div>
          </div>
        </div>
        <button
          id="btn-reset-simulator"
          onClick={() => handleApplyPreset(PRESETS[0])}
          className="text-[8px] px-1.5 py-1 bg-slate-900 border border-slate-700 rounded text-slate-400 hover:text-slate-200 flex items-center gap-1"
        >
          <RefreshCw className="w-2.5 h-2.5" />
          Reset
        </button>
      </div>
    </div>
  );
}
