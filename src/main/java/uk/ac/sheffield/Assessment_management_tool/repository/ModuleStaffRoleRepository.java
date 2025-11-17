package uk.ac.sheffield.Assessment_management_tool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.ac.sheffield.Assessment_management_tool.domain.entity.Module;
import uk.ac.sheffield.Assessment_management_tool.domain.entity.ModuleStaffRole;
import uk.ac.sheffield.Assessment_management_tool.domain.entity.User;
import uk.ac.sheffield.Assessment_management_tool.domain.enums.ModuleRole;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ModuleStaffRoleRepository extends JpaRepository<ModuleStaffRole, UUID> {
    
    List<ModuleStaffRole> findByModule(Module module);
    
    List<ModuleStaffRole> findByModuleId(UUID moduleId);
    
    List<ModuleStaffRole> findByUser(User user);
    
    List<ModuleStaffRole> findByUserId(UUID userId);
    
    List<ModuleStaffRole> findByModuleAndUser(Module module, User user);
    
    List<ModuleStaffRole> findByModuleAndRole(Module module, ModuleRole role);
    
    Optional<ModuleStaffRole> findByModuleAndUserAndRole(Module module, User user, ModuleRole role);
    
    boolean existsByModuleAndUserAndRole(Module module, User user, ModuleRole role);
    
    void deleteByModuleAndUserAndRole(Module module, User user, ModuleRole role);
}
