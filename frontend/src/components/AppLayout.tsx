import { useQuery } from '@tanstack/react-query';
import { Outlet } from 'react-router';
import { api } from '../lib/api';
import { useRealtime } from '../lib/useRealtime';
import { useAuth } from '../context/AuthContext';
import { StatusBar } from './StatusBar';
import type { Machine, TelemetryMessage, AlertMessage } from '../types';

export interface AppOutletContext {
  machines: Machine[];
  latestByMachine: Record<number, TelemetryMessage>;
  historyByMachine: Record<number, TelemetryMessage[]>;
  alerts: AlertMessage[];
  connected: boolean;
}

/** Shared shell for every authenticated screen: top status bar + nav, one WebSocket connection for the whole app. */
export function AppLayout() {
  const { email, role, logout } = useAuth();
  const { data: machines = [] } = useQuery({ queryKey: ['machines'], queryFn: api.machines.list });
  const { latestByMachine, historyByMachine, alerts, connected } = useRealtime();

  const context: AppOutletContext = { machines, latestByMachine, historyByMachine, alerts, connected };

  return (
    <div className="min-h-screen">
      <StatusBar
        connected={connected}
        machineCount={machines.length}
        activeAlerts={alerts.length}
        userEmail={email!}
        userRole={role!}
        onLogout={logout}
      />
      <Outlet context={context} />
    </div>
  );
}
