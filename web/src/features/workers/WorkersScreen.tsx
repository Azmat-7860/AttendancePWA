import React, { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Plus, Phone, Users, ShieldAlert, CheckCircle2, X, Briefcase } from 'lucide-react';
import { useAuth } from '../auth/useAuth';

interface WorkerItem {
  id: string;
  name: string;
  phone: string | null;
  skill: string | null;
  payType: 'DAILY' | 'HOURLY' | 'MONTHLY';
  rate: string;
  active: boolean;
}

export const WorkersScreen: React.FC = () => {
  const { t } = useTranslation();
  const { user } = useAuth();
  const isOwner = user?.role === 'OWNER';

  const [workers, setWorkers] = useState<WorkerItem[]>([
    {
      id: '1',
      name: 'Suresh Naik',
      phone: '+91 98765 43210',
      skill: 'Mason',
      payType: 'DAILY',
      rate: '600.00',
      active: true,
    },
    {
      id: '2',
      name: 'Ramesh Rout',
      phone: '+91 98765 43211',
      skill: 'Carpenter',
      payType: 'DAILY',
      rate: '650.00',
      active: true,
    },
    {
      id: '3',
      name: 'Pooja Behera',
      phone: '+91 98765 43212',
      skill: 'Helper',
      payType: 'DAILY',
      rate: '500.00',
      active: true,
    },
    {
      id: '4',
      name: 'Dinesh Sethi',
      phone: '+91 98765 43213',
      skill: 'Electrician',
      payType: 'DAILY',
      rate: '700.00',
      active: false,
    },
  ]);

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [name, setName] = useState('');
  const [phone, setPhone] = useState('');
  const [skill, setSkill] = useState('');
  const [payType, setPayType] = useState<'DAILY' | 'HOURLY' | 'MONTHLY'>('DAILY');
  const [rate, setRate] = useState('');
  const [error, setError] = useState<string | null>(null);

  const handleCreateWorker = (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) {
      setError('Worker name is required');
      return;
    }
    const parsedRate = parseFloat(rate);
    if (isNaN(parsedRate) || parsedRate < 0) {
      setError('Please enter a valid wage rate');
      return;
    }

    const newWorker: WorkerItem = {
      id: Math.random().toString(36).substring(2, 9),
      name: name.trim(),
      phone: phone.trim() || null,
      skill: skill.trim() || null,
      payType,
      rate: parsedRate.toFixed(2),
      active: true,
    };

    setWorkers([newWorker, ...workers]);
    setName('');
    setPhone('');
    setSkill('');
    setRate('');
    setError(null);
    setIsModalOpen(false);
  };

  const toggleWorkerStatus = (id: string) => {
    if (!isOwner) return;
    setWorkers(workers.map((w) => (w.id === id ? { ...w, active: !w.active } : w)));
  };

  return (
    <div className="space-y-4">
      {/* Header Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between bg-white p-4 rounded-2xl border border-slate-200 shadow-sm gap-3">
        <div>
          <h2 className="text-xl font-bold text-slate-900 flex items-center space-x-2">
            <Users className="w-5 h-5 text-emerald-700" />
            <span>{t('workers.title')}</span>
          </h2>
          <p className="text-xs text-slate-500 mt-0.5">{t('workers.subtitle')}</p>
        </div>

        {isOwner && (
          <button
            onClick={() => setIsModalOpen(true)}
            className="bg-emerald-700 hover:bg-emerald-800 text-white font-bold text-xs px-4 py-3 rounded-xl shadow min-h-[48px] flex items-center justify-center space-x-1.5 transition-all w-full sm:w-auto"
          >
            <Plus className="w-4 h-4" />
            <span>{t('workers.addWorkerButton')}</span>
          </button>
        )}
      </div>

      {/* Supervisor View Notice */}
      {!isOwner && (
        <div className="flex items-start space-x-3 bg-amber-50 border border-amber-200 text-amber-900 p-3.5 rounded-xl text-xs">
          <ShieldAlert className="w-4 h-4 text-amber-600 flex-shrink-0 mt-0.5" />
          <span className="font-semibold">{t('workers.supervisorNotice')}</span>
        </div>
      )}

      {/* Workers Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
        {workers.map((w) => (
          <div
            key={w.id}
            className={`bg-white p-4 rounded-xl border shadow-sm transition-all ${
              w.active ? 'border-slate-200' : 'border-slate-200 bg-slate-50 opacity-75'
            }`}
          >
            <div className="flex items-start justify-between">
              <div>
                <h3 className="text-sm font-bold text-slate-900">{w.name}</h3>
                {w.skill && (
                  <span className="inline-flex items-center text-[10px] font-semibold bg-slate-100 text-slate-700 px-2 py-0.5 rounded mt-1">
                    <Briefcase className="w-3 h-3 mr-1 text-slate-400" />
                    {w.skill}
                  </span>
                )}
              </div>
              <div className="text-right">
                <span className="text-sm font-extrabold text-emerald-800 block">
                  ₹{w.rate}
                </span>
                <span className="text-[10px] text-slate-400 uppercase font-semibold">
                  {w.payType}
                </span>
              </div>
            </div>

            <div className="mt-3 pt-2.5 border-t border-slate-100 flex items-center justify-between text-xs">
              <div className="flex items-center text-slate-500">
                {w.phone ? (
                  <>
                    <Phone className="w-3.5 h-3.5 mr-1 text-slate-400" />
                    <span>{w.phone}</span>
                  </>
                ) : (
                  <span className="text-slate-400 text-[11px]">No phone listed</span>
                )}
              </div>

              {isOwner && (
                <button
                  onClick={() => toggleWorkerStatus(w.id)}
                  className={`text-[11px] font-semibold min-h-[44px] px-3 py-1.5 rounded-lg border transition-colors ${
                    w.active
                      ? 'text-rose-700 bg-rose-50 border-rose-200 hover:bg-rose-100'
                      : 'text-emerald-700 bg-emerald-50 border-emerald-200 hover:bg-emerald-100'
                  }`}
                >
                  {w.active ? t('workers.deactivate') : t('workers.reactivate')}
                </button>
              )}
            </div>
          </div>
        ))}
      </div>

      {/* Add Worker Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 bg-slate-900/40 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl max-w-md w-full p-6 shadow-xl border border-slate-200 animate-in fade-in zoom-in-95">
            <div className="flex items-center justify-between pb-3 border-b border-slate-100">
              <h3 className="font-bold text-slate-900 text-base">
                {t('workers.createModalTitle')}
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

            <form onSubmit={handleCreateWorker} className="mt-4 space-y-3.5">
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  {t('workers.nameLabel')} *
                </label>
                <input
                  type="text"
                  required
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder={t('workers.namePlaceholder')}
                  className="w-full text-sm px-3.5 py-2.5 rounded-xl border border-slate-300 focus:outline-none focus:ring-2 focus:ring-emerald-600"
                />
              </div>

              <div className="grid grid-cols-2 gap-2.5">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">
                    {t('workers.skillLabel')}
                  </label>
                  <input
                    type="text"
                    value={skill}
                    onChange={(e) => setSkill(e.target.value)}
                    placeholder={t('workers.skillPlaceholder')}
                    className="w-full text-sm px-3.5 py-2.5 rounded-xl border border-slate-300 focus:outline-none focus:ring-2 focus:ring-emerald-600"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">
                    {t('workers.phoneLabel')}
                  </label>
                  <input
                    type="tel"
                    value={phone}
                    onChange={(e) => setPhone(e.target.value)}
                    placeholder={t('workers.phonePlaceholder')}
                    className="w-full text-sm px-3.5 py-2.5 rounded-xl border border-slate-300 focus:outline-none focus:ring-2 focus:ring-emerald-600"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-2.5">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">
                    {t('workers.payTypeLabel')}
                  </label>
                  <select
                    value={payType}
                    onChange={(e) => setPayType(e.target.value as 'DAILY' | 'HOURLY' | 'MONTHLY')}
                    className="w-full text-sm px-3.5 py-2.5 rounded-xl border border-slate-300 focus:outline-none focus:ring-2 focus:ring-emerald-600 bg-white"
                  >
                    <option value="DAILY">{t('workers.payTypeDaily')}</option>
                    <option value="HOURLY">{t('workers.payTypeHourly')}</option>
                    <option value="MONTHLY">{t('workers.payTypeMonthly')}</option>
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">
                    {t('workers.rateLabel')} *
                  </label>
                  <input
                    type="number"
                    step="0.01"
                    min="0"
                    required
                    value={rate}
                    onChange={(e) => setRate(e.target.value)}
                    placeholder={t('workers.ratePlaceholder')}
                    className="w-full text-sm px-3.5 py-2.5 rounded-xl border border-slate-300 focus:outline-none focus:ring-2 focus:ring-emerald-600"
                  />
                </div>
              </div>

              <div className="flex items-center justify-end space-x-2 pt-3 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setIsModalOpen(false)}
                  className="px-4 py-2.5 text-xs font-semibold text-slate-600 hover:bg-slate-100 rounded-xl min-h-[48px]"
                >
                  {t('workers.cancel')}
                </button>
                <button
                  type="submit"
                  className="px-5 py-2.5 text-xs font-bold bg-emerald-700 hover:bg-emerald-800 text-white rounded-xl shadow min-h-[48px] flex items-center space-x-1"
                >
                  <CheckCircle2 className="w-4 h-4 mr-1" />
                  <span>{t('workers.saveWorker')}</span>
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
