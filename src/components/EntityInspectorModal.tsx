import { DatabaseEntity } from '../types';
import { X, Key, Link2, Database, ShieldCheck } from 'lucide-react';

interface Props {
  entity: DatabaseEntity | null;
  onClose: () => void;
}

export function EntityInspectorModal({ entity, onClose }: Props) {
  if (!entity) return null;

  return (
    <div
      id="entity-inspector-modal"
      className="fixed inset-0 z-50 bg-black/80 backdrop-blur-sm flex items-center justify-center p-4"
    >
      <div className="bg-slate-950 border border-slate-700 rounded-lg max-w-2xl w-full max-h-[85vh] flex flex-col shadow-2xl overflow-hidden">
        {/* Modal Header */}
        <div className="flex items-center justify-between px-4 py-2.5 bg-slate-900 border-b border-slate-800">
          <div className="flex items-center gap-2">
            <Database className="w-4 h-4 text-emerald-400" />
            <div>
              <div className="flex items-center gap-2">
                <span className="text-sm font-bold text-slate-100 font-mono">
                  {entity.name}
                </span>
                <span className="text-[10px] px-1.5 py-0.2 bg-slate-800 text-slate-400 font-mono rounded">
                  {entity.tableName}
                </span>
                {entity.badge && (
                  <span className="text-[9px] px-1.5 py-0.2 bg-emerald-950 text-emerald-400 border border-emerald-800 rounded font-mono">
                    {entity.badge}
                  </span>
                )}
              </div>
              <p className="text-[11px] text-slate-400 mt-0.5">{entity.description}</p>
            </div>
          </div>
          <button
            id="btn-close-entity-modal"
            onClick={onClose}
            className="p-1 rounded bg-slate-800 hover:bg-slate-700 text-slate-400 hover:text-white"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Modal Body */}
        <div className="p-4 overflow-y-auto custom-scrollbar flex flex-col gap-3 text-xs">
          {/* Integrity Rules */}
          {(entity.deleteRule || entity.indexes) && (
            <div className="p-2.5 bg-slate-900/60 border border-slate-800 rounded flex flex-col gap-1 text-[10px] font-mono">
              {entity.deleteRule && (
                <div className="flex items-center gap-1.5 text-blue-400">
                  <ShieldCheck className="w-3.5 h-3.5 shrink-0" />
                  <span>Referential Integrity: {entity.deleteRule}</span>
                </div>
              )}
              {entity.indexes && entity.indexes.length > 0 && (
                <div className="text-slate-400">
                  <span className="text-slate-500">Indexes: </span>
                  {entity.indexes.join(' • ')}
                </div>
              )}
            </div>
          )}

          {/* Fields Table */}
          <div className="border border-slate-800 rounded overflow-hidden">
            <table className="w-full text-left border-collapse text-[10px] font-mono">
              <thead>
                <tr className="bg-slate-900 text-slate-400 border-b border-slate-800">
                  <th className="p-2 font-semibold">Field Name</th>
                  <th className="p-2 font-semibold">SQLite Type</th>
                  <th className="p-2 font-semibold">Attributes</th>
                  <th className="p-2 font-semibold">Default / Rules</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60 text-slate-300">
                {entity.fields.map((f) => (
                  <tr key={f.name} className="hover:bg-slate-900/40">
                    <td className="p-2 font-medium flex items-center gap-1">
                      {f.isPk && <Key className="w-3 h-3 text-emerald-400 shrink-0" />}
                      {f.isFk && <Link2 className="w-3 h-3 text-blue-400 shrink-0" />}
                      <span className={f.isPk ? 'text-emerald-400 font-bold' : f.isFk ? 'text-blue-400 font-medium' : ''}>
                        {f.name}
                      </span>
                    </td>
                    <td className="p-2 text-slate-400">{f.type}</td>
                    <td className="p-2">
                      <div className="flex flex-wrap gap-1">
                        {f.isPk && (
                          <span className="px-1 bg-emerald-950 text-emerald-400 border border-emerald-800 rounded text-[8px]">
                            PK
                          </span>
                        )}
                        {f.isFk && (
                          <span className="px-1 bg-blue-950 text-blue-400 border border-blue-800 rounded text-[8px]">
                            FK → {f.fkTarget}
                          </span>
                        )}
                        {f.isNullable && (
                          <span className="px-1 bg-slate-800 text-slate-400 rounded text-[8px]">
                            NULLABLE
                          </span>
                        )}
                        {f.isIndex && (
                          <span className="px-1 bg-purple-950 text-purple-400 border border-purple-800 rounded text-[8px]">
                            INDEX
                          </span>
                        )}
                      </div>
                    </td>
                    <td className="p-2 text-slate-500">
                      {f.defaultVal && <span className="text-slate-400">{f.defaultVal}</span>}
                      {f.note && <span className="italic block text-[9px] text-slate-500">{f.note}</span>}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {/* Generated Room Annotation Preview */}
          <div className="bg-slate-900/90 p-2.5 rounded border border-slate-800">
            <div className="flex justify-between items-center text-[10px] text-slate-500 mb-1 font-mono">
              <span>// Kotlin Room Entity Specification</span>
              <span>Primary Key: UUID String</span>
            </div>
            <pre className="text-[9px] font-mono text-emerald-300/90 leading-tight overflow-x-auto whitespace-pre p-1">
{`@Entity(
  tableName = "${entity.tableName}"${entity.deleteRule ? `,\n  // Referential Integrity: ${entity.deleteRule}` : ''}
)
data class ${entity.name}Entity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
${entity.fields.filter(f => !f.isPk).slice(0, 4).map(f => `  val ${f.name}: ${f.type.split(' ')[0]}${f.isNullable ? '?' : ''}`).join(',\n')},
  // ... (${entity.fields.length - 5} additional properties)
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis(),
  val isDeleted: Boolean = false,
  val deletedAt: Long? = null
)`}
            </pre>
          </div>
        </div>

        {/* Modal Footer */}
        <div className="px-4 py-2 bg-slate-900 border-t border-slate-800 flex justify-between items-center text-[10px] text-slate-500 font-mono">
          <span>All timestamps UTC Epoch Milliseconds</span>
          <button
            id="btn-dismiss-entity-modal"
            onClick={onClose}
            className="px-3 py-1 bg-slate-800 hover:bg-slate-700 text-slate-200 rounded"
          >
            Close Inspector
          </button>
        </div>
      </div>
    </div>
  );
}
