package uk.ac.sheffield.Assessment_management_tool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.ac.sheffield.Assessment_management_tool.domain.entity.Module;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ModuleRepository extends JpaRepository<Module, UUID> {
    
    Optional<Module> findByCode(String code);
    
    @Query("SELECT m FROM Module m WHERE " +
           "LOWER(m.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(m.title) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Module> searchModules(@Param("search") String search);
}
