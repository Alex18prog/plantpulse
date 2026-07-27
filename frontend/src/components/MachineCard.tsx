import { Line, LineChart, ResponsiveContainer, YAxis } from 'recharts';
import { Link } from 'react-router';
import { GaugeDial } from './GaugeDial';
import type { Machine, TelemetryMessage } from '../types';

interface MachineCardProps {
  machine: Machine;
  latest?: TelemetryMessage;
  history: TelemetryMessage[];
}

const STATUS_META: Record<Machine['status'], { label: string; color: string }> = {
  OPERATIONAL: { label: 'Operational', color: 'var(--color-signal-green)' },
  MAINTENANCE: { label: 'Maintenance', color: 'var(--color-signal-amber)' },
  DOWN: { label: 'Down', color: 'var(--color-signal-red)' },
};

export function MachineCard({ machine, latest, history }: MachineCardProps) {
  const meta = STATUS_META[machine.status];
  const temperature = latest?.temperature ?? machine.baselineTemperature;
  const vibration = latest?.vibration ?? machine.baselineVibration;

  return (
    <Link
      to={`/machines/${machine.id}`}
      className="bg-panel border border-steel rounded-xl p-4 flex flex-col gap-3 hover:border-steel-light transition-colors"
    >
      <div className="flex items-start justify-between">
        <div>
          <h3 className="font-display font-semibold text-ink-100 leading-tight">{machine.name}</h3>
          <p className="text-xs text-ink-500 font-mono">{machine.type} · {machine.location}</p>
        </div>
        <div className="flex items-center gap-1.5 shrink-0">
          <span className="relative inline-flex w-2 h-2">
            <span
              className="absolute inline-flex w-2 h-2 rounded-full pulse-dot"
              style={{ color: meta.color, backgroundColor: meta.color }}
            />
            <span className="relative w-2 h-2 rounded-full" style={{ backgroundColor: meta.color }} />
          </span>
          <span className="text-xs text-ink-300">{meta.label}</span>
        </div>
      </div>

      <div className="flex items-center justify-around py-1">
        <GaugeDial
          value={temperature}
          min={20}
          max={110}
          warning={75}
          critical={90}
          unit="°C"
          label="Temp"
        />
        <GaugeDial
          value={vibration}
          min={0}
          max={12}
          warning={6}
          critical={9}
          unit="mm/s"
          label="Vibration"
        />
      </div>

      <div className="h-10 -mx-1">
        <ResponsiveContainer width="100%" height="100%">
          <LineChart data={history}>
            <YAxis hide domain={['dataMin - 2', 'dataMax + 2']} />
            <Line
              type="monotone"
              dataKey="temperature"
              stroke="var(--color-data-blue)"
              strokeWidth={1.5}
              dot={false}
              isAnimationActive={false}
            />
          </LineChart>
        </ResponsiveContainer>
      </div>

      <div className="flex justify-between text-[11px] font-mono text-ink-500 border-t border-steel pt-2">
        <span>RPM {latest?.rpm ?? '—'}</span>
        <span>#{machine.id.toString().padStart(4, '0')}</span>
      </div>
    </Link>
  );
}
