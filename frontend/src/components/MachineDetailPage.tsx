import { useState, type FormEvent } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link, useParams } from 'react-router';
import { ArrowLeft } from 'lucide-react';
import { api } from '../lib/api';
import { useAuth } from '../context/AuthContext';

const STATUS_META: Record<string, { label: string; color: string }> = {
  OPERATIONAL: { label: 'Operational', color: 'var(--color-signal-green)' },
  MAINTENANCE: { label: 'Maintenance', color: 'var(--color-signal-amber)' },
  DOWN: { label: 'Down', color: 'var(--color-signal-red)' },
};

export function MachineDetailPage() {
  const { id } = useParams();
  const machineId = Number(id);
  const { role } = useAuth();
  const isAdmin = role === 'ADMIN';
  const queryClient = useQueryClient();

  const { data: machine, isLoading } = useQuery({
    queryKey: ['machines', machineId],
    queryFn: () => api.machines.get(machineId),
  });

  const [intervalInput, setIntervalInput] = useState<string | null>(null);

  const updateMachine = useMutation({
    mutationFn: (maintenanceIntervalDays: number | null) =>
      api.machines.update(machineId, { ...machine!, maintenanceIntervalDays }),
    onSuccess: (updated) => {
      queryClient.setQueryData(['machines', machineId], updated);
      queryClient.invalidateQueries({ queryKey: ['machines'] });
      setIntervalInput(null);
    },
  });

  if (isLoading || !machine) {
    return (
      <main className="max-w-4xl mx-auto px-6 py-6">
        <p className="text-ink-500 text-sm">Loading…</p>
      </main>
    );
  }

  const meta = STATUS_META[machine.status];
  const currentIntervalValue = intervalInput ?? (machine.maintenanceIntervalDays?.toString() ?? '');

  const handleSaveInterval = (e: FormEvent) => {
    e.preventDefault();
    const parsed = currentIntervalValue.trim() === '' ? null : Number(currentIntervalValue);
    updateMachine.mutate(parsed);
  };

  return (
    <main className="max-w-4xl mx-auto px-6 py-6 flex flex-col gap-4">
      <Link to="/" className="inline-flex items-center gap-1.5 text-xs font-mono text-ink-500 hover:text-ink-300 w-fit">
        <ArrowLeft size={14} /> Back to dashboard
      </Link>

      <div className="bg-panel border border-steel rounded-xl p-5 flex items-start justify-between">
        <div>
          <h2 className="font-display font-bold text-xl text-ink-100">{machine.name}</h2>
          <p className="text-xs text-ink-500 font-mono mt-1">
            {machine.type} · {machine.location} · #{machine.id.toString().padStart(4, '0')}
          </p>
        </div>
        <div className="flex items-center gap-1.5 shrink-0">
          <span className="w-2 h-2 rounded-full" style={{ backgroundColor: meta.color }} />
          <span className="text-xs text-ink-300">{meta.label}</span>
        </div>
      </div>

      <div className="bg-panel border border-steel rounded-xl p-5">
        <h3 className="font-display text-sm font-semibold text-ink-300 uppercase tracking-wider mb-4">
          Preventive maintenance
        </h3>

        <div className="flex flex-col gap-3 text-sm">
          <div className="flex justify-between">
            <span className="text-ink-500">Install date</span>
            <span className="font-mono text-ink-100">{machine.installDate ?? '—'}</span>
          </div>

          {isAdmin ? (
            <form onSubmit={handleSaveInterval} className="flex items-end gap-3 pt-2 border-t border-steel">
              <div className="flex-1">
                <label htmlFor="interval" className="block text-[11px] font-mono uppercase tracking-wider text-ink-500 mb-1.5">
                  Interval (days)
                </label>
                <input
                  id="interval"
                  type="number"
                  min={1}
                  value={currentIntervalValue}
                  onChange={(e) => setIntervalInput(e.target.value)}
                  placeholder="Disabled"
                  className="w-full bg-panel-raised border border-steel rounded-lg px-3 py-2 text-sm text-ink-100 outline-none focus:border-data-blue"
                />
              </div>
              <button
                type="submit"
                disabled={updateMachine.isPending}
                className="bg-data-blue text-graphite font-display font-semibold text-sm rounded-lg px-4 py-2 disabled:opacity-60"
              >
                Save
              </button>
            </form>
          ) : (
            <div className="flex justify-between pt-2 border-t border-steel">
              <span className="text-ink-500">Interval</span>
              <span className="font-mono text-ink-100">
                {machine.maintenanceIntervalDays ? `${machine.maintenanceIntervalDays} days` : 'Disabled'}
              </span>
            </div>
          )}
        </div>
      </div>
    </main>
  );
}
