import { useState, type FormEvent } from 'react';
import { Activity, LoaderCircle } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export function LoginPage() {
  const { login } = useAuth();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await login(email, password);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Login failed');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center px-6">
      <div className="w-full max-w-sm">
        <div className="flex items-center gap-2.5 justify-center mb-8">
          <Activity size={24} style={{ color: 'var(--color-signal-amber)' }} />
          <div>
            <h1 className="font-display font-bold text-xl leading-none text-ink-100">PlantPulse</h1>
            <p className="text-[11px] text-ink-500 font-mono mt-1">Condition monitoring &amp; maintenance</p>
          </div>
        </div>

        <form
          onSubmit={handleSubmit}
          className="bg-panel border border-steel rounded-xl p-6 flex flex-col gap-4"
        >
          <div>
            <label htmlFor="email" className="block text-[11px] font-mono uppercase tracking-wider text-ink-500 mb-1.5">
              Email
            </label>
            <input
              id="email"
              type="email"
              autoComplete="username"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full bg-panel-raised border border-steel rounded-lg px-3 py-2 text-sm text-ink-100 outline-none focus:border-data-blue"
              placeholder="you@plantpulse.dev"
            />
          </div>

          <div>
            <label htmlFor="password" className="block text-[11px] font-mono uppercase tracking-wider text-ink-500 mb-1.5">
              Password
            </label>
            <input
              id="password"
              type="password"
              autoComplete="current-password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full bg-panel-raised border border-steel rounded-lg px-3 py-2 text-sm text-ink-100 outline-none focus:border-data-blue"
              placeholder="••••••••"
            />
          </div>

          {error && (
            <p className="text-xs font-mono" style={{ color: 'var(--color-signal-red)' }}>
              {error}
            </p>
          )}

          <button
            type="submit"
            disabled={submitting}
            className="mt-2 flex items-center justify-center gap-2 bg-data-blue text-graphite font-display font-semibold text-sm rounded-lg px-4 py-2.5 disabled:opacity-60"
          >
            {submitting && <LoaderCircle size={14} className="animate-spin" />}
            {submitting ? 'Signing in…' : 'Sign in'}
          </button>
        </form>

        <p className="text-[11px] text-ink-500 font-mono text-center mt-4">
          Demo: admin@plantpulse.dev / admin123 · marta.ruiz@plantpulse.dev / tech123
        </p>
      </div>
    </div>
  );
}
