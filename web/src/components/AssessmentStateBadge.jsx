import { Badge } from './UI';

const STATE_COLORS = {
  // Draft states
  DRAFT: 'default',
  
  // CW/TEST flow
  READY_FOR_CHECK: 'info',
  CHANGES_REQUIRED: 'warning',
  RELEASED: 'success',
  
  // EXAM flow - before exam
  EXAM_OFFICER_CHECK: 'info',
  EXAM_CHANGES_REQUIRED: 'warning',
  EXAM_APPROVED: 'primary',
  
  // EXAM flow - after exam
  MARKING_IN_PROGRESS: 'info',
  EXTERNAL_FEEDBACK: 'info',
  EXTERNAL_CHANGES_REQUIRED: 'warning',
  SETTER_RESPONSE: 'info',
  SETTER_CHANGES_MADE: 'warning',
  FINALISED: 'success',
  PUBLISHED: 'success',
  ARCHIVED: 'default',
  
  // Special states
  ON_HOLD: 'danger',
};

const STATE_LABELS = {
  DRAFT: 'Draft',
  READY_FOR_CHECK: 'Ready for Check',
  CHANGES_REQUIRED: 'Changes Required',
  RELEASED: 'Released',
  EXAM_OFFICER_CHECK: 'Exam Officer Check',
  EXAM_CHANGES_REQUIRED: 'Exam Changes Required',
  EXAM_APPROVED: 'Exam Approved',
  MARKING_IN_PROGRESS: 'Marking in Progress',
  EXTERNAL_FEEDBACK: 'External Feedback',
  EXTERNAL_CHANGES_REQUIRED: 'External Changes Required',
  SETTER_RESPONSE: 'Setter Response',
  SETTER_CHANGES_MADE: 'Setter Changes Made',
  FINALISED: 'Finalised',
  PUBLISHED: 'Published',
  ARCHIVED: 'Archived',
  ON_HOLD: 'On Hold',
};

const AssessmentStateBadge = ({ state }) => {
  const variant = STATE_COLORS[state] || 'default';
  const label = STATE_LABELS[state] || state;
  
  return <Badge variant={variant}>{label}</Badge>;
};

export default AssessmentStateBadge;
