import React from 'react';

export const App: React.FC = () => {
  return (
    <main className="min-h-screen flex flex-col items-center justify-center p-4">
      <div className="max-w-md w-full bg-white rounded-lg shadow-sm p-6 border border-gray-200">
        <h1 className="text-xl font-bold text-gray-900 mb-2">Worker Attendance PWA</h1>
        <p className="text-sm text-gray-600 mb-4">
          Offline-first attendance and payroll management.
        </p>
        <div className="flex items-center space-x-2 text-xs text-emerald-700 bg-emerald-50 px-3 py-2 rounded">
          <span className="inline-block w-2 h-2 rounded-full bg-emerald-500"></span>
          <span>System Shell Ready</span>
        </div>
      </div>
    </main>
  );
};

export default App;

