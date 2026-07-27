import { useQuery } from '@tanstack/react-query';
import { CartesianGrid, Legend, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { api } from '../lib/api';

function formatTime(iso: string) {
  return new Date(iso).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
}

export function TelemetryHistoryChart({ machineId }: { machineId: number }) {
  const { data: history = [], isLoading } = useQuery({
    queryKey: ['machines', machineId, 'telemetry-history'],
    queryFn: () => api.machines.telemetryHistory(machineId),
    refetchInterval: 30000,
  });

  if (isLoading) {
    return <p className="text-ink-500 text-sm">Loading history…</p>;
  }

  if (history.length === 0) {
    return (
      <p className="text-ink-500 text-xs">
        No samples yet — readings are recorded periodically, check back shortly.
      </p>
    );
  }

  return (
    <div className="h-64">
      <ResponsiveContainer width="100%" height="100%">
        <LineChart data={history} margin={{ top: 4, right: 8, bottom: 0, left: -12 }}>
          <CartesianGrid stroke="var(--color-steel)" strokeDasharray="3 3" vertical={false} />
          <XAxis
            dataKey="timestamp"
            tickFormatter={formatTime}
            stroke="var(--color-ink-500)"
            tick={{ fontSize: 11, fontFamily: 'var(--font-mono)' }}
            minTickGap={40}
          />
          <YAxis stroke="var(--color-ink-500)" tick={{ fontSize: 11, fontFamily: 'var(--font-mono)' }} />
          <Tooltip
            labelFormatter={(value) => formatTime(value as string)}
            contentStyle={{
              background: 'var(--color-panel-raised)',
              border: '1px solid var(--color-steel)',
              borderRadius: 8,
              fontSize: 12,
              fontFamily: 'var(--font-mono)',
            }}
          />
          <Legend wrapperStyle={{ fontSize: 11, fontFamily: 'var(--font-mono)' }} />
          <Line
            type="monotone"
            dataKey="temperature"
            name="Temp (°C)"
            stroke="var(--color-data-blue)"
            strokeWidth={1.5}
            dot={false}
            isAnimationActive={false}
          />
          <Line
            type="monotone"
            dataKey="vibration"
            name="Vibration (mm/s)"
            stroke="var(--color-signal-amber)"
            strokeWidth={1.5}
            dot={false}
            isAnimationActive={false}
          />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
}
