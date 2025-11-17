# Assessment Management Tool

A comprehensive web-based system for managing academic assessments through their complete lifecycle, from creation to publication. Built with Spring Boot and React, this tool supports multiple assessment types (coursework, tests, and exams) with robust role-based workflows and state management.

## Overview

The Assessment Management Tool streamlines the assessment process for universities by providing a structured workflow system that enforces quality checks and maintains audit trails. The system supports independent checker requirements, external examiner feedback, and flexible role assignments while ensuring compliance with academic regulations.

## Key Features

- **Multi-Type Assessment Support**: Manage coursework (CW), tests (TEST), and exams (EXAM) with type-specific workflows
- **24-State Workflow Engine**: Comprehensive state machine with transition validation and permission checks
- **Role-Based Access Control**: Support for setters, checkers, module leads, moderators, exams officers, and external examiners
- **Independent Checker Validation**: Automatic enforcement of checker independence rules
- **Assessment Content Management**: Upload and track assessment materials with secure file handling
- **Transition History**: Complete audit trail of all state changes with timestamps and user attribution
- **Module Staff Management**: Organize teaching staff with role assignments (MODULE_LEAD, MODERATOR, STAFF)
- **External Examiner Integration**: Dedicated workflow steps for external feedback and responses

## Technology Stack

**Backend:**
- Spring Boot 3.5.7
- Java 21
- H2 Database (in-memory)
- Spring Security (session-based authentication)
- Flyway Database Migrations
- Spring Data JPA

**Frontend:**
- React 18
- Vite
- React Query (TanStack Query)
- React Router
- Tailwind CSS
- Axios

## Getting Started

### Prerequisites

- Java 21 or higher
- Node.js 18 or higher
- Maven 3.8+

### Quick Start

```bash
# Clone the repository
git clone https://github.com/mvovides/Assessment_management_tool.git
cd Assessment_management_tool

# Install frontend dependencies
cd web
npm install
cd ..

# Run the application (Windows)
./start-app.ps1

# Or run manually:
# Terminal 1 - Backend
./mvnw spring-boot:run

# Terminal 2 - Frontend
cd web
npm run dev
```

The application will be available at:
- Frontend: http://localhost:5174
- Backend API: http://localhost:8080

### Default Login Credentials

```
Admin: admin@sheffield.ac.uk / admin123
Academic: bob.brown@sheffield.ac.uk / admin123
```

## Architecture

### Assessment State Workflows

**Coursework (CW) Flow:**
DRAFT → READY_FOR_CHECK → RELEASED → DEADLINE_PASSED → MARKING → MODERATED → FEEDBACK_RETURNED → APPROVED → PUBLISHED

**Test (TEST) Flow:**
DRAFT → READY_FOR_CHECK → TEST_TAKEN → MARKING → MODERATED → RESULTS_RETURNED → APPROVED → PUBLISHED

**Exam (EXAM) Flow:**
DRAFT → READY_FOR_CHECK → EXAM_OFFICER_CHECK → EXTERNAL_FEEDBACK → SETTER_RESPONSE → FINAL_CHECK → SENT_TO_PRINTING → EXAM_TAKEN → MARKING → ADMIN_MARK_CHECK → MODERATED → APPROVED → PUBLISHED

### Role Permissions

- **SETTER**: Creates assessment content, submits for checking
- **CHECKER**: Reviews and approves/rejects assessment content (must be independent)
- **MODULE_LEAD**: Manages module staff and assessments
- **MODERATOR**: Reviews marked assessments, auto-assigned as checker
- **EXAMS_OFFICER**: Manages exam-specific workflows
- **EXTERNAL_EXAMINER**: Provides external feedback on exam assessments
- **ADMIN**: Full system access with override capabilities

## Project Structure

```
Assessment_management_tool/
├── src/main/java/uk/ac/sheffield/Assessment_management_tool/
│   ├── controller/          # REST API endpoints
│   ├── service/             # Business logic layer
│   ├── domain/
│   │   ├── entity/          # JPA entities
│   │   └── enums/           # Type enumerations
│   ├── repository/          # Data access layer
│   ├── dto/                 # Data transfer objects
│   ├── security/            # Authentication & authorization
│   └── mapper/              # Entity-DTO mappers
├── src/main/resources/
│   ├── db/migration/        # Flyway database migrations
│   └── application.properties
├── web/                     # React frontend
│   ├── src/
│   │   ├── pages/          # Page components
│   │   ├── components/     # Reusable UI components
│   │   ├── api/            # API client
│   │   └── context/        # React context providers
│   └── public/
└── docs/                    # UML diagrams and documentation
```

## Database Schema

The system uses 11 main entities:
- **User**: System users with base types (ACADEMIC, TEACHING_SUPPORT, EXTERNAL_EXAMINER)
- **Module**: Academic modules with staff assignments
- **Assessment**: Assessment instances with type-specific workflows
- **AssessmentRoleAssignment**: User role assignments per assessment
- **AssessmentTransition**: Complete history of state changes
- **ModuleStaffRole**: Module staff with roles (MODULE_LEAD, MODERATOR, STAFF)
- **CheckerFeedback**: Feedback from checkers
- **ExternalExaminerFeedback**: External examiner feedback
- **SetterResponse**: Setter responses to feedback
- **ModuleExternalExaminer**: External examiner assignments to modules

## Development

### Running Tests

```bash
# Backend tests
./mvnw test

# Frontend tests
cd web
npm test
```

### Database Migrations

Database schema is managed through Flyway migrations:
- `V1__init.sql`: Initial schema
- `V2__seed.sql`: Test data
- `V3__add_assessment_content_fields.sql`: Content management fields

### Building for Production

```bash
# Build backend
./mvnw clean package

# Build frontend
cd web
npm run build
```

## Contributing

This project was developed as part of an academic assessment management system. Contributions should follow the existing code structure and maintain compatibility with the state machine workflows.

## License

This project is part of academic coursework at the University of Sheffield.

## Support

For issues or questions, please open an issue on the GitHub repository.
