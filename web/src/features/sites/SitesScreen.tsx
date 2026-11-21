import React from 'react';
import { Plus, MapPin } from 'lucide-react';

export const SitesScreen: React.FC = () => {
  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between bg-white p-4 rounded-2xl border border-slate-200 shadow-sm">
        <div>
          <h2 className="text-xl font-bold text-slate-900">Job Sites</h2>
          <p className="text-xs text-slate-500">Active project sites and geographic work locations</p>
        </div>
        <button className="bg-emerald-700 hover:bg-emerald-800 text-white font-bold text-xs px-4 py-2.5 rounded-xl shadow min-h-[48px] flex items-center space-x-1.5 transition-all">
          <Plus className="w-4 h-4" />
          <span>New Site</span>
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
        {[
          { name: 'Downtown Metro Station', address: 'Plot 42, Outer Ring Road, Sector 5', active: true, workers: 24 },
          { name: 'Harbor Bridge Extension', address: 'Pier 7, Coastal Highway, Port Zone', active: true, workers: 16 },
        ].map((site, i) => (
          <div key={i} className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm">
            <div className="flex items-start justify-between">
              <div>
                <h3 className="text-sm font-bold text-slate-900">{site.name}</h3>
                <div className="flex items-center text-xs text-slate-500 mt-1">
                  <MapPin className="w-3.5 h-3.5 mr-1 text-slate-400 flex-shrink-0" />
                  <span>{site.address}</span>
                </div>
              </div>
              <span className="bg-emerald-50 text-emerald-800 border border-emerald-200 text-[10px] font-bold px-2 py-0.5 rounded-full">
                Active
              </span>
            </div>
            <div className="mt-3 pt-2 border-t border-slate-100 flex items-center justify-between text-xs text-slate-600">
              <span>Allocated Crew:</span>
              <span className="font-bold text-slate-900">{site.workers} Workers</span>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
