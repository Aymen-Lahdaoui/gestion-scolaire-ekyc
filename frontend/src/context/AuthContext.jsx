import React, { createContext, useContext, useState, useEffect } from 'react';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    try {
      const savedToken = localStorage.getItem('jwt_token');
      const savedUser = localStorage.getItem('user_info');
      if (savedToken && savedUser) {
        setToken(savedToken);
        setUser(JSON.parse(savedUser));
      }
    } catch (e) {
      console.error('Failed to restore auth state', e);
    } finally {
      setLoading(false);
    }
  }, []);

  const login = (jwtResponse) => {
    // jwtResponse contains: token, id, nom, prenom, email, role, statutKyc
    const { token: jwtToken, ...userInfo } = jwtResponse;
    setToken(jwtToken);
    setUser(userInfo);
    localStorage.setItem('jwt_token', jwtToken);
    localStorage.setItem('user_info', JSON.stringify(userInfo));
  };

  const logout = () => {
    setToken(null);
    setUser(null);
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('user_info');
  };

  const updateUser = (newFields) => {
    setUser((prev) => {
      const updated = { ...prev, ...newFields };
      localStorage.setItem('user_info', JSON.stringify(updated));
      return updated;
    });
  };

  const value = {
    user,
    token,
    loading,
    login,
    logout,
    updateUser,
    isAuthenticated: !!token,
    isAdmin: user?.role === 'ADMINISTRATEUR',
    isTeacher: user?.role === 'ENSEIGNANT',
    isStudent: user?.role === 'ETUDIANT',
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
