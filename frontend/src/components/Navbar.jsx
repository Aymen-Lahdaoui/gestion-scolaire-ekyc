import React from 'react';
import { useAuth } from '../context/AuthContext';
import { LogOut, User, ShieldCheck, Sparkles } from 'lucide-react';
import { KycBadge } from './StatusBadge';

const Navbar = () => {
  const { user, logout, isStudent } = useAuth();

  return (
    <header className="bg-white border-b border-slate-200 sticky top-0 z-30 shadow-sm">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        {/* Logo & Platform Name */}
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-emerald-600 to-teal-500 flex items-center justify-center text-white shadow-md shadow-emerald-500/20">
            <Sparkles className="w-5 h-5" />
          </div>
          <div>
            <h1 className="text-base font-bold text-slate-900 tracking-tight leading-none">
              EduVision <span className="text-emerald-600 font-extrabold">eKYC</span>
            </h1>
            <p className="text-xs text-slate-500 mt-0.5">Plateforme Scolaire Intelligente</p>
          </div>
        </div>

        {/* User Info & Actions */}
        {user && (
          <div className="flex items-center gap-4">
            {isStudent && <KycBadge status={user.statutKyc} />}
            
            <div className="flex items-center gap-3 pl-3 border-l border-slate-200">
              <div className="text-right">
                <p className="text-sm font-semibold text-slate-800 leading-none">
                  {user.prenom} {user.nom}
                </p>
                <span className="text-[11px] font-medium uppercase tracking-wider text-emerald-600">
                  {user.role}
                </span>
              </div>
              
              <div className="w-9 h-9 rounded-full bg-slate-100 border border-slate-200 flex items-center justify-center text-slate-600 font-bold text-sm">
                {user.prenom?.charAt(0) || <User className="w-4 h-4" />}
              </div>

              <button
                onClick={logout}
                title="Se déconnecter"
                className="p-2 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition-colors ml-1"
              >
                <LogOut className="w-4 h-4" />
              </button>
            </div>
          </div>
        )}
      </div>
    </header>
  );
};

export default Navbar;
