import React, { useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { aiVisionService } from '../../services/api';
import WebcamCapture from '../../components/WebcamCapture';
import {
  Upload,
  Camera,
  CheckCircle2,
  AlertTriangle,
  XCircle,
  Eye,
  RefreshCw,
  ShieldCheck,
  UserCheck,
  FileText,
  ArrowRight,
} from 'lucide-react';

const StudentKycOnboarding = () => {
  const { user, updateUser } = useAuth();
  const navigate = useNavigate();
  const webcamRef = useRef(null);

  const [step, setStep] = useState(1); // 1: CIN Upload, 2: Liveness & Selfie, 3: Biometric Result
  const [cinFile, setCinFile] = useState(null);
  const [cinPreview, setCinPreview] = useState(null);
  const [selfieBlob, setSelfieBlob] = useState(null);
  const [selfiePreview, setSelfiePreview] = useState(null);
  const [loading, setLoading] = useState(false);
  const [livenessFeedback, setLivenessFeedback] = useState({
    blink: false,
    headTurn: false,
  });
  const [ekycResult, setEkycResult] = useState(null);
  const [error, setError] = useState(null);

  // Étape 1 : Gestion du fichier CIN
  const handleCinChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setCinFile(file);
      setCinPreview(URL.createObjectURL(file));
      setError(null);
    }
  };

  // Étape 2 : Capture de selfie et test de vivacité
  const handleCaptureSelfie = async () => {
    if (!webcamRef.current) return;
    setError(null);
    setLoading(true);

    try {
      const blob = await webcamRef.current.captureBlob();
      const base64 = webcamRef.current.captureBase64();

      if (!blob) {
        throw new Error('Échec de la capture vidéo.');
      }

      setSelfieBlob(blob);
      setSelfiePreview(base64);

      // Vérification rapide de la présence de visage
      const livenessRes = await aiVisionService.checkLivenessFrame(blob);
      if (!livenessRes.data.is_live) {
        setError('Visage non détecté ou mal éclairé. Veuillez vous positionner face à la caméra.');
        setLoading(false);
        return;
      }

      setLivenessFeedback({
        blink: livenessRes.data.blink_detected,
        headTurn: livenessRes.data.head_turn_detected,
      });

      // Lancement du matching biométrique eKYC complet
      await executeEkycVerification(blob);
    } catch (err) {
      console.error(err);
      setError(err.response?.data?.detail || err.message || 'Erreur lors de la capture.');
    } finally {
      setLoading(false);
    }
  };

  // Étape 3 : Exécution de l'algorithme eKYC complet
  const executeEkycVerification = async (capturedSelfieBlob) => {
    setLoading(true);
    setError(null);

    try {
      const response = await aiVisionService.verifyEkyc(cinFile, capturedSelfieBlob, user?.id);
      const data = response.data;
      setEkycResult(data);
      setStep(3);

      // Mettre à jour le statut dans le profil utilisateur local
      if (data.statut_kyc) {
        updateUser({ statutKyc: data.statut_kyc });
      }
    } catch (err) {
      console.error('Erreur eKYC:', err);
      setError(
        err.response?.data?.detail ||
          'Une erreur est survenue lors de la comparaison biométrique. Veuillez réessayer.'
      );
    } finally {
      setLoading(false);
    }
  };

  const restartKyc = () => {
    setStep(1);
    setCinFile(null);
    setCinPreview(null);
    setSelfieBlob(null);
    setSelfiePreview(null);
    setEkycResult(null);
    setError(null);
  };

  return (
    <div className="max-w-4xl mx-auto py-8 px-4 sm:px-6">
      {/* Header */}
      <div className="text-center mb-8">
        <div className="inline-flex items-center gap-2 px-3 py-1 bg-emerald-50 text-emerald-700 rounded-full text-xs font-bold mb-3 border border-emerald-200">
          <ShieldCheck className="w-4 h-4" /> Onboarding Biométrique Sécurisé
        </div>
        <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
          Vérification d'Identité Étudiant (eKYC)
        </h1>
        <p className="text-sm text-slate-600 max-w-xl mx-auto mt-2">
          Validation automatique en moins d'une minute par comparaison entre votre Carte d'Identité et votre selfie en direct.
        </p>
      </div>

      {/* Stepper Indicator */}
      <div className="flex items-center justify-center mb-10">
        <div className="flex items-center space-x-4">
          <div
            className={`flex items-center justify-center w-9 h-9 rounded-full font-bold text-sm ${
              step >= 1 ? 'bg-emerald-600 text-white shadow-md shadow-emerald-600/30' : 'bg-slate-200 text-slate-600'
            }`}
          >
            1
          </div>
          <span className="text-xs font-semibold text-slate-700">Pièce d'Identité</span>
          <div className={`w-12 h-1 rounded ${step >= 2 ? 'bg-emerald-600' : 'bg-slate-200'}`}></div>

          <div
            className={`flex items-center justify-center w-9 h-9 rounded-full font-bold text-sm ${
              step >= 2 ? 'bg-emerald-600 text-white shadow-md shadow-emerald-600/30' : 'bg-slate-200 text-slate-600'
            }`}
          >
            2
          </div>
          <span className="text-xs font-semibold text-slate-700">Selfie & Liveness</span>
          <div className={`w-12 h-1 rounded ${step >= 3 ? 'bg-emerald-600' : 'bg-slate-200'}`}></div>

          <div
            className={`flex items-center justify-center w-9 h-9 rounded-full font-bold text-sm ${
              step === 3 ? 'bg-emerald-600 text-white shadow-md shadow-emerald-600/30' : 'bg-slate-200 text-slate-600'
            }`}
          >
            3
          </div>
          <span className="text-xs font-semibold text-slate-700">Validation</span>
        </div>
      </div>

      {/* Error Alert */}
      {error && (
        <div className="mb-6 p-4 rounded-xl bg-rose-50 border border-rose-200 flex items-start gap-3">
          <XCircle className="w-5 h-5 text-rose-600 shrink-0 mt-0.5" />
          <div>
            <h4 className="text-sm font-bold text-rose-800">Échec du contrôle</h4>
            <p className="text-sm text-rose-700 mt-0.5">{error}</p>
          </div>
        </div>
      )}

      {/* STEP 1: CIN Upload */}
      {step === 1 && (
        <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6 sm:p-8">
          <h2 className="text-lg font-bold text-slate-900 mb-2 flex items-center gap-2">
            <FileText className="w-5 h-5 text-emerald-600" /> Étape 1 : Photo de la Carte d'Identité (CIN)
          </h2>
          <p className="text-xs text-slate-500 mb-6">
            Déposez une photo claire du recto de votre CIN. Le modèle OCR extraira automatiquement votre nom et numéro de pièce.
          </p>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6 items-center">
            <label className="border-2 border-dashed border-slate-300 hover:border-emerald-500 rounded-2xl p-8 flex flex-col items-center justify-center cursor-pointer bg-slate-50 hover:bg-emerald-50/40 transition group">
              <Upload className="w-10 h-10 text-slate-400 group-hover:text-emerald-600 mb-3 transition-colors" />
              <p className="text-sm font-bold text-slate-700 group-hover:text-emerald-700 text-center">
                Cliquez pour importer votre CIN
              </p>
              <p className="text-xs text-slate-500 mt-1">PNG, JPG jusqu'à 10MB</p>
              <input
                type="file"
                accept="image/*"
                onChange={handleCinChange}
                className="hidden"
              />
            </label>

            {/* Preview Box */}
            <div className="w-full aspect-[3/2] bg-slate-100 rounded-2xl border border-slate-200 flex items-center justify-center overflow-hidden relative">
              {cinPreview ? (
                <>
                  <img src={cinPreview} alt="Aperçu CIN" className="w-full h-full object-cover" />
                  <div className="absolute top-2 right-2 bg-emerald-600 text-white text-xs font-bold px-2 py-1 rounded-md">
                    Image chargée
                  </div>
                </>
              ) : (
                <div className="text-center p-4 text-slate-400">
                  <UserCheck className="w-12 h-12 mx-auto mb-2 opacity-40" />
                  <p className="text-xs font-medium">Aperçu du document d'identité</p>
                </div>
              )}
            </div>
          </div>

          <div className="mt-8 flex justify-end">
            <button
              onClick={() => setStep(2)}
              disabled={!cinFile}
              className="flex items-center gap-2 px-6 py-3 bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-sm rounded-xl shadow-md disabled:opacity-40 disabled:cursor-not-allowed transition"
            >
              Passer au test de présence (Liveness) <ArrowRight className="w-4 h-4" />
            </button>
          </div>
        </div>
      )}

      {/* STEP 2: Live Webcam & Liveness Test */}
      {step === 2 && (
        <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6 sm:p-8">
          <h2 className="text-lg font-bold text-slate-900 mb-2 flex items-center gap-2">
            <Camera className="w-5 h-5 text-emerald-600" /> Étape 2 : Test de Vivacité & Capture Selfie
          </h2>
          <p className="text-xs text-slate-500 mb-6">
            Positionnez votre visage bien en face de la caméra. Le système vérifie l'absence de fraude par photo/écran statique.
          </p>

          <div className="max-w-lg mx-auto">
            <WebcamCapture ref={webcamRef} overlayText="Placez votre visage au centre" />

            <div className="mt-6 flex flex-col sm:flex-row items-center justify-between gap-4">
              <button
                onClick={() => setStep(1)}
                className="text-xs font-bold text-slate-500 hover:text-slate-800"
              >
                ← Changer la photo CIN
              </button>

              <button
                onClick={handleCaptureSelfie}
                disabled={loading}
                className="w-full sm:w-auto flex items-center justify-center gap-2 px-6 py-3 bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-sm rounded-xl shadow-md disabled:opacity-50 transition"
              >
                {loading ? (
                  <>
                    <RefreshCw className="w-4 h-4 animate-spin" /> Analyse biométrique en cours...
                  </>
                ) : (
                  <>
                    <Camera className="w-4 h-4" /> Capturer & Valider l'identité
                  </>
                )}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* STEP 3: Results Dashboard */}
      {step === 3 && ekycResult && (
        <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6 sm:p-8">
          <div className="text-center mb-6">
            {ekycResult.statut_kyc === 'VALIDE' && (
              <div className="inline-flex p-3 bg-emerald-100 text-emerald-700 rounded-full mb-3">
                <CheckCircle2 className="w-10 h-10" />
              </div>
            )}
            {ekycResult.statut_kyc === 'EN_REVISION' && (
              <div className="inline-flex p-3 bg-amber-100 text-amber-700 rounded-full mb-3">
                <AlertTriangle className="w-10 h-10" />
              </div>
            )}
            {ekycResult.statut_kyc === 'REJETE' && (
              <div className="inline-flex p-3 bg-rose-100 text-rose-700 rounded-full mb-3">
                <XCircle className="w-10 h-10" />
              </div>
            )}

            <h3 className="text-2xl font-bold text-slate-900">{ekycResult.message}</h3>
            <p className="text-xs text-slate-500 mt-1">
              Résultats du moteur d'analyse biométrique ArcFace & MediaPipe
            </p>
          </div>

          {/* Metric Cards */}
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-6">
            <div className="bg-slate-50 p-4 rounded-xl border border-slate-200 text-center">
              <span className="text-xs text-slate-500 font-semibold uppercase">Score de Similarité</span>
              <p
                className={`text-2xl font-extrabold mt-1 ${
                  ekycResult.similarity_score >= 80
                    ? 'text-emerald-600'
                    : ekycResult.similarity_score >= 60
                    ? 'text-amber-600'
                    : 'text-rose-600'
                }`}
              >
                {ekycResult.similarity_score}%
              </p>
              <span className="text-[11px] text-slate-400">Distance: {ekycResult.face_distance}</span>
            </div>

            <div className="bg-slate-50 p-4 rounded-xl border border-slate-200 text-center">
              <span className="text-xs text-slate-500 font-semibold uppercase">Test de Vivacité</span>
              <p className="text-2xl font-extrabold text-emerald-600 mt-1">
                {ekycResult.is_live ? 'Authentique' : 'Non Conforme'}
              </p>
              <span className="text-[11px] text-slate-400">EAR: {ekycResult.liveness_details?.eye_aspect_ratio}</span>
            </div>

            <div className="bg-slate-50 p-4 rounded-xl border border-slate-200 text-center">
              <span className="text-xs text-slate-500 font-semibold uppercase">Données OCR CIN</span>
              <p className="text-sm font-bold text-slate-800 mt-1">
                {ekycResult.ocr_result?.cin_numero || 'Non extrait'}
              </p>
              <span className="text-[11px] text-slate-400">
                {ekycResult.ocr_result?.prenom} {ekycResult.ocr_result?.nom}
              </span>
            </div>
          </div>

          {/* Visual Comparison */}
          <div className="grid grid-cols-2 gap-4 max-w-md mx-auto mb-8">
            <div className="text-center">
              <p className="text-xs font-bold text-slate-600 mb-1.5">Photo CIN</p>
              <div className="aspect-[3/4] rounded-xl overflow-hidden border border-slate-200 shadow-sm bg-slate-100">
                {cinPreview && <img src={cinPreview} alt="CIN" className="w-full h-full object-cover" />}
              </div>
            </div>
            <div className="text-center">
              <p className="text-xs font-bold text-slate-600 mb-1.5">Selfie Direct</p>
              <div className="aspect-[3/4] rounded-xl overflow-hidden border border-slate-200 shadow-sm bg-slate-100">
                {selfiePreview && <img src={selfiePreview} alt="Selfie" className="w-full h-full object-cover" />}
              </div>
            </div>
          </div>

          {/* Actions */}
          <div className="flex justify-center gap-4">
            {ekycResult.statut_kyc === 'VALIDE' ? (
              <button
                onClick={() => navigate('/student')}
                className="px-8 py-3 bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-sm rounded-xl shadow-md transition"
              >
                Accéder à mon Espace Étudiant →
              </button>
            ) : (
              <>
                <button
                  onClick={restartKyc}
                  className="px-6 py-3 bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold text-sm rounded-xl transition"
                >
                  Recommencer le test
                </button>
                <button
                  onClick={() => navigate('/student')}
                  className="px-6 py-3 bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-sm rounded-xl transition"
                >
                  Continuer vers mon espace
                </button>
              </>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default StudentKycOnboarding;
