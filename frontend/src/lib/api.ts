import type { LoginResponse, Machine, SparePart, TelemetryMessage, WorkOrder } from '../types';

// Empty by default — same relative /api/** path this app has always used,
// proxied same-origin by Vite in dev and by nginx in the Docker image. Only
// needed when the frontend is served from somewhere that can't proxy to the
// backend itself, e.g. a static host like GitHub Pages.
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '';

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

  const res = await fetch(`${API_BASE_URL}/api${path}`, { headers, ...init });

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
    get: (id: number) => request<Machine>(`/machines/${id}`),
    update: (id: number, machine: Machine) =>
      request<Machine>(`/machines/${id}`, { method: 'PUT', body: JSON.stringify(machine) }),
    telemetryHistory: (id: number) => request<TelemetryMessage[]>(`/machines/${id}/telemetry-history`),
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
