import React, { useState, useEffect, useCallback } from 'react';
import { AuthContext, Role, UserProfile } from './authContextDef';
import { logger } from '../../lib/logger';

const STORAGE_KEY = 'attendance_session';

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<UserProfile | null>(null);
  const [accessToken, setAccessToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  // Restore cached session on launch (works 100% offline)
  useEffect(() => {
    try {
      const cached = localStorage.getItem(STORAGE_KEY);
      if (cached) {
        const parsed = JSON.parse(cached);
        if (parsed.user && parsed.accessToken) {
          setUser(parsed.user);
          setAccessToken(parsed.accessToken);
          logger.debug('Restored session from offline storage for user ID:', parsed.user.id);
        }
      }
    } catch (e) {
      logger.error('Failed to parse cached session', e);
    } finally {
      setIsLoading(false);
    }
  }, []);

  const refreshSession = useCallback(async () => {
    if (!navigator.onLine) {
      logger.debug('Device is offline, keeping local cached session active');
      return;
    }

    try {
      const res = await fetch('/api/v1/auth/refresh', { method: 'POST' });
      if (res.ok) {
        const data = await res.json();
        setUser(data.user);
        setAccessToken(data.accessToken);
        localStorage.setItem(STORAGE_KEY, JSON.stringify({ user: data.user, accessToken: data.accessToken }));
        logger.debug('Rotated access token successfully for user ID:', data.user.id);
      }
    } catch {
      // Network hiccup: keep local session
      logger.warn('Token refresh failed (network down). Preserving offline session.');
    }
  }, []);

  // Proactive periodic token refresh every 10 minutes while online
  useEffect(() => {
    if (!user) return;
    const interval = setInterval(() => {
      refreshSession();
    }, 10 * 60 * 1000);

    const onOnline = () => {
      refreshSession();
    };
    window.addEventListener('online', onOnline);

    return () => {
      clearInterval(interval);
      window.removeEventListener('online', onOnline);
    };
  }, [user, refreshSession]);

  const loginWithGoogle = async (idToken: string) => {
    setIsLoading(true);
    try {
      const response = await fetch('/api/v1/auth/google', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ idToken }),
      });

      if (!response.ok) {
        const err = await response.json().catch(() => ({}));
        throw new Error(err.detail || 'Google authentication failed');
      }

      const data = await response.json();
      const sessionUser: UserProfile = data.user;
      const token: string = data.accessToken;

      setUser(sessionUser);
      setAccessToken(token);
      localStorage.setItem(STORAGE_KEY, JSON.stringify({ user: sessionUser, accessToken: token }));
      logger.info('User successfully authenticated with Google:', sessionUser.id);
    } finally {
      setIsLoading(false);
    }
  };

  const devLogin = async (role: Role) => {
    setIsLoading(true);
    try {
      const response = await fetch(`/api/v1/auth/dev-login?role=${role}`, {
        method: 'POST',
      });

      if (response.ok) {
        const data = await response.json();
        const sessionUser: UserProfile = data.user;
        const token: string = data.accessToken;

        setUser(sessionUser);
        setAccessToken(token);
        localStorage.setItem(STORAGE_KEY, JSON.stringify({ user: sessionUser, accessToken: token }));
        logger.info('User authenticated via server dev-login for role:', role);
        return;
      }
    } catch (e) {
      logger.warn('Backend server unreachable, falling back to local offline dev session', e);
    } finally {
      setIsLoading(false);
    }

    // Offline fallback for testing when backend server is off
    const devUser: UserProfile = {
      id: role === 'OWNER' ? '00000000-0000-0000-0000-000000000001' : '00000000-0000-0000-0000-000000000002',
      email: role === 'OWNER' ? 'owner@acme.example.com' : 'supervisor@acme.example.com',
      name: role === 'OWNER' ? 'Alice Owner' : 'Bob Supervisor',
      role,
      organizationId: '11111111-1111-1111-1111-111111111111',
    };
    const mockToken = `mock-dev-jwt-${role.toLowerCase()}`;

    setUser(devUser);
    setAccessToken(mockToken);
    localStorage.setItem(STORAGE_KEY, JSON.stringify({ user: devUser, accessToken: mockToken }));
    logger.info('Offline test session established for role:', role);
  };

  const logout = async () => {
    try {
      await fetch('/api/v1/auth/logout', { method: 'POST' }).catch(() => {});
    } finally {
      setUser(null);
      setAccessToken(null);
      localStorage.removeItem(STORAGE_KEY);
      logger.info('User logged out successfully');
    }
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        accessToken,
        isAuthenticated: !!user,
        isLoading,
        loginWithGoogle,
        devLogin,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};
