import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { moduleApi, assessmentApi } from '../api/client';
import { useAuth } from '../context/AuthContext';
import { Card, Badge } from '../components/UI';
import AssessmentStateBadge from '../components/AssessmentStateBadge';

const DashboardPage = () => {
  const { user } = useAuth();

  const { data: modules = [] } = useQuery({
    queryKey: ['modules', 'user-modules'],
    queryFn: moduleApi.getAll,
  });

  // Filter to only show modules where user has a role (userRole is set by backend)
  const userModules = modules.filter(module => module.userRole);

  const { data: assessments = [] } = useQuery({
    queryKey: ['assessments'],
    queryFn: assessmentApi.getAll,
  });

  // Filter assessments needing action from the current user
  const actionableAssessments = assessments.filter((assessment) => {
    const state = assessment.currentState;
    
    // Draft assessments for module staff
    if (state === 'DRAFT') {
      return assessment.userCanEdit;
    }
    
    // Ready for checking
    if (state === 'READY_FOR_CHECK' || state === 'EXAM_OFFICER_CHECK') {
      return assessment.userCanProgress;
    }
    
    // Changes required - setter needs to act
    if (state === 'CHANGES_REQUIRED' || state === 'EXAM_CHANGES_REQUIRED') {
      return assessment.roles?.includes('SETTER');
    }
    
    // External feedback needed
    if (state === 'EXTERNAL_FEEDBACK') {
      return assessment.roles?.includes('EXTERNAL_EXAMINER');
    }
    
    // Setter response needed
    if (state === 'SETTER_RESPONSE' || state === 'EXTERNAL_CHANGES_REQUIRED') {
      return assessment.roles?.includes('SETTER');
    }
    
    return false;
  });

  const formatUserRole = () => {
    const userData = user?.user || user;
    if (!userData) return '';
    
    const baseType = userData.baseType || '';
    const isEO = userData.examsOfficer;
    
    const roleLabel = {
      'ACADEMIC': 'Academic Staff',
      'TEACHING_SUPPORT': 'Teaching Support',
      'EXTERNAL_EXAMINER': 'External Examiner'
    }[baseType] || baseType;
    
    return isEO ? `${roleLabel} (Exams Officer)` : roleLabel;
  };

  return (
    <div className="space-y-6">
      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Dashboard</h1>
          <p className="mt-2 text-gray-600">
            Welcome back, {user?.user?.name || user?.name}
          </p>
        </div>
        
        {/* User Info Card */}
        <Card className="min-w-[250px]">
          <div className="text-sm space-y-2">
            <div>
              <span className="font-semibold text-gray-700">Name:</span>
              <p className="text-gray-900">{user?.user?.name || user?.name}</p>
            </div>
            <div>
              <span className="font-semibold text-gray-700">Email:</span>
              <p className="text-gray-600 text-xs">{user?.user?.email || user?.email}</p>
            </div>
            <div>
              <span className="font-semibold text-gray-700">Role:</span>
              <div className="mt-1">
                <Badge variant={user?.user?.examsOfficer || user?.examsOfficer ? 'primary' : 'default'}>
                  {formatUserRole()}
                </Badge>
              </div>
            </div>
          </div>
        </Card>
      </div>

      {/* Action Items */}
      <Card>
        <h2 className="text-xl font-semibold mb-4">Items Requiring Your Action</h2>
        {actionableAssessments.length === 0 ? (
          <p className="text-gray-500">No items require your attention at this time.</p>
        ) : (
          <div className="space-y-3">
            {actionableAssessments.map((assessment) => (
              <Link
                key={assessment.id}
                to={`/assessments/${assessment.id}`}
                className="block p-4 border border-gray-200 rounded-lg hover:border-primary-500 hover:shadow-md transition-all"
              >
                <div className="flex items-center justify-between">
                  <div className="flex-1">
                    <div className="flex items-center gap-2 mb-1">
                      <h3 className="font-semibold text-gray-900">{assessment.title}</h3>
                      <Badge variant="default">{assessment.assessmentType}</Badge>
                    </div>
                    <p className="text-sm text-gray-600">{assessment.moduleName}</p>
                  </div>
                  <AssessmentStateBadge state={assessment.currentState} />
                </div>
              </Link>
            ))}
          </div>
        )}
      </Card>

      {/* My Modules */}
      <Card>
        <h2 className="text-xl font-semibold mb-4">My Modules</h2>
        {userModules.length === 0 ? (
          <p className="text-gray-500">You are not assigned to any modules.</p>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {userModules.map((module) => (
              <Link
                key={module.id}
                to={`/modules/${module.id}`}
                className="block p-4 border border-gray-200 rounded-lg hover:border-primary-500 hover:shadow-md transition-all"
              >
                <h3 className="font-semibold text-gray-900">{module.code}</h3>
                <p className="text-sm text-gray-600 mt-1">{module.title}</p>
                <div className="mt-3 flex items-center justify-between text-sm">
                  <span className="text-gray-500">
                    {module.assessmentCount || 0} assessments
                  </span>
                  <Badge variant={module.userRole === 'MODULE_LEAD' ? 'primary' : module.userRole === 'MODERATOR' ? 'success' : 'default'}>
                    {module.userRole === 'MODULE_LEAD' ? 'Module Lead' : 
                     module.userRole === 'MODERATOR' ? 'Moderator' : 
                     module.userRole === 'STAFF' ? 'Staff' : (module.userRole || 'Staff')}
                  </Badge>
                </div>
              </Link>
            ))}
          </div>
        )}
      </Card>

      {/* Quick Stats */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <Card>
          <div className="text-center">
            <p className="text-3xl font-bold text-primary-600">{userModules.length}</p>
            <p className="text-gray-600 mt-1">My Modules</p>
          </div>
        </Card>
        <Card>
          <div className="text-center">
            <p className="text-3xl font-bold text-yellow-600">{actionableAssessments.length}</p>
            <p className="text-gray-600 mt-1">Action Required</p>
          </div>
        </Card>
        <Card>
          <div className="text-center">
            <p className="text-3xl font-bold text-green-600">
              {assessments.filter((a) => a.currentState === 'RELEASED' || a.currentState === 'PUBLISHED').length}
            </p>
            <p className="text-gray-600 mt-1">Released/Published</p>
          </div>
        </Card>
      </div>
    </div>
  );
};

export default DashboardPage;
