package uk.ac.sheffield.Assessment_management_tool.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import uk.ac.sheffield.Assessment_management_tool.domain.enums.ModuleRole;

import java.util.UUID;

@Entity
@Table(
    name = "module_staff_role",
    uniqueConstraints = @UniqueConstraint(columnNames = {"module_id", "user_id", "role"})
)
public class ModuleStaffRole {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModuleRole role;
    
    // Constructors
    public ModuleStaffRole() {}
    
    public ModuleStaffRole(Module module, User user, ModuleRole role) {
        this.module = module;
        this.user = user;
        this.role = role;
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public Module getModule() {
        return module;
    }
    
    public void setModule(Module module) {
        this.module = module;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public ModuleRole getRole() {
        return role;
    }
    
    public void setRole(ModuleRole role) {
        this.role = role;
    }
}
