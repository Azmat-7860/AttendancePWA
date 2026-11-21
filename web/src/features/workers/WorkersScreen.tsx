import React from 'react';
import { Plus, Phone } from 'lucide-react';

export const WorkersScreen: React.FC = () => {
  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between bg-white p-4 rounded-2xl border border-slate-200 shadow-sm">
        <div>
          <h2 className="text-xl font-bold text-slate-900">Workers Roster</h2>
          <p className="text-xs text-slate-500">Manage rates, skills, and site allocations</p>
        </div>
        <button className="bg-emerald-700 hover:bg-emerald-800 text-white font-bold text-xs px-4 py-2.5 rounded-xl shadow min-h-[48px] flex items-center space-x-1.5 transition-all">
          <Plus className="w-4 h-4" />
          <span>Add Worker</span>
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
        {[
          { name: 'Suresh Naik', skill: 'MASON', type: 'DAILY', rate: '₹600.00', phone: '+91 98765 43210' },
          { name: 'Ramesh Rout', skill: 'CARPENTER', type: 'DAILY', rate: '₹650.00', phone: '+91 98765 43211' },
          { name: 'Pooja Behera', skill: 'HELPER', type: 'DAILY', rate: '₹500.00', phone: '+91 98765 43212' },
        ].map((w, i) => (
          <div key={i} className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm">
            <div className="flex items-start justify-between">
              <div>
                <h3 className="text-sm font-bold text-slate-900">{w.name}</h3>
                <span className="inline-block bg-slate-100 text-slate-700 text-[10px] font-bold px-2 py-0.5 rounded mt-1">
                  {w.skill}
                </span>
              </div>
              <div className="text-right">
                <span className="text-sm font-extrabold text-emerald-800">{w.rate}</span>
                <span className="text-[10px] text-slate-400 block uppercase font-semibold">{w.type}</span>
              </div>
            </div>
            <div className="mt-3 pt-2 border-t border-slate-100 flex items-center text-xs text-slate-500">
              <Phone className="w-3.5 h-3.5 mr-1.5 text-slate-400" />
              <span>{w.phone}</span>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
