import type { Machine, WorkOrder } from '../types';

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`/api${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...init,
  });
  if (!res.ok) {
    throw new Error(`API ${path} failed: ${res.status}`);
  }
  // 204 No Content
  if (res.status === 204) return undefined as T;
  return res.json() as Promise<T>;
}

export const api = {
  machines: {
    list: () => request<Machine[]>('/machines'),
  },
  workOrders: {
    list: () => request<WorkOrder[]>('/work-orders'),
    updateStatus: (id: number, status: WorkOrder['status']) =>
      request<WorkOrder>(`/work-orders/${id}/status?status=${status}`, { method: 'PATCH' }),
  },
  alerts: {
    listActive: () => request(`/alerts`),
    resolve: (id: number) => request(`/alerts/${id}/resolve`, { method: 'PATCH' }),
  },
};
