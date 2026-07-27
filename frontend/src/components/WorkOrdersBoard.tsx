import { useMutation, useQueryClient } from '@tanstack/react-query';
import { api } from '../lib/api';
import type { WorkOrder, WorkOrderStatus } from '../types';

const COLUMNS: { status: WorkOrderStatus; label: string }[] = [
  { status: 'PENDING', label: 'Pending' },
  { status: 'IN_PROGRESS', label: 'In progress' },
  { status: 'DONE', label: 'Done' },
];

function PriorityBadge({ priority }: { priority: WorkOrder['priority'] }) {
  const colors: Record<WorkOrder['priority'], string> = {
    LOW: 'text-ink-500',
    MEDIUM: 'text-data-blue',
    HIGH: 'text-signal-amber',
    CRITICAL: 'text-signal-red',
  };
  return <span className={`text-[10px] font-mono uppercase ${colors[priority]}`}>{priority}</span>;
}

export function WorkOrdersBoard({ workOrders }: { workOrders: WorkOrder[] }) {
  const queryClient = useQueryClient();
  const mutation = useMutation({
    mutationFn: ({ id, status }: { id: number; status: WorkOrderStatus }) =>
      api.workOrders.updateStatus(id, status),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['work-orders'] }),
  });

  return (
    <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
      {COLUMNS.map((col) => {
        const items = workOrders.filter((wo) => wo.status === col.status);
        return (
          <div key={col.status} className="bg-panel border border-steel rounded-xl flex flex-col">
            <div className="px-4 py-3 border-b border-steel flex items-center justify-between">
              <h4 className="font-display text-sm font-semibold text-ink-100">{col.label}</h4>
              <span className="text-xs font-mono text-ink-500">{items.length}</span>
            </div>
            <div className="flex-1 p-3 flex flex-col gap-2 min-h-24">
              {items.map((wo) => {
                const next = col.status === 'PENDING' ? 'IN_PROGRESS' : col.status === 'IN_PROGRESS' ? 'DONE' : null;
                return (
                  <div key={wo.id} className="bg-panel-raised border border-steel rounded-lg p-3">
                    <div className="flex justify-between items-start gap-2">
                      <p className="text-xs text-ink-100 leading-snug">{wo.description}</p>
                      <PriorityBadge priority={wo.priority} />
                    </div>
                    <div className="flex justify-between items-center mt-2">
                      <span className="text-[10px] font-mono text-ink-500">{wo.machine.name}</span>
                      {next && (
                        <button
                          onClick={() => mutation.mutate({ id: wo.id, status: next })}
                          className="text-[10px] font-mono uppercase text-data-blue hover:underline"
                        >
                          Move to {next.replace('_', ' ').toLowerCase()}
                        </button>
                      )}
                    </div>
                  </div>
                );
              })}
              {items.length === 0 && <p className="text-[11px] text-ink-500 px-1">Nothing here.</p>}
            </div>
          </div>
        );
      })}
    </div>
  );
}
