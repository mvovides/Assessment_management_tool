import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { userApi, moduleApi } from '../api/client';
import { Card, Button, Input, Select, Badge, Modal } from '../components/UI';
import { useAuth } from '../context/AuthContext';

const AdminPage = () => {
  const queryClient = useQueryClient();
  const { user: currentUser } = useAuth();
  const [activeTab, setActiveTab] = useState('users');
  const [showUserModal, setShowUserModal] = useState(false);
  const [showModuleModal, setShowModuleModal] = useState(false);
  const [editingModule, setEditingModule] = useState(null);
  const [uploadMessage, setUploadMessage] = useState('');
  const [newUser, setNewUser] = useState({
    email: '',
    password: '',
    name: '',
    baseType: 'ACADEMIC',
    isExamsOfficer: false,
  });
  const [newModule, setNewModule] = useState({
    code: '',
    title: '',
    moduleLeadId: '',
    moduleModeratorId: '',
    staffIds: [],
  });
  const [showExternalExaminerModal, setShowExternalExaminerModal] = useState(false);
  const [selectedModuleForEE, setSelectedModuleForEE] = useState(null);
  const [selectedExternalExaminerId, setSelectedExternalExaminerId] = useState('');

  const { data: users = [] } = useQuery({
    queryKey: ['users'],
    queryFn: userApi.getAll,
  });

  const { data: modules = [] } = useQuery({
    queryKey: ['modules'],
    queryFn: moduleApi.getAll,
  });

  const createUserMutation = useMutation({
    mutationFn: userApi.create,
    onSuccess: () => {
      queryClient.invalidateQueries(['users']);
      setShowUserModal(false);
      setNewUser({
        email: '',
        password: '',
        name: '',
        baseType: 'ACADEMIC',
        isExamsOfficer: false,
      });
    },
  });

  const toggleExamsOfficerMutation = useMutation({
    mutationFn: (userId) => userApi.toggleExamsOfficer(userId),
    onSuccess: () => {
      queryClient.invalidateQueries(['users']);
    },
    onError: (error) => {
      const message = error.response?.data?.message || 'Failed to update Exams Officer status';
      alert(message);
    },
  });

  const createModuleMutation = useMutation({
    mutationFn: moduleApi.create,
    onSuccess: () => {
      queryClient.invalidateQueries(['modules']);
      setShowModuleModal(false);
      setEditingModule(null);
      setNewModule({ code: '', title: '', moduleLeadId: '', moduleModeratorId: '', staffIds: [] });
    },
  });

  const updateModuleMutation = useMutation({
    mutationFn: ({ id, data }) => moduleApi.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries(['modules']);
      setShowModuleModal(false);
      setEditingModule(null);
      setNewModule({ code: '', title: '', moduleLeadId: '', moduleModeratorId: '', staffIds: [] });
    },
  });

  const deleteModuleMutation = useMutation({
    mutationFn: moduleApi.delete,
    onSuccess: () => {
      queryClient.invalidateQueries(['modules']);
    },
  });

  const addExternalExaminerMutation = useMutation({
    mutationFn: ({ moduleId, userId }) => moduleApi.addExternalExaminer(moduleId, userId),
    onSuccess: () => {
      queryClient.invalidateQueries(['modules']);
      setShowExternalExaminerModal(false);
      setSelectedExternalExaminerId('');
    },
    onError: (error) => {
      alert(error.response?.data?.message || 'Failed to add external examiner');
    },
  });

  const removeExternalExaminerMutation = useMutation({
    mutationFn: ({ moduleId, userId }) => moduleApi.removeExternalExaminer(moduleId, userId),
    onSuccess: () => {
      queryClient.invalidateQueries(['modules']);
    },
    onError: (error) => {
      alert(error.response?.data?.message || 'Failed to remove external examiner');
    },
  });

  const handleCreateUser = (e) => {
    e.preventDefault();
    createUserMutation.mutate(newUser);
  };

  const handleCreateModule = (e) => {
    e.preventDefault();
    const moduleData = {
      code: newModule.code,
      title: newModule.title,
      academicYear: newModule.academicYear || '2024/25', // Use existing or default to current year
      moduleLeadId: newModule.moduleLeadId || null,
      moduleModeratorId: newModule.moduleModeratorId || null,
      staffIds: newModule.staffIds,
    };
    
    if (editingModule) {
      updateModuleMutation.mutate({ id: editingModule.id, data: moduleData });
    } else {
      createModuleMutation.mutate(moduleData);
    }
  };

  const handleEditModule = (module) => {
    setEditingModule(module);
    setNewModule({
      code: module.code,
      title: module.title,
      moduleLeadId: module.staff?.find(s => s.role === 'MODULE_LEAD')?.userId || '',
      moduleModeratorId: module.staff?.find(s => s.role === 'MODERATOR')?.userId || '',
      staffIds: module.staff?.filter(s => s.role === 'STAFF').map(s => s.userId) || [],
      academicYear: module.academicYear, // Keep for backend update
    });
    setShowModuleModal(true);
  };

  const handleDeleteModule = (moduleId) => {
    if (window.confirm('Are you sure you want to delete this module? This action cannot be undone.')) {
      deleteModuleMutation.mutate(moduleId);
    }
  };

  const handleManageExternalExaminers = (module) => {
    setSelectedModuleForEE(module);
    setShowExternalExaminerModal(true);
  };

  const handleAddExternalExaminer = (e) => {
    e.preventDefault();
    if (!selectedExternalExaminerId || !selectedModuleForEE) return;
    
    addExternalExaminerMutation.mutate({
      moduleId: selectedModuleForEE.id,
      userId: selectedExternalExaminerId,
    });
  };

  const handleRemoveExternalExaminer = (userId) => {
    if (!selectedModuleForEE) return;
    
    if (window.confirm('Are you sure you want to remove this external examiner from the module?')) {
      removeExternalExaminerMutation.mutate({
        moduleId: selectedModuleForEE.id,
        userId: userId,
      });
    }
  };

  const handleCloseModuleModal = () => {
    setShowModuleModal(false);
    setEditingModule(null);
    setNewModule({ code: '', title: '', moduleLeadId: '', moduleModeratorId: '', staffIds: [] });
  };

  const handleStaffSelection = (e) => {
    const options = e.target.options;
    const selected = [];
    for (let i = 0; i < options.length; i++) {
      if (options[i].selected) {
        selected.push(options[i].value);
      }
    }
    setNewModule({ ...newModule, staffIds: selected });
  };

  const handleToggleEO = (user) => {
    // Count current Exams Officers who are academics
    const academicEOs = users.filter(u => u.baseType === 'ACADEMIC' && u.examsOfficer);
    
    // If trying to remove EO status
    if (user.examsOfficer) {
      // Check if this is the last academic EO
      if (academicEOs.length <= 1) {
        alert('Cannot remove Exams Officer status. There must be at least one Academic Exams Officer.');
        return;
      }
      
      // Check if user is trying to demote themselves
      if (currentUser && user.id === currentUser.id) {
        alert('You cannot remove your own Exams Officer status. Ask another Exams Officer to do this.');
        return;
      }
    }
    
    toggleExamsOfficerMutation.mutate(user.id);
  };

  const handleFileUpload = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    setUploadMessage('Uploading...');
    
    const formData = new FormData();
    formData.append('file', file);

    try {
      const response = await fetch('/api/admin/import/modules', {
        method: 'POST',
        body: formData,
        credentials: 'include',
      });

      if (response.ok) {
        const result = await response.json();
        setUploadMessage(`Success!\n${result.errors || 'Imported successfully'}`);
        // Refresh data
        queryClient.invalidateQueries(['modules']);
      } else {
        const error = await response.json();
        setUploadMessage(`Error: ${error.error || 'Upload failed'}`);
      }
    } catch (error) {
      setUploadMessage(`Error: ${error.message}`);
    }

    // Clear file input
    e.target.value = '';
  };

  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-bold text-gray-900">Administration</h1>

      {/* Tabs */}
      <div className="border-b border-gray-200">
        <nav className="-mb-px flex space-x-8">
          <button
            onClick={() => setActiveTab('users')}
            className={`py-4 px-1 border-b-2 font-medium text-sm ${
              activeTab === 'users'
                ? 'border-primary-600 text-primary-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            }`}
          >
            Users
          </button>
          <button
            onClick={() => setActiveTab('modules')}
            className={`py-4 px-1 border-b-2 font-medium text-sm ${
              activeTab === 'modules'
                ? 'border-primary-600 text-primary-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            }`}
          >
            Modules
          </button>
          <button
            onClick={() => setActiveTab('import')}
            className={`py-4 px-1 border-b-2 font-medium text-sm ${
              activeTab === 'import'
                ? 'border-primary-600 text-primary-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            }`}
          >
            CSV Import
          </button>
        </nav>
      </div>

      {/* Users Tab */}
      {activeTab === 'users' && (
        <div className="space-y-4">
          <div className="flex justify-between items-center">
            <h2 className="text-xl font-semibold">User Management</h2>
            <Button onClick={() => setShowUserModal(true)}>Create User</Button>
          </div>

          <Card>
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead>
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Name
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Email
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Type
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      EO Status
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Actions
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {users.map((user) => (
                    <tr key={user.id}>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        {user.name}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">
                        {user.email}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <Badge variant="default">{user.baseType}</Badge>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        {user.baseType === 'ACADEMIC' && user.examsOfficer && (
                          <Badge variant="primary">Exams Officer</Badge>
                        )}
                        {user.baseType === 'ACADEMIC' && !user.examsOfficer && (
                          <span className="text-sm text-gray-500">-</span>
                        )}
                        {user.baseType !== 'ACADEMIC' && (
                          <span className="text-sm text-gray-400">N/A</span>
                        )}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium space-x-2">
                        {user.baseType === 'ACADEMIC' && (
                          <Button
                            size="sm"
                            variant={user.examsOfficer ? 'danger' : 'primary'}
                            onClick={() => handleToggleEO(user)}
                            disabled={toggleExamsOfficerMutation.isLoading}
                          >
                            {user.examsOfficer ? 'Remove EO' : 'Make EO'}
                          </Button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </Card>
        </div>
      )}

      {/* Modules Tab */}
      {activeTab === 'modules' && (
        <div className="space-y-4">
          <div className="flex justify-between items-center">
            <h2 className="text-xl font-semibold">Module Management</h2>
            <Button onClick={() => setShowModuleModal(true)}>Create Module</Button>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {modules.map((module) => (
              <Card key={module.id}>
                <div className="flex justify-between items-start mb-2">
                  <h3 className="text-lg font-semibold text-gray-900">{module.code}</h3>
                  <div className="flex gap-1">
                    <Button
                      size="sm"
                      variant="secondary"
                      onClick={() => handleEditModule(module)}
                      disabled={updateModuleMutation.isLoading}
                    >
                      Edit
                    </Button>
                    <Button
                      size="sm"
                      variant="danger"
                      onClick={() => handleDeleteModule(module.id)}
                      disabled={deleteModuleMutation.isLoading}
                    >
                      Delete
                    </Button>
                  </div>
                </div>
                <p className="text-sm text-gray-600 mt-1">{module.title}</p>
                <div className="mt-4 space-y-2">
                  <div className="flex items-center justify-between text-sm">
                    <span className="text-gray-500">{module.assessmentCount || 0} assessments</span>
                    <span className="text-gray-500">{module.staffCount || 0} staff</span>
                  </div>
                  {module.externalExaminers && module.externalExaminers.length > 0 && (
                    <div className="text-xs text-gray-600">
                      <span className="font-medium">External Examiners:</span> {module.externalExaminers.length}
                    </div>
                  )}
                  <Button
                    size="sm"
                    variant="secondary"
                    onClick={() => handleManageExternalExaminers(module)}
                    className="w-full mt-2"
                  >
                    📋 Manage External Examiners
                  </Button>
                </div>
              </Card>
            ))}
          </div>
        </div>
      )}

      {/* CSV Import Tab */}
      {activeTab === 'import' && (
        <div className="space-y-6">
          <h2 className="text-xl font-semibold">CSV Import - Modules & Assessments</h2>
          
          {uploadMessage && (
            <div className={`px-4 py-3 rounded ${
              uploadMessage.includes('Success') ? 'bg-green-50 border border-green-200 text-green-700' : 'bg-red-50 border border-red-200 text-red-700'
            }`}>
              <pre className="text-sm whitespace-pre-wrap">{uploadMessage}</pre>
            </div>
          )}
          
          <Card>
            <h3 className="text-lg font-semibold text-gray-900 mb-4">Import Modules with Assessments</h3>
            
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  CSV File Format (No Header Row)
                </label>
                <p className="text-sm text-gray-600 mb-2">
                  Format: <code className="bg-gray-100 px-1 rounded">moduleCode,moduleTitle,moduleLead,moderators,type1,title1,type2,title2,...</code>
                </p>
                <div className="p-3 bg-gray-50 rounded text-xs font-mono overflow-x-auto">
                  <strong>Example:</strong>
                  <pre className="mt-1 text-gray-700">
COM1001,Introduction to Software Engineering,Phil McMinn,"Kirill Bogdanov, Tahsin Khan, Donghwan Shin",cw,Programming Assignment,cw,Requirements Specification{'\n'}
COM4507,Software and Hardware Verification,Georg Struth,,exam,Final Exam{'\n'}
COM107,Systems and Networks,Prosanta Gope,James Mapp,cw,Lab Assessment,cw,Test,exam,Final Exam
                  </pre>
                </div>
                <div className="mt-2 p-2 bg-blue-50 border border-blue-200 rounded text-xs">
                  <strong>Column Details:</strong>
                  <ul className="list-disc ml-4 mt-1 space-y-1">
                    <li><strong>Column A:</strong> Module code (required)</li>
                    <li><strong>Column B:</strong> Module title (required)</li>
                    <li><strong>Column C:</strong> Module lead (required) - assigned MODULE_LEAD role</li>
                    <li><strong>Column D:</strong> Moderators (optional) - comma-separated list, can be blank. Assigned MODERATOR role</li>
                    <li><strong>Column E onward:</strong> Assessment pairs - type,title,type,title,... (optional)</li>
                  </ul>
                  <div className="mt-2 pt-2 border-t border-blue-300">
                    <strong>Important Notes:</strong>
                    <ul className="list-disc ml-4 mt-1 space-y-1">
                      <li>Moderators are <strong>optional</strong> - leave column D blank to assign later</li>
                      <li>Multiple moderators can be comma-separated (use quotes if needed)</li>
                      <li>Assessment types: <code>exam</code>, <code>cw</code>, or <code>test</code> (test = coursework)</li>
                      <li>Moderators automatically become checkers for all module assessments</li>
                      <li>Empty assessment pairs at the end are ignored</li>
                    </ul>
                  </div>
                </div>
              </div>
              
              <div>
                <input
                  type="file"
                  accept=".csv"
                  id="modulesFile"
                  className="hidden"
                  onChange={handleFileUpload}
                />
                <Button
                  onClick={() => document.getElementById('modulesFile').click()}
                  variant="primary"
                >
                  Choose CSV File to Import
                </Button>
              </div>
            </div>
          </Card>
        </div>
      )}

      {/* Create User Modal */}
      <Modal
        isOpen={showUserModal}
        onClose={() => setShowUserModal(false)}
        title="Create User"
      >
        <form onSubmit={handleCreateUser} className="space-y-4">
          <Input
            label="Name"
            value={newUser.name}
            onChange={(e) => setNewUser({ ...newUser, name: e.target.value })}
            required
          />
          <Input
            label="Email"
            type="email"
            value={newUser.email}
            onChange={(e) => setNewUser({ ...newUser, email: e.target.value })}
            required
          />
          <Input
            label="Password"
            type="password"
            value={newUser.password}
            onChange={(e) => setNewUser({ ...newUser, password: e.target.value })}
            required
          />
          <Select
            label="Base Type"
            value={newUser.baseType}
            onChange={(e) => setNewUser({ ...newUser, baseType: e.target.value })}
            options={[
              { value: 'TEACHING_SUPPORT', label: 'Teaching Support' },
              { value: 'ACADEMIC', label: 'Academic' },
              { value: 'EXTERNAL_EXAMINER', label: 'External Examiner' },
            ]}
          />
          <div className="flex items-center">
            <input
              type="checkbox"
              id="isExamsOfficer"
              checked={newUser.isExamsOfficer}
              onChange={(e) => setNewUser({ ...newUser, isExamsOfficer: e.target.checked })}
              className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
            />
            <label htmlFor="isExamsOfficer" className="ml-2 block text-sm text-gray-900">
              Exams Officer
            </label>
          </div>
          <div className="flex gap-2">
            <Button type="submit" disabled={createUserMutation.isLoading}>
              {createUserMutation.isLoading ? 'Creating...' : 'Create User'}
            </Button>
            <Button type="button" variant="secondary" onClick={() => setShowUserModal(false)}>
              Cancel
            </Button>
          </div>
          {createUserMutation.isError && (
            <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded text-sm">
              {createUserMutation.error.message || 'Failed to create user'}
            </div>
          )}
        </form>
      </Modal>

      {/* Create Module Modal */}
      <Modal
        isOpen={showModuleModal}
        onClose={handleCloseModuleModal}
        title={editingModule ? 'Edit Module' : 'Create Module'}
      >
        <form onSubmit={handleCreateModule} className="space-y-4">
          <Input
            label="Module Code"
            value={newModule.code}
            onChange={(e) => setNewModule({ ...newModule, code: e.target.value })}
            required
            placeholder="e.g. COM2008"
          />
          <Input
            label="Module Title"
            value={newModule.title}
            onChange={(e) => setNewModule({ ...newModule, title: e.target.value })}
            required
            placeholder="e.g. Software Engineering"
          />

          
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Module Lead <span className="text-red-500">*</span>
            </label>
            <select
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500"
              value={newModule.moduleLeadId}
              onChange={(e) => setNewModule({ ...newModule, moduleLeadId: e.target.value })}
              required
            >
              <option value="">-- Select Module Lead --</option>
              {users
                .filter((user) => user.baseType === 'ACADEMIC' || user.baseType === 'TEACHING_SUPPORT')
                .map((user) => (
                  <option key={user.id} value={user.id}>
                    {user.name} ({user.email}) - {user.baseType}
                  </option>
                ))}
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Module Moderator <span className="text-red-500">*</span>
            </label>
            <p className="text-xs text-gray-500 mb-1">
              Automatically assigned as checker for all assessments in this module
            </p>
            <select
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500"
              value={newModule.moduleModeratorId}
              onChange={(e) => setNewModule({ ...newModule, moduleModeratorId: e.target.value })}
              required
            >
              <option value="">-- Select Module Moderator --</option>
              {users
                .filter((user) => 
                  (user.baseType === 'ACADEMIC' || user.baseType === 'TEACHING_SUPPORT') &&
                  user.id !== newModule.moduleLeadId
                )
                .map((user) => (
                  <option key={user.id} value={user.id}>
                    {user.name} ({user.email}) - {user.baseType}
                  </option>
                ))}
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Additional Staff (Optional)
            </label>
            <p className="text-xs text-gray-500 mb-2">
              Hold Ctrl (Windows) or Cmd (Mac) to select multiple staff members
            </p>
            <select
              multiple
              size="5"
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500"
              value={newModule.staffIds}
              onChange={handleStaffSelection}
            >
              {users
                .filter(
                  (user) =>
                    (user.baseType === 'ACADEMIC' || user.baseType === 'TEACHING_SUPPORT') &&
                    user.id !== newModule.moduleLeadId &&
                    user.id !== newModule.moduleModeratorId
                )
                .map((user) => (
                  <option key={user.id} value={user.id}>
                    {user.name} ({user.email}) - {user.baseType}
                  </option>
                ))}
            </select>
          </div>

          <div className="flex gap-2">
            <Button type="submit" disabled={createModuleMutation.isLoading || updateModuleMutation.isLoading}>
              {editingModule 
                ? (updateModuleMutation.isLoading ? 'Updating...' : 'Update Module')
                : (createModuleMutation.isLoading ? 'Creating...' : 'Create Module')
              }
            </Button>
            <Button type="button" variant="secondary" onClick={handleCloseModuleModal}>
              Cancel
            </Button>
          </div>
          {(createModuleMutation.isError || updateModuleMutation.isError) && (
            <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded text-sm">
              {createModuleMutation.error?.response?.data?.message || 
               updateModuleMutation.error?.response?.data?.message || 
               'Failed to save module'}
            </div>
          )}
        </form>
      </Modal>

      {/* Manage External Examiners Modal */}
      <Modal
        isOpen={showExternalExaminerModal}
        onClose={() => {
          setShowExternalExaminerModal(false);
          setSelectedModuleForEE(null);
          setSelectedExternalExaminerId('');
        }}
        title={`Manage External Examiners - ${selectedModuleForEE?.code || ''}`}
      >
        <div className="space-y-4">
          {/* Current External Examiners */}
          <div>
            <h3 className="text-sm font-medium text-gray-700 mb-2">Current External Examiners</h3>
            {selectedModuleForEE?.externalExaminers && selectedModuleForEE.externalExaminers.length > 0 ? (
              <div className="space-y-2">
                {selectedModuleForEE.externalExaminers.map((examiner) => (
                  <div
                    key={examiner.id}
                    className="flex items-center justify-between p-3 bg-gray-50 rounded-lg border border-gray-200"
                  >
                    <div>
                      <p className="text-sm font-medium text-gray-900">{examiner.name}</p>
                      <p className="text-xs text-gray-500">{examiner.email}</p>
                    </div>
                    <Button
                      size="sm"
                      variant="danger"
                      onClick={() => handleRemoveExternalExaminer(examiner.id)}
                      disabled={removeExternalExaminerMutation.isLoading}
                    >
                      Remove
                    </Button>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-sm text-gray-500 py-3 px-4 bg-gray-50 rounded-lg border border-gray-200">
                No external examiners assigned to this module yet.
              </p>
            )}
          </div>

          {/* Add New External Examiner */}
          <div className="border-t pt-4">
            <h3 className="text-sm font-medium text-gray-700 mb-2">Add External Examiner</h3>
            <form onSubmit={handleAddExternalExaminer} className="space-y-3">
              <select
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500"
                value={selectedExternalExaminerId}
                onChange={(e) => setSelectedExternalExaminerId(e.target.value)}
                required
              >
                <option value="">-- Select External Examiner --</option>
                {users
                  .filter((user) => 
                    user.baseType === 'EXTERNAL_EXAMINER' &&
                    !selectedModuleForEE?.externalExaminers?.some(ee => ee.id === user.id)
                  )
                  .map((user) => (
                    <option key={user.id} value={user.id}>
                      {user.name} ({user.email})
                    </option>
                  ))}
              </select>
              <div className="flex gap-2">
                <Button
                  type="submit"
                  disabled={addExternalExaminerMutation.isLoading || !selectedExternalExaminerId}
                >
                  {addExternalExaminerMutation.isLoading ? 'Adding...' : 'Add External Examiner'}
                </Button>
                <Button
                  type="button"
                  variant="secondary"
                  onClick={() => {
                    setShowExternalExaminerModal(false);
                    setSelectedModuleForEE(null);
                    setSelectedExternalExaminerId('');
                  }}
                >
                  Close
                </Button>
              </div>
            </form>
          </div>

          {users.filter(u => u.baseType === 'EXTERNAL_EXAMINER').length === 0 && (
            <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-3">
              <p className="text-sm text-yellow-800">
                <strong>Note:</strong> No external examiner users exist yet. Please create external examiner users first in the Users tab.
              </p>
            </div>
          )}
        </div>
      </Modal>
    </div>
  );
};

export default AdminPage;
