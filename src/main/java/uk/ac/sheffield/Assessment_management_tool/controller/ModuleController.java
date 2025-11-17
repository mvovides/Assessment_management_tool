package uk.ac.sheffield.Assessment_management_tool.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import uk.ac.sheffield.Assessment_management_tool.dto.request.CreateModuleRequest;
import uk.ac.sheffield.Assessment_management_tool.dto.response.ModuleDto;
import uk.ac.sheffield.Assessment_management_tool.security.CustomUserDetails;
import uk.ac.sheffield.Assessment_management_tool.service.ModuleService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ModuleController {
    
    private static final Logger logger = LoggerFactory.getLogger(ModuleController.class);
    
    private final ModuleService moduleService;
    
    public ModuleController(ModuleService moduleService) {
        this.moduleService = moduleService;
    }
    
    @GetMapping("/modules")
    public ResponseEntity<List<ModuleDto>> getModules(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String year) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UUID currentUserId = null;
        
        logger.info("getModules called - Authentication: {}", authentication != null ? authentication.getName() : "null");
        
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            
            logger.info("Principal type: {}", principal.getClass().getName());
            
            // Check if principal is CustomUserDetails (not anonymousUser string)
            if (principal instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) principal;
                currentUserId = userDetails.getUser().getId();
                logger.info("Extracted userId: {} for user: {}", currentUserId, userDetails.getUser().getEmail());
            } else {
                logger.warn("Principal is not CustomUserDetails: {}", principal);
            }
        }
        
        return ResponseEntity.ok(moduleService.searchModules(search, year, currentUserId));
    }
    
    @GetMapping("/modules/{id}")
    public ResponseEntity<ModuleDto> getModuleById(@PathVariable UUID id) {
        return ResponseEntity.ok(moduleService.getModuleById(id));
    }
    
    @PostMapping("/admin/modules")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ModuleDto> createModule(@Valid @RequestBody CreateModuleRequest request) {
        ModuleDto module = moduleService.createModule(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(module);
    }
    
    @PutMapping("/admin/modules/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ModuleDto> updateModule(@PathVariable UUID id, @Valid @RequestBody CreateModuleRequest request) {
        ModuleDto module = moduleService.updateModule(id, request);
        return ResponseEntity.ok(module);
    }
    
    @DeleteMapping("/admin/modules/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteModule(@PathVariable UUID id) {
        moduleService.deleteModule(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/admin/modules/{moduleId}/external-examiners/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> addExternalExaminer(@PathVariable UUID moduleId, @PathVariable UUID userId) {
        moduleService.addExternalExaminer(moduleId, userId);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/admin/modules/{moduleId}/external-examiners/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeExternalExaminer(@PathVariable UUID moduleId, @PathVariable UUID userId) {
        moduleService.removeExternalExaminer(moduleId, userId);
        return ResponseEntity.noContent().build();
    }
}
