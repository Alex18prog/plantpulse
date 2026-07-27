import { useQuery } from '@tanstack/react-query';
import { useOutletContext } from 'react-router';
import { api } from '../lib/api';
import { MachineCard } from './MachineCard';
import { AlertsFeed } from './AlertsFeed';
import { WorkOrdersBoard } from './WorkOrdersBoard';
import type { AppOutletContext } from './AppLayout';

export function Dashboard() {
  const { machines, latestByMachine, historyByMachine, alerts } = useOutletContext<AppOutletContext>();
  const { data: workOrders = [] } = useQuery({
    queryKey: ['work-orders'],
    queryFn: api.workOrders.list,
    refetchInterval: 5000,
  });

  return (
    <main className="max-w-7xl mx-auto px-6 py-6 flex flex-col gap-6">
      <section>
        <h2 className="font-display text-sm font-semibold text-ink-300 uppercase tracking-wider mb-3">
          Plant floor
        </h2>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          {machines.map((machine) => (
            <MachineCard
              key={machine.id}
              machine={machine}
              latest={latestByMachine[machine.id]}
              history={historyByMachine[machine.id] ?? []}
            />
          ))}
        </div>
      </section>

      <section className="grid grid-cols-1 lg:grid-cols-[1fr_320px] gap-4 items-start">
        <div>
          <h2 className="font-display text-sm font-semibold text-ink-300 uppercase tracking-wider mb-3">
            Work orders
          </h2>
          <WorkOrdersBoard workOrders={workOrders} />
        </div>
        <div className="h-[420px]">
          <AlertsFeed alerts={alerts} />
        </div>
      </section>
    </main>
  );
}
