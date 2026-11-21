import React from 'react';
import { Calendar, Building2, CheckCircle2 } from 'lucide-react';

export const AttendanceScreen: React.FC = () => {
  const today = new Date().toLocaleDateString('en-IN', {
    weekday: 'long',
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  });

  return (
    <div className="space-y-5">
      {/* Date & Site Filter Bar */}
      <div className="bg-white p-4 rounded-2xl border border-slate-200 shadow-sm flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div className="flex items-center space-x-2.5">
          <Calendar className="w-5 h-5 text-emerald-700" />
          <span className="text-sm font-bold text-slate-900">{today}</span>
        </div>

        <div className="flex items-center space-x-2">
          <Building2 className="w-4 h-4 text-slate-500" />
          <select className="text-xs font-semibold bg-slate-50 border border-slate-300 rounded-lg px-2.5 py-2 text-slate-800 focus:outline-none focus:ring-2 focus:ring-emerald-500">
            <option>Downtown Metro Station (Site #1)</option>
            <option>Harbor Bridge Extension (Site #2)</option>
          </select>
        </div>
      </div>

      {/* Quick Action: Mark All Present (1-tap AGENTS.md rule) */}
      <div className="flex items-center justify-between bg-emerald-50 border border-emerald-200 p-4 rounded-2xl">
        <div>
          <h3 className="text-sm font-bold text-emerald-950">Active Roster: 24 Workers</h3>
          <p className="text-xs text-emerald-800">One-tap bulk mark with exception overrides</p>
        </div>
        <button className="bg-emerald-700 hover:bg-emerald-800 active:bg-emerald-900 text-white font-bold text-xs px-4 py-2.5 rounded-xl shadow min-h-[48px] flex items-center space-x-1.5 transition-all">
          <CheckCircle2 className="w-4 h-4" />
          <span>Mark All Present</span>
        </button>
      </div>

      {/* Sample Attendance Cards */}
      <div className="space-y-3">
        {[
          { name: 'Suresh Naik', skill: 'Mason', rate: '₹600/day', status: 'PRESENT' },
          { name: 'Ramesh Rout', skill: 'Carpenter', rate: '₹650/day', status: 'HALF_DAY' },
          { name: 'Pooja Behera', skill: 'Helper', rate: '₹500/day', status: 'ABSENT' },
        ].map((worker, i) => (
          <div
            key={i}
            className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm flex items-center justify-between"
          >
            <div>
              <div className="text-sm font-bold text-slate-900">{worker.name}</div>
              <div className="text-xs text-slate-500">{worker.skill} · {worker.rate}</div>
            </div>

            {/* Attendance Status Buttons */}
            <div className="flex items-center space-x-1.5">
              <span
                className={`px-3 py-1.5 rounded-lg text-xs font-bold ${
                  worker.status === 'PRESENT'
                    ? 'bg-emerald-600 text-white'
                    : worker.status === 'HALF_DAY'
                    ? 'bg-amber-100 text-amber-900 border border-amber-300'
                    : 'bg-rose-100 text-rose-900 border border-rose-300'
                }`}
              >
                {worker.status}
              </span>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
