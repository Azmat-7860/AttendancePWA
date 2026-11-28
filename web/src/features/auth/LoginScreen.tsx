import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { useAuth } from './useAuth';
import { Shield, UserCheck, AlertCircle, Sparkles, Loader2 } from 'lucide-react';
import { SiteIllustrationScene } from './SiteIllustrationScene';

declare global {
  interface Window {
    google?: {
      accounts: {
        id: {
          initialize: (config: {
            client_id: string;
            callback: (response: { credential: string }) => void;
          }) => void;
          renderButton: (
            parent: HTMLElement,
            options: { theme: string; size: string; width: string }
          ) => void;
          prompt: () => void;
        };
      };
    };
  }
}

export const LoginScreen: React.FC = () => {
  const { t } = useTranslation();
  const { loginWithGoogle, devLogin, isLoading } = useAuth();
  const [error, setError] = useState<string | null>(null);

  // Initialize Google Identity Services if available
  useEffect(() => {
    const clientId = import.meta.env.VITE_GOOGLE_CLIENT_ID || 'test-google-client-id.apps.googleusercontent.com';

    if (window.google?.accounts?.id) {
      window.google.accounts.id.initialize({
        client_id: clientId,
        callback: async (response: { credential: string }) => {
          try {
            await loginWithGoogle(response.credential);
          } catch (err: unknown) {
            setError(err instanceof Error ? err.message : t('auth.loginFailed'));
          }
        },
      });

      const buttonContainer = document.getElementById('google-btn-container');
      if (buttonContainer) {
        window.google.accounts.id.renderButton(buttonContainer, {
          theme: 'filled_black',
          size: 'large',
          width: '100%',
        });
      }
    }
  }, [loginWithGoogle, t]);

  const handleSimulatedGoogleLogin = async () => {
    setError(null);
    try {
      await loginWithGoogle('test-google-id-token');
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : t('auth.loginFailed');
      setError(msg);
    }
  };

  return (
    <div className="relative h-[100dvh] min-h-[520px] w-full bg-slate-900 flex flex-col justify-between items-center overflow-hidden">
      {/* Full-Screen Atmospheric Construction Site Background */}
      <SiteIllustrationScene />

      {/* TOP: Compact Brand Header floating in the upper sky */}
      <div className="relative z-10 w-full pt-4 sm:pt-7 px-4 text-center">
        <div className="w-9 h-9 sm:w-11 sm:h-11 bg-emerald-500/20 border border-emerald-400/35 backdrop-blur-md rounded-xl sm:rounded-2xl mx-auto flex items-center justify-center text-emerald-300 text-sm sm:text-base font-black shadow-lg shadow-emerald-950/50 mb-1.5">
          WA
        </div>
        <h1 className="text-xl sm:text-3xl font-extrabold text-white tracking-tight drop-shadow">
          {t('app.title')}
        </h1>
        <p className="text-[11px] sm:text-sm text-slate-300 mt-0.5 max-w-xs mx-auto leading-snug">
          {t('auth.signInSubtitle')}
        </p>
      </div>

      {/* MIDDLE: Centered Glassmorphism Login Box over the tall crane & skyline */}
      <div className="relative z-10 w-full px-4 my-auto py-1 flex justify-center items-center anim-card-enter">
        <div className="w-full max-w-sm rounded-3xl bg-slate-900/45 backdrop-blur-xl border border-white/15 shadow-[0_16px_48px_rgba(0,0,0,0.5)] p-4 sm:p-6">
          {/* Error Notification Alert */}
          {error && (
            <div className="mb-3 p-3 rounded-xl bg-rose-500/20 border border-rose-400/35 backdrop-blur-md text-rose-100 text-xs flex items-start space-x-2">
              <AlertCircle className="w-4 h-4 text-rose-300 flex-shrink-0 mt-0.5" />
              <div className="flex-1">
                <span className="font-semibold">{t('common.error')}: </span>
                {error}
              </div>
            </div>
          )}

          {/* Official GIS Button Container (if script loaded) */}
          <div id="google-btn-container" className="mb-2.5 empty:hidden"></div>

          {/* Glassmorphism Google Sign-In Button (48px+ tap target) */}
          <button
            onClick={handleSimulatedGoogleLogin}
            disabled={isLoading}
            className="w-full min-h-[48px] flex items-center justify-center space-x-3 bg-white/15 hover:bg-white/20 active:scale-[0.99] border border-white/25 text-white font-bold rounded-2xl backdrop-blur-md shadow-lg transition-all duration-200 focus:outline-none focus:ring-4 focus:ring-emerald-400/30 disabled:opacity-60"
          >
            {isLoading ? (
              <div className="flex items-center space-x-2 text-slate-100">
                <Loader2 className="w-5 h-5 animate-spin text-emerald-400" />
                <span className="text-sm font-semibold">{t('auth.loggingIn')}</span>
              </div>
            ) : (
              <>
                <span className="w-6 h-6 rounded-full bg-white flex items-center justify-center shadow-sm">
                  <svg className="w-4 h-4" viewBox="0 0 24 24">
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
                </span>
                <span className="text-sm">{t('auth.googleSignIn')}</span>
              </>
            )}
          </button>

          {/* Developer Quick-Login Box (Side-by-Side 2 columns even on mobile to avoid vertical scroll) */}
          <div className="mt-3.5 pt-3 border-t border-white/10">
            <div className="flex items-center space-x-1.5 text-[10px] font-bold text-slate-300 mb-2 uppercase tracking-wider">
              <Sparkles className="w-3 h-3 text-amber-400" />
              <span>{t('auth.devSwitcherTitle')}</span>
            </div>

            <div className="grid grid-cols-2 gap-2">
              {/* Quick Login Owner */}
              <button
                type="button"
                disabled={isLoading}
                onClick={() => devLogin('OWNER')}
                className="group flex items-center justify-center space-x-1.5 py-2 px-2.5 bg-purple-500/20 hover:bg-purple-500/30 active:scale-[0.98] border border-purple-400/30 text-purple-100 rounded-xl text-[11px] font-bold backdrop-blur-md transition-all min-h-[48px] text-center leading-tight"
              >
                <Shield className="w-3.5 h-3.5 text-purple-300 flex-shrink-0 transition-transform group-hover:scale-110" />
                <span>{t('auth.loginAsOwner')}</span>
              </button>

              {/* Quick Login Supervisor */}
              <button
                type="button"
                disabled={isLoading}
                onClick={() => devLogin('SUPERVISOR')}
                className="group flex items-center justify-center space-x-1.5 py-2 px-2.5 bg-sky-500/20 hover:bg-sky-500/30 active:scale-[0.98] border border-sky-400/30 text-sky-100 rounded-xl text-[11px] font-bold backdrop-blur-md transition-all min-h-[48px] text-center leading-tight"
              >
                <UserCheck className="w-3.5 h-3.5 text-sky-300 flex-shrink-0 transition-transform group-hover:scale-110" />
                <span>{t('auth.loginAsSupervisor')}</span>
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* BOTTOM SPACER: Keeps Worker, Owner & Supervisor visible without scrolling */}
      <div className="relative z-10 h-28 sm:h-36 w-full pointer-events-none shrink-0" aria-hidden="true" />
    </div>
  );
};
