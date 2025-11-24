package uk.ac.sheffield.Assessment_management_tool.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.sheffield.Assessment_management_tool.domain.entity.Module;
import uk.ac.sheffield.Assessment_management_tool.domain.entity.ModuleStaffRole;
import uk.ac.sheffield.Assessment_management_tool.domain.entity.User;
import uk.ac.sheffield.Assessment_management_tool.domain.enums.ModuleRole;
import uk.ac.sheffield.Assessment_management_tool.domain.enums.UserBaseType;
import uk.ac.sheffield.Assessment_management_tool.dto.request.CreateModuleRequest;
import uk.ac.sheffield.Assessment_management_tool.dto.response.ModuleDto;
import uk.ac.sheffield.Assessment_management_tool.dto.response.ModuleStaffDto;
import uk.ac.sheffield.Assessment_management_tool.dto.response.UserDto;
import uk.ac.sheffield.Assessment_management_tool.mapper.EntityMapper;
import uk.ac.sheffield.Assessment_management_tool.repository.AssessmentRepository;
import uk.ac.sheffield.Assessment_management_tool.repository.ModuleRepository;
import uk.ac.sheffield.Assessment_management_tool.repository.ModuleStaffRoleRepository;
import uk.ac.sheffield.Assessment_management_tool.repository.UserRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ModuleService {
    
    private final ModuleRepository moduleRepository;
    private final ModuleStaffRoleRepository moduleStaffRoleRepository;
    private final UserRepository userRepository;
    private final AssessmentRepository assessmentRepository;
    
    public ModuleService(ModuleRepository moduleRepository, 
                        ModuleStaffRoleRepository moduleStaffRoleRepository,
                        UserRepository userRepository,
                        AssessmentRepository assessmentRepository) {
        this.moduleRepository = moduleRepository;
        this.moduleStaffRoleRepository = moduleStaffRoleRepository;
        this.userRepository = userRepository;
        this.assessmentRepository = assessmentRepository;
    }
    
    public ModuleDto createModule(CreateModuleRequest request) {
        if (moduleRepository.findByCode(request.getCode()).isPresent()) {
            throw new IllegalArgumentException("Module already exists: " + request.getCode());
        }
        
        Module module = new Module(request.getCode(), request.getTitle());
        module = moduleRepository.save(module);
        
        assignStaffToModule(module, request);
        return EntityMapper.toModuleDto(module);
    }
    
    public ModuleDto getModuleById(UUID id) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Module not found"));
        
        ModuleDto dto = EntityMapper.toModuleDto(module);
        List<ModuleStaffRole> staffRoles = moduleStaffRoleRepository.findByModuleId(id);
        
        dto.setStaff(staffRoles.stream()
                .filter(sr -> sr.getUser().getBaseType() != UserBaseType.EXTERNAL_EXAMINER)
                .map(sr -> new ModuleStaffDto(sr.getId(), sr.getUser().getId(), 
                        sr.getUser().getName(), sr.getUser().getEmail(), sr.getRole()))
                .collect(Collectors.toList()));
        
        dto.setExternalExaminers(staffRoles.stream()
                .filter(sr -> sr.getUser().getBaseType() == UserBaseType.EXTERNAL_EXAMINER)
                .map(sr -> EntityMapper.toUserDto(sr.getUser()))
                .collect(Collectors.toList()));
        
        return dto;
    }
    
    public List<ModuleDto> getAllModules() {
        return moduleRepository.findAll().stream()
                .map(this::enrichModuleDto)
                .collect(Collectors.toList());
    }
    
    public List<ModuleDto> searchModules(String search, String year, UUID currentUserId) {
        List<Module> modules = search == null ? 
                moduleRepository.findAll() : moduleRepository.searchModules(search);
        
        if (currentUserId != null) {
            modules = filterModulesForUser(modules, currentUserId);
        }
        
        return modules.stream()
                .map(module -> enrichModuleDtoForUser(module, currentUserId))
                .collect(Collectors.toList());
    }
    
    public ModuleDto updateModule(UUID id, CreateModuleRequest request) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Module not found"));
        
        module.setCode(request.getCode());
        module.setTitle(request.getTitle());
        module = moduleRepository.save(module);
        
        moduleStaffRoleRepository.deleteAll(moduleStaffRoleRepository.findByModuleId(id));
        assignStaffToModule(module, request);
        
        return getModuleById(id);
    }
    
    public void deleteModule(UUID id) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Module not found"));
        
        moduleStaffRoleRepository.deleteAll(moduleStaffRoleRepository.findByModuleId(id));
        moduleRepository.delete(module);
    }
    
    public void addExternalExaminer(UUID moduleId, UUID userId) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new IllegalArgumentException("Module not found"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (user.getBaseType() != UserBaseType.EXTERNAL_EXAMINER) {
            throw new IllegalArgumentException("User is not an external examiner");
        }
        
        moduleStaffRoleRepository.save(new ModuleStaffRole(module, user, ModuleRole.STAFF));
    }
    
    public void removeExternalExaminer(UUID moduleId, UUID userId) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new IllegalArgumentException("Module not found"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        moduleStaffRoleRepository.deleteAll(
                moduleStaffRoleRepository.findByModuleAndUser(module, user));
    }
    
    private void assignStaffToModule(Module module, CreateModuleRequest request) {
        User moduleLead = userRepository.findById(request.getModuleLeadId())
                .orElseThrow(() -> new IllegalArgumentException("Module lead not found"));
        moduleStaffRoleRepository.save(new ModuleStaffRole(module, moduleLead, ModuleRole.MODULE_LEAD));
        
        if (request.getModuleModeratorId() != null && 
            !request.getModuleModeratorId().equals(request.getModuleLeadId())) {
            User moderator = userRepository.findById(request.getModuleModeratorId())
                    .orElseThrow(() -> new IllegalArgumentException("Moderator not found"));
            moduleStaffRoleRepository.save(new ModuleStaffRole(module, moderator, ModuleRole.MODERATOR));
        }
        
        if (request.getStaffIds() != null) {
            for (UUID staffId : request.getStaffIds()) {
                if (!staffId.equals(request.getModuleLeadId()) && 
                    !staffId.equals(request.getModuleModeratorId())) {
                    User staff = userRepository.findById(staffId)
                            .orElseThrow(() -> new IllegalArgumentException("Staff not found"));
                    moduleStaffRoleRepository.save(new ModuleStaffRole(module, staff, ModuleRole.STAFF));
                }
            }
        }
    }
    
    private ModuleDto enrichModuleDto(Module module) {
        ModuleDto dto = EntityMapper.toModuleDto(module);
        List<ModuleStaffRole> staffRoles = moduleStaffRoleRepository.findByModuleId(module.getId());
        
        dto.setStaffCount((int) staffRoles.stream()
                .filter(sr -> sr.getUser().getBaseType() != UserBaseType.EXTERNAL_EXAMINER)
                .count());
        dto.setAssessmentCount(assessmentRepository.findByModuleId(module.getId()).size());
        dto.setExternalExaminers(staffRoles.stream()
                .filter(sr -> sr.getUser().getBaseType() == UserBaseType.EXTERNAL_EXAMINER)
                .map(sr -> EntityMapper.toUserDto(sr.getUser()))
                .collect(Collectors.toList()));
        
        return dto;
    }
    
    private ModuleDto enrichModuleDtoForUser(Module module, UUID userId) {
        ModuleDto dto = enrichModuleDto(module);
        
        if (userId != null) {
            moduleStaffRoleRepository.findByModuleId(module.getId()).stream()
                    .filter(sr -> sr.getUser().getId().equals(userId))
                    .findFirst()
                    .ifPresent(sr -> dto.setUserRole(sr.getRole().name()));
        }
        
        return dto;
    }
    
    private List<Module> filterModulesForUser(List<Module> modules, UUID userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return modules;
        
        UserBaseType baseType = user.getBaseType();
        if (baseType == UserBaseType.ACADEMIC || baseType == UserBaseType.EXTERNAL_EXAMINER) {
            List<UUID> userModuleIds = moduleStaffRoleRepository.findByUserId(userId).stream()
                    .map(role -> role.getModule().getId())
                    .collect(Collectors.toList());
            return modules.stream()
                    .filter(module -> userModuleIds.contains(module.getId()))
                    .collect(Collectors.toList());
        }
        
        return modules;
    }
}
