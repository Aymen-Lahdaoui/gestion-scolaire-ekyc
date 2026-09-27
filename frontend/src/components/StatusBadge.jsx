import React from 'react';

export const KycBadge = ({ status }) => {
  const map = {
    VALIDE: {
      bg: 'bg-emerald-50 text-emerald-700 border-emerald-200',
      dot: 'bg-emerald-500',
      label: 'eKYC Validé'
    },
    EN_REVISION: {
      bg: 'bg-amber-50 text-amber-700 border-amber-200',
      dot: 'bg-amber-500',
      label: 'En Révision Manuelle'
    },
    REJETE: {
      bg: 'bg-rose-50 text-rose-700 border-rose-200',
      dot: 'bg-rose-500',
      label: 'eKYC Rejeté'
    },
    EN_ATTENTE: {
      bg: 'bg-slate-100 text-slate-700 border-slate-200',
      dot: 'bg-slate-400',
      label: 'Non Vérifié'
    },
  };

  const current = map[status] || map.EN_ATTENTE;

  return (
    <span className={`inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold border ${current.bg}`}>
      <span className={`w-1.5 h-1.5 rounded-full ${current.dot}`}></span>
      {current.label}
    </span>
  );
};

export const AttendanceBadge = ({ status }) => {
  const map = {
    PRESENT: {
      bg: 'bg-emerald-500 text-white border-emerald-600',
      label: 'Présent'
    },
    RETARD: {
      bg: 'bg-amber-500 text-white border-amber-600',
      label: 'En retard'
    },
    ABSENT: {
      bg: 'bg-rose-500 text-white border-rose-600',
      label: 'Absent'
    },
  };

  const current = map[status] || map.ABSENT;

  return (
    <span className={`inline-flex items-center justify-center px-2.5 py-0.5 rounded-md text-xs font-bold uppercase tracking-wider ${current.bg}`}>
      {current.label}
    </span>
  );
};
