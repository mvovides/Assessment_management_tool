package uk.ac.sheffield.Assessment_management_tool.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.sheffield.Assessment_management_tool.domain.entity.User;
import uk.ac.sheffield.Assessment_management_tool.domain.enums.UserBaseType;
import uk.ac.sheffield.Assessment_management_tool.dto.request.CreateUserRequest;
import uk.ac.sheffield.Assessment_management_tool.dto.response.CreateUserResponse;
import uk.ac.sheffield.Assessment_management_tool.dto.response.UserDto;
import uk.ac.sheffield.Assessment_management_tool.mapper.EntityMapper;
import uk.ac.sheffield.Assessment_management_tool.repository.UserRepository;

import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
    private static final String CHAR_DIGITS = "0123456789";
    private static final String CHAR_SPECIAL = "!@#$%&*";
    
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    public CreateUserResponse createUser(CreateUserRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + request.getEmail());
        }
        
        // Validate Exams Officer can only be Academic
        if (request.isExamsOfficer() && request.getBaseType() != UserBaseType.ACADEMIC) {
            throw new IllegalArgumentException("Only academics can be exams officers");
        }
        
        // Generate temporary password
        String tempPassword = generateTemporaryPassword();
        
        // Create user
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(tempPassword));
        user.setBaseType(request.getBaseType());
        user.setExamsOfficer(request.isExamsOfficer());
        
        user = userRepository.save(user);
        
        return new CreateUserResponse(EntityMapper.toUserDto(user), tempPassword);
    }
    
    public UserDto getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        return EntityMapper.toUserDto(user);
    }
    
    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));
        return EntityMapper.toUserDto(user);
    }
    
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(EntityMapper::toUserDto)
                .collect(Collectors.toList());
    }
    
    public UserDto toggleUserActive(UUID id) {
        // Active field has been removed - this method is now a no-op but kept for backwards compatibility
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        
        return EntityMapper.toUserDto(user);
    }
    
    public UserDto toggleExamsOfficer(UUID id, UUID currentUserId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        
        if (user.getBaseType() != UserBaseType.ACADEMIC) {
            throw new IllegalArgumentException("Only academics can be exams officers");
        }
        
        // If trying to remove EO status, check if this is the last academic EO
        if (user.isExamsOfficer()) {
            long academicEOCount = userRepository.findAll().stream()
                    .filter(u -> u.getBaseType() == UserBaseType.ACADEMIC && u.isExamsOfficer())
                    .count();
            
            if (academicEOCount <= 1) {
                throw new IllegalArgumentException("Cannot remove Exams Officer status. There must be at least one Academic Exams Officer.");
            }
            
            // Check if user is trying to demote themselves
            if (id.equals(currentUserId)) {
                throw new IllegalArgumentException("You cannot remove your own Exams Officer status. Ask another Exams Officer to do this.");
            }
        }
        
        user.setExamsOfficer(!user.isExamsOfficer());
        user = userRepository.save(user);
        
        return EntityMapper.toUserDto(user);
    }
    
    public void changePassword(UUID userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    
    private String generateTemporaryPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(12);
        
        String allChars = CHAR_LOWER + CHAR_UPPER + CHAR_DIGITS + CHAR_SPECIAL;
        
        // Ensure at least one of each type
        password.append(CHAR_LOWER.charAt(random.nextInt(CHAR_LOWER.length())));
        password.append(CHAR_UPPER.charAt(random.nextInt(CHAR_UPPER.length())));
        password.append(CHAR_DIGITS.charAt(random.nextInt(CHAR_DIGITS.length())));
        password.append(CHAR_SPECIAL.charAt(random.nextInt(CHAR_SPECIAL.length())));
        
        // Fill the rest randomly
        for (int i = 4; i < 12; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }
        
        // Shuffle
        char[] chars = password.toString().toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        
        return new String(chars);
    }
}
