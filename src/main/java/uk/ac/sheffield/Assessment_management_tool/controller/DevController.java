package uk.ac.sheffield.Assessment_management_tool.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import uk.ac.sheffield.Assessment_management_tool.domain.entity.User;
import uk.ac.sheffield.Assessment_management_tool.repository.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dev")
public class DevController {
    
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    
    public DevController(PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }
    
    @GetMapping("/hash")
    public ResponseEntity<Map<String, String>> generateHash(@RequestParam String password) {
        String hash = passwordEncoder.encode(password);
        Map<String, String> response = new HashMap<>();
        response.put("password", password);
        response.put("hash", hash);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/reset-all-passwords")
    public ResponseEntity<Map<String, Object>> resetAllPasswords(@RequestParam(defaultValue = "admin123") String password) {
        String newHash = passwordEncoder.encode(password);
        List<User> allUsers = userRepository.findAll();
        
        for (User user : allUsers) {
            user.setPasswordHash(newHash);
        }
        
        userRepository.saveAll(allUsers);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "All user passwords have been reset");
        response.put("password", password);
        response.put("usersUpdated", allUsers.size());
        response.put("newHash", newHash);
        
        return ResponseEntity.ok(response);
    }
}
