/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** Prefix for REST calls, e.g. "https://api.example.com". Empty (relative /api/**) by default. */
  readonly VITE_API_BASE_URL?: string;
  /** SockJS endpoint URL, e.g. "https://api.example.com/ws". Relative "/ws" by default. */
  readonly VITE_WS_BASE_URL?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
