import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { statsService } from '../../services/api';
import StatCard from '../../components/StatCard';
import { KycBadge } from '../../components/StatusBadge';
import {
  Calendar,
  CheckCircle,
  Clock,
  XCircle,
  ShieldAlert,
  ArrowRight,
  BookOpen,
} from 'lucide-react';

const StudentDashboard = () => {
  const { user } = useAuth();
  const [stats, setStats] = useState({
    tauxPresence: 92.5,
    totalSeances: 24,
    seancesPresentes: 22,
    retards: 1,
    absences: 1,
  });

  const [courses] = useState([
    { id: 1, nom: 'Intelligence Artificielle & Vision', prof: 'Prof. Dupont', salle: 'Lab IA-01', heure: '08:30 - 10:30', presence: '95%' },
    { id: 2, nom: 'Architecture des Systèmes Distribués', prof: 'Prof. Martin', salle: 'Amphi B', heure: '10:45 - 12:45', presence: '88%' },
    { id: 3, nom: 'Sécurité & Cryptographie', prof: 'Prof. Bennani', salle: 'Salle 204', heure: '14:00 - 16:00', presence: '100%' },
  ]);

  return (
    <div className="max-w-7xl mx-auto py-8 px-4 sm:px-6 lg:px-8 space-y-8">
      {/* Kyc Alert Banner if not validated */}
      {user?.statutKyc !== 'VALIDE' && (
        <div className="p-5 rounded-2xl bg-gradient-to-r from-amber-500 to-orange-500 text-white shadow-lg flex flex-col sm:flex-row items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <div className="p-3 bg-white/20 rounded-xl backdrop-blur-sm">
              <ShieldAlert className="w-6 h-6" />
            </div>
            <div>
              <h4 className="font-bold text-base">Profil Biométrique en attente de vérification</h4>
              <p className="text-xs text-amber-100 mt-0.5">
                Passez l'étape eKYC pour activer l'émargement automatique sans contact en salle de cours.
              </p>
            </div>
          </div>
          <Link
            to="/student/ekyc"
            className="px-5 py-2.5 bg-white text-amber-700 font-bold text-xs rounded-xl shadow hover:bg-amber-50 whitespace-nowrap transition"
          >
            Vérifier mon identité →
          </Link>
        </div>
      )}

      {/* Welcome header */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-extrabold text-slate-900 tracking-tight">
            Bonjour, {user?.prenom} 👋
          </h1>
          <p className="text-xs text-slate-500 mt-1">
            Consultez votre taux d'assiduité et vos séances de cours
          </p>
        </div>
        <KycBadge status={user?.statutKyc} />
      </div>

      {/* KPI Stats */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
        <StatCard
          title="Taux Global d'Assiduité"
          value={`${stats.tauxPresence}%`}
          subtitle="Seuil minimal requis : 80%"
          icon={CheckCircle}
          color="emerald"
        />
        <StatCard
          title="Séances Assistées"
          value={`${stats.seancesPresentes} / ${stats.totalSeances}`}
          subtitle="Présences confirmées"
          icon={BookOpen}
          color="blue"
        />
        <StatCard
          title="Retards Enregistrés"
          value={stats.retards}
          subtitle="Arrivées > 15 min"
          icon={Clock}
          color="amber"
        />
        <StatCard
          title="Absences Non Justifiées"
          value={stats.absences}
          subtitle="Dernier mois"
          icon={XCircle}
          color="rose"
        />
      </div>

      {/* Courses List */}
      <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
        <div className="p-6 border-b border-slate-100 flex items-center justify-between">
          <h2 className="text-base font-bold text-slate-900 flex items-center gap-2">
            <Calendar className="w-5 h-5 text-emerald-600" /> Mes Cours & Assiduité par Matière
          </h2>
        </div>

        <div className="divide-y divide-slate-100">
          {courses.map((c) => (
            <div key={c.id} className="p-6 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 hover:bg-slate-50/60 transition">
              <div>
                <h3 className="text-sm font-bold text-slate-900">{c.nom}</h3>
                <div className="flex items-center gap-4 text-xs text-slate-500 mt-1">
                  <span>👨‍🏫 {c.prof}</span>
                  <span>📍 {c.salle}</span>
                  <span>⏰ {c.heure}</span>
                </div>
              </div>
              <div className="flex items-center gap-3">
                <div className="text-right">
                  <span className="text-xs text-slate-400">Assiduité</span>
                  <p className="text-sm font-extrabold text-emerald-600">{c.presence}</p>
                </div>
                <div className="w-24 bg-slate-100 h-2.5 rounded-full overflow-hidden">
                  <div
                    className="bg-emerald-500 h-full rounded-full"
                    style={{ width: c.presence }}
                  ></div>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default StudentDashboard;
