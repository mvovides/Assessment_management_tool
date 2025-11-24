package uk.ac.sheffield.Assessment_management_tool.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

@Entity
@Table(
    name = "module",
    uniqueConstraints = @UniqueConstraint(columnNames = {"code"})
)
public class Module {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @NotBlank
    @Column(nullable = false, unique = true)
    private String code;
    
    @NotBlank
    @Column(nullable = false)
    private String title;
    
    public Module() {}
    
    public Module(String code, String title) {
        this.code = code;
        this.title = title;
    }
    
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
}
