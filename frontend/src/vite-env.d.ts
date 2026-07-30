/// <reference types="vite/client" />

interface ImportMetaEnv {
  /**
   * Prefix for REST calls and the derived SockJS endpoint (see api.ts and
   * useRealtime.ts), e.g. "https://api.example.com". Must be http(s) or
   * omitted — never ws(s), SockJS negotiates that upgrade itself. Empty
   * (relative /api/** and /ws) by default.
   */
  readonly VITE_API_BASE_URL?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
