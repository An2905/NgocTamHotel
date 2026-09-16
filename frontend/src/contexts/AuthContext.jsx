import { createContext, useContext, useState, useEffect } from 'react';
import { apiRequest } from '../api/client.js';

const AuthContext = createContext();

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = sessionStorage.getItem('currentUser');
    return stored ? JSON.parse(stored) : null;
  });

  const [token, setToken] = useState(() => {
    return sessionStorage.getItem('accessToken') || null;
  });

  function saveAuth(authResponse) {
    setToken(authResponse.token);
    const userData = {
      id: authResponse.userId,
      username: authResponse.username,
      email: authResponse.email,
      role: authResponse.role,
    };
    setUser(userData);
    sessionStorage.setItem('accessToken', authResponse.token);
    sessionStorage.setItem('currentUser', JSON.stringify(userData));
  }

  async function login(credentials) {
    const auth = await apiRequest('/auth/login', {
      method: 'POST',
      body: JSON.stringify(credentials),
    });
    saveAuth(auth);
    return auth;
  }

  async function register(account) {
    const result = await apiRequest('/auth/register', {
      method: 'POST',
      body: JSON.stringify(account),
    });
    return result;
  }

  function logout() {
    setUser(null);
    setToken(null);
    sessionStorage.removeItem('accessToken');
    sessionStorage.removeItem('currentUser');
  }

  const isAuthenticated = Boolean(token);
  const isAdmin = user?.role === 'ADMIN';

  return (
    <AuthContext.Provider value={{ user, token, isAuthenticated, isAdmin, login, register, logout, saveAuth }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
