package uk.ac.sheffield.Assessment_management_tool.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uk.ac.sheffield.Assessment_management_tool.domain.entity.CsvImportJob;
import uk.ac.sheffield.Assessment_management_tool.service.CsvImportService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/import")
@PreAuthorize("hasRole('ADMIN')")
public class CsvImportController {
    
    private final CsvImportService csvImportService;
    
    public CsvImportController(CsvImportService csvImportService) {
        this.csvImportService = csvImportService;
    }
    
    @PostMapping("/modules")
    public ResponseEntity<?> importModules(@RequestParam("file") MultipartFile file,
                                          @RequestParam(value = "academicYear", required = false, defaultValue = "2024/25") String academicYear) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(createError("File is required"));
        }
        
        if (!file.getOriginalFilename().endsWith(".csv")) {
            return ResponseEntity.badRequest().body(createError("File must be a CSV"));
        }
        
        try {
            CsvImportJob job = csvImportService.importModulesWithAssessments(file, academicYear);
            return ResponseEntity.status(HttpStatus.CREATED).body(job);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createError("Failed to import modules: " + e.getMessage()));
        }
    }
    
    @GetMapping("/jobs")
    public ResponseEntity<List<CsvImportJob>> getAllImportJobs() {
        return ResponseEntity.ok(csvImportService.getAllImportJobs());
    }
    
    @GetMapping("/jobs/{id}")
    public ResponseEntity<?> getImportJob(@PathVariable UUID id) {
        try {
            CsvImportJob job = csvImportService.getImportJobById(id);
            return ResponseEntity.ok(job);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createError(e.getMessage()));
        }
    }
    
    private Map<String, String> createError(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }
}
