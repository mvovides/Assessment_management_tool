package uk.ac.sheffield.Assessment_management_tool.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Entity
@Table(name = "module_external_examiner")
public class ModuleExternalExaminer {
    
    @EmbeddedId
    private ModuleExternalExaminerId id;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("moduleId")
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    // Constructors
    public ModuleExternalExaminer() {}
    
    public ModuleExternalExaminer(Module module, User user) {
        this.module = module;
        this.user = user;
        this.id = new ModuleExternalExaminerId(module.getId(), user.getId());
    }
    
    // Getters and Setters
    public ModuleExternalExaminerId getId() {
        return id;
    }
    
    public void setId(ModuleExternalExaminerId id) {
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
}
