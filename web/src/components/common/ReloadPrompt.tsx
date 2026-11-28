import React from 'react';
import { useRegisterSW } from 'virtual:pwa-register/react';
import { RefreshCw, X } from 'lucide-react';

export const ReloadPrompt: React.FC = () => {
  const {
    needRefresh: [needRefresh, setNeedRefresh],
    updateServiceWorker,
  } = useRegisterSW({
    onRegistered(r) {
      // Periodically check for SW updates (every hour)
      if (r) {
        setInterval(() => {
          r.update();
        }, 60 * 60 * 1000);
      }
    },
  });

  const close = () => {
    setNeedRefresh(false);
  };

  if (!needRefresh) {
    return null;
  }

  return (
    <aside
      aria-label="App update available"
      className="fixed bottom-20 left-4 right-4 md:left-auto md:right-6 md:w-96 z-50 bg-slate-900 text-white p-4 rounded-2xl shadow-2xl border border-slate-700 animate-in fade-in slide-in-from-bottom-5"
    >
      <div className="flex items-start justify-between">
        <div className="flex items-start space-x-3">
          <div className="bg-emerald-500/20 text-emerald-400 p-2 rounded-xl mt-0.5">
            <RefreshCw className="w-5 h-5 animate-spin" />
          </div>
          <div>
            <h4 className="text-sm font-bold text-white">Update Available</h4>
            <p className="text-xs text-slate-300 mt-1 leading-relaxed">
              A new version of the app is ready. Reload when you finish your current edits.
            </p>
          </div>
        </div>
        <button
          onClick={close}
          className="text-slate-400 hover:text-white p-1 rounded-lg"
          aria-label="Dismiss update notification"
        >
          <X className="w-4 h-4" />
        </button>
      </div>

      <div className="mt-3 flex items-center justify-end space-x-2 pt-2 border-t border-slate-800">
        <button
          onClick={close}
          className="text-xs font-semibold px-3 py-2 rounded-xl text-slate-400 hover:text-white min-h-[44px]"
        >
          Dismiss
        </button>
        <button
          onClick={() => updateServiceWorker(true)}
          className="text-xs font-bold px-4 py-2 bg-emerald-600 hover:bg-emerald-500 text-white rounded-xl shadow min-h-[44px] flex items-center space-x-1.5"
        >
          <span>Reload Now</span>
        </button>
      </div>
    </aside>
  );
};
