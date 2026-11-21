import React from 'react';
import { Outlet, NavLink } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../../features/auth/useAuth';
import { SyncStatusBar } from '../common/SyncStatusBar';
import {
  CalendarCheck,
  Users,
  Building2,
  ShieldCheck,
  LogOut,
  UserCheck,
} from 'lucide-react';

export const AppLayout: React.FC = () => {
  const { t } = useTranslation();
  const { user, logout } = useAuth();

  const isOwner = user?.role === 'OWNER';

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col text-slate-900 pb-20 md:pb-0">
      {/* 1. Global Offline-Sync Status Indicator */}
      <SyncStatusBar status="synced" />

      {/* 2. Top Header Bar (High Contrast for Sunlight) */}
      <header className="bg-slate-900 text-white shadow-md sticky top-0 z-30">
        <div className="max-w-4xl mx-auto px-4 py-3 flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <div className="w-8 h-8 rounded-lg bg-emerald-500 flex items-center justify-center font-bold text-white shadow-inner">
              WA
            </div>
            <div>
              <h1 className="text-base font-bold tracking-tight leading-tight">
                {t('app.title')}
              </h1>
              <p className="text-[11px] text-slate-400 leading-none">
                Acme Constructions
              </p>
            </div>
          </div>

          {/* User Info & Sign Out */}
          {user && (
            <div className="flex items-center space-x-3">
              <div className="text-right hidden sm:block">
                <div className="text-xs font-semibold text-slate-100">{user.name}</div>
                <div className="text-[10px] text-slate-400">{user.email}</div>
              </div>

              {/* Role Badge */}
              <span
                className={`inline-flex items-center px-2 py-0.5 rounded-full text-[11px] font-bold ${
                  isOwner
                    ? 'bg-purple-100 text-purple-900 border border-purple-300'
                    : 'bg-sky-100 text-sky-900 border border-sky-300'
                }`}
              >
                {isOwner ? (
                  <ShieldCheck className="w-3 h-3 mr-1 text-purple-700" />
                ) : (
                  <UserCheck className="w-3 h-3 mr-1 text-sky-700" />
                )}
                {user.role}
              </span>

              {/* Sign Out Button (minimum 48px tap target) */}
              <button
                onClick={logout}
                title={t('nav.logout')}
                className="w-10 h-10 min-w-[44px] min-h-[44px] flex items-center justify-center rounded-lg hover:bg-slate-800 active:bg-slate-700 text-slate-300 transition-colors"
                aria-label={t('nav.logout')}
              >
                <LogOut className="w-5 h-5" />
              </button>
            </div>
          )}
        </div>
      </header>

      {/* 3. Main Content Outlet */}
      <main className="flex-1 max-w-4xl w-full mx-auto p-4 sm:p-6">
        <Outlet />
      </main>

      {/* 4. Bottom Mobile Navigation Bar (Tap targets at least 48px) */}
      <nav
        aria-label="Bottom Navigation"
        className="fixed bottom-0 left-0 right-0 bg-white border-t border-slate-200 shadow-lg z-40 md:static md:border-t-0 md:bg-transparent md:shadow-none md:mt-auto"
      >
        <div className="max-w-4xl mx-auto flex items-center justify-around md:justify-center md:space-x-8 px-2 py-1">
          <NavLink
            to="/"
            end
            className={({ isActive }) =>
              `flex flex-col items-center justify-center w-full min-h-[48px] py-1 text-[11px] font-semibold transition-colors ${
                isActive ? 'text-emerald-700 font-bold' : 'text-slate-500 hover:text-slate-900'
              }`
            }
          >
            <CalendarCheck className="w-5 h-5 mb-0.5" />
            <span>{t('nav.attendance')}</span>
          </NavLink>

          <NavLink
            to="/workers"
            className={({ isActive }) =>
              `flex flex-col items-center justify-center w-full min-h-[48px] py-1 text-[11px] font-semibold transition-colors ${
                isActive ? 'text-emerald-700 font-bold' : 'text-slate-500 hover:text-slate-900'
              }`
            }
          >
            <Users className="w-5 h-5 mb-0.5" />
            <span>{t('nav.workers')}</span>
          </NavLink>

          <NavLink
            to="/sites"
            className={({ isActive }) =>
              `flex flex-col items-center justify-center w-full min-h-[48px] py-1 text-[11px] font-semibold transition-colors ${
                isActive ? 'text-emerald-700 font-bold' : 'text-slate-500 hover:text-slate-900'
              }`
            }
          >
            <Building2 className="w-5 h-5 mb-0.5" />
            <span>{t('nav.sites')}</span>
          </NavLink>

          <NavLink
            to="/team"
            className={({ isActive }) =>
              `flex flex-col items-center justify-center w-full min-h-[48px] py-1 text-[11px] font-semibold transition-colors ${
                isActive ? 'text-emerald-700 font-bold' : 'text-slate-500 hover:text-slate-900'
              }`
            }
          >
            <ShieldCheck className="w-5 h-5 mb-0.5" />
            <span>{t('nav.team')}</span>
          </NavLink>
        </div>
      </nav>
    </div>
  );
};
