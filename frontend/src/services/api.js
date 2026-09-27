import axios from 'axios';

export const BACKEND_URL = 'http://localhost:8080/api';
export const AI_SERVICE_URL = 'http://localhost:8000/api/v1';

// Spring Boot API client
export const api = axios.create({
  baseURL: BACKEND_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// AI Service API client
export const aiApi = axios.create({
  baseURL: AI_SERVICE_URL,
});

// Intercepteur pour injecter automatiquement le token JWT
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('jwt_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Authentication APIs
export const authService = {
  login: (email, password) => api.post('/auth/login', { email, motDePasse: password }),
  register: (data) => api.post('/auth/register', data),
};

// Seances & Classes APIs
export const seanceService = {
  getAll: () => api.get('/seances'),
  getById: (id) => api.get(`/seances/${id}`),
  getStudentsForSession: (id) => api.get(`/seances/${id}/students`),
  launchSession: (id) => api.put(`/seances/${id}/launch`),
  closeSession: (id) => api.put(`/seances/${id}/close`),
  create: (data) => api.post('/seances', data),
};

// Emargements APIs
export const emargementService = {
  markPresent: (data) => api.post('/emargements', data),
  updateManually: (id, statut) => api.put(`/emargements/${id}`, { statut }),
  getSessionReport: (seanceId) => api.get(`/emargements/seance/${seanceId}/report`),
};

// KYC & Identity APIs
export const kycService = {
  getPending: () => api.get('/kyc/pending'),
  validateManually: (userId) => api.put(`/kyc/${userId}/validate`),
  rejectManually: (userId) => api.put(`/kyc/${userId}/reject`),
  setInRevision: (userId) => api.put(`/kyc/${userId}/revision`),
};

// Statistiques APIs
export const statsService = {
  getPresenceReport: (params) => api.get('/stats/presence', { params }),
  getStudentPresenceRate: (studentId) => api.get(`/stats/presence/etudiant/${studentId}`),
};

// AI & Vision APIs
export const aiVisionService = {
  // Recognize face from live video frame
  recognizeFace: async (blob, threshold = 0.40) => {
    const formData = new FormData();
    formData.append('image', blob, 'frame.jpg');
    return aiApi.post(`/recognize-face?threshold=${threshold}`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },

  // eKYC verification (CIN + Selfie)
  verifyEkyc: async (cinFile, selfieBlob, studentId) => {
    const formData = new FormData();
    formData.append('cin_card', cinFile);
    formData.append('selfie', selfieBlob, 'selfie.jpg');
    if (studentId) {
      formData.append('etudiant_id', studentId);
    }
    return aiApi.post('/ekyc/verify', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },

  // Live liveness check on single frame
  checkLivenessFrame: async (frameBlob) => {
    const formData = new FormData();
    formData.append('frame', frameBlob, 'frame.jpg');
    return aiApi.post('/liveness/check-frame', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },
};
