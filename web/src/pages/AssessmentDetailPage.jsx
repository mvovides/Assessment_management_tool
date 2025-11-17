import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useParams, Link } from 'react-router-dom';
import { assessmentApi, userApi, moduleApi } from '../api/client';
import { Card, Button, Badge, Modal, Input, Select } from '../components/UI';
import AssessmentStateBadge from '../components/AssessmentStateBadge';
import { useAuth } from '../context/AuthContext';

const AssessmentDetailPage = () => {
  const { assessmentId } = useParams();
  const queryClient = useQueryClient();
  const { user } = useAuth();
  const [showFeedbackModal, setShowFeedbackModal] = useState(false);
  const [showResponseModal, setShowResponseModal] = useState(false);
  const [showRoleModal, setShowRoleModal] = useState(false);
  const [showTransitionModal, setShowTransitionModal] = useState(false);
  const [feedbackText, setFeedbackText] = useState('');
  const [responseText, setResponseText] = useState('');
  const [transitionNote, setTransitionNote] = useState('');
  const [targetState, setTargetState] = useState('');
  const [selectedUserId, setSelectedUserId] = useState('');
  const [selectedRole, setSelectedRole] = useState('SETTER');
  const [showSubmitContentModal, setShowSubmitContentModal] = useState(false);
  const [contentData, setContentData] = useState({
    description: '',
    fileName: '',
    fileUrl: '',
  });

  const { data: assessment, isLoading } = useQuery({
    queryKey: ['assessments', assessmentId],
    queryFn: () => assessmentApi.getById(assessmentId),
  });

  const { data: transitions = [] } = useQuery({
    queryKey: ['assessments', assessmentId, 'transitions'],
    queryFn: () => assessmentApi.getTransitions(assessmentId),
  });

  const { data: assessmentRoles = [] } = useQuery({
    queryKey: ['assessments', assessmentId, 'roles'],
    queryFn: () => assessmentApi.getRoles(assessmentId),
    enabled: !!assessmentId,
  });

  const { data: allUsers = [] } = useQuery({
    queryKey: ['users'],
    queryFn: () => userApi.getAll(),
    enabled: showRoleModal,
  });

  const { data: moduleDetails } = useQuery({
    queryKey: ['modules', assessment?.moduleId],
    queryFn: () => moduleApi.getById(assessment.moduleId),
    enabled: !!assessment?.moduleId,
  });

  const progressMutation = useMutation({
    mutationFn: (data) => assessmentApi.progress(assessmentId, data),
    onSuccess: () => {
      queryClient.invalidateQueries(['assessments', assessmentId]);
      queryClient.invalidateQueries(['assessments', assessmentId, 'transitions']);
      setTargetState('');
      setTransitionNote('');
      setShowTransitionModal(false);
    },
  });

  const feedbackMutation = useMutation({
    mutationFn: (feedback) => assessmentApi.submitCheckerFeedback(assessmentId, feedback),
    onSuccess: () => {
      queryClient.invalidateQueries(['assessments', assessmentId]);
      queryClient.invalidateQueries(['assessments', assessmentId, 'transitions']);
      setShowFeedbackModal(false);
      setFeedbackText('');
    },
  });

  const externalFeedbackMutation = useMutation({
    mutationFn: (feedback) => assessmentApi.submitExternalFeedback(assessmentId, feedback),
    onSuccess: () => {
      queryClient.invalidateQueries(['assessments', assessmentId]);
      queryClient.invalidateQueries(['assessments', assessmentId, 'transitions']);
      setShowFeedbackModal(false);
      setFeedbackText('');
    },
  });

  const setterResponseMutation = useMutation({
    mutationFn: (response) => assessmentApi.submitSetterResponse(assessmentId, response),
    onSuccess: () => {
      queryClient.invalidateQueries(['assessments', assessmentId]);
      queryClient.invalidateQueries(['assessments', assessmentId, 'transitions']);
      setShowResponseModal(false);
      setResponseText('');
    },
  });

  const assignRoleMutation = useMutation({
    mutationFn: (data) => assessmentApi.assignRole(assessmentId, data),
    onSuccess: () => {
      queryClient.invalidateQueries(['assessments', assessmentId, 'roles']);
      queryClient.invalidateQueries(['assessments', assessmentId]);
      setShowRoleModal(false);
      setSelectedUserId('');
    },
  });

  const removeRoleMutation = useMutation({
    mutationFn: ({ userId, role }) => assessmentApi.removeRole(assessmentId, userId, role),
    onSuccess: () => {
      queryClient.invalidateQueries(['assessments', assessmentId, 'roles']);
      queryClient.invalidateQueries(['assessments', assessmentId]);
    },
  });

  const submitContentMutation = useMutation({
    mutationFn: (data) => assessmentApi.submitContent(assessmentId, data),
    onSuccess: () => {
      queryClient.invalidateQueries(['assessments', assessmentId]);
      setShowSubmitContentModal(false);
      setContentData({ description: '', fileName: '', fileUrl: '' });
    },
  });

  if (isLoading) {
    return <div className="text-center py-12">Loading assessment...</div>;
  }

  if (!assessment) {
    return <div className="text-center py-12">Assessment not found.</div>;
  }

  const handleProgress = (toState) => {
    // For certain transitions, show modal to collect feedback/notes
    if (toState === 'CHANGES_REQUIRED' || toState === 'EXAM_CHANGES_REQUIRED') {
      setTargetState(toState);
      setShowTransitionModal(true);
    } else {
      progressMutation.mutate({ targetState: toState, note: '' });
    }
  };

  const handleSubmitTransition = (e) => {
    e.preventDefault();
    progressMutation.mutate({ 
      targetState: targetState, 
      note: transitionNote 
    });
  };

  const handleSubmitFeedback = (e) => {
    e.preventDefault();
    feedbackMutation.mutate({ feedback: feedbackText });
  };

  const handleSubmitExternalFeedback = (e) => {
    e.preventDefault();
    externalFeedbackMutation.mutate({ feedback: feedbackText });
  };

  const handleSubmitSetterResponse = (e) => {
    e.preventDefault();
    setterResponseMutation.mutate({ response: responseText });
  };

  const handleSubmitContent = (e) => {
    e.preventDefault();
    submitContentMutation.mutate(contentData);
  };

  const handleAssignRole = (e) => {
    e.preventDefault();
    if (!selectedUserId) return;
    assignRoleMutation.mutate({
      userId: selectedUserId,
      role: selectedRole,
    });
  };

  const handleRemoveRole = (userId, role) => {
    if (confirm(`Are you sure you want to remove this ${role} role?`)) {
      removeRoleMutation.mutate({ userId, role });
    }
  };

  // Check if current user is module lead or admin
  const canManageRoles = user?.baseType === 'TEACHING_SUPPORT' || 
    moduleDetails?.staff?.some(s => s.userId === user?.id && s.role === 'MODULE_LEAD');

  // Get eligible users for setter role (module staff, but NOT current checkers)
  const eligibleSetters = allUsers.filter(u => {
    if (u.baseType !== 'ACADEMIC') return false;
    if (!moduleDetails?.staff?.some(s => s.userId === u.id)) return false;
    
    // Cannot be setter if already a checker
    const isChecker = assessmentRoles.some(r => r.id === u.id && r.role === 'CHECKER');
    return !isChecker;
  });

  // Get eligible users for checker role (academics not on module, except moderator, and NOT current setters)
  const eligibleCheckers = allUsers.filter(u => {
    if (u.baseType !== 'ACADEMIC') return false;
    
    const isModuleStaff = moduleDetails?.staff?.some(s => 
      s.userId === u.id && (s.role === 'MODULE_LEAD' || s.role === 'STAFF')
    );
    
    const isModerator = moduleDetails?.staff?.some(s => 
      s.userId === u.id && s.role === 'MODERATOR'
    );
    
    // Cannot be checker if already a setter
    const isSetter = assessmentRoles.some(r => r.id === u.id && r.role === 'SETTER');
    
    // Can be checker if: moderator OR (not module staff AND not setter)
    return !isSetter && (isModerator || !isModuleStaff);
  });

  // Group roles by role type
  const setters = assessmentRoles.filter(r => r.role === 'SETTER');
  const checkers = assessmentRoles.filter(r => r.role === 'CHECKER');

  // Check if current user is a setter or checker
  // Prioritize actual assigned roles over base type (ADMIN)
  const isSetter = assessment?.roles?.includes('SETTER');
  const isChecker = assessment?.roles?.includes('CHECKER');
  const isAdmin = assessment?.roles?.includes('ADMIN') && !isSetter && !isChecker;

  const canSubmitCheckerFeedback =
    assessment?.currentState === 'READY_FOR_CHECK' && assessment?.userCanProgress;

  const canSubmitExternalFeedback =
    assessment?.currentState === 'EXTERNAL_FEEDBACK' &&
    assessment?.roles?.includes('EXTERNAL_EXAMINER');

  const canSubmitSetterResponse =
    (assessment?.currentState === 'SETTER_RESPONSE' ||
      assessment?.currentState === 'EXTERNAL_CHANGES_REQUIRED') &&
    assessment?.roles?.includes('SETTER');

  return (
    <div className="space-y-6">
      {/* Assessment Header */}
      <div>
        <div className="flex items-center gap-2 mb-2">
          <Link
            to={`/modules/${assessment.moduleId}`}
            className="text-primary-600 hover:text-primary-700"
          >
            ← Back to Module
          </Link>
        </div>
        <div className="flex items-start justify-between">
          <div>
            <div className="flex items-center gap-3 mb-2">
              <h1 className="text-3xl font-bold text-gray-900">{assessment.title}</h1>
              <Badge variant="default">{assessment.assessmentType}</Badge>
            </div>
            <p className="text-lg text-gray-600">{assessment.moduleName}</p>
            {assessment.examDate && (
              <p className="text-gray-600 mt-1">
                Exam Date: {new Date(assessment.examDate).toLocaleDateString('en-GB')}
              </p>
            )}
          </div>
          <AssessmentStateBadge state={assessment.currentState} />
        </div>
      </div>

      {/* User Roles */}
      {assessment.roles && assessment.roles.length > 0 && (
        <Card>
          <h3 className="font-semibold mb-2">Your Roles</h3>
          <div className="flex gap-2">
            {assessment.roles
              .filter((role) => {
                // Filter out ADMIN if user has actual assignment roles (SETTER/CHECKER)
                if (role === 'ADMIN') {
                  const hasActualRole = assessment.roles.some(r => r === 'SETTER' || r === 'CHECKER');
                  return !hasActualRole;
                }
                return true;
              })
              .map((role) => (
                <Badge key={role} variant="primary">
                  {role}
                </Badge>
              ))}
          </div>
        </Card>
      )}

      {/* Assessment Content */}
      <Card>
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-xl font-semibold">Assessment Content</h2>
          {(isSetter || isAdmin) && assessment?.currentState === 'DRAFT' && (
            <Button 
              onClick={() => {
                // Prepopulate with existing content if editing
                if (assessment?.description) {
                  setContentData({
                    description: assessment.description || '',
                    fileName: assessment.fileName || '',
                    fileUrl: assessment.fileUrl || ''
                  });
                }
                setShowSubmitContentModal(true);
              }} 
              variant="primary"
            >
              {assessment?.description ? '✏️ Edit Content' : '📤 Upload Content'}
            </Button>
          )}
        </div>

        {assessment?.description ? (
          <div className="space-y-3">
            <div>
              <h3 className="text-sm font-semibold text-gray-700 mb-1">Description</h3>
              <p className="text-gray-900 whitespace-pre-wrap">{assessment.description}</p>
            </div>
            
            {assessment.fileName && (
              <div className="p-4 bg-blue-50 border border-blue-200 rounded-lg">
                <div className="flex items-center gap-3">
                  <span className="text-3xl">📄</span>
                  <div className="flex-1">
                    <p className="font-medium text-gray-900">{assessment.fileName}</p>
                    {assessment.fileUrl && (
                      <a
                        href={assessment.fileUrl}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="text-sm text-primary-600 hover:text-primary-700 underline"
                      >
                        View/Download File
                      </a>
                    )}
                  </div>
                </div>
              </div>
            )}
            
            {isSetter && assessment?.currentState === 'DRAFT' && (
              <Button
                onClick={() => {
                  setContentData({
                    description: assessment?.description || '',
                    fileName: assessment?.fileName || '',
                    fileUrl: assessment?.fileUrl || '',
                  });
                  setShowSubmitContentModal(true);
                }}
                variant="secondary"
                size="sm"
              >
                ✏️ Edit Content
              </Button>
            )}
          </div>
        ) : (
          <div className="text-center py-8">
            <p className="text-gray-500 mb-2">
              {isSetter
                ? 'No assessment content uploaded yet. Click "Upload Content" to add your assessment details.'
                : 'Assessment content has not been uploaded by the setter yet.'}
            </p>
            {isSetter && assessment?.currentState === 'DRAFT' && (
              <p className="text-sm text-blue-600">
                💡 Upload your assessment questions, marking scheme, and any supporting materials.
              </p>
            )}
          </div>
        )}
      </Card>

      {/* Role Management - Setters and Checkers */}
      <Card>
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-xl font-semibold">Setters & Checkers</h2>
          {canManageRoles && (
            <Button onClick={() => setShowRoleModal(true)} variant="primary">
              Assign Role
            </Button>
          )}
        </div>

        <div className="grid md:grid-cols-2 gap-6">
          {/* Setters */}
          <div>
            <h3 className="font-semibold text-gray-700 mb-3 flex items-center gap-2">
              <span className="w-2 h-2 bg-blue-500 rounded-full"></span>
              Setters
            </h3>
            {setters.length === 0 ? (
              <p className="text-gray-500 text-sm italic">No setters assigned yet</p>
            ) : (
              <div className="space-y-2">
                {setters.map((setter) => (
                  <div
                    key={setter.id}
                    className="flex items-center justify-between p-3 bg-blue-50 border border-blue-200 rounded-lg"
                  >
                    <div>
                      <p className="font-medium text-gray-900">{setter.name}</p>
                      <p className="text-sm text-gray-600">{setter.email}</p>
                    </div>
                    {canManageRoles && (
                      <Button
                        variant="danger"
                        size="sm"
                        onClick={() => handleRemoveRole(setter.id, 'SETTER')}
                        disabled={removeRoleMutation.isLoading}
                      >
                        Remove
                      </Button>
                    )}
                  </div>
                ))}
              </div>
            )}
            <div className="mt-3 p-3 bg-blue-50 border-l-4 border-blue-400 text-sm text-gray-700">
              <p className="font-semibold mb-1">📝 Setter Responsibilities:</p>
              <ul className="list-disc list-inside space-y-1 text-xs">
                <li>Create assessment questions and marking scheme</li>
                <li>Submit for checking when ready</li>
                <li>Address checker feedback and make revisions</li>
                <li>Respond to external examiner feedback (for exams)</li>
              </ul>
            </div>
          </div>

          {/* Checkers */}
          <div>
            <h3 className="font-semibold text-gray-700 mb-3 flex items-center gap-2">
              <span className="w-2 h-2 bg-green-500 rounded-full"></span>
              Checkers
            </h3>
            {checkers.length === 0 ? (
              <p className="text-gray-500 text-sm italic">No checkers assigned yet</p>
            ) : (
              <div className="space-y-2">
                {checkers.map((checker) => (
                  <div
                    key={checker.id}
                    className="flex items-center justify-between p-3 bg-green-50 border border-green-200 rounded-lg"
                  >
                    <div>
                      <p className="font-medium text-gray-900">{checker.name}</p>
                      <p className="text-sm text-gray-600">{checker.email}</p>
                      {moduleDetails?.staff?.some(
                        s => s.userId === checker.id && s.role === 'MODERATOR'
                      ) && (
                        <Badge variant="success" size="sm" className="mt-1">
                          Module Moderator
                        </Badge>
                      )}
                    </div>
                    {canManageRoles && (
                      <Button
                        variant="danger"
                        size="sm"
                        onClick={() => handleRemoveRole(checker.id, 'CHECKER')}
                        disabled={removeRoleMutation.isLoading}
                      >
                        Remove
                      </Button>
                    )}
                  </div>
                ))}
              </div>
            )}
            <div className="mt-3 p-3 bg-green-50 border-l-4 border-green-400 text-sm text-gray-700">
              <p className="font-semibold mb-1">✅ Checker Responsibilities:</p>
              <ul className="list-disc list-inside space-y-1 text-xs">
                <li>Review assessment for quality and clarity</li>
                <li>Provide feedback on improvements needed</li>
                <li>Approve when ready or request changes</li>
                <li>Must be independent (not module lead/staff/setter)</li>
              </ul>
            </div>
          </div>
        </div>

        {/* Important Note */}
        <div className="mt-4 p-4 bg-yellow-50 border border-yellow-200 rounded-lg">
          <p className="text-sm text-gray-700">
            <span className="font-semibold">⚠️ Important:</span> Checkers must be independent of the
            module. Module leads and staff cannot be checkers (except the module moderator who is
            auto-assigned). Setters are typically module staff or the module lead.
          </p>
        </div>

        {/* Feedback Note */}
        <div className="mt-4 p-4 bg-purple-50 border border-purple-200 rounded-lg">
          <p className="text-sm text-gray-700">
            <span className="font-semibold">💬 Checker Feedback Guidelines:</span> Feedback should
            not reveal question content. Use comments like "Question 2a) - unclear, please rephrase"
            or "Weightings sum to 80 instead of 100". For content-specific feedback, upload a separate
            document to secure storage and reference it here.
          </p>
        </div>
      </Card>

      {/* State Transition Actions */}
      {(assessment.allowedTargets?.length > 0 ||
        canSubmitCheckerFeedback ||
        canSubmitExternalFeedback ||
        canSubmitSetterResponse) && (
        <Card>
          <h2 className="text-xl font-semibold mb-4">Workflow Actions</h2>
          
          {/* Current State Display */}
          <div className="mb-6 p-4 bg-gradient-to-r from-blue-50 to-indigo-50 border border-blue-200 rounded-lg">
            <p className="text-sm text-gray-600 mb-2">Current State</p>
            <div className="flex items-center gap-3">
              <AssessmentStateBadge state={assessment.currentState} />
              <p className="text-sm text-gray-700">
                {assessment?.currentState === 'DRAFT' && 'Assessment is being created'}
                {assessment?.currentState === 'READY_FOR_CHECK' && 'Waiting for checker review'}
                {assessment?.currentState === 'CHANGES_REQUIRED' && 'Setter needs to make revisions'}
                {assessment?.currentState === 'RELEASED' && 'Assessment is ready for students (coursework)'}
                {assessment?.currentState === 'TEST_TAKEN' && 'Test has been administered'}
                {assessment?.currentState === 'EXAM_OFFICER_CHECK' && 'Awaiting exams officer review'}
                {assessment?.currentState === 'FINAL_CHECK' && 'Awaiting final approval'}
                {assessment?.currentState === 'PUBLISHED' && 'Assessment workflow complete'}
              </p>
            </div>
          </div>

          {/* Setter Actions */}
          {isSetter && (
            <div className="mb-6">
              <h3 className="font-semibold text-gray-700 mb-3 flex items-center gap-2">
                <span className="w-3 h-3 bg-blue-500 rounded-full"></span>
                Setter Actions
              </h3>
              <div className="flex flex-wrap gap-2">
                {assessment?.currentState === 'DRAFT' && assessment?.allowedTargets?.includes('READY_FOR_CHECK') && (
                  <Button
                    onClick={() => handleProgress('READY_FOR_CHECK')}
                    disabled={progressMutation.isLoading}
                    variant="primary"
                  >
                    📤 Submit for Checking
                  </Button>
                )}
                {assessment?.currentState === 'READY_FOR_CHECK' && assessment?.allowedTargets?.includes('DRAFT') && (
                  <Button
                    onClick={() => handleProgress('DRAFT')}
                    disabled={progressMutation.isLoading}
                    variant="secondary"
                  >
                    ↩️ Revert to Draft (Edit Content)
                  </Button>
                )}
                {assessment?.currentState === 'CHANGES_REQUIRED' && assessment?.allowedTargets?.includes('READY_FOR_CHECK') && (
                  <Button
                    onClick={() => handleProgress('READY_FOR_CHECK')}
                    disabled={progressMutation.isLoading}
                    variant="primary"
                  >
                    🔄 Resubmit (Changes Made)
                  </Button>
                )}
                {assessment?.currentState === 'EXAM_CHANGES_REQUIRED' && assessment?.allowedTargets?.includes('READY_FOR_CHECK') && (
                  <Button
                    onClick={() => handleProgress('READY_FOR_CHECK')}
                    disabled={progressMutation.isLoading}
                    variant="primary"
                  >
                    🔄 Resubmit (Changes Made)
                  </Button>
                )}
                {canSubmitSetterResponse && (
                  <Button onClick={() => setShowResponseModal(true)} variant="primary">
                    💬 Respond to External Examiner
                  </Button>
                )}
              </div>
              {isSetter && assessment?.currentState === 'CHANGES_REQUIRED' && (
                <p className="text-sm text-orange-600 mt-2">
                  ⚠️ Please address the checker's feedback before resubmitting
                </p>
              )}
              {isSetter && assessment?.currentState === 'READY_FOR_CHECK' && assessment?.allowedTargets?.includes('DRAFT') && (
                <p className="text-sm text-blue-600 mt-2">
                  💡 You can revert to draft to edit the assessment content before the checker reviews it
                </p>
              )}
            </div>
          )}

          {/* Checker Actions */}
          {isChecker && (
            <div className="mb-6">
              <h3 className="font-semibold text-gray-700 mb-3 flex items-center gap-2">
                <span className="w-3 h-3 bg-green-500 rounded-full"></span>
                Checker Actions
              </h3>
              <div className="flex flex-wrap gap-2">
                {assessment?.currentState === 'READY_FOR_CHECK' && (
                  <>
                    {assessment?.allowedTargets?.includes('RELEASED') && (
                      <Button
                        onClick={() => handleProgress('RELEASED')}
                        disabled={progressMutation.isLoading}
                        variant="success"
                      >
                        ✅ Approve & Release (Coursework)
                      </Button>
                    )}
                    {assessment?.allowedTargets?.includes('TEST_TAKEN') && (
                      <Button
                        onClick={() => handleProgress('TEST_TAKEN')}
                        disabled={progressMutation.isLoading}
                        variant="success"
                      >
                        ✅ Approve (Test Ready)
                      </Button>
                    )}
                    {assessment?.allowedTargets?.includes('EXAM_OFFICER_CHECK') && (
                      <Button
                        onClick={() => handleProgress('EXAM_OFFICER_CHECK')}
                        disabled={progressMutation.isLoading}
                        variant="success"
                      >
                        ✅ Approve & Send to Exams Officer
                      </Button>
                    )}
                    {(assessment?.allowedTargets?.includes('CHANGES_REQUIRED') || 
                      assessment?.allowedTargets?.includes('EXAM_CHANGES_REQUIRED')) && (
                      <Button
                        onClick={() => {
                          const targetState = assessment?.allowedTargets?.includes('EXAM_CHANGES_REQUIRED') 
                            ? 'EXAM_CHANGES_REQUIRED' 
                            : 'CHANGES_REQUIRED';
                          setTargetState(targetState);
                          setShowTransitionModal(true);
                        }}
                        disabled={progressMutation.isLoading}
                        variant="danger"
                      >
                        ❌ Request Changes
                      </Button>
                    )}
                  </>
                )}
                {canSubmitCheckerFeedback && (
                  <Button onClick={() => setShowFeedbackModal(true)} variant="primary">
                    💬 Submit Checker Feedback
                  </Button>
                )}
              </div>
              {isChecker && assessment?.currentState === 'READY_FOR_CHECK' && (
                <p className="text-sm text-blue-600 mt-2">
                  ℹ️ Review the assessment carefully before approving or requesting changes
                </p>
              )}
            </div>
          )}

          {/* External Examiner Actions */}
          {canSubmitExternalFeedback && (
            <div className="mb-6">
              <h3 className="font-semibold text-gray-700 mb-3 flex items-center gap-2">
                <span className="w-3 h-3 bg-purple-500 rounded-full"></span>
                External Examiner Actions
              </h3>
              <div className="flex flex-wrap gap-2">
                <Button onClick={() => setShowFeedbackModal(true)} variant="primary">
                  📝 Submit External Examiner Feedback
                </Button>
              </div>
            </div>
          )}

          {/* Module Lead / Exams Officer / Other Role Actions */}
          {!isSetter && !isChecker && assessment?.allowedTargets?.length > 0 && (
            <div className="mb-6">
              <h3 className="font-semibold text-gray-700 mb-3 flex items-center gap-2">
                <span className="w-3 h-3 bg-indigo-500 rounded-full"></span>
                Available Actions
              </h3>
              <div className="flex flex-wrap gap-2">
                {assessment?.allowedTargets?.map((state) => {
                  let label = state.replace(/_/g, ' ');
                  let icon = '➡️';
                  let variant = 'primary';
                  
                  // Customize button labels and styles
                  if (state.includes('DEADLINE_PASSED')) {
                    label = 'Record Deadline Passed';
                    icon = '📅';
                  } else if (state.includes('FEEDBACK_RETURNED')) {
                    label = 'Return Feedback to Students';
                    icon = '📬';
                  } else if (state.includes('FINAL_CHECK')) {
                    label = 'Send for Final Check';
                    icon = '🔍';
                  } else if (state.includes('PUBLISHED')) {
                    label = 'Publish Assessment';
                    icon = '✅';
                    variant = 'success';
                  } else if (state.includes('MODERATION_COMPLETE')) {
                    label = 'Complete Moderation';
                    icon = '✓';
                    variant = 'success';
                  }
                  
                  return (
                    <Button
                      key={state}
                      onClick={() => handleProgress(state)}
                      disabled={progressMutation.isLoading}
                      variant={variant}
                    >
                      {icon} {label}
                    </Button>
                  );
                })}
              </div>
            </div>
          )}

          {/* Error Display */}
          {progressMutation.isError && (
            <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded text-sm">
              {progressMutation.error.message || 'Failed to progress assessment'}
            </div>
          )}

          {/* Workflow Help */}
          <div className="mt-4 p-3 bg-gray-50 border border-gray-200 rounded text-xs text-gray-600">
            <p className="font-semibold mb-1">📋 Workflow Notes:</p>
            <ul className="list-disc list-inside space-y-1">
              <li>Setters submit assessments for checking when ready</li>
              <li>Checkers can approve or request changes (unlimited revisions allowed)</li>
              <li>For exams, additional external examiner review may be required</li>
              <li>All feedback is tracked but question content is stored securely separately</li>
            </ul>
          </div>
        </Card>
      )}

      {/* Transition History */}
      <Card>
        <h2 className="text-xl font-semibold mb-4">Transition History</h2>
        {transitions.length === 0 ? (
          <p className="text-gray-500">No transitions yet.</p>
        ) : (
          <div className="space-y-4">
            {transitions.map((transition, index) => (
              <div key={index} className="border-l-4 border-primary-600 pl-4 py-2">
                <div className="flex items-center justify-between mb-1">
                  <div className="flex items-center gap-2">
                    <AssessmentStateBadge state={transition.fromState} />
                    <span className="text-gray-400">→</span>
                    <AssessmentStateBadge state={transition.toState} />
                  </div>
                  <span className="text-sm text-gray-500">
                    {transition.at ? new Date(transition.at).toLocaleString('en-GB') : 'Unknown date'}
                  </span>
                </div>
                <p className="text-sm text-gray-700">
                  By: {transition.byDisplayName || 'Unknown user'}
                </p>
                {transition.note && (
                  <p className="text-sm text-gray-600 mt-1 italic">"{transition.note}"</p>
                )}
              </div>
            ))}
          </div>
        )}
      </Card>

      {/* Checker Feedback Modal */}
      <Modal
        isOpen={showFeedbackModal && (canSubmitCheckerFeedback || canSubmitExternalFeedback)}
        onClose={() => {
          setShowFeedbackModal(false);
          setFeedbackText('');
        }}
        title={canSubmitExternalFeedback ? 'External Examiner Feedback' : 'Checker Feedback'}
      >
        <form
          onSubmit={
            canSubmitExternalFeedback ? handleSubmitExternalFeedback : handleSubmitFeedback
          }
          className="space-y-4"
        >
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Feedback</label>
            <textarea
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
              rows="6"
              value={feedbackText}
              onChange={(e) => setFeedbackText(e.target.value)}
              required
              placeholder="Enter your feedback..."
            />
          </div>
          <div className="flex gap-2">
            <Button
              type="submit"
              disabled={feedbackMutation.isLoading || externalFeedbackMutation.isLoading}
            >
              {feedbackMutation.isLoading || externalFeedbackMutation.isLoading
                ? 'Submitting...'
                : 'Submit Feedback'}
            </Button>
            <Button
              type="button"
              variant="secondary"
              onClick={() => {
                setShowFeedbackModal(false);
                setFeedbackText('');
              }}
            >
              Cancel
            </Button>
          </div>
          {(feedbackMutation.isError || externalFeedbackMutation.isError) && (
            <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded text-sm">
              {feedbackMutation.error?.message ||
                externalFeedbackMutation.error?.message ||
                'Failed to submit feedback'}
            </div>
          )}
        </form>
      </Modal>

      {/* Setter Response Modal */}
      <Modal
        isOpen={showResponseModal && canSubmitSetterResponse}
        onClose={() => {
          setShowResponseModal(false);
          setResponseText('');
        }}
        title="Setter Response"
      >
        <form onSubmit={handleSubmitSetterResponse} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Response</label>
            <textarea
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
              rows="6"
              value={responseText}
              onChange={(e) => setResponseText(e.target.value)}
              required
              placeholder="Enter your response to the external examiner feedback..."
            />
          </div>
          <div className="flex gap-2">
            <Button type="submit" disabled={setterResponseMutation.isLoading}>
              {setterResponseMutation.isLoading ? 'Submitting...' : 'Submit Response'}
            </Button>
            <Button
              type="button"
              variant="secondary"
              onClick={() => {
                setShowResponseModal(false);
                setResponseText('');
              }}
            >
              Cancel
            </Button>
          </div>
          {setterResponseMutation.isError && (
            <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded text-sm">
              {setterResponseMutation.error.message || 'Failed to submit response'}
            </div>
          )}
        </form>
      </Modal>

      {/* Assign Role Modal */}
      <Modal
        isOpen={showRoleModal}
        onClose={() => {
          setShowRoleModal(false);
          setSelectedUserId('');
          setSelectedRole('SETTER');
        }}
        title="Assign Role to Assessment"
      >
        <form onSubmit={handleAssignRole} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Role</label>
            <select
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
              value={selectedRole}
              onChange={(e) => {
                setSelectedRole(e.target.value);
                setSelectedUserId(''); // Reset user selection when role changes
              }}
            >
              <option value="SETTER">Setter</option>
              <option value="CHECKER">Checker</option>
            </select>
            <p className="text-xs text-gray-500 mt-1">
              {selectedRole === 'SETTER'
                ? 'Setters create and manage the assessment'
                : 'Checkers review and approve the assessment (must be independent)'}
            </p>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">User</label>
            <select
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
              value={selectedUserId}
              onChange={(e) => setSelectedUserId(e.target.value)}
              required
            >
              <option value="">Select a user...</option>
              {selectedRole === 'SETTER'
                ? eligibleSetters.map((user) => (
                    <option key={user.id} value={user.id}>
                      {user.name} ({user.email})
                    </option>
                  ))
                : eligibleCheckers.map((user) => (
                    <option key={user.id} value={user.id}>
                      {user.name} ({user.email})
                      {moduleDetails?.staff?.some(
                        (s) => s.userId === user.id && s.role === 'MODERATOR'
                      ) && ' - Module Moderator'}
                    </option>
                  ))}
            </select>
            {selectedRole === 'SETTER' && eligibleSetters.length === 0 && (
              <p className="text-xs text-red-600 mt-1">
                No eligible users. Setters must be module staff.
              </p>
            )}
            {selectedRole === 'CHECKER' && eligibleCheckers.length === 0 && (
              <p className="text-xs text-red-600 mt-1">
                No eligible users. Checkers must be independent academics (not module lead/staff).
              </p>
            )}
          </div>

          {selectedRole === 'CHECKER' && (
            <div className="p-3 bg-yellow-50 border border-yellow-200 rounded text-sm">
              <p className="font-semibold text-gray-800 mb-1">⚠️ Checker Independence:</p>
              <ul className="text-gray-700 text-xs space-y-1 list-disc list-inside">
                <li>Cannot be module lead</li>
                <li>Cannot be module staff (except moderator)</li>
                <li>Cannot be a setter on this assessment</li>
                <li>Must be an academic user</li>
              </ul>
            </div>
          )}

          {selectedRole === 'SETTER' && (
            <div className="p-3 bg-blue-50 border border-blue-200 rounded text-sm">
              <p className="font-semibold text-gray-800 mb-1">📝 Setter Guidelines:</p>
              <ul className="text-gray-700 text-xs space-y-1 list-disc list-inside">
                <li>Module leads are usually setters</li>
                <li>Multiple setters can be assigned for team assessments</li>
                <li>Setters cannot be checkers on their own assessments</li>
              </ul>
            </div>
          )}

          <div className="flex gap-2">
            <Button
              type="submit"
              disabled={
                assignRoleMutation.isLoading ||
                !selectedUserId ||
                (selectedRole === 'SETTER' && eligibleSetters.length === 0) ||
                (selectedRole === 'CHECKER' && eligibleCheckers.length === 0)
              }
            >
              {assignRoleMutation.isLoading ? 'Assigning...' : 'Assign Role'}
            </Button>
            <Button
              type="button"
              variant="secondary"
              onClick={() => {
                setShowRoleModal(false);
                setSelectedUserId('');
                setSelectedRole('SETTER');
              }}
            >
              Cancel
            </Button>
          </div>

          {assignRoleMutation.isError && (
            <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded text-sm">
              {assignRoleMutation.error.response?.data?.message ||
                assignRoleMutation.error.message ||
                'Failed to assign role. User may already have this role or not meet requirements.'}
            </div>
          )}
        </form>
      </Modal>

      {/* Request Changes Modal */}
      <Modal
        isOpen={showTransitionModal}
        onClose={() => {
          setShowTransitionModal(false);
          setTransitionNote('');
          setTargetState(null);
        }}
        title="Request Changes"
      >
        <form onSubmit={handleSubmitTransition} className="space-y-4">
          <div className="p-3 bg-orange-50 border border-orange-200 rounded text-sm mb-4">
            <p className="font-semibold text-gray-800 mb-2">💬 Feedback Guidelines:</p>
            <ul className="text-gray-700 text-xs space-y-1 list-disc list-inside">
              <li>Do NOT include actual question content in this feedback</li>
              <li>Reference questions by number/section (e.g., "Question 2a)")</li>
              <li>Focus on clarity, structure, weightings, and marking scheme</li>
              <li>Upload detailed content feedback to secure storage separately</li>
              <li>Be constructive and specific about required changes</li>
            </ul>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Feedback / Revision Notes <span className="text-red-500">*</span>
            </label>
            <textarea
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
              rows="8"
              value={transitionNote}
              onChange={(e) => setTransitionNote(e.target.value)}
              required
              placeholder="E.g., Question 2a) - Instructions unclear, please rephrase without revealing answer. Marking scheme for Q3 totals 15 marks but should be 20. Question 4b) diagram is too small to read clearly."
            />
            <p className="text-xs text-gray-500 mt-1">
              Provide clear, actionable feedback for the setter to address
            </p>
          </div>

          <div className="p-3 bg-blue-50 border border-blue-200 rounded text-sm">
            <p className="font-semibold text-gray-800 mb-1">🔄 Revision Cycle:</p>
            <p className="text-gray-700 text-xs">
              The setter will address your feedback and can resubmit for checking. This process can
              repeat as many times as needed to ensure quality. Each revision is tracked in the
              transition history.
            </p>
          </div>

          <div className="flex gap-2">
            <Button type="submit" disabled={progressMutation.isLoading || !transitionNote.trim()}>
              {progressMutation.isLoading ? 'Submitting...' : '❌ Request Changes'}
            </Button>
            <Button
              type="button"
              variant="secondary"
              onClick={() => {
                setShowTransitionModal(false);
                setTransitionNote('');
                setTargetState(null);
              }}
            >
              Cancel
            </Button>
          </div>

          {progressMutation.isError && (
            <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded text-sm">
              {progressMutation.error.message || 'Failed to request changes'}
            </div>
          )}
        </form>
      </Modal>

      {/* Submit Content Modal */}
      <Modal
        isOpen={showSubmitContentModal}
        onClose={() => {
          setShowSubmitContentModal(false);
          setContentData({ description: '', fileName: '', fileUrl: '' });
        }}
        title="Upload Assessment Content"
      >
        <form onSubmit={handleSubmitContent} className="space-y-4">
          <div className="p-3 bg-blue-50 border border-blue-200 rounded text-sm mb-4">
            <p className="font-semibold text-gray-800 mb-2">📋 Content Guidelines:</p>
            <ul className="text-gray-700 text-xs space-y-1 list-disc list-inside">
              <li>Upload assessment questions, marking scheme, and instructions</li>
              <li>Store actual content files in secure storage (SharePoint/OneDrive)</li>
              <li>Provide a link to the secure storage location</li>
              <li>Include any special instructions or materials needed</li>
              <li>This content will be reviewed by the assigned checker</li>
            </ul>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Assessment Description <span className="text-red-500">*</span>
            </label>
            <textarea
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
              rows="6"
              value={contentData.description}
              onChange={(e) => setContentData({ ...contentData, description: e.target.value })}
              required
              placeholder="Describe the assessment, including the number of questions, format, topics covered, time allowed, etc."
            />
            <p className="text-xs text-gray-500 mt-1">
              Provide an overview of the assessment structure and content
            </p>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              File Name
            </label>
            <input
              type="text"
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
              value={contentData.fileName}
              onChange={(e) => setContentData({ ...contentData, fileName: e.target.value })}
              placeholder="e.g., COM101_Assignment2_2024.pdf"
            />
            <p className="text-xs text-gray-500 mt-1">
              Name of the assessment document stored in secure storage
            </p>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Secure File URL
            </label>
            <input
              type="url"
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
              value={contentData.fileUrl}
              onChange={(e) => setContentData({ ...contentData, fileUrl: e.target.value })}
              placeholder="https://sharepoint.com/..."
            />
            <p className="text-xs text-gray-500 mt-1">
              Link to the secure storage location (SharePoint, OneDrive, etc.)
            </p>
          </div>

          <div className="p-3 bg-yellow-50 border border-yellow-200 rounded text-sm">
            <p className="font-semibold text-gray-800 mb-1">⚠️ Important:</p>
            <p className="text-gray-700 text-xs">
              After uploading content, you can submit this assessment for checking. The checker will
              review your work and either approve it or request changes. You can update the content
              while the assessment is in DRAFT state.
            </p>
          </div>

          <div className="flex gap-2">
            <Button
              type="submit"
              disabled={submitContentMutation.isLoading || !contentData.description.trim()}
            >
              {submitContentMutation.isLoading ? 'Uploading...' : '📤 Upload Content'}
            </Button>
            <Button
              type="button"
              variant="secondary"
              onClick={() => {
                setShowSubmitContentModal(false);
                setContentData({ description: '', fileName: '', fileUrl: '' });
              }}
            >
              Cancel
            </Button>
          </div>

          {submitContentMutation.isError && (
            <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded text-sm">
              {submitContentMutation.error.response?.data?.message ||
                submitContentMutation.error.message ||
                'Failed to upload content'}
            </div>
          )}
        </form>
      </Modal>
    </div>
  );
};

export default AssessmentDetailPage;
