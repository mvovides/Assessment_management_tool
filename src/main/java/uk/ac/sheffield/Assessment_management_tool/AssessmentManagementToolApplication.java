package uk.ac.sheffield.Assessment_management_tool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AssessmentManagementToolApplication {

	public static void main(String[] args) {
		SpringApplication.run(AssessmentManagementToolApplication.class, args);
	}

}
