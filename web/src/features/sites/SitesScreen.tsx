import React, { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Plus, MapPin, Building2, ShieldAlert, CheckCircle2, X } from 'lucide-react';
import { useAuth } from '../auth/useAuth';

interface SiteItem {
  id: string;
  name: string;
  address: string | null;
  active: boolean;
  workerCount: number;
}

export const SitesScreen: React.FC = () => {
  const { t } = useTranslation();
  const { user } = useAuth();
  const isOwner = user?.role === 'OWNER';

  const [sites, setSites] = useState<SiteItem[]>([
    {
      id: '1',
      name: 'Downtown Metro Station',
      address: 'Plot 42, Outer Ring Road, Sector 5',
      active: true,
      workerCount: 24,
    },
    {
      id: '2',
      name: 'Harbor Bridge Extension',
      address: 'Pier 7, Coastal Highway, Port Zone',
      active: true,
      workerCount: 16,
    },
    {
      id: '3',
      name: 'Old Town Water Pipeline',
      address: 'Crossroads 12, Heritage District',
      active: false,
      workerCount: 0,
    },
  ]);

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [newSiteName, setNewSiteName] = useState('');
  const [newSiteAddress, setNewSiteAddress] = useState('');
  const [error, setError] = useState<string | null>(null);

  const handleCreateSite = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newSiteName.trim()) {
      setError('Site name is required');
      return;
    }

    if (sites.some((s) => s.name.toLowerCase() === newSiteName.trim().toLowerCase())) {
      setError('A site with this name already exists');
      return;
    }

    const newSite: SiteItem = {
      id: Math.random().toString(36).substring(2, 9),
      name: newSiteName.trim(),
      address: newSiteAddress.trim() || null,
      active: true,
      workerCount: 0,
    };

    setSites([newSite, ...sites]);
    setNewSiteName('');
    setNewSiteAddress('');
    setError(null);
    setIsModalOpen(false);
  };

  const toggleSiteStatus = (siteId: string) => {
    if (!isOwner) return;
    setSites(
      sites.map((s) => (s.id === siteId ? { ...s, active: !s.active } : s))
    );
  };

  return (
    <div className="space-y-4">
      {/* Header Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between bg-white p-4 rounded-2xl border border-slate-200 shadow-sm gap-3">
        <div>
          <h2 className="text-xl font-bold text-slate-900 flex items-center space-x-2">
            <Building2 className="w-5 h-5 text-emerald-700" />
            <span>{t('sites.title')}</span>
          </h2>
          <p className="text-xs text-slate-500 mt-0.5">{t('sites.subtitle')}</p>
        </div>

        {isOwner && (
          <button
            onClick={() => setIsModalOpen(true)}
            className="bg-emerald-700 hover:bg-emerald-800 text-white font-bold text-xs px-4 py-3 rounded-xl shadow min-h-[48px] flex items-center justify-center space-x-1.5 transition-all w-full sm:w-auto"
          >
            <Plus className="w-4 h-4" />
            <span>{t('sites.newSiteButton')}</span>
          </button>
        )}
      </div>

      {/* Supervisor Read-Only Banner */}
      {!isOwner && (
        <div className="flex items-start space-x-3 bg-amber-50 border border-amber-200 text-amber-900 p-3.5 rounded-xl text-xs">
          <ShieldAlert className="w-4 h-4 text-amber-600 flex-shrink-0 mt-0.5" />
          <div>
            <span className="font-semibold">{t('sites.supervisorNotice')}</span>
          </div>
        </div>
      )}

      {/* Sites Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
        {sites.map((site) => (
          <div
            key={site.id}
            className={`bg-white p-4 rounded-xl border shadow-sm transition-all ${
              site.active ? 'border-slate-200' : 'border-slate-200 bg-slate-50 opacity-75'
            }`}
          >
            <div className="flex items-start justify-between">
              <div>
                <h3 className="text-sm font-bold text-slate-900 flex items-center space-x-1.5">
                  <span>{site.name}</span>
                </h3>
                {site.address && (
                  <div className="flex items-center text-xs text-slate-500 mt-1">
                    <MapPin className="w-3.5 h-3.5 mr-1 text-slate-400 flex-shrink-0" />
                    <span>{site.address}</span>
                  </div>
                )}
              </div>
              <span
                className={`text-[10px] font-bold px-2.5 py-0.5 rounded-full border ${
                  site.active
                    ? 'bg-emerald-50 text-emerald-800 border-emerald-200'
                    : 'bg-slate-100 text-slate-600 border-slate-300'
                }`}
              >
                {site.active ? t('sites.statusActive') : t('sites.statusInactive')}
              </span>
            </div>

            <div className="mt-4 pt-3 border-t border-slate-100 flex items-center justify-between text-xs">
              <span className="text-slate-500">
                Crew:{' '}
                <strong className="text-slate-900 font-bold">
                  {site.workerCount} Workers
                </strong>
              </span>

              {isOwner && (
                <button
                  onClick={() => toggleSiteStatus(site.id)}
                  className={`text-[11px] font-semibold min-h-[44px] px-3 py-1.5 rounded-lg border transition-colors ${
                    site.active
                      ? 'text-rose-700 bg-rose-50 border-rose-200 hover:bg-rose-100'
                      : 'text-emerald-700 bg-emerald-50 border-emerald-200 hover:bg-emerald-100'
                  }`}
                >
                  {site.active ? t('sites.deactivate') : t('sites.reactivate')}
                </button>
              )}
            </div>
          </div>
        ))}
      </div>

      {/* Create Site Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 bg-slate-900/40 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl max-w-md w-full p-6 shadow-xl border border-slate-200 animate-in fade-in zoom-in-95">
            <div className="flex items-center justify-between pb-3 border-b border-slate-100">
              <h3 className="font-bold text-slate-900 text-base">
                {t('sites.createModalTitle')}
              </h3>
              <button
                onClick={() => setIsModalOpen(false)}
                className="text-slate-400 hover:text-slate-600 p-1 rounded-lg"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {error && (
              <div className="mt-3 p-2.5 bg-rose-50 border border-rose-200 text-rose-800 text-xs rounded-lg">
                {error}
              </div>
            )}

            <form onSubmit={handleCreateSite} className="mt-4 space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  {t('sites.nameLabel')} *
                </label>
                <input
                  type="text"
                  required
                  value={newSiteName}
                  onChange={(e) => setNewSiteName(e.target.value)}
                  placeholder={t('sites.namePlaceholder')}
                  className="w-full text-sm px-3.5 py-2.5 rounded-xl border border-slate-300 focus:outline-none focus:ring-2 focus:ring-emerald-600 focus:border-transparent"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  {t('sites.addressLabel')}
                </label>
                <input
                  type="text"
                  value={newSiteAddress}
                  onChange={(e) => setNewSiteAddress(e.target.value)}
                  placeholder={t('sites.addressPlaceholder')}
                  className="w-full text-sm px-3.5 py-2.5 rounded-xl border border-slate-300 focus:outline-none focus:ring-2 focus:ring-emerald-600 focus:border-transparent"
                />
              </div>

              <div className="flex items-center justify-end space-x-2 pt-3 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setIsModalOpen(false)}
                  className="px-4 py-2.5 text-xs font-semibold text-slate-600 hover:bg-slate-100 rounded-xl min-h-[48px]"
                >
                  {t('sites.cancel')}
                </button>
                <button
                  type="submit"
                  className="px-5 py-2.5 text-xs font-bold bg-emerald-700 hover:bg-emerald-800 text-white rounded-xl shadow min-h-[48px] flex items-center space-x-1"
                >
                  <CheckCircle2 className="w-4 h-4 mr-1" />
                  <span>{t('sites.saveSite')}</span>
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
