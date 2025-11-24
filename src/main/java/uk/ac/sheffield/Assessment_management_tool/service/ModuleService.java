package uk.ac.sheffield.Assessment_management_tool.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    
    private static final Logger logger = LoggerFactory.getLogger(ModuleService.class);
    
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
        // Check if module already exists (by code only)
        if (moduleRepository.findByCode(request.getCode()).isPresent()) {
            throw new IllegalArgumentException("Module already exists: " + request.getCode());
        }
        
        Module module = new Module();
        module.setCode(request.getCode());
        module.setTitle(request.getTitle());
        
        module = moduleRepository.save(module);
        
        // Add module lead (mandatory)
        User moduleLead = userRepository.findById(request.getModuleLeadId())
                .orElseThrow(() -> new IllegalArgumentException("Module lead not found: " + request.getModuleLeadId()));
        
        ModuleStaffRole leadRole = new ModuleStaffRole(module, moduleLead, ModuleRole.MODULE_LEAD);
        moduleStaffRoleRepository.save(leadRole);
        
        // Add module moderator if specified
        if (request.getModuleModeratorId() != null) {
            // Skip if moderator is the same as module lead
            if (!request.getModuleModeratorId().equals(request.getModuleLeadId())) {
                User moderator = userRepository.findById(request.getModuleModeratorId())
                        .orElseThrow(() -> new IllegalArgumentException("Module moderator not found: " + request.getModuleModeratorId()));
                
                ModuleStaffRole moderatorRole = new ModuleStaffRole(module, moderator, ModuleRole.MODERATOR);
                moduleStaffRoleRepository.save(moderatorRole);
            }
        }
        
        // Add staff members if specified
        if (request.getStaffIds() != null && !request.getStaffIds().isEmpty()) {
            for (UUID staffId : request.getStaffIds()) {
                // Skip if this is the module lead or moderator (already added)
                if (staffId.equals(request.getModuleLeadId()) || staffId.equals(request.getModuleModeratorId())) {
                    continue;
                }
                
                User staff = userRepository.findById(staffId)
                        .orElseThrow(() -> new IllegalArgumentException("Staff member not found: " + staffId));
                
                ModuleStaffRole staffRole = new ModuleStaffRole(module, staff, ModuleRole.STAFF);
                moduleStaffRoleRepository.save(staffRole);
            }
        }
        
        return EntityMapper.toModuleDto(module);
    }
    
    public ModuleDto getModuleById(UUID id) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Module not found: " + id));
        
        ModuleDto dto = EntityMapper.toModuleDto(module);
        
        // Add staff information
        List<ModuleStaffRole> staffRoles = moduleStaffRoleRepository.findByModuleId(id);
        List<ModuleStaffDto> staff = staffRoles.stream()
                .filter(staffRole -> staffRole.getUser().getBaseType() != UserBaseType.EXTERNAL_EXAMINER)
                .map(staffRole -> new ModuleStaffDto(
                    staffRole.getId(),
                    staffRole.getUser().getId(),
                    staffRole.getUser().getName(),
                    staffRole.getUser().getEmail(),
                    staffRole.getRole()
                ))
                .collect(Collectors.toList());
        dto.setStaff(staff);
        
        // Add external examiners
        List<UserDto> externalExaminers = staffRoles.stream()
                .filter(staffRole -> staffRole.getUser().getBaseType() == UserBaseType.EXTERNAL_EXAMINER)
                .map(staffRole -> EntityMapper.toUserDto(staffRole.getUser()))
                .collect(Collectors.toList());
        dto.setExternalExaminers(externalExaminers);
        
        return dto;
    }
    
    public List<ModuleDto> getAllModules() {
        return moduleRepository.findAll().stream()
                .map(module -> {
                    ModuleDto dto = EntityMapper.toModuleDto(module);
                    // Set counts
                    List<ModuleStaffRole> allStaff = moduleStaffRoleRepository.findByModuleId(module.getId());
                    dto.setStaffCount((int) allStaff.stream()
                            .filter(sr -> sr.getUser().getBaseType() != UserBaseType.EXTERNAL_EXAMINER)
                            .count());
                    dto.setAssessmentCount(assessmentRepository.findByModuleId(module.getId()).size());
                    
                    // Add external examiners
                    List<UserDto> externalExaminers = allStaff.stream()
                            .filter(sr -> sr.getUser().getBaseType() == UserBaseType.EXTERNAL_EXAMINER)
                            .map(sr -> EntityMapper.toUserDto(sr.getUser()))
                            .collect(Collectors.toList());
                    dto.setExternalExaminers(externalExaminers);
                    
                    return dto;
                })
                .collect(Collectors.toList());
    }
    
    public List<ModuleDto> searchModules(String search, String year, UUID currentUserId) {
        logger.info("searchModules called with currentUserId: {}", currentUserId);
        
        List<Module> modules;
        
        if (search == null) {
            modules = moduleRepository.findAll();
        } else {
            modules = moduleRepository.searchModules(search);
        }
        
        logger.info("Found {} total modules before filtering", modules.size());
        
        // Filter modules based on user role
        if (currentUserId != null) {
            User currentUser = userRepository.findById(currentUserId).orElse(null);
            
            if (currentUser != null) {
                UserBaseType baseType = currentUser.getBaseType();
                logger.info("User {} has baseType: {}", currentUser.getEmail(), baseType);
                
                // If user is ACADEMIC or EXTERNAL_EXAMINER, only show modules they're assigned to
                if (baseType == UserBaseType.ACADEMIC || baseType == UserBaseType.EXTERNAL_EXAMINER) {
                    
                    List<ModuleStaffRole> userModuleRoles = moduleStaffRoleRepository.findByUserId(currentUserId);
                    logger.info("User has {} module staff roles", userModuleRoles.size());
                    
                    List<UUID> userModuleIds = userModuleRoles.stream()
                            .map(role -> role.getModule().getId())
                            .collect(Collectors.toList());
                    
                    logger.info("User is assigned to module IDs: {}", userModuleIds);
                    
                    // Filter to only modules the user is assigned to
                    modules = modules.stream()
                            .filter(module -> userModuleIds.contains(module.getId()))
                            .collect(Collectors.toList());
                    
                    logger.info("After filtering: {} modules remaining", modules.size());
                }
                // TEACHING_SUPPORT users see all modules (no filtering)
            } else {
                logger.warn("User not found with ID: {}", currentUserId);
            }
        } else {
            logger.info("No currentUserId provided, returning all modules");
        }
        
        return modules.stream()
                .map(module -> {
                    ModuleDto dto = EntityMapper.toModuleDto(module);
                    // Set counts
                    List<ModuleStaffRole> allStaff = moduleStaffRoleRepository.findByModuleId(module.getId());
                    dto.setStaffCount((int) allStaff.stream()
                            .filter(sr -> sr.getUser().getBaseType() != UserBaseType.EXTERNAL_EXAMINER)
                            .count());
                    dto.setAssessmentCount(assessmentRepository.findByModuleId(module.getId()).size());
                    
                    // Add external examiners
                    List<UserDto> externalExaminers = allStaff.stream()
                            .filter(sr -> sr.getUser().getBaseType() == UserBaseType.EXTERNAL_EXAMINER)
                            .map(sr -> EntityMapper.toUserDto(sr.getUser()))
                            .collect(Collectors.toList());
                    dto.setExternalExaminers(externalExaminers);
                    
                    // Set user's role on this module if currentUserId is provided
                    if (currentUserId != null) {
                        allStaff.stream()
                            .filter(sr -> sr.getUser().getId().equals(currentUserId))
                            .findFirst()
                            .ifPresent(sr -> dto.setUserRole(sr.getRole().name()));
                    }
                    
                    return dto;
                })
                .collect(Collectors.toList());
    }
    
    public ModuleDto updateModule(UUID id, CreateModuleRequest request) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Module not found: " + id));
        
        module.setCode(request.getCode());
        module.setTitle(request.getTitle());
        
        module = moduleRepository.save(module);
        
        // Update staff assignments
        // First, remove all existing staff roles
        List<ModuleStaffRole> existingRoles = moduleStaffRoleRepository.findByModuleId(id);
        moduleStaffRoleRepository.deleteAll(existingRoles);
        
        // Add module lead (mandatory)
        User moduleLead = userRepository.findById(request.getModuleLeadId())
                .orElseThrow(() -> new IllegalArgumentException("Module lead not found: " + request.getModuleLeadId()));
        
        ModuleStaffRole leadRole = new ModuleStaffRole(module, moduleLead, ModuleRole.MODULE_LEAD);
        moduleStaffRoleRepository.save(leadRole);
        
        // Add module moderator if specified
        if (request.getModuleModeratorId() != null) {
            // Skip if moderator is the same as module lead
            if (!request.getModuleModeratorId().equals(request.getModuleLeadId())) {
                User moderator = userRepository.findById(request.getModuleModeratorId())
                        .orElseThrow(() -> new IllegalArgumentException("Module moderator not found: " + request.getModuleModeratorId()));
                
                ModuleStaffRole moderatorRole = new ModuleStaffRole(module, moderator, ModuleRole.MODERATOR);
                moduleStaffRoleRepository.save(moderatorRole);
            }
        }
        
        // Add staff members if specified
        if (request.getStaffIds() != null && !request.getStaffIds().isEmpty()) {
            for (UUID staffId : request.getStaffIds()) {
                // Skip if this is the module lead or moderator (already added)
                if (staffId.equals(request.getModuleLeadId()) || staffId.equals(request.getModuleModeratorId())) {
                    continue;
                }
                
                User staff = userRepository.findById(staffId)
                        .orElseThrow(() -> new IllegalArgumentException("Staff member not found: " + staffId));
                
                ModuleStaffRole staffRole = new ModuleStaffRole(module, staff, ModuleRole.STAFF);
                moduleStaffRoleRepository.save(staffRole);
            }
        }
        
        return getModuleById(id);
    }
    
    public void deleteModule(UUID id) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Module not found: " + id));
        
        // Delete all staff roles for this module
        List<ModuleStaffRole> staffRoles = moduleStaffRoleRepository.findByModuleId(id);
        moduleStaffRoleRepository.deleteAll(staffRoles);
        
        // Delete the module
        moduleRepository.delete(module);
    }
    
    public void addExternalExaminer(UUID moduleId, UUID userId) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new IllegalArgumentException("Module not found: " + moduleId));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        if (user.getBaseType() != UserBaseType.EXTERNAL_EXAMINER) {
            throw new IllegalArgumentException("User is not an external examiner");
        }
        
        // Add external examiner role - treating as a staff role
        ModuleStaffRole externalRole = new ModuleStaffRole(module, user, ModuleRole.STAFF);
        moduleStaffRoleRepository.save(externalRole);
    }
    
    public void removeExternalExaminer(UUID moduleId, UUID userId) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new IllegalArgumentException("Module not found: " + moduleId));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        // Remove the external examiner's staff role
        List<ModuleStaffRole> roles = moduleStaffRoleRepository.findByModuleAndUser(module, user);
        moduleStaffRoleRepository.deleteAll(roles);
    }
}
