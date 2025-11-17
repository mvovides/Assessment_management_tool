import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useParams, Link } from 'react-router-dom';
import { moduleApi, assessmentApi } from '../api/client';
import { useAuth } from '../context/AuthContext';
import { Card, Button, Badge, Modal, Input, Select } from '../components/UI';
import AssessmentStateBadge from '../components/AssessmentStateBadge';

const ModuleDetailPage = () => {
  const { moduleId } = useParams();
  const { hasRole } = useAuth();
  const queryClient = useQueryClient();
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [newAssessment, setNewAssessment] = useState({
    title: '',
    type: 'CW',
    examDate: '',
  });

  const { data: module, isLoading } = useQuery({
    queryKey: ['modules', moduleId],
    queryFn: () => moduleApi.getById(moduleId),
  });

  const { data: assessments = [] } = useQuery({
    queryKey: ['modules', moduleId, 'assessments'],
    queryFn: () => assessmentApi.getByModule(moduleId),
  });

  const createMutation = useMutation({
    mutationFn: (data) => assessmentApi.create({ ...data, moduleId }),
    onSuccess: () => {
      queryClient.invalidateQueries(['modules', moduleId, 'assessments']);
      setShowCreateModal(false);
      setNewAssessment({ title: '', type: 'CW', examDate: '' });
    },
  });

  const handleCreateAssessment = (e) => {
    e.preventDefault();
    const data = { ...newAssessment };
    if (newAssessment.type !== 'EXAM') {
      delete data.examDate;
    }
    createMutation.mutate(data);
  };

  if (isLoading) {
    return <div className="text-center py-12">Loading module details...</div>;
  }

  if (!module) {
    return <div className="text-center py-12">Module not found.</div>;
  }

  const canCreateAssessment = hasRole('ROLE_ADMIN') || module.userRole === 'MODULE_LEADER';

  return (
    <div className="space-y-6">
      {/* Module Header */}
      <div>
        <div className="flex items-center gap-2 mb-2">
          <Link to="/modules" className="text-primary-600 hover:text-primary-700">
            ← Back to Modules
          </Link>
        </div>
        <div className="flex items-start justify-between">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">{module.code}</h1>
            <p className="text-lg text-gray-600 mt-1">{module.title}</p>
          </div>
          {canCreateAssessment && (
            <Button onClick={() => setShowCreateModal(true)}>Create Assessment</Button>
          )}
        </div>
      </div>

      {/* Staff */}
      <Card>
        <h2 className="text-xl font-semibold mb-4">Module Staff</h2>
        <div className="space-y-2">
          {module.staff && module.staff.length > 0 ? (
            module.staff.map((staff) => (
              <div key={staff.id} className="flex items-center justify-between py-2 border-b last:border-b-0">
                <div>
                  <span className="text-gray-900 font-medium">{staff.name}</span>
                  <span className="text-sm text-gray-500 ml-2">({staff.email})</span>
                </div>
                <Badge variant={staff.role === 'MODULE_LEAD' ? 'primary' : staff.role === 'MODERATOR' ? 'success' : 'default'}>
                  {staff.role === 'MODULE_LEAD' ? 'Module Lead' : 
                   staff.role === 'MODERATOR' ? 'Moderator' : 
                   staff.role === 'STAFF' ? 'Staff' : staff.role}
                </Badge>
              </div>
            ))
          ) : (
            <p className="text-gray-500">No staff assigned.</p>
          )}
        </div>
      </Card>

      {/* External Examiners */}
      {module.externalExaminers && module.externalExaminers.length > 0 && (
        <Card>
          <h2 className="text-xl font-semibold mb-4">External Examiners</h2>
          <div className="space-y-2">
            {module.externalExaminers.map((examiner) => (
              <div key={examiner.id} className="flex items-center justify-between py-2 border-b last:border-b-0">
                <span className="text-gray-900">{examiner.name}</span>
                <span className="text-sm text-gray-500">{examiner.email}</span>
              </div>
            ))}
          </div>
        </Card>
      )}

      {/* Assessments */}
      <Card>
        <h2 className="text-xl font-semibold mb-4">Assessments</h2>
        {assessments.length === 0 ? (
          <p className="text-gray-500">No assessments created yet.</p>
        ) : (
          <div className="space-y-3">
            {assessments.map((assessment) => (
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
                    {assessment.examDate && (
                      <p className="text-sm text-gray-600">
                        Exam Date: {new Date(assessment.examDate).toLocaleDateString()}
                      </p>
                    )}
                  </div>
                  <AssessmentStateBadge state={assessment.currentState} />
                </div>
              </Link>
            ))}
          </div>
        )}
      </Card>

      {/* Create Assessment Modal */}
      <Modal
        isOpen={showCreateModal}
        onClose={() => setShowCreateModal(false)}
        title="Create Assessment"
      >
        <form onSubmit={handleCreateAssessment} className="space-y-4">
          <Input
            label="Title"
            value={newAssessment.title}
            onChange={(e) => setNewAssessment({ ...newAssessment, title: e.target.value })}
            required
          />
          <Select
            label="Assessment Type"
            value={newAssessment.type}
            onChange={(e) => setNewAssessment({ ...newAssessment, type: e.target.value })}
            options={[
              { value: 'CW', label: 'Coursework' },
              { value: 'TEST', label: 'Test' },
              { value: 'EXAM', label: 'Exam' },
            ]}
          />
          {newAssessment.type === 'EXAM' && (
            <Input
              label="Exam Date"
              type="date"
              value={newAssessment.examDate}
              onChange={(e) => setNewAssessment({ ...newAssessment, examDate: e.target.value })}
              required
            />
          )}
          <div className="flex gap-2">
            <Button type="submit" disabled={createMutation.isLoading}>
              {createMutation.isLoading ? 'Creating...' : 'Create'}
            </Button>
            <Button type="button" variant="secondary" onClick={() => setShowCreateModal(false)}>
              Cancel
            </Button>
          </div>
          {createMutation.isError && (
            <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded text-sm">
              {createMutation.error.message || 'Failed to create assessment'}
            </div>
          )}
        </form>
      </Modal>
    </div>
  );
};

export default ModuleDetailPage;
