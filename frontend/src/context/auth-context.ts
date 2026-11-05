import { createContext, useContext } from 'react';
import type { AuthUser, TokenResponse } from '@/types';

export interface AuthContextValue {
  user: AuthUser | null;
  isLoading: boolean;
  login: (tokens: TokenResponse) => void;
  logout: () => void;
}

export const AuthContext = createContext<AuthContextValue | null>(null);

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
