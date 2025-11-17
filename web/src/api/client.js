import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Auth API
export const authApi = {
  login: (credentials) => api.post('/auth/login', credentials).then(res => res.data),
  logout: () => api.post('/auth/logout').then(res => res.data),
  getCurrentUser: () => api.get('/auth/me').then(res => res.data),
};

// User API
export const userApi = {
  getAll: () => api.get('/admin/users').then(res => res.data),
  getAllUsers: () => api.get('/admin/users').then(res => res.data),
  getUserById: (id) => api.get(`/admin/users/${id}`).then(res => res.data),
  create: (data) => api.post('/admin/users', data).then(res => res.data),
  createUser: (data) => api.post('/admin/users', data).then(res => res.data),
  toggleActive: (id) => api.patch(`/admin/users/${id}/toggle-active`).then(res => res.data),
  toggleExamsOfficer: (id) => api.patch(`/admin/users/${id}/toggle-exams-officer`).then(res => res.data),
};

// Module API
export const moduleApi = {
  getAll: () => api.get('/modules').then(res => res.data),
  getModules: (params) => api.get('/modules', { params }).then(res => res.data),
  getById: (id) => api.get(`/modules/${id}`).then(res => res.data),
  getModuleById: (id) => api.get(`/modules/${id}`).then(res => res.data),
  create: (data) => api.post('/admin/modules', data).then(res => res.data),
  createModule: (data) => api.post('/admin/modules', data).then(res => res.data),
  update: (id, data) => api.put(`/admin/modules/${id}`, data).then(res => res.data),
  updateModule: (id, data) => api.put(`/admin/modules/${id}`, data).then(res => res.data),
  delete: (id) => api.delete(`/admin/modules/${id}`).then(res => res.data),
  deleteModule: (id) => api.delete(`/admin/modules/${id}`).then(res => res.data),
  getModuleAssessments: (id) => api.get(`/modules/${id}/assessments`).then(res => res.data),
  addExternalExaminer: (moduleId, userId) => api.post(`/admin/modules/${moduleId}/external-examiners/${userId}`).then(res => res.data),
  removeExternalExaminer: (moduleId, userId) => api.delete(`/admin/modules/${moduleId}/external-examiners/${userId}`).then(res => res.data),
};

// Assessment API
export const assessmentApi = {
  getAll: () => api.get('/assessments').then(res => res.data),
  getAssessmentById: (id) => api.get(`/assessments/${id}`).then(res => res.data),
  getById: (id) => api.get(`/assessments/${id}`).then(res => res.data),
  getByModule: (moduleId) => api.get(`/modules/${moduleId}/assessments`).then(res => res.data),
  create: (data) => api.post(`/admin/modules/${data.moduleId}/assessments`, data).then(res => res.data),
  createAssessment: (moduleId, data) => api.post(`/admin/modules/${moduleId}/assessments`, data).then(res => res.data),
  getTransitions: (id) => api.get(`/assessments/${id}/transitions`).then(res => res.data),
  progress: (id, data) => api.post(`/assessments/${id}/progress`, data).then(res => res.data),
  progressAssessment: (id, data) => api.post(`/assessments/${id}/progress`, data).then(res => res.data),
  overrideTransition: (id, data) => api.post(`/admin/assessments/${id}/override`, data).then(res => res.data),
  submitCheckerFeedback: (id, feedback) => api.post(`/assessments/${id}/checker-feedback`, feedback).then(res => res.data),
  submitExternalFeedback: (id, feedback) => api.post(`/assessments/${id}/external-feedback`, feedback).then(res => res.data),
  submitSetterResponse: (id, response) => api.post(`/assessments/${id}/setter-response`, response).then(res => res.data),
  // Role management
  getRoles: (id) => api.get(`/assessments/${id}/roles`).then(res => res.data),
  assignRole: (id, data) => api.post(`/assessments/${id}/roles`, data).then(res => res.data),
  removeRole: (id, userId, role) => api.delete(`/assessments/${id}/roles/${userId}/${role}`).then(res => res.data),
  // Content submission
  submitContent: (id, data) => api.post(`/assessments/${id}/submit-content`, data).then(res => res.data),
};

export default api;
