import { createContext, useContext, useState, useEffect } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { authApi } from '../api/client';

const AuthContext = createContext(null);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [viewMode, setViewMode] = useState('standard'); // 'standard' or 'admin'
  const queryClient = useQueryClient();

  useEffect(() => {
    checkAuth();
  }, []);

  const checkAuth = async () => {
    try {
      const data = await authApi.getCurrentUser();
      // Backend returns { user: {...}, roles: [...] }
      setUser({ ...data.user, roles: data.roles });
    } catch (err) {
      // 403 is expected when not logged in
      if (err.response?.status !== 403) {
        console.error('Auth check error:', err);
      }
      setUser(null);
    } finally {
      setLoading(false);
    }
  };

  const login = async (credentials) => {
    try {
      setError(null);
      const data = await authApi.login(credentials);
      // Backend returns { user: {...}, roles: [...] }
      setUser({ ...data.user, roles: data.roles });
      // Clear all cached queries to ensure fresh data for new user
      queryClient.clear();
      return { success: true };
    } catch (err) {
      const message = err.response?.data?.message || err.message || 'Login failed';
      setError(message);
      console.error('Login error:', err);
      // Don't throw, return error instead
      return { success: false, error: message };
    }
  };

  const logout = async () => {
    try {
      await authApi.logout();
    } catch (err) {
      console.error('Logout error:', err);
    } finally {
      setUser(null);
      setViewMode('standard');
      // Clear all cached queries on logout to prevent stale data
      queryClient.clear();
    }
  };

  const toggleViewMode = () => {
    setViewMode((prev) => (prev === 'standard' ? 'admin' : 'standard'));
  };

  const isExamsOfficer = user?.examsOfficer || false;
  const isTeachingSupport = user?.baseType === 'TEACHING_SUPPORT';
  const canSwitchView = isExamsOfficer && user?.baseType === 'ACADEMIC';

  const value = {
    user,
    loading,
    error,
    login,
    logout,
    isAuthenticated: !!user,
    hasRole: (role) => user?.roles?.includes(role) || false,
    viewMode,
    toggleViewMode,
    isExamsOfficer,
    isTeachingSupport,
    canSwitchView,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
