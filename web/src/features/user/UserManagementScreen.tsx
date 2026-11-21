import React, { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../auth/useAuth';
import {
  UserPlus,
  Shield,
  UserCheck,
  CheckCircle,
  XCircle,
  Info,
} from 'lucide-react';

interface TeamMember {
  id: string;
  email: string;
  name: string;
  role: 'OWNER' | 'SUPERVISOR';
  active: boolean;
  createdAt: string;
}

export const UserManagementScreen: React.FC = () => {
  const { t } = useTranslation();
  const { user } = useAuth();
  const isOwner = user?.role === 'OWNER';

  // Seed sample team members for immediate visual testing
  const [members, setMembers] = useState<TeamMember[]>([
    {
      id: '1',
      email: 'alice.owner@acme.example.com',
      name: 'Alice Owner',
      role: 'OWNER',
      active: true,
      createdAt: '2026-11-01T08:00:00Z',
    },
    {
      id: '2',
      email: 'bob.supervisor@acme.example.com',
      name: 'Bob Supervisor',
      role: 'SUPERVISOR',
      active: true,
      createdAt: '2026-11-05T09:30:00Z',
    },
    {
      id: '3',
      email: 'carlos.site1@acme.example.com',
      name: 'Carlos Site Lead',
      role: 'SUPERVISOR',
      active: false,
      createdAt: '2026-11-10T11:15:00Z',
    },
  ]);

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [newEmail, setNewEmail] = useState('');
  const [newName, setNewName] = useState('');
  const [newRole, setNewRole] = useState<'OWNER' | 'SUPERVISOR'>('SUPERVISOR');

  const handleInvite = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newEmail || !newName) return;

    const newMember: TeamMember = {
      id: Date.now().toString(),
      email: newEmail.toLowerCase().trim(),
      name: newName.trim(),
      role: newRole,
      active: true,
      createdAt: new Date().toISOString(),
    };

    setMembers([newMember, ...members]);
    setNewEmail('');
    setNewName('');
    setIsModalOpen(false);
  };

  const toggleStatus = (id: string) => {
    if (!isOwner) return;
    setMembers(
      members.map((m) => (m.id === id ? { ...m, active: !m.active } : m))
    );
  };

  const toggleRole = (id: string) => {
    if (!isOwner) return;
    setMembers(
      members.map((m) =>
        m.id === id
          ? { ...m, role: m.role === 'OWNER' ? 'SUPERVISOR' : 'OWNER' }
          : m
      )
    );
  };

  return (
    <div className="space-y-6">
      {/* 1. Header & Invite Action */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 bg-white p-5 rounded-2xl border border-slate-200 shadow-sm">
        <div>
          <h2 className="text-xl font-bold text-slate-900 tracking-tight">
            {t('team.title')}
          </h2>
          <p className="text-xs text-slate-500 mt-0.5">
            {t('team.subtitle')}
          </p>
        </div>

        {isOwner ? (
          <button
            onClick={() => setIsModalOpen(true)}
            className="inline-flex items-center justify-center space-x-2 bg-emerald-700 hover:bg-emerald-800 active:bg-emerald-900 text-white font-bold px-4 py-2.5 rounded-xl shadow text-xs min-h-[48px] transition-all"
          >
            <UserPlus className="w-4 h-4" />
            <span>{t('team.inviteButton')}</span>
          </button>
        ) : (
          <div className="flex items-center space-x-1.5 text-xs text-amber-800 bg-amber-50 border border-amber-200 px-3 py-2 rounded-xl">
            <Info className="w-4 h-4 text-amber-600 flex-shrink-0" />
            <span>{t('team.ownerOnlyNotice')}</span>
          </div>
        )}
      </div>

      {/* 2. Team Members Cards Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {members.map((member) => {
          const isMemberOwner = member.role === 'OWNER';

          return (
            <div
              key={member.id}
              className={`bg-white rounded-2xl p-5 border transition-all shadow-sm ${
                member.active
                  ? 'border-slate-200'
                  : 'border-slate-200 bg-slate-50 opacity-75'
              }`}
            >
              <div className="flex items-start justify-between">
                <div className="flex items-center space-x-3">
                  <div
                    className={`w-10 h-10 rounded-xl flex items-center justify-center font-bold text-sm ${
                      isMemberOwner
                        ? 'bg-purple-100 text-purple-800'
                        : 'bg-sky-100 text-sky-800'
                    }`}
                  >
                    {member.name.charAt(0)}
                  </div>
                  <div>
                    <h3 className="text-sm font-bold text-slate-900">
                      {member.name}
                    </h3>
                    <p className="text-xs text-slate-500">{member.email}</p>
                  </div>
                </div>

                {/* Role Pill */}
                <span
                  className={`inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold ${
                    isMemberOwner
                      ? 'bg-purple-100 text-purple-900 border border-purple-200'
                      : 'bg-sky-100 text-sky-900 border border-sky-200'
                  }`}
                >
                  {isMemberOwner ? (
                    <Shield className="w-3 h-3 mr-1" />
                  ) : (
                    <UserCheck className="w-3 h-3 mr-1" />
                  )}
                  {member.role}
                </span>
              </div>

              {/* Status and Action Buttons */}
              <div className="mt-4 pt-4 border-t border-slate-100 flex items-center justify-between">
                <div className="flex items-center space-x-1.5 text-xs font-semibold">
                  {member.active ? (
                    <>
                      <CheckCircle className="w-3.5 h-3.5 text-emerald-600" />
                      <span className="text-emerald-700">
                        {t('team.statusActive')}
                      </span>
                    </>
                  ) : (
                    <>
                      <XCircle className="w-3.5 h-3.5 text-rose-500" />
                      <span className="text-rose-700">
                        {t('team.statusDeactivated')}
                      </span>
                    </>
                  )}
                </div>

                {/* Owner-Only Action Controls */}
                {isOwner && (
                  <div className="flex items-center space-x-2">
                    <button
                      onClick={() => toggleRole(member.id)}
                      className="px-2.5 py-1.5 text-[11px] font-bold text-slate-700 hover:text-slate-900 bg-slate-100 hover:bg-slate-200 rounded-lg min-h-[36px] transition-colors"
                    >
                      {t('team.changeRole')}
                    </button>
                    <button
                      onClick={() => toggleStatus(member.id)}
                      className={`px-2.5 py-1.5 text-[11px] font-bold rounded-lg min-h-[36px] transition-colors ${
                        member.active
                          ? 'text-rose-700 bg-rose-50 hover:bg-rose-100 border border-rose-200'
                          : 'text-emerald-700 bg-emerald-50 hover:bg-emerald-100 border border-emerald-200'
                      }`}
                    >
                      {member.active
                        ? t('team.deactivate')
                        : t('team.reactivate')}
                    </button>
                  </div>
                )}
              </div>
            </div>
          );
        })}
      </div>

      {/* 3. Invite Member Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 bg-slate-900/60 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white max-w-md w-full rounded-2xl p-6 shadow-2xl border border-slate-100">
            <h3 className="text-lg font-bold text-slate-900 mb-1">
              {t('team.inviteModalTitle')}
            </h3>
            <p className="text-xs text-slate-500 mb-4">
              {t('team.subtitle')}
            </p>

            <form onSubmit={handleInvite} className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">
                  {t('team.inviteNameLabel')}
                </label>
                <input
                  type="text"
                  required
                  value={newName}
                  onChange={(e) => setNewName(e.target.value)}
                  placeholder={t('team.inviteNamePlaceholder')}
                  className="w-full px-3 py-2.5 border border-slate-300 rounded-xl text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none"
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">
                  {t('team.inviteEmailLabel')}
                </label>
                <input
                  type="email"
                  required
                  value={newEmail}
                  onChange={(e) => setNewEmail(e.target.value)}
                  placeholder={t('team.inviteEmailPlaceholder')}
                  className="w-full px-3 py-2.5 border border-slate-300 rounded-xl text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none"
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">
                  {t('team.inviteRoleLabel')}
                </label>
                <select
                  value={newRole}
                  onChange={(e) =>
                    setNewRole(e.target.value as 'OWNER' | 'SUPERVISOR')
                  }
                  className="w-full px-3 py-2.5 border border-slate-300 rounded-xl text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none bg-white"
                >
                  <option value="SUPERVISOR">{t('team.roleSupervisor')}</option>
                  <option value="OWNER">{t('team.roleOwner')}</option>
                </select>
              </div>

              <div className="pt-2 flex items-center justify-end space-x-2">
                <button
                  type="button"
                  onClick={() => setIsModalOpen(false)}
                  className="px-4 py-2.5 text-xs font-bold text-slate-600 hover:text-slate-900 min-h-[48px]"
                >
                  {t('team.cancel')}
                </button>
                <button
                  type="submit"
                  className="px-5 py-2.5 text-xs font-bold bg-emerald-700 hover:bg-emerald-800 text-white rounded-xl shadow min-h-[48px]"
                >
                  {t('team.sendInvite')}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
