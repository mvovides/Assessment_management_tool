import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { assessmentApi } from '../api/client';
import { Card, Badge } from '../components/UI';
import AssessmentStateBadge from '../components/AssessmentStateBadge';
import { useAuth } from '../context/AuthContext';

const MyAssessmentsPage = () => {
  const { user } = useAuth();

  const { data: assessments = [], isLoading } = useQuery({
    queryKey: ['assessments', 'my-assessments'],
    queryFn: assessmentApi.getAll,
  });

  // Group assessments by role
  const groupedAssessments = assessments.reduce((acc, assessment) => {
    const roles = assessment.roles || [];
    
    if (roles.includes('SETTER')) {
      acc.setter.push(assessment);
    }
    if (roles.includes('CHECKER')) {
      acc.checker.push(assessment);
    }
    if (roles.includes('EXTERNAL_EXAMINER')) {
      acc.externalExaminer.push(assessment);
    }
    
    // If no specific role but user can see it (e.g., module staff), add to other
    if (roles.length === 0 || (roles.length === 1 && roles[0] === 'ADMIN')) {
      acc.other.push(assessment);
    }
    
    return acc;
  }, { setter: [], checker: [], externalExaminer: [], other: [] });

  const renderAssessmentCard = (assessment) => (
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
          {assessment.examDate && (
            <p className="text-sm text-gray-500 mt-1">
              Exam Date: {new Date(assessment.examDate).toLocaleDateString('en-GB')}
            </p>
          )}
        </div>
        <AssessmentStateBadge state={assessment.currentState} />
      </div>
    </Link>
  );

  if (isLoading) {
    return (
      <div className="space-y-6">
        <h1 className="text-3xl font-bold text-gray-900">My Assessments</h1>
        <p className="text-gray-500">Loading...</p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-gray-900">My Assessments</h1>
        <p className="mt-2 text-gray-600">
          All assessments where you have an assigned role
        </p>
      </div>

      {/* Setter Assessments */}
      {groupedAssessments.setter.length > 0 && (
        <Card>
          <div className="mb-4">
            <div className="flex items-center gap-2 mb-2">
              <span className="w-3 h-3 bg-blue-500 rounded-full"></span>
              <h2 className="text-xl font-semibold">As Setter</h2>
              <Badge variant="primary">{groupedAssessments.setter.length}</Badge>
            </div>
            <p className="text-sm text-gray-600">
              Assessments where you are responsible for creating and submitting content
            </p>
          </div>
          <div className="space-y-3">
            {groupedAssessments.setter.map(renderAssessmentCard)}
          </div>
        </Card>
      )}

      {/* Checker Assessments */}
      {groupedAssessments.checker.length > 0 && (
        <Card>
          <div className="mb-4">
            <div className="flex items-center gap-2 mb-2">
              <span className="w-3 h-3 bg-green-500 rounded-full"></span>
              <h2 className="text-xl font-semibold">As Checker</h2>
              <Badge variant="success">{groupedAssessments.checker.length}</Badge>
            </div>
            <p className="text-sm text-gray-600">
              Assessments where you review and approve content (independent checker)
            </p>
          </div>
          <div className="space-y-3">
            {groupedAssessments.checker.map(renderAssessmentCard)}
          </div>
        </Card>
      )}

      {/* External Examiner Assessments */}
      {groupedAssessments.externalExaminer.length > 0 && (
        <Card>
          <div className="mb-4">
            <div className="flex items-center gap-2 mb-2">
              <span className="w-3 h-3 bg-purple-500 rounded-full"></span>
              <h2 className="text-xl font-semibold">As External Examiner</h2>
              <Badge variant="default">{groupedAssessments.externalExaminer.length}</Badge>
            </div>
            <p className="text-sm text-gray-600">
              Assessments where you provide external feedback
            </p>
          </div>
          <div className="space-y-3">
            {groupedAssessments.externalExaminer.map(renderAssessmentCard)}
          </div>
        </Card>
      )}

      {/* Other Assessments (Module Staff) */}
      {groupedAssessments.other.length > 0 && (
        <Card>
          <div className="mb-4">
            <div className="flex items-center gap-2 mb-2">
              <span className="w-3 h-3 bg-gray-500 rounded-full"></span>
              <h2 className="text-xl font-semibold">Module Assessments</h2>
              <Badge variant="default">{groupedAssessments.other.length}</Badge>
            </div>
            <p className="text-sm text-gray-600">
              Assessments from modules where you are staff (lead, moderator, or staff member)
            </p>
          </div>
          <div className="space-y-3">
            {groupedAssessments.other.map(renderAssessmentCard)}
          </div>
        </Card>
      )}

      {/* No Assessments */}
      {assessments.length === 0 && (
        <Card>
          <div className="text-center py-12">
            <p className="text-gray-500 text-lg mb-2">No assessments found</p>
            <p className="text-gray-400 text-sm">
              You are not currently involved in any assessments
            </p>
          </div>
        </Card>
      )}

      {/* Summary Stats */}
      {assessments.length > 0 && (
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <Card>
            <div className="text-center">
              <p className="text-3xl font-bold text-primary-600">{assessments.length}</p>
              <p className="text-gray-600 mt-1">Total Assessments</p>
            </div>
          </Card>
          <Card>
            <div className="text-center">
              <p className="text-3xl font-bold text-blue-600">{groupedAssessments.setter.length}</p>
              <p className="text-gray-600 mt-1">As Setter</p>
            </div>
          </Card>
          <Card>
            <div className="text-center">
              <p className="text-3xl font-bold text-green-600">{groupedAssessments.checker.length}</p>
              <p className="text-gray-600 mt-1">As Checker</p>
            </div>
          </Card>
          <Card>
            <div className="text-center">
              <p className="text-3xl font-bold text-purple-600">{groupedAssessments.externalExaminer.length}</p>
              <p className="text-gray-600 mt-1">As External Examiner</p>
            </div>
          </Card>
        </div>
      )}
    </div>
  );
};

export default MyAssessmentsPage;
