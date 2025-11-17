package uk.ac.sheffield.Assessment_management_tool.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import uk.ac.sheffield.Assessment_management_tool.domain.entity.Assessment;
import uk.ac.sheffield.Assessment_management_tool.domain.entity.CsvImportJob;
import uk.ac.sheffield.Assessment_management_tool.domain.entity.Module;
import uk.ac.sheffield.Assessment_management_tool.domain.entity.ModuleStaffRole;
import uk.ac.sheffield.Assessment_management_tool.domain.entity.User;
import uk.ac.sheffield.Assessment_management_tool.domain.enums.*;
import uk.ac.sheffield.Assessment_management_tool.repository.AssessmentRepository;
import uk.ac.sheffield.Assessment_management_tool.repository.CsvImportJobRepository;
import uk.ac.sheffield.Assessment_management_tool.repository.ModuleRepository;
import uk.ac.sheffield.Assessment_management_tool.repository.ModuleStaffRoleRepository;
import uk.ac.sheffield.Assessment_management_tool.repository.UserRepository;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class CsvImportService {
    
    private final UserRepository userRepository;
    private final ModuleRepository moduleRepository;
    private final AssessmentRepository assessmentRepository;
    private final ModuleStaffRoleRepository moduleStaffRoleRepository;
    private final CsvImportJobRepository importJobRepository;
    private final PasswordEncoder passwordEncoder;
    
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
    private static final SecureRandom RANDOM = new SecureRandom();
    
    public CsvImportService(
            UserRepository userRepository,
            ModuleRepository moduleRepository,
            AssessmentRepository assessmentRepository,
            ModuleStaffRoleRepository moduleStaffRoleRepository,
            CsvImportJobRepository importJobRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.moduleRepository = moduleRepository;
        this.assessmentRepository = assessmentRepository;
        this.moduleStaffRoleRepository = moduleStaffRoleRepository;
        this.importJobRepository = importJobRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    /**
     * Import modules with assessments from CSV file
     * Expected format: moduleCode,moduleTitle,moduleLead,staff,assessmentType1,assessmentTitle1,assessmentType2,assessmentTitle2,...
     * Example: COM1001,Introduction to Software Engineering,Phil McMinn,"Kirill Bogdanov, Tahsin Khan",cw,Programming Assignment,cw,Requirements Specification
     * Note: Staff names are comma-separated in quotes. Assessment types and titles come in pairs.
     */
    public CsvImportJob importModulesWithAssessments(MultipartFile file, String academicYear) {
        CsvImportJob job = new CsvImportJob(file.getOriginalFilename());
        job.setStatus(ImportJobStatus.RUNNING);
        job = importJobRepository.save(job);
        
        List<String> errors = new ArrayList<>();
        int moduleCount = 0;
        int assessmentCount = 0;
        
        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .builder()
                     .setSkipHeaderRecord(false) // No header in the file
                     .setTrim(true)
                     .build())) {
            
            int lineNumber = 0;
            
            for (CSVRecord record : csvParser) {
                lineNumber++;
                try {
                    if (record.size() < 2) {
                        errors.add("Line " + lineNumber + ": Insufficient columns");
                        continue;
                    }
                    
                    String moduleCode = record.get(0);
                    String moduleTitle = record.get(1);
                    String moduleLead = record.size() > 2 ? record.get(2) : "";
                    String moduleModerator = record.size() > 3 ? record.get(3) : "";
                    String staff = record.size() > 4 ? record.get(4) : "";
                    
                    // Validate required fields
                    if (moduleCode == null || moduleCode.trim().isEmpty()) {
                        errors.add("Line " + lineNumber + ": Module code is required");
                        continue;
                    }
                    if (moduleTitle == null || moduleTitle.trim().isEmpty()) {
                        errors.add("Line " + lineNumber + ": Module title is required");
                        continue;
                    }
                    if (moduleLead == null || moduleLead.trim().isEmpty()) {
                        errors.add("Line " + lineNumber + ": Module lead is required");
                        continue;
                    }
                    if (moduleModerator == null || moduleModerator.trim().isEmpty()) {
                        errors.add("Line " + lineNumber + ": Module moderator is required");
                        continue;
                    }
                    
                    // Check if module already exists
                    Optional<Module> existingModule = moduleRepository.findByCodeAndAcademicYear(
                            moduleCode.trim().toUpperCase(), academicYear);
                    
                    Module module;
                    if (existingModule.isPresent()) {
                        module = existingModule.get();
                        // Update module title if different
                        if (!module.getTitle().equals(moduleTitle.trim())) {
                            module.setTitle(moduleTitle.trim());
                            moduleRepository.save(module);
                        }
                    } else {
                        // Create new module
                        module = new Module();
                        module.setCode(moduleCode.trim().toUpperCase());
                        module.setTitle(moduleTitle.trim());
                        module.setAcademicYear(academicYear);
                        module = moduleRepository.save(module);
                        moduleCount++;
                    }
                    
                    // Assign module lead (mandatory)
                    Optional<User> moduleLeadUser = userRepository.findByName(moduleLead.trim());
                    if (moduleLeadUser.isPresent()) {
                        // Check if already assigned
                        if (!moduleStaffRoleRepository.existsByModuleAndUserAndRole(
                                module, moduleLeadUser.get(), ModuleRole.MODULE_LEAD)) {
                            ModuleStaffRole leadRole = new ModuleStaffRole(
                                module, moduleLeadUser.get(), ModuleRole.MODULE_LEAD);
                            moduleStaffRoleRepository.save(leadRole);
                        }
                    } else {
                        errors.add("Line " + lineNumber + ": Module lead '" + moduleLead.trim() + "' not found");
                        continue; // Skip this module if lead not found
                    }
                    
                    // Assign module moderator (mandatory)
                    Optional<User> moderatorUser = userRepository.findByName(moduleModerator.trim());
                    if (moderatorUser.isPresent()) {
                        // Don't add if same as module lead
                        if (!moderatorUser.get().getId().equals(moduleLeadUser.get().getId())) {
                            // Check if already assigned
                            if (!moduleStaffRoleRepository.existsByModuleAndUserAndRole(
                                    module, moderatorUser.get(), ModuleRole.MODERATOR)) {
                                ModuleStaffRole moderatorRole = new ModuleStaffRole(
                                    module, moderatorUser.get(), ModuleRole.MODERATOR);
                                moduleStaffRoleRepository.save(moderatorRole);
                            }
                        }
                    } else {
                        errors.add("Line " + lineNumber + ": Module moderator '" + moduleModerator.trim() + "' not found");
                        continue; // Skip this module if moderator not found
                    }
                    
                    // Assign staff members if provided
                    if (staff != null && !staff.trim().isEmpty()) {
                        // Split staff names by comma (outside quotes)
                        String[] staffNames = staff.split(",");
                        for (String staffName : staffNames) {
                            String trimmedName = staffName.trim();
                            if (!trimmedName.isEmpty()) {
                                Optional<User> staffUser = userRepository.findByName(trimmedName);
                                if (staffUser.isPresent()) {
                                    // Don't add if already module lead or moderator
                                    boolean isLead = moduleStaffRoleRepository.existsByModuleAndUserAndRole(
                                            module, staffUser.get(), ModuleRole.MODULE_LEAD);
                                    boolean isModerator = moduleStaffRoleRepository.existsByModuleAndUserAndRole(
                                            module, staffUser.get(), ModuleRole.MODERATOR);
                                    // Check if already assigned as staff
                                    boolean isStaff = moduleStaffRoleRepository.existsByModuleAndUserAndRole(
                                            module, staffUser.get(), ModuleRole.STAFF);
                                    
                                    if (!isLead && !isModerator && !isStaff) {
                                        ModuleStaffRole staffRole = new ModuleStaffRole(
                                            module, staffUser.get(), ModuleRole.STAFF);
                                        moduleStaffRoleRepository.save(staffRole);
                                    }
                                } else {
                                    errors.add("Line " + lineNumber + ": Staff member '" + trimmedName + "' not found");
                                }
                            }
                        }
                    }
                    
                    // Parse assessments (starting from column 5, pairs of type and title)
                    int assessmentsInLine = 0;
                    for (int i = 5; i < record.size() - 1; i += 2) {
                        String typeStr = record.get(i);
                        String assessmentTitle = record.get(i + 1);
                        
                        // Skip empty pairs
                        if ((typeStr == null || typeStr.trim().isEmpty()) && 
                            (assessmentTitle == null || assessmentTitle.trim().isEmpty())) {
                            continue;
                        }
                        
                        if (typeStr == null || typeStr.trim().isEmpty()) {
                            errors.add("Line " + lineNumber + ", Assessment " + (assessmentsInLine + 1) + ": Type is required");
                            continue;
                        }
                        
                        if (assessmentTitle == null || assessmentTitle.trim().isEmpty()) {
                            errors.add("Line " + lineNumber + ", Assessment " + (assessmentsInLine + 1) + ": Title is required");
                            continue;
                        }
                        
                        // Parse assessment type
                        AssessmentType type;
                        try {
                            type = AssessmentType.valueOf(typeStr.trim().toUpperCase());
                        } catch (IllegalArgumentException e) {
                            errors.add("Line " + lineNumber + ", Assessment " + (assessmentsInLine + 1) + 
                                    ": Invalid type '" + typeStr + "'. Must be EXAM or CW");
                            continue;
                        }
                        
                        // Create assessment
                        Assessment assessment = new Assessment();
                        assessment.setModule(module);
                        assessment.setTitle(assessmentTitle.trim());
                        assessment.setType(type);
                        assessment.setCurrentState(AssessmentState.DRAFT);
                        // Exam date can be set later by admin
                        
                        assessmentRepository.save(assessment);
                        assessmentCount++;
                        assessmentsInLine++;
                    }
                    
                } catch (Exception e) {
                    errors.add("Line " + lineNumber + ": " + e.getMessage());
                }
            }
            
            if (errors.isEmpty()) {
                job.setStatus(ImportJobStatus.COMPLETED);
                job.setErrors("Successfully imported " + moduleCount + " modules and " + assessmentCount + " assessments");
            } else {
                job.setStatus(ImportJobStatus.FAILED);
                job.setErrors("Imported " + moduleCount + " modules and " + assessmentCount + 
                        " assessments. Errors:\n" + String.join("\n", errors));
            }
            
        } catch (Exception e) {
            job.setStatus(ImportJobStatus.FAILED);
            job.setErrors("Failed to parse CSV: " + e.getMessage());
        }
        
        return importJobRepository.save(job);
    }
    
    /**
     * Import users from CSV file
     * Expected format: name,email,baseType,isExamsOfficer
     * Example: John Doe,john.doe@sheffield.ac.uk,ACADEMIC,false
     */
    public CsvImportJob importUsers(MultipartFile file) {
        CsvImportJob job = new CsvImportJob(file.getOriginalFilename());
        job.setStatus(ImportJobStatus.RUNNING);
        job = importJobRepository.save(job);
        
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        
        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setIgnoreHeaderCase(true)
                     .setTrim(true)
                     .build())) {
            
            int lineNumber = 1; // Start at 1 (header is 0)
            
            for (CSVRecord record : csvParser) {
                lineNumber++;
                try {
                    String name = record.get("name");
                    String email = record.get("email");
                    String baseTypeStr = record.get("baseType");
                    String isExamsOfficerStr = record.get("isExamsOfficer");
                    
                    // Validate required fields
                    if (name == null || name.trim().isEmpty()) {
                        errors.add("Line " + lineNumber + ": Name is required");
                        continue;
                    }
                    if (email == null || email.trim().isEmpty()) {
                        errors.add("Line " + lineNumber + ": Email is required");
                        continue;
                    }
                    if (baseTypeStr == null || baseTypeStr.trim().isEmpty()) {
                        errors.add("Line " + lineNumber + ": Base type is required");
                        continue;
                    }
                    
                    // Check if user already exists
                    if (userRepository.findByEmail(email.trim()).isPresent()) {
                        errors.add("Line " + lineNumber + ": User with email " + email + " already exists");
                        continue;
                    }
                    
                    // Parse base type
                    UserBaseType baseType;
                    try {
                        baseType = UserBaseType.valueOf(baseTypeStr.trim().toUpperCase());
                    } catch (IllegalArgumentException e) {
                        errors.add("Line " + lineNumber + ": Invalid base type '" + baseTypeStr + "'. Must be ACADEMIC, TEACHING_SUPPORT, or EXTERNAL_EXAMINER");
                        continue;
                    }
                    
                    // Parse isExamsOfficer
                    boolean isExamsOfficer = false;
                    if (isExamsOfficerStr != null && !isExamsOfficerStr.trim().isEmpty()) {
                        isExamsOfficer = Boolean.parseBoolean(isExamsOfficerStr.trim());
                    }
                    
                    // Generate random password
                    String generatedPassword = generateRandomPassword(12);
                    
                    // Create user
                    User user = new User();
                    user.setName(name.trim());
                    user.setEmail(email.trim().toLowerCase());
                    user.setPasswordHash(passwordEncoder.encode(generatedPassword));
                    user.setBaseType(baseType);
                    user.setExamsOfficer(isExamsOfficer);
                    
                    userRepository.save(user);
                    successCount++;
                    
                    // Log the generated password (in real app, send via email)
                    System.out.println("Created user: " + email + " with password: " + generatedPassword);
                    
                } catch (Exception e) {
                    errors.add("Line " + lineNumber + ": " + e.getMessage());
                }
            }
            
            if (errors.isEmpty()) {
                job.setStatus(ImportJobStatus.COMPLETED);
                job.setErrors("Successfully imported " + successCount + " users");
            } else {
                job.setStatus(ImportJobStatus.FAILED);
                job.setErrors("Imported " + successCount + " users. Errors:\n" + String.join("\n", errors));
            }
            
        } catch (Exception e) {
            job.setStatus(ImportJobStatus.FAILED);
            job.setErrors("Failed to parse CSV: " + e.getMessage());
        }
        
        return importJobRepository.save(job);
    }
    
    /**
     * Import modules from CSV file
     * Expected format: code,title,academicYear
     * Example: COM1001,Introduction to Programming,2024/25
     */
    public CsvImportJob importModules(MultipartFile file) {
        CsvImportJob job = new CsvImportJob(file.getOriginalFilename());
        job.setStatus(ImportJobStatus.RUNNING);
        job = importJobRepository.save(job);
        
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        
        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setIgnoreHeaderCase(true)
                     .setTrim(true)
                     .build())) {
            
            int lineNumber = 1;
            
            for (CSVRecord record : csvParser) {
                lineNumber++;
                try {
                    String code = record.get("code");
                    String title = record.get("title");
                    String academicYear = record.get("academicYear");
                    
                    // Validate required fields
                    if (code == null || code.trim().isEmpty()) {
                        errors.add("Line " + lineNumber + ": Module code is required");
                        continue;
                    }
                    if (title == null || title.trim().isEmpty()) {
                        errors.add("Line " + lineNumber + ": Module title is required");
                        continue;
                    }
                    if (academicYear == null || academicYear.trim().isEmpty()) {
                        errors.add("Line " + lineNumber + ": Academic year is required");
                        continue;
                    }
                    
                    // Check if module already exists
                    if (moduleRepository.findByCodeAndAcademicYear(code.trim(), academicYear.trim()).isPresent()) {
                        errors.add("Line " + lineNumber + ": Module " + code + " for " + academicYear + " already exists");
                        continue;
                    }
                    
                    // Create module
                    Module module = new Module();
                    module.setCode(code.trim().toUpperCase());
                    module.setTitle(title.trim());
                    module.setAcademicYear(academicYear.trim());
                    
                    moduleRepository.save(module);
                    successCount++;
                    
                } catch (Exception e) {
                    errors.add("Line " + lineNumber + ": " + e.getMessage());
                }
            }
            
            if (errors.isEmpty()) {
                job.setStatus(ImportJobStatus.COMPLETED);
                job.setErrors("Successfully imported " + successCount + " modules");
            } else {
                job.setStatus(ImportJobStatus.FAILED);
                job.setErrors("Imported " + successCount + " modules. Errors:\n" + String.join("\n", errors));
            }
            
        } catch (Exception e) {
            job.setStatus(ImportJobStatus.FAILED);
            job.setErrors("Failed to parse CSV: " + e.getMessage());
        }
        
        return importJobRepository.save(job);
    }
    
    /**
     * Import assessments from CSV file
     * Expected format: moduleCode,academicYear,title,type,examDate
     * Example: COM1001,2024/25,Final Exam,EXAM,2025-05-15
     */
    public CsvImportJob importAssessments(MultipartFile file) {
        CsvImportJob job = new CsvImportJob(file.getOriginalFilename());
        job.setStatus(ImportJobStatus.RUNNING);
        job = importJobRepository.save(job);
        
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        
        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setIgnoreHeaderCase(true)
                     .setTrim(true)
                     .build())) {
            
            int lineNumber = 1;
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            
            for (CSVRecord record : csvParser) {
                lineNumber++;
                try {
                    String moduleCode = record.get("moduleCode");
                    String academicYear = record.get("academicYear");
                    String title = record.get("title");
                    String typeStr = record.get("type");
                    String examDateStr = record.get("examDate");
                    
                    // Validate required fields
                    if (moduleCode == null || moduleCode.trim().isEmpty()) {
                        errors.add("Line " + lineNumber + ": Module code is required");
                        continue;
                    }
                    if (academicYear == null || academicYear.trim().isEmpty()) {
                        errors.add("Line " + lineNumber + ": Academic year is required");
                        continue;
                    }
                    if (title == null || title.trim().isEmpty()) {
                        errors.add("Line " + lineNumber + ": Assessment title is required");
                        continue;
                    }
                    if (typeStr == null || typeStr.trim().isEmpty()) {
                        errors.add("Line " + lineNumber + ": Assessment type is required");
                        continue;
                    }
                    
                    // Find module
                    Optional<Module> moduleOpt = moduleRepository.findByCodeAndAcademicYear(
                            moduleCode.trim().toUpperCase(), academicYear.trim());
                    if (moduleOpt.isEmpty()) {
                        errors.add("Line " + lineNumber + ": Module " + moduleCode + " for " + academicYear + " not found");
                        continue;
                    }
                    
                    // Parse assessment type
                    AssessmentType type;
                    try {
                        type = AssessmentType.valueOf(typeStr.trim().toUpperCase());
                    } catch (IllegalArgumentException e) {
                        errors.add("Line " + lineNumber + ": Invalid assessment type '" + typeStr + "'. Must be EXAM or CW");
                        continue;
                    }
                    
                    // Parse exam date (optional for CW)
                    LocalDate examDate = null;
                    if (examDateStr != null && !examDateStr.trim().isEmpty()) {
                        try {
                            examDate = LocalDate.parse(examDateStr.trim(), dateFormatter);
                        } catch (Exception e) {
                            errors.add("Line " + lineNumber + ": Invalid date format '" + examDateStr + "'. Use yyyy-MM-dd");
                            continue;
                        }
                    } else if (type == AssessmentType.EXAM) {
                        errors.add("Line " + lineNumber + ": Exam date is required for EXAM assessments");
                        continue;
                    }
                    
                    // Create assessment
                    Assessment assessment = new Assessment();
                    assessment.setModule(moduleOpt.get());
                    assessment.setTitle(title.trim());
                    assessment.setType(type);
                    assessment.setExamDate(examDate);
                    assessment.setCurrentState(AssessmentState.DRAFT);
                    
                    assessmentRepository.save(assessment);
                    successCount++;
                    
                } catch (Exception e) {
                    errors.add("Line " + lineNumber + ": " + e.getMessage());
                }
            }
            
            if (errors.isEmpty()) {
                job.setStatus(ImportJobStatus.COMPLETED);
                job.setErrors("Successfully imported " + successCount + " assessments");
            } else {
                job.setStatus(ImportJobStatus.FAILED);
                job.setErrors("Imported " + successCount + " assessments. Errors:\n" + String.join("\n", errors));
            }
            
        } catch (Exception e) {
            job.setStatus(ImportJobStatus.FAILED);
            job.setErrors("Failed to parse CSV: " + e.getMessage());
        }
        
        return importJobRepository.save(job);
    }
    
    /**
     * Get all import jobs ordered by most recent first
     */
    public List<CsvImportJob> getAllImportJobs() {
        return importJobRepository.findAllByOrderByCreatedAtDesc();
    }
    
    /**
     * Get import job by ID
     */
    public CsvImportJob getImportJobById(UUID id) {
        return importJobRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Import job not found: " + id));
    }
    
    /**
     * Generate a random secure password
     */
    private String generateRandomPassword(int length) {
        StringBuilder password = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            password.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return password.toString();
    }
}
