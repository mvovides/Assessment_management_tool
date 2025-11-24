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
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        if (request.isExamsOfficer() && request.getBaseType() != UserBaseType.ACADEMIC) {
            throw new IllegalArgumentException("Only academics can be exams officers");
        }
        
        String tempPassword = generateTemporaryPassword();
        User user = new User(request.getName(), request.getEmail(), 
                passwordEncoder.encode(tempPassword), request.getBaseType());
        user.setExamsOfficer(request.isExamsOfficer());
        user = userRepository.save(user);
        
        return new CreateUserResponse(EntityMapper.toUserDto(user), tempPassword);
    }
    
    public UserDto getUserById(UUID id) {
        return EntityMapper.toUserDto(userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found")));
    }
    
    public UserDto getUserByEmail(String email) {
        return EntityMapper.toUserDto(userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found")));
    }
    
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(EntityMapper::toUserDto)
                .collect(Collectors.toList());
    }
    
    public UserDto toggleUserActive(UUID id) {
        return EntityMapper.toUserDto(userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found")));
    }
    
    public UserDto toggleExamsOfficer(UUID id, UUID currentUserId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (user.getBaseType() != UserBaseType.ACADEMIC) {
            throw new IllegalArgumentException("Only academics can be exams officers");
        }
        
        if (user.isExamsOfficer()) {
            validateCanRemoveExamsOfficer(id, currentUserId);
        }
        
        user.setExamsOfficer(!user.isExamsOfficer());
        return EntityMapper.toUserDto(userRepository.save(user));
    }
    
    public void changePassword(UUID userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    
    private void validateCanRemoveExamsOfficer(UUID id, UUID currentUserId) {
        long eoCount = userRepository.findAll().stream()
                .filter(u -> u.getBaseType() == UserBaseType.ACADEMIC && u.isExamsOfficer())
                .count();
        
        if (eoCount <= 1) {
            throw new IllegalArgumentException("Must have at least one Exams Officer");
        }
        
        if (id.equals(currentUserId)) {
            throw new IllegalArgumentException("Cannot remove your own Exams Officer status");
        }
    }
    
    private String generateTemporaryPassword() {
        SecureRandom random = new SecureRandom();
        String allChars = CHAR_LOWER + CHAR_UPPER + CHAR_DIGITS + CHAR_SPECIAL;
        StringBuilder password = new StringBuilder();
        
        password.append(CHAR_LOWER.charAt(random.nextInt(CHAR_LOWER.length())));
        password.append(CHAR_UPPER.charAt(random.nextInt(CHAR_UPPER.length())));
        password.append(CHAR_DIGITS.charAt(random.nextInt(CHAR_DIGITS.length())));
        password.append(CHAR_SPECIAL.charAt(random.nextInt(CHAR_SPECIAL.length())));
        
        for (int i = 4; i < 12; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }
        
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
