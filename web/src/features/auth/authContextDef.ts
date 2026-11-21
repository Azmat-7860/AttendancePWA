import { createContext } from 'react';

export type Role = 'OWNER' | 'SUPERVISOR';

export interface UserProfile {
  id: string;
  email: string;
  name: string;
  role: Role;
  organizationId: string;
}

export interface AuthContextType {
  user: UserProfile | null;
  accessToken: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  loginWithGoogle: (idToken: string) => Promise<void>;
  devLogin: (role: Role) => void;
  logout: () => Promise<void>;
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);
