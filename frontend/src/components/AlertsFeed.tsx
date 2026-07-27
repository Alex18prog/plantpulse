import { AlertTriangle, OctagonAlert } from 'lucide-react';
import type { AlertMessage } from '../types';

interface AlertsFeedProps {
  alerts: AlertMessage[];
}

export function AlertsFeed({ alerts }: AlertsFeedProps) {
  return (
    <div className="bg-panel border border-steel rounded-xl flex flex-col h-full">
      <div className="px-4 py-3 border-b border-steel">
        <h3 className="font-display font-semibold text-sm text-ink-100">Alert log</h3>
        <p className="text-xs text-ink-500">Live threshold breaches from the plant floor</p>
      </div>
      <div className="flex-1 overflow-y-auto divide-y divide-steel">
        {alerts.length === 0 && (
          <p className="text-xs text-ink-500 p-4">No alerts yet. Readings are within range.</p>
        )}
        {alerts.map((alert) => {
          const critical = alert.severity === 'CRITICAL';
          return (
            <div key={`${alert.alertId}-${alert.timestamp}`} className="px-4 py-2.5 flex gap-2.5 items-start">
              {critical ? (
                <OctagonAlert size={16} className="mt-0.5 shrink-0" style={{ color: 'var(--color-signal-red)' }} />
              ) : (
                <AlertTriangle size={16} className="mt-0.5 shrink-0" style={{ color: 'var(--color-signal-amber)' }} />
              )}
              <div className="min-w-0">
                <p className="text-xs text-ink-100 leading-snug">{alert.message}</p>
                <p className="text-[10px] font-mono text-ink-500 mt-0.5">
                  {new Date(alert.timestamp).toLocaleTimeString()}
                  {alert.linkedWorkOrderId ? ` · WO#${alert.linkedWorkOrderId} opened` : ''}
                </p>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
