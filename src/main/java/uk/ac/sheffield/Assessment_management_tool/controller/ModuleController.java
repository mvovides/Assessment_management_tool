package uk.ac.sheffield.Assessment_management_tool.controller;

import jakarta.validation.Valid;
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
    
    private final ModuleService moduleService;
    
    public ModuleController(ModuleService moduleService) {
        this.moduleService = moduleService;
    }
    
    @GetMapping("/modules")
    public ResponseEntity<List<ModuleDto>> getModules(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String year) {
        
        UUID currentUserId = null;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof CustomUserDetails) {
            currentUserId = ((CustomUserDetails) auth.getPrincipal()).getUser().getId();
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
