package uk.ac.sheffield.Assessment_management_tool.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import uk.ac.sheffield.Assessment_management_tool.domain.entity.User;
import uk.ac.sheffield.Assessment_management_tool.domain.enums.UserBaseType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {
    
    private final User user;
    
    public CustomUserDetails(User user) {
        this.user = user;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        // Add role based on base type
        switch (user.getBaseType()) {
            case TEACHING_SUPPORT:
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                break;
            case ACADEMIC:
                authorities.add(new SimpleGrantedAuthority("ROLE_ACADEMIC"));
                if (user.isExamsOfficer()) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_EXAMS_OFFICER"));
                }
                break;
            case EXTERNAL_EXAMINER:
                authorities.add(new SimpleGrantedAuthority("ROLE_EXTERNAL_EXAMINER"));
                break;
        }
        
        return authorities;
    }
    
    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }
    
    @Override
    public String getUsername() {
        return user.getEmail();
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
    
    public User getUser() {
        return user;
    }
}
