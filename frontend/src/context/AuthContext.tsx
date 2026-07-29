import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { api, setAuthToken, setUnauthorizedHandler, setWakingHandler } from '../lib/api';
import type { Role } from '../types';

interface AuthState {
  token: string | null;
  email: string | null;
  role: Role | null;
}

interface AuthContextValue extends AuthState {
  isAuthenticated: boolean;
  isWakingUp: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

const EMPTY_STATE: AuthState = { token: null, email: null, role: null };

/**
 * Token lives only in this component's state (React memory) — never
 * localStorage/sessionStorage — so it disappears on reload, matching the
 * "session, not persisted credential" trust model chosen for this app.
 */
export function AuthProvider({ children }: { children: ReactNode }) {
  const [state, setState] = useState<AuthState>(EMPTY_STATE);
  const [isWakingUp, setIsWakingUp] = useState(false);

  const logout = () => {
    setAuthToken(null);
    setState(EMPTY_STATE);
  };

  useEffect(() => {
    setUnauthorizedHandler(logout);
    setWakingHandler(setIsWakingUp);
    return () => {
      setUnauthorizedHandler(null);
      setWakingHandler(null);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const login = async (email: string, password: string) => {
    const response = await api.auth.login(email, password);
    setAuthToken(response.token);
    setState({ token: response.token, email: response.email, role: response.role });
  };

  const value = useMemo<AuthContextValue>(
    () => ({ ...state, isAuthenticated: state.token !== null, isWakingUp, login, logout }),
    [state, isWakingUp],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within an AuthProvider');
  return ctx;
}
