import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Minus, Plus } from 'lucide-react';
import { api } from '../lib/api';
import { useAuth } from '../context/AuthContext';
import type { SparePart } from '../types';

function rowTone(part: SparePart): { label: string; rowClassName: string; labelClassName: string } {
  if (part.stockQuantity <= 0) {
    return { label: 'Out of stock', rowClassName: 'border-signal-red/40 bg-signal-red/5', labelClassName: 'text-signal-red' };
  }
  if (part.stockQuantity < part.minStockThreshold) {
    return { label: 'Low stock', rowClassName: 'border-signal-amber/40 bg-signal-amber/5', labelClassName: 'text-signal-amber' };
  }
  return { label: 'OK', rowClassName: 'border-steel', labelClassName: 'text-signal-green' };
}

export function SparePartsPage() {
  const { role } = useAuth();
  const isAdmin = role === 'ADMIN';
  const queryClient = useQueryClient();

  const { data: spareParts = [], isLoading } = useQuery({
    queryKey: ['spare-parts'],
    queryFn: api.spareParts.list,
  });

  const adjustStock = useMutation({
    mutationFn: ({ id, delta }: { id: number; delta: number }) => api.spareParts.adjustStock(id, delta),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['spare-parts'] }),
  });

  return (
    <main className="max-w-7xl mx-auto px-6 py-6">
      <h2 className="font-display text-sm font-semibold text-ink-300 uppercase tracking-wider mb-3">
        Spare parts inventory
      </h2>

      <div className="bg-panel border border-steel rounded-xl overflow-hidden">
        <table className="w-full text-sm">
          <thead>
            <tr className="text-left text-[11px] font-mono uppercase tracking-wider text-ink-500 border-b border-steel">
              <th className="px-4 py-3 font-medium">Part</th>
              <th className="px-4 py-3 font-medium">Stock</th>
              <th className="px-4 py-3 font-medium">Min threshold</th>
              <th className="px-4 py-3 font-medium">Unit cost</th>
              <th className="px-4 py-3 font-medium">Status</th>
              {isAdmin && <th className="px-4 py-3 font-medium text-right">Adjust</th>}
            </tr>
          </thead>
          <tbody>
            {spareParts.map((part) => {
              const tone = rowTone(part);
              return (
                <tr key={part.id} className={`border-b last:border-b-0 ${tone.rowClassName}`}>
                  <td className="px-4 py-3 text-ink-100">{part.name}</td>
                  <td className="px-4 py-3 font-mono text-ink-100">{part.stockQuantity}</td>
                  <td className="px-4 py-3 font-mono text-ink-500">{part.minStockThreshold}</td>
                  <td className="px-4 py-3 font-mono text-ink-500">
                    {part.unitCost != null ? `$${part.unitCost.toFixed(2)}` : '—'}
                  </td>
                  <td className="px-4 py-3">
                    <span className={`text-[10px] font-mono uppercase ${tone.labelClassName}`}>{tone.label}</span>
                  </td>
                  {isAdmin && (
                    <td className="px-4 py-3">
                      <div className="flex items-center justify-end gap-1.5">
                        <button
                          type="button"
                          onClick={() => adjustStock.mutate({ id: part.id, delta: -1 })}
                          disabled={part.stockQuantity <= 0 || adjustStock.isPending}
                          className="w-7 h-7 flex items-center justify-center rounded-md border border-steel text-ink-300 hover:text-ink-100 hover:border-steel-light disabled:opacity-40"
                          title="Decrease stock"
                        >
                          <Minus size={13} />
                        </button>
                        <button
                          type="button"
                          onClick={() => adjustStock.mutate({ id: part.id, delta: 1 })}
                          disabled={adjustStock.isPending}
                          className="w-7 h-7 flex items-center justify-center rounded-md border border-steel text-ink-300 hover:text-ink-100 hover:border-steel-light disabled:opacity-40"
                          title="Increase stock"
                        >
                          <Plus size={13} />
                        </button>
                      </div>
                    </td>
                  )}
                </tr>
              );
            })}
            {!isLoading && spareParts.length === 0 && (
              <tr>
                <td colSpan={isAdmin ? 6 : 5} className="px-4 py-6 text-center text-ink-500 text-xs">
                  No spare parts on record.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </main>
  );
}
