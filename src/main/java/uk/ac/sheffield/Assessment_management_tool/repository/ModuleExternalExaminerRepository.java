package uk.ac.sheffield.Assessment_management_tool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.ac.sheffield.Assessment_management_tool.domain.entity.Module;
import uk.ac.sheffield.Assessment_management_tool.domain.entity.ModuleExternalExaminer;
import uk.ac.sheffield.Assessment_management_tool.domain.entity.ModuleExternalExaminerId;
import uk.ac.sheffield.Assessment_management_tool.domain.entity.User;

import java.util.List;
import java.util.UUID;

@Repository
public interface ModuleExternalExaminerRepository extends JpaRepository<ModuleExternalExaminer, ModuleExternalExaminerId> {
    
    List<ModuleExternalExaminer> findByModule(Module module);
    
    List<ModuleExternalExaminer> findByModuleId(UUID moduleId);
    
    List<ModuleExternalExaminer> findByUser(User user);
    
    List<ModuleExternalExaminer> findByUserId(UUID userId);
    
    boolean existsByModuleAndUser(Module module, User user);
    
    void deleteByModuleAndUser(Module module, User user);
}
