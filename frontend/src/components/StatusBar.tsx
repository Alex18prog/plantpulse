import { Activity } from 'lucide-react';

export function StatusBar({ connected, machineCount, activeAlerts }: {
  connected: boolean;
  machineCount: number;
  activeAlerts: number;
}) {
  return (
    <header className="border-b border-steel bg-panel px-6 py-4 flex items-center justify-between">
      <div className="flex items-center gap-2.5">
        <Activity size={20} style={{ color: 'var(--color-signal-amber)' }} />
        <div>
          <h1 className="font-display font-bold text-lg leading-none text-ink-100">PlantPulse</h1>
          <p className="text-[11px] text-ink-500 font-mono">Condition monitoring &amp; maintenance</p>
        </div>
      </div>

      <div className="flex items-center gap-6 font-mono text-xs">
        <div className="flex items-center gap-2">
          <span
            className="w-2 h-2 rounded-full"
            style={{ backgroundColor: connected ? 'var(--color-signal-green)' : 'var(--color-signal-red)' }}
          />
          <span className="text-ink-300">{connected ? 'Live' : 'Reconnecting…'}</span>
        </div>
        <div className="text-ink-300">
          <span className="text-ink-100">{machineCount}</span> machines
        </div>
        <div className="text-ink-300">
          <span style={{ color: activeAlerts > 0 ? 'var(--color-signal-red)' : undefined }}>{activeAlerts}</span> active alerts
        </div>
      </div>
    </header>
  );
}
