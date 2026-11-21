import React, { useState, useEffect } from 'react';
import { AuthContext, Role, UserProfile } from './authContextDef';
import { logger } from '../../lib/logger';

const STORAGE_KEY = 'attendance_session';

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<UserProfile | null>(null);
  const [accessToken, setAccessToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);

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

  const devLogin = (role: Role) => {
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
    logger.info('Developer logged in with role:', role);
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
