import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { moduleApi, userApi } from '../api/client';
import { useAuth } from '../context/AuthContext';
import { Card, Input, Badge, Button, Modal, Select } from '../components/UI';

const ModulesPage = () => {
  const { hasRole, user, viewMode, isTeachingSupport } = useAuth();
  const queryClient = useQueryClient();
  const [searchTerm, setSearchTerm] = useState('');
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [newModule, setNewModule] = useState({
    code: '',
    title: '',
    moduleLeadId: '',
    moduleModeratorId: '',
    staffIds: [],
  });

  // In admin view (EO or Teaching Support), show all modules
  // In standard view, show only user's modules (handled by backend)
  const showAllModules = viewMode === 'admin' || isTeachingSupport;
  
  const { data: modules = [], isLoading } = useQuery({
    queryKey: ['modules', showAllModules],
    queryFn: moduleApi.getAll,
  });

  const { data: users = [] } = useQuery({
    queryKey: ['users'],
    queryFn: userApi.getAll,
    enabled: showCreateModal, // Only load when modal is open
  });

  const createModuleMutation = useMutation({
    mutationFn: moduleApi.create,
    onSuccess: () => {
      queryClient.invalidateQueries(['modules']);
      setShowCreateModal(false);
      setNewModule({
        code: '',
        title: '',
        moduleLeadId: '',
        moduleModeratorId: '',
        staffIds: [],
      });
    },
  });

  const handleCreateModule = (e) => {
    e.preventDefault();
    // Clean up the data before sending
    const moduleData = {
      code: newModule.code,
      title: newModule.title,
      academicYear: '2024/25', // Default academic year (field kept for database compatibility)
      moduleLeadId: newModule.moduleLeadId || null,
      moduleModeratorId: newModule.moduleModeratorId || null,
      staffIds: newModule.staffIds,
    };
    createModuleMutation.mutate(moduleData);
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

  // Filter users to show only academics and teaching support
  const availableStaff = users.filter(
    (user) => user.baseType === 'ACADEMIC' || user.baseType === 'TEACHING_SUPPORT'
  );

  const filteredModules = modules
    .filter(
      (module) =>
        module.code.toLowerCase().includes(searchTerm.toLowerCase()) ||
        module.title.toLowerCase().includes(searchTerm.toLowerCase())
    )
    .sort((a, b) => a.code.localeCompare(b.code));

  if (isLoading) {
    return <div className="text-center py-12">Loading modules...</div>;
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold text-gray-900">
          Modules {showAllModules && <span className="text-lg text-gray-500">(All Modules)</span>}
        </h1>
        {(hasRole('ROLE_ADMIN') || showAllModules) && (
          <Button onClick={() => setShowCreateModal(true)}>Create Module</Button>
        )}
      </div>

      <Card>
        <Input
          placeholder="Search modules by code or name..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
        />
      </Card>

      {filteredModules.length === 0 ? (
        <Card>
          <p className="text-center text-gray-500">No modules found.</p>
        </Card>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredModules.map((module) => (
            <Link key={module.id} to={`/modules/${module.id}`}>
              <Card className="h-full hover:shadow-lg transition-shadow cursor-pointer">
                <div className="flex flex-col h-full">
                  <div className="flex items-start justify-between mb-2">
                    <h3 className="text-lg font-semibold text-gray-900">{module.code}</h3>
                    {module.userRole && (
                      <Badge variant={module.userRole === 'MODULE_LEAD' ? 'primary' : module.userRole === 'MODERATOR' ? 'success' : 'default'}>
                        {module.userRole === 'MODULE_LEAD' ? 'Module Lead' : 
                         module.userRole === 'MODERATOR' ? 'Moderator' : 
                         module.userRole === 'STAFF' ? 'Staff' : module.userRole}
                      </Badge>
                    )}
                  </div>
                  <p className="text-gray-600 mb-4 flex-1">{module.title}</p>
                  <div className="flex items-center justify-between text-sm text-gray-500 pt-4 border-t">
                    <span>{module.assessmentCount || 0} assessments</span>
                    <span>{module.staffCount || 0} staff</span>
                  </div>
                </div>
              </Card>
            </Link>
          ))}
        </div>
      )}

      {/* Create Module Modal */}
      <Modal
        isOpen={showCreateModal}
        onClose={() => setShowCreateModal(false)}
        title="Create Module"
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
              {availableStaff.map((user) => (
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
              {availableStaff
                .filter((user) => user.id !== newModule.moduleLeadId)
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
              {availableStaff
                .filter(
                  (user) =>
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
            <Button type="submit" disabled={createModuleMutation.isLoading}>
              {createModuleMutation.isLoading ? 'Creating...' : 'Create Module'}
            </Button>
            <Button type="button" variant="secondary" onClick={() => setShowCreateModal(false)}>
              Cancel
            </Button>
          </div>
          {createModuleMutation.isError && (
            <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded text-sm">
              {createModuleMutation.error.response?.data?.message || 'Failed to create module'}
            </div>
          )}
        </form>
      </Modal>
    </div>
  );
};

export default ModulesPage;
