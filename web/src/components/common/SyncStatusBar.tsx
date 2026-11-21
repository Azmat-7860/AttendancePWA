import React from 'react';
import { useTranslation } from 'react-i18next';
import { CheckCircle2, RefreshCw, Clock, AlertTriangle } from 'lucide-react';

export type SyncState = 'synced' | 'syncing' | 'notSynced' | 'needsAttention';

interface SyncStatusBarProps {
  status?: SyncState;
  pendingCount?: number;
}

export const SyncStatusBar: React.FC<SyncStatusBarProps> = ({
  status = 'synced',
  pendingCount = 0,
}) => {
  const { t } = useTranslation();

  const config = {
    synced: {
      bg: 'bg-emerald-50 border-emerald-200 text-emerald-800',
      icon: <CheckCircle2 className="w-4 h-4 text-emerald-600 flex-shrink-0" />,
      label: t('sync.synced'),
    },
    syncing: {
      bg: 'bg-blue-50 border-blue-200 text-blue-800',
      icon: <RefreshCw className="w-4 h-4 text-blue-600 animate-spin flex-shrink-0" />,
      label: t('sync.syncing'),
    },
    notSynced: {
      bg: 'bg-amber-50 border-amber-300 text-amber-900 border-dashed',
      icon: <Clock className="w-4 h-4 text-amber-600 flex-shrink-0" />,
      label: `${pendingCount} ${t('sync.notSynced')}`,
    },
    needsAttention: {
      bg: 'bg-rose-50 border-rose-300 text-rose-900',
      icon: <AlertTriangle className="w-4 h-4 text-rose-600 flex-shrink-0" />,
      label: t('sync.needsAttention'),
    },
  }[status];

  return (
    <div
      role="status"
      className={`w-full px-3 py-1.5 border-b text-xs font-medium flex items-center justify-between transition-colors ${config.bg}`}
    >
      <div className="flex items-center space-x-2">
        {config.icon}
        <span>{config.label}</span>
      </div>
      <span className="text-[10px] text-gray-500 uppercase tracking-wider font-semibold">
        Offline-Ready
      </span>
    </div>
  );
};
