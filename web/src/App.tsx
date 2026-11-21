import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './features/auth/AuthContext';
import { useAuth } from './features/auth/useAuth';
import { LoginScreen } from './features/auth/LoginScreen';
import { AppLayout } from './components/layout/AppLayout';
import { AttendanceScreen } from './features/attendance/AttendanceScreen';
import { WorkersScreen } from './features/workers/WorkersScreen';
import { SitesScreen } from './features/sites/SitesScreen';
import { UserManagementScreen } from './features/user/UserManagementScreen';

const ProtectedRoutes: React.FC = () => {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div className="min-h-screen bg-slate-900 flex items-center justify-center text-white text-sm">
        <div className="animate-spin w-6 h-6 border-2 border-emerald-500 border-t-transparent rounded-full mr-3"></div>
        <span>Loading session...</span>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return <AppLayout />;
};

const AuthRoute: React.FC = () => {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return null;
  }

  if (isAuthenticated) {
    return <Navigate to="/" replace />;
  }

  return <LoginScreen />;
};

export const App: React.FC = () => {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<AuthRoute />} />
          <Route element={<ProtectedRoutes />}>
            <Route path="/" element={<AttendanceScreen />} />
            <Route path="/attendance" element={<AttendanceScreen />} />
            <Route path="/workers" element={<WorkersScreen />} />
            <Route path="/sites" element={<SitesScreen />} />
            <Route path="/team" element={<UserManagementScreen />} />
          </Route>
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
};

export default App;
