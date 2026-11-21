import React, { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useAuth } from './useAuth';
import { Shield, UserCheck, AlertCircle, Sparkles } from 'lucide-react';

export const LoginScreen: React.FC = () => {
  const { t } = useTranslation();
  const { loginWithGoogle, devLogin, isLoading } = useAuth();
  const [error, setError] = useState<string | null>(null);

  const handleSimulatedGoogleLogin = async () => {
    setError(null);
    try {
      // In production, Google Identity Services popup returns this ID token.
      // For immediate visual test, we pass a test token:
      await loginWithGoogle('test-google-id-token');
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : t('auth.loginFailed');
      setError(msg);
    }
  };

  return (
    <div className="min-h-screen bg-slate-900 flex flex-col justify-center items-center p-4">
      <div className="max-w-md w-full bg-white rounded-2xl shadow-2xl p-6 sm:p-8 border border-slate-100">
        {/* App Logo & Badge */}
        <div className="text-center mb-6">
          <div className="w-14 h-14 bg-emerald-600 rounded-2xl mx-auto flex items-center justify-center text-white text-2xl font-black shadow-lg mb-3">
            WA
          </div>
          <h2 className="text-2xl font-extrabold text-slate-900 tracking-tight">
            {t('app.title')}
          </h2>
          <p className="text-xs text-slate-500 mt-1">
            {t('auth.signInSubtitle')}
          </p>
        </div>

        {/* Error Notification Alert */}
        {error && (
          <div className="mb-5 p-3 rounded-lg bg-rose-50 border border-rose-200 text-rose-800 text-xs flex items-start space-x-2">
            <AlertCircle className="w-4 h-4 text-rose-600 flex-shrink-0 mt-0.5" />
            <div>
              <span className="font-semibold">{t('common.error')}: </span>
              {error}
            </div>
          </div>
        )}

        {/* 1. Official Google Sign-In Button (48px+ tap target) */}
        <button
          onClick={handleSimulatedGoogleLogin}
          disabled={isLoading}
          className="w-full min-h-[50px] flex items-center justify-center space-x-3 bg-white hover:bg-slate-50 active:bg-slate-100 border-2 border-slate-300 text-slate-800 font-bold rounded-xl shadow-sm transition-all focus:ring-4 focus:ring-emerald-100"
        >
          <svg className="w-5 h-5" viewBox="0 0 24 24">
            <path
              fill="#4285F4"
              d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
            />
            <path
              fill="#34A853"
              d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
            />
            <path
              fill="#FBBC05"
              d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z"
            />
            <path
              fill="#EA4335"
              d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z"
            />
          </svg>
          <span className="text-sm">
            {isLoading ? t('auth.loggingIn') : t('auth.googleSignIn')}
          </span>
        </button>

        {/* 2. Instant Developer Quick-Login Box */}
        <div className="mt-8 pt-6 border-t border-slate-100">
          <div className="flex items-center space-x-1.5 text-xs font-bold text-slate-600 mb-3 uppercase tracking-wider">
            <Sparkles className="w-3.5 h-3.5 text-amber-500" />
            <span>{t('auth.devSwitcherTitle')}</span>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
            {/* Quick Login Owner */}
            <button
              type="button"
              onClick={() => devLogin('OWNER')}
              className="flex items-center justify-center space-x-2 py-3 px-3 bg-purple-50 hover:bg-purple-100 border border-purple-200 text-purple-900 rounded-xl text-xs font-bold transition-all min-h-[48px]"
            >
              <Shield className="w-4 h-4 text-purple-700 flex-shrink-0" />
              <span>{t('auth.loginAsOwner')}</span>
            </button>

            {/* Quick Login Supervisor */}
            <button
              type="button"
              onClick={() => devLogin('SUPERVISOR')}
              className="flex items-center justify-center space-x-2 py-3 px-3 bg-sky-50 hover:bg-sky-100 border border-sky-200 text-sky-900 rounded-xl text-xs font-bold transition-all min-h-[48px]"
            >
              <UserCheck className="w-4 h-4 text-sky-700 flex-shrink-0" />
              <span>{t('auth.loginAsSupervisor')}</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
