package uk.ac.sheffield.Assessment_management_tool.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import uk.ac.sheffield.Assessment_management_tool.dto.request.CreateUserRequest;
import uk.ac.sheffield.Assessment_management_tool.dto.response.CreateUserResponse;
import uk.ac.sheffield.Assessment_management_tool.dto.response.UserDto;
import uk.ac.sheffield.Assessment_management_tool.security.CustomUserDetails;
import uk.ac.sheffield.Assessment_management_tool.service.UserService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN') or hasRole('EXAMS_OFFICER')")
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }
    
    @PostMapping
    public ResponseEntity<CreateUserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        CreateUserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<UserDto> toggleActive(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.toggleUserActive(id));
    }
    
    @PatchMapping("/{id}/toggle-exams-officer")
    public ResponseEntity<UserDto> toggleExamsOfficer(@PathVariable UUID id, Authentication authentication) {
        CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
        UUID currentUserId = currentUser.getUser().getId();
        return ResponseEntity.ok(userService.toggleExamsOfficer(id, currentUserId));
    }
}
