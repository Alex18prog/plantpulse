import type { LoginResponse, Machine, SparePart, WorkOrder } from '../types';

let authToken: string | null = null;
let onUnauthorized: (() => void) | null = null;

/** Called by AuthContext on login/logout. Kept in a module-level variable (in-memory, never persisted) so the fetch wrapper can attach it without threading it through every call site. */
export function setAuthToken(token: string | null) {
  authToken = token;
}

/** Called by AuthContext to react to a token that the backend has rejected (missing, malformed, or expired). */
export function setUnauthorizedHandler(handler: (() => void) | null) {
  onUnauthorized = handler;
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const headers: Record<string, string> = { 'Content-Type': 'application/json' };
  if (authToken) headers.Authorization = `Bearer ${authToken}`;

  const res = await fetch(`/api${path}`, { headers, ...init });

  if (!res.ok) {
    if (res.status === 401 && onUnauthorized) onUnauthorized();
    const body = await res.json().catch(() => null);
    throw new Error(body?.message ?? `API ${path} failed: ${res.status}`);
  }
  // 204 No Content
  if (res.status === 204) return undefined as T;
  return res.json() as Promise<T>;
}

export const api = {
  auth: {
    login: (email: string, password: string) =>
      request<LoginResponse>('/auth/login', { method: 'POST', body: JSON.stringify({ email, password }) }),
  },
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
  spareParts: {
    list: () => request<SparePart[]>('/spare-parts'),
    adjustStock: (id: number, delta: number) =>
      request<SparePart>(`/spare-parts/${id}/stock?delta=${delta}`, { method: 'PATCH' }),
  },
};
