import type { LoginResponse, Machine, SparePart, TelemetryMessage, WorkOrder } from '../types';

// Empty by default — same relative /api/** path this app has always used,
// proxied same-origin by Vite in dev and by nginx in the Docker image. Only
// needed when the frontend is served from somewhere that can't proxy to the
// backend itself, e.g. a static host like GitHub Pages. Exported so
// useRealtime.ts can derive the SockJS endpoint from the same host instead
// of needing its own separate env var.
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '';

let authToken: string | null = null;
let onUnauthorized: (() => void) | null = null;
let onWaking: ((waking: boolean) => void) | null = null;

/** Called by AuthContext on login/logout. Kept in a module-level variable (in-memory, never persisted) so the fetch wrapper can attach it without threading it through every call site. */
export function setAuthToken(token: string | null) {
  authToken = token;
}

/** Called by AuthContext to react to a token that the backend has rejected (missing, malformed, or expired). */
export function setUnauthorizedHandler(handler: (() => void) | null) {
  onUnauthorized = handler;
}

/** Called by AuthContext to surface "server is waking up" — see the retry loop in request() below. */
export function setWakingHandler(handler: ((waking: boolean) => void) | null) {
  onWaking = handler;
}

// Free-tier hosts (e.g. Render) spin down when idle and take a while to
// spin back up, answering with 503 (or just being unreachable) in the
// meantime. Only these two conditions are retried — anything else (400,
// 401, 403, 404, ...) is a real response and fails immediately.
const WAKE_RETRY_MIN_MS = 5000;
const WAKE_RETRY_MAX_MS = 8000;
const WAKE_RETRY_BUDGET_MS = 90000;

function wakeRetryDelay() {
  return WAKE_RETRY_MIN_MS + Math.random() * (WAKE_RETRY_MAX_MS - WAKE_RETRY_MIN_MS);
}

function sleep(ms: number) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const headers: Record<string, string> = { 'Content-Type': 'application/json' };
  if (authToken) headers.Authorization = `Bearer ${authToken}`;

  const deadline = Date.now() + WAKE_RETRY_BUDGET_MS;
  let waking = false;

  while (true) {
    let res: Response;
    try {
      res = await fetch(`${API_BASE_URL}/api${path}`, { headers, ...init });
    } catch {
      if (Date.now() >= deadline) {
        if (waking) onWaking?.(false);
        throw new Error('Could not reach the server. Please check your connection and try again.');
      }
      waking = true;
      onWaking?.(true);
      await sleep(wakeRetryDelay());
      continue;
    }

    if (res.status === 503) {
      if (Date.now() >= deadline) {
        if (waking) onWaking?.(false);
        throw new Error('The demo server is taking too long to wake up. Please try again in a moment.');
      }
      waking = true;
      onWaking?.(true);
      await sleep(wakeRetryDelay());
      continue;
    }

    if (waking) onWaking?.(false);

    if (!res.ok) {
      if (res.status === 401 && onUnauthorized) onUnauthorized();
      const body = await res.json().catch(() => null);
      throw new Error(body?.message ?? `API ${path} failed: ${res.status}`);
    }
    // 204 No Content
    if (res.status === 204) return undefined as T;
    return res.json() as Promise<T>;
  }
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
