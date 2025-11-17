import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Layout = ({ children }) => {
  const { user, logout, hasRole, canSwitchView, viewMode, toggleViewMode } = useAuth();

  return (
    <div className="min-h-screen bg-gray-50">
      <nav className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between h-16">
            <div className="flex">
              <Link to="/" className="flex items-center px-2 text-xl font-bold text-primary-600">
                Assessment Management
              </Link>
              <div className="hidden sm:ml-6 sm:flex sm:space-x-4">
                <Link
                  to="/dashboard"
                  className="inline-flex items-center px-3 py-2 text-sm font-medium text-gray-700 hover:text-gray-900 hover:bg-gray-50 rounded-md"
                >
                  Dashboard
                </Link>
                <Link
                  to="/modules"
                  className="inline-flex items-center px-3 py-2 text-sm font-medium text-gray-700 hover:text-gray-900 hover:bg-gray-50 rounded-md"
                >
                  Modules
                </Link>
                <Link
                  to="/my-assessments"
                  className="inline-flex items-center px-3 py-2 text-sm font-medium text-gray-700 hover:text-gray-900 hover:bg-gray-50 rounded-md"
                >
                  My Assessments
                </Link>
                {(hasRole('ROLE_ADMIN') || hasRole('ROLE_EXAMS_OFFICER')) && (
                  <Link
                    to="/admin"
                    className="inline-flex items-center px-3 py-2 text-sm font-medium text-gray-700 hover:text-gray-900 hover:bg-gray-50 rounded-md"
                  >
                    Admin
                  </Link>
                )}
              </div>
            </div>
            <div className="flex items-center gap-3">
              {canSwitchView && (
                <button
                  onClick={toggleViewMode}
                  className="px-3 py-2 text-sm font-medium text-primary-600 bg-primary-50 border border-primary-200 rounded-md hover:bg-primary-100 transition-colors"
                >
                  {viewMode === 'standard' ? '🔧 Switch to EO View' : '👤 Switch to Standard View'}
                </button>
              )}
              <span className="text-sm text-gray-700">
                {user?.user?.name}
              </span>
              <button
                onClick={logout}
                className="px-4 py-2 text-sm font-medium text-white bg-primary-600 rounded-md hover:bg-primary-700 transition-colors"
              >
                Logout
              </button>
            </div>
          </div>
        </div>
      </nav>
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {children}
      </main>
    </div>
  );
};

export default Layout;
