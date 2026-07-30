import { Component, type ErrorInfo, type ReactNode } from 'react';
import { AlertTriangle } from 'lucide-react';

interface Props {
  children: ReactNode;
}

interface State {
  error: Error | null;
}

/**
 * Catches errors thrown synchronously during render or in lifecycle
 * methods/effect bodies anywhere below this in the tree, and shows a
 * readable message instead of leaving a blank page — the alternative
 * we shipped with for a while, which cost real time to diagnose.
 *
 * Doesn't catch errors thrown in event handlers or in truly async code
 * (promise callbacks, setTimeout, etc.) — React explicitly excludes
 * those from error boundary handling. Those still need their own
 * try/catch at the source (see useRealtime.ts for an example).
 */
export class ErrorBoundary extends Component<Props, State> {
  state: State = { error: null };

  static getDerivedStateFromError(error: Error): State {
    return { error };
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    console.error('Unhandled error caught by ErrorBoundary:', error, info.componentStack);
  }

  render() {
    const { error } = this.state;
    if (!error) return this.props.children;

    return (
      <div className="min-h-screen flex items-center justify-center px-6">
        <div className="w-full max-w-sm text-center">
          <AlertTriangle size={28} className="mx-auto mb-4" style={{ color: 'var(--color-signal-red)' }} />
          <h1 className="font-display font-bold text-lg text-ink-100 mb-1.5">Something went wrong</h1>
          <p className="text-xs font-mono text-ink-500 mb-6 break-words">{error.message}</p>
          <button
            type="button"
            onClick={() => window.location.reload()}
            className="bg-data-blue text-graphite font-display font-semibold text-sm rounded-lg px-4 py-2.5"
          >
            Reload
          </button>
        </div>
      </div>
    );
  }
}
