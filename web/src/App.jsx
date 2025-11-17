import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import Layout from './components/Layout';

// Pages
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import ModulesPage from './pages/ModulesPage';
import ModuleDetailPage from './pages/ModuleDetailPage';
import AssessmentDetailPage from './pages/AssessmentDetailPage';
import MyAssessmentsPage from './pages/MyAssessmentsPage';
import AdminPage from './pages/AdminPage';

function App() {
  const { user, loading } = useAuth();

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading...</p>
        </div>
      </div>
    );
  }

  return (
    <Routes>
      <Route path="/login" element={user ? <Navigate to="/dashboard" /> : <LoginPage />} />
      
      <Route path="/" element={<Navigate to="/dashboard" />} />
      
      <Route
        path="/dashboard"
        element={
          <ProtectedRoute>
            <Layout>
              <DashboardPage />
            </Layout>
          </ProtectedRoute>
        }
      />
      
      <Route
        path="/modules"
        element={
          <ProtectedRoute>
            <Layout>
              <ModulesPage />
            </Layout>
          </ProtectedRoute>
        }
      />
      
      <Route
        path="/modules/:moduleId"
        element={
          <ProtectedRoute>
            <Layout>
              <ModuleDetailPage />
            </Layout>
          </ProtectedRoute>
        }
      />
      
      <Route
        path="/my-assessments"
        element={
          <ProtectedRoute>
            <Layout>
              <MyAssessmentsPage />
            </Layout>
          </ProtectedRoute>
        }
      />
      
      <Route
        path="/assessments/:assessmentId"
        element={
          <ProtectedRoute>
            <Layout>
              <AssessmentDetailPage />
            </Layout>
          </ProtectedRoute>
        }
      />
      
      <Route
        path="/admin"
        element={
          <ProtectedRoute requiredRole="ROLE_ADMIN">
            <Layout>
              <AdminPage />
            </Layout>
          </ProtectedRoute>
        }
      />
    </Routes>
  );
}

export default App;
