import { useCallback, useEffect, useState, type ReactNode } from 'react';
import type { AuthUser, TokenResponse } from '@/types';
import { refreshAccessToken } from '@/api';
import { queryClient } from '@/lib';
import { AuthContext } from './auth-context';

interface JwtPayload {
  sub?: string;
  user_id?: number;
  uuid?: string;
  roles?: string[];
  exp?: number;
}

function parseJwt(token: string): JwtPayload | null {
  try {
    const parts = token.split('.');
    if (parts.length !== 3) return null;
    const base64Url = parts[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      window
        .atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    return JSON.parse(jsonPayload) as JwtPayload;
  } catch {
    return null;
  }
}

function getUserFromToken(token: string | null): AuthUser | null {
  if (!token) return null;
  const payload = parseJwt(token);
  if (!payload || !payload.sub) return null;
  if (payload.exp && payload.exp * 1000 <= Date.now()) {
    return null;
  }
  return {
    id: typeof payload.user_id === 'number' ? payload.user_id : 0,
    username: payload.sub,
    uuid: typeof payload.uuid === 'string' ? payload.uuid : null,
    roles: Array.isArray(payload.roles) ? payload.roles : [],
  };
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(() =>
    getUserFromToken(localStorage.getItem('accessToken'))
  );
  const [isLoading, setIsLoading] = useState<boolean>(() => {
    const hasAccess = Boolean(localStorage.getItem('accessToken'));
    const hasRefresh = Boolean(localStorage.getItem('refreshToken'));
    return !hasAccess && hasRefresh;
  });

  const login = useCallback((tokens: TokenResponse) => {
    localStorage.setItem('accessToken', tokens.accessToken);
    localStorage.setItem('refreshToken', tokens.refreshToken);
    const parsed = getUserFromToken(tokens.accessToken);
    setUser(parsed);
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    queryClient.removeQueries({ queryKey: ['user-profile'] });
    queryClient.removeQueries({ queryKey: ['notifications'] });
    setUser(null);
  }, []);

  useEffect(() => {
    const accessToken = localStorage.getItem('accessToken');
    const refreshToken = localStorage.getItem('refreshToken');

    if (
      !user &&
      refreshToken &&
      (!accessToken || (parseJwt(accessToken)?.exp ?? 0) * 1000 <= Date.now())
    ) {
      let isMounted = true;
      refreshAccessToken(refreshToken)
        .then((tokens) => {
          if (isMounted) {
            login(tokens);
          }
        })
        .catch(() => {
          if (isMounted) {
            logout();
          }
        })
        .finally(() => {
          if (isMounted) {
            setIsLoading(false);
          }
        });
      return () => {
        isMounted = false;
      };
    }
  }, [user, login, logout]);

  useEffect(() => {
    const handleStorage = (event: StorageEvent) => {
      if (event.key === 'accessToken') {
        setUser(getUserFromToken(event.newValue));
      }
    };
    window.addEventListener('storage', handleStorage);
    return () => window.removeEventListener('storage', handleStorage);
  }, []);

  return (
    <AuthContext.Provider value={{ user, isLoading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}
