import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { BrowserRouter } from 'react-router'
import './index.css'
import App from './App.tsx'
import { AuthProvider } from './context/AuthContext'
import { ErrorBoundary } from './components/ErrorBoundary'

// api.ts's request() already retries on 503/network failure with its own
// backoff and user-facing messaging; react-query's default retry would just
// compound on top of that silently, so it's turned off here.
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: false,
    },
  },
});

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <ErrorBoundary>
      <AuthProvider>
        <QueryClientProvider client={queryClient}>
          {/* BASE_URL is Vite's own env var reflecting the configured `base` (see
              vite.config.ts) — "/" normally, "/plantpulse/" for the GitHub Pages
              build. Without this, routes are matched against the raw pathname
              ("/plantpulse/"), which none of them match, so <Routes> silently
              renders nothing once logged in. */}
          <BrowserRouter basename={import.meta.env.BASE_URL}>
            <App />
          </BrowserRouter>
        </QueryClientProvider>
      </AuthProvider>
    </ErrorBoundary>
  </StrictMode>,
)
