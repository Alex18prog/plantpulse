import { useEffect, useRef, useState } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import type { AlertMessage, TelemetryMessage } from '../types';

const HISTORY_LENGTH = 20;

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
    const client = new Client({
      webSocketFactory: () => new SockJS('/ws') as unknown as WebSocket,
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
