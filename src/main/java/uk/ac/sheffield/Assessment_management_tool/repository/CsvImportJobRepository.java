package uk.ac.sheffield.Assessment_management_tool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.ac.sheffield.Assessment_management_tool.domain.entity.CsvImportJob;
import uk.ac.sheffield.Assessment_management_tool.domain.enums.ImportJobStatus;

import java.util.List;
import java.util.UUID;

@Repository
public interface CsvImportJobRepository extends JpaRepository<CsvImportJob, UUID> {
    
    List<CsvImportJob> findByStatusOrderByCreatedAtDesc(ImportJobStatus status);
    
    List<CsvImportJob> findAllByOrderByCreatedAtDesc();
}
