# 📁 Project Structure Overview

## Root Directory Structure

```
Assessment_management_tool/
│
├── 📄 START_HERE.md              ⭐ START HERE - Quick setup guide
├── 📄 README.md                  📚 Complete project documentation
├── 📄 QUICKSTART.md              🏃 Quick reference for developers
├── 📄 CSV_IMPORT_GUIDE.md        📊 CSV import format guide
│
├── 🚀 start-backend.bat          Double-click to start backend server
├── 🚀 start-frontend.bat         Double-click to start frontend app
│
├── 📂 src/                       ☕ Java backend source code
│   ├── main/java/                Main application code
│   │   └── uk/ac/sheffield/Assessment_management_tool/
│   │       ├── controller/       🌐 REST API endpoints
│   │       ├── service/          💼 Business logic layer
│   │       ├── repository/       💾 Database access layer
│   │       ├── domain/           📦 Entities and enums
│   │       ├── dto/              📨 Data transfer objects
│   │       ├── security/         🔒 Authentication & authorization
│   │       ├── exception/        ⚠️ Error handling
│   │       ├── mapper/           🔄 Entity-DTO mappers
│   │       └── scheduled/        ⏰ Background jobs
│   │
│   ├── main/resources/           Configuration files
│   │   ├── application.properties   App configuration
│   │   └── db/migration/            Database migrations
│   │       ├── V1__init.sql         Schema creation
│   │       └── V2__seed.sql         Test user data (auto-runs)
│   │
│   └── test/java/                🧪 Unit and integration tests
│
├── 📂 web/                       ⚛️ React frontend application
│   ├── src/
│   │   ├── components/           🧩 Reusable UI components
│   │   ├── pages/                📄 Application pages
│   │   ├── hooks/                🪝 Custom React hooks
│   │   ├── lib/                  🛠️ Utilities & API client
│   │   └── App.jsx               Main React component
│   │
│   ├── package.json              NPM dependencies
│   └── vite.config.js            Vite configuration
│
├── 📂 examples/                  📝 Sample CSV files for import
│   ├── users.csv                 Example user data
│   ├── modules.csv               Example module data
│   └── assessments.csv           Example assessment data
│
├── 📂 docs/                      📚 Additional documentation
│   ├── SESSION_SUMMARY.md        Development session notes
│   ├── IMPLEMENTATION_STATUS.md  Feature implementation status
│   ├── CSV_FEATURE_COMPLETE.md   CSV import documentation
│   └── ...                       Other technical docs
│
├── 📂 ops/                       🐳 Operations & deployment
│   └── docker-compose.yml        Docker configuration
│
├── 📄 pom.xml                    Maven build configuration
└── 📄 mvnw, mvnw.cmd            Maven wrapper scripts

```

## 🗂️ Backend Package Structure (Java)

```
uk.ac.sheffield.Assessment_management_tool/
│
├── 📦 controller/                 REST API Controllers
│   ├── AuthController.java       🔐 Login, logout, current user
│   ├── UserController.java       👥 User management (CRUD)
│   ├── ModuleController.java     📚 Module management
│   ├── AssessmentController.java 📝 Assessment CRUD & workflow
│   ├── CsvImportController.java  📊 Bulk CSV import
│   └── DevController.java        🛠️ Dev utilities (password hash)
│
├── 📦 service/                    Business Logic Services
│   ├── UserService.java          User operations
│   ├── ModuleService.java        Module operations
│   ├── AssessmentService.java    Assessment CRUD
│   ├── TransitionService.java    ⚙️ State machine & validation
│   └── CsvImportService.java     Bulk import processing
│
├── 📦 repository/                 Data Access Layer (Spring Data JPA)
│   ├── UserRepository.java
│   ├── ModuleRepository.java
│   ├── AssessmentRepository.java
│   ├── AssessmentRoleRepository.java
│   ├── AssessmentTransitionRepository.java
│   ├── CheckerFeedbackRepository.java
│   └── ... (11 repositories total)
│
├── 📦 domain/                     Domain Model
│   ├── entity/                   JPA Entities
│   │   ├── User.java
│   │   ├── Module.java
│   │   ├── Assessment.java
│   │   ├── AssessmentTransition.java
│   │   ├── CheckerFeedback.java
│   │   └── ... (10 entities total)
│   │
│   └── enums/                    Enumerations
│       ├── AssessmentState.java  DRAFT, READY_FOR_CHECK, etc.
│       ├── AssessmentType.java   CW, TEST, EXAM
│       ├── UserBaseType.java     ACADEMIC, TEACHING_SUPPORT, etc.
│       └── ... (5 enums total)
│
├── 📦 dto/                        Data Transfer Objects
│   ├── request/                  API Request DTOs
│   │   ├── LoginRequest.java
│   │   ├── CreateUserRequest.java
│   │   ├── CreateModuleRequest.java
│   │   └── ... (6 request DTOs)
│   │
│   └── response/                 API Response DTOs
│       ├── UserDto.java
│       ├── ModuleDto.java
│       ├── AssessmentDto.java
│       └── ... (6 response DTOs)
│
├── 📦 security/                   Security Configuration
│   ├── SecurityConfig.java       Spring Security setup
│   ├── CustomUserDetailsService  Authentication service
│   └── CustomUserDetails         User principal wrapper
│
├── 📦 exception/                  Error Handling
│   └── GlobalExceptionHandler    Centralized exception handling
│
├── 📦 mapper/                     Entity-DTO Mapping
│   └── EntityMapper.java         Manual mapping utilities
│
├── 📦 scheduled/                  Background Jobs
│   └── ExamAutoProgressScheduler Automatic state transitions
│
└── AssessmentManagementToolApplication.java  🚀 Main application class

```

## ⚛️ Frontend Structure (React)

```
web/src/
│
├── 📂 components/                 Reusable UI Components
│   ├── Layout.jsx                Page layout wrapper
│   ├── ProtectedRoute.jsx        Auth-protected routes
│   ├── DataTable.jsx             Generic data table
│   ├── LoadingSpinner.jsx        Loading indicator
│   └── ... (more components)
│
├── 📂 pages/                      Application Pages
│   ├── LoginPage.jsx             🔐 Login form
│   ├── DashboardPage.jsx         🏠 Main dashboard
│   ├── ModulesPage.jsx           📚 Module list & search
│   ├── ModuleDetailPage.jsx      Module details with assessments
│   ├── AssessmentDetailPage.jsx  📝 Assessment workflow page
│   ├── AdminPage.jsx             ⚙️ Admin controls (CSV import)
│   └── UsersPage.jsx             👥 User management
│
├── 📂 hooks/                      Custom React Hooks
│   └── useAuth.js                Authentication state management
│
├── 📂 lib/                        Utilities & API Client
│   ├── api.js                    🌐 Axios API client
│   ├── queryClient.js            React Query configuration
│   └── utils.js                  Helper functions
│
├── App.jsx                        Main app component with routing
├── main.jsx                       React entry point
└── index.css                      Global styles (Tailwind)

```

## 🗄️ Database Schema

```
📊 Tables (14 total):
├── app_user                      Users (admin, academics, examiners)
├── module                        Academic modules
├── module_staff_role             Staff assignments to modules
├── module_external_examiner      External examiners for modules
├── assessment                    Assessments (CW, TEST, EXAM)
├── assessment_role               Setter/Checker assignments
├── assessment_transition         State change audit trail
├── checker_feedback              Feedback from checkers
├── external_examiner_feedback    External examiner feedback (EXAM only)
├── setter_response               Setter's response to feedback
├── csv_import_job                Import job tracking
└── flyway_schema_history         Flyway migration tracking
```

## 🔑 Key Files to Know

| File | Purpose |
|------|---------|
| `START_HERE.md` | ⭐ **Your first stop** - setup guide |
| `README.md` | Complete technical documentation |
| `pom.xml` | Maven dependencies & build config |
| `application.properties` | Backend configuration (DB, security) |
| `SecurityConfig.java` | Authentication & authorization rules |
| `TransitionService.java` | Assessment state machine logic |
| `V2__seed.sql` | Test user accounts (auto-created) |
| `web/src/lib/api.js` | Frontend API client |
| `web/src/App.jsx` | React routing configuration |

## 🎯 Important Directories

| Directory | What's There |
|-----------|--------------|
| `src/main/java/.../controller/` | REST API endpoints - **start here** for backend |
| `web/src/pages/` | React pages - **start here** for frontend |
| `src/main/resources/db/migration/` | Database schema & seed data |
| `examples/` | Sample CSV files for testing imports |
| `docs/` | Additional technical documentation |

## 🚀 Quick Navigation

**Want to:**
- **Start the app?** → `START_HERE.md`
- **Understand the API?** → `README.md` (API Documentation section)
- **Import CSV data?** → `CSV_IMPORT_GUIDE.md`
- **Modify backend?** → `src/main/java/.../controller/` or `service/`
- **Modify frontend?** → `web/src/pages/` or `components/`
- **Change database?** → `src/main/resources/db/migration/`
- **See seed users?** → `src/main/resources/db/migration/V2__seed.sql`

---

**Navigation:** [START_HERE.md](START_HERE.md) | [README.md](README.md) | [CSV_IMPORT_GUIDE.md](CSV_IMPORT_GUIDE.md)
