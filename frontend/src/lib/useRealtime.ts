import { useEffect, useRef, useState } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { API_BASE_URL } from './api';
import type { AlertMessage, TelemetryMessage } from '../types';

const HISTORY_LENGTH = 20;

// SockJS wants an http(s) URL — it negotiates the WebSocket upgrade itself,
// so this must NOT be a ws(s):// URL (that throws a SyntaxError inside
// SockJS's constructor; see the git history for a production incident this
// caused). Derived from API_BASE_URL rather than its own env var since it's
// always the same host as the REST API: empty by default gives the
// existing relative "/ws", otherwise "<API_BASE_URL>/ws".
const WS_BASE_URL = `${API_BASE_URL}/ws`;

/**
 * Subscribes to /topic/telemetry and /topic/alerts over STOMP.
 * Keeps a short rolling history per machine so cards can render sparklines
 * without every component re-subscribing individually.
 */
export function useRealtime() {
  const [latestByMachine, setLatestByMachine] = useState<Record<number, TelemetryMessage>>({});
  const [historyByMachine, setHistoryByMachine] = useState<Record<number, TelemetryMessage[]>>({});
  const [alerts, setAlerts] = useState<AlertMessage[]>([]);
  const [connected, setConnected] = useState(false);
  const clientRef = useRef<Client | null>(null);

  useEffect(() => {
    // Belt-and-suspenders: SockJS throws a SyntaxError inside its constructor for a
    // ws(s):// URL, and that throw happens inside stompjs's own connection scheduling —
    // outside React's call stack, so not even an Error Boundary catches it (confirmed by
    // reproducing the incident this comment refers to). WS_BASE_URL can no longer be
    // misconfigured this way (see above), but failing loudly instead of silently never
    // connecting is worth the two lines if that ever changes.
    if (WS_BASE_URL.startsWith('ws:') || WS_BASE_URL.startsWith('wss:')) {
      console.error(`useRealtime: WS_BASE_URL must be http(s) or relative, not "${WS_BASE_URL}". Skipping connection.`);
      return;
    }

    const client = new Client({
      webSocketFactory: () => new SockJS(WS_BASE_URL) as unknown as WebSocket,
      reconnectDelay: 3000,
      onConnect: () => {
        setConnected(true);

        client.subscribe('/topic/telemetry', (frame) => {
          const msg: TelemetryMessage = JSON.parse(frame.body);
          setLatestByMachine((prev) => ({ ...prev, [msg.machineId]: msg }));
          setHistoryByMachine((prev) => {
            const existing = prev[msg.machineId] ?? [];
            const next = [...existing, msg].slice(-HISTORY_LENGTH);
            return { ...prev, [msg.machineId]: next };
          });
        });

        client.subscribe('/topic/alerts', (frame) => {
          const msg: AlertMessage = JSON.parse(frame.body);
          setAlerts((prev) => [msg, ...prev].slice(0, 50));
        });
      },
      onDisconnect: () => setConnected(false),
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
    };
  }, []);

  return { latestByMachine, historyByMachine, alerts, connected };
}
