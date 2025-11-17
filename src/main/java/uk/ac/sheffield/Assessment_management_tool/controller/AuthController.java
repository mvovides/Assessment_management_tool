package uk.ac.sheffield.Assessment_management_tool.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import uk.ac.sheffield.Assessment_management_tool.dto.request.LoginRequest;
import uk.ac.sheffield.Assessment_management_tool.dto.response.UserDto;
import uk.ac.sheffield.Assessment_management_tool.dto.response.UserProfileDto;
import uk.ac.sheffield.Assessment_management_tool.mapper.EntityMapper;
import uk.ac.sheffield.Assessment_management_tool.security.CustomUserDetails;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final AuthenticationManager authenticationManager;
    
    public AuthController(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }
    
    @GetMapping("/csrf")
    public ResponseEntity<Void> getCsrfToken() {
        // This endpoint just triggers CSRF token generation
        // The token is automatically added to the response cookie by Spring Security
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        try {
            // Authenticate
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            
            // Set security context
            SecurityContext securityContext = SecurityContextHolder.getContext();
            securityContext.setAuthentication(authentication);
            
            // Create new session
            HttpSession session = httpRequest.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);
            
            // Get user details
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            UserDto userDto = EntityMapper.toUserDto(userDetails.getUser());
            
            List<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(new UserProfileDto(userDto, roles));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Invalid email or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UserDto userDto = EntityMapper.toUserDto(userDetails.getUser());
        
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(new UserProfileDto(userDto, roles));
    }
}
