# Quick Start Guide - Assessment Management Tool

## 🎯 What I've Built So Far

I've created a solid **foundation** for your Assessment Management Tool. Here's what's ready:

### ✅ Complete Components

1. **Project Infrastructure**
   - Maven project with all necessary dependencies
   - Spring Boot 3.5.7 with Java 21
   - PostgreSQL database integration
   - Flyway migration system

2. **Data Layer (100% Complete)**
   - 11 JPA entities covering the entire domain model
   - 6 enums for type safety
   - 11 Spring Data repositories with custom queries
   - Complete database schema with constraints and indexes

3. **Database Migrations**
   - V1__init.sql: Full schema with all tables
   - V2__seed.sql: Test data with 6 users, 1 module, 3 assessments

4. **DevOps Setup**
   - Docker Compose configuration
   - Dockerfile for containerization
   - Environment configuration templates

5. **Documentation**
   - Comprehensive README
   - Project structure documentation
   - Development roadmap

## 🚀 Getting Started

### Option 1: Docker (Easiest)

```powershell
# Navigate to your project
cd c:\Users\marvo\Desktop\Assessment_management_tool
#do
npm i 
#run
./start-app.ps1 


## 📊 Verify It's Working

Once the application starts, you should see:
- Flyway migrations running automatically
- "Started AssessmentManagementToolApplication" message
- Server running on port 8080

Visit: http://localhost:8080/swagger-ui.html (once controllers are implemented)

## 🧪 Test Database Access

You can verify the seed data was loaded:

```powershell
# Connect to database
psql -U assessment_user -d assessment_db -h localhost

# Check users
SELECT name, email, base_type FROM app_user;

# Check modules
SELECT code, title, academic_year FROM module;

# Check assessments
SELECT title, type, current_state FROM assessment;

# Exit
\q
```

## 📝 What's Next?

To complete the system, you need to implement (in recommended order):

### Priority 1: Make it Functional (Backend API)
1. **Security Layer**
   - Spring Security configuration
   - UserDetailsService implementation
   - Session management
   - Password encoding

2. **Core Services**
   - TransitionService (state machine logic)
   - ValidationService (independence checks)
   - UserService, ModuleService, AssessmentService

3. **DTOs and Mappers**
   - Create DTOs for API requests/responses
   - MapStruct mappers or manual converters

4. **REST Controllers**
   - Authentication endpoints
   - User management
   - Module management
   - Assessment operations
   - Transitions and feedback

### Priority 2: Advanced Features
5. **Scheduled Jobs**
   - Auto-transition for EXAM_TAKEN state
   - Working day calculator

6. **CSV Import**
   - CSV parser
   - Validation logic
   - Bulk import endpoints

### Priority 3: Frontend
7. **React Setup**
   - Initialize Vite project
   - Configure routing and state management
   - Set up Tailwind CSS

8. **UI Components**
   - Authentication pages
   - Dashboard
   - Module and Assessment views
   - Admin panels

### Priority 4: Testing & Deployment
9. **Tests**
   - Unit tests
   - Integration tests with Testcontainers
   - E2E tests with Playwright

10. **Deployment**
    - Docker Compose with all services
    - Production configuration
    - CI/CD pipelines

## 💡 Development Tips

### Hot Reload
The project includes Spring Boot DevTools. Changes to Java classes will trigger automatic restart.

### Database Migrations
To reset the database:
```powershell
./mvnw flyway:clean flyway:migrate
```

### View Logs
```powershell
# Logs are in console output
# To save to file:
./mvnw spring-boot:run > app.log 2>&1
```

### Code Quality
Run before committing:
```powershell
# Compile and run tests
./mvnw clean verify

# Check code coverage (opens in browser)
start target/site/jacoco/index.html
```

## 🔑 Seed User Credentials

Use these to test when authentication is implemented:

| User | Email | Password | Role | Special |
|------|-------|----------|------|---------|
| Admin | admin@sheffield.ac.uk | password123 | Teaching Support | Can override |
| Dr. Alice Anderson | alice.anderson@sheffield.ac.uk | password123 | Academic | Module Lead |
| Dr. Bob Brown | bob.brown@sheffield.ac.uk | password123 | Academic | Setter |
| Dr. Carol Chen | carol.chen@sheffield.ac.uk | password123 | Academic | Checker |
| Dr. David Davies | david.davies@sheffield.ac.uk | password123 | Academic | Exams Officer |
| Prof. Emma Edwards | emma.edwards@external.ac.uk | password123 | External Examiner | - |

## 📂 Project Structure

```
Assessment_management_tool/
├── src/main/java/.../
│   ├── domain/
│   │   ├── entity/          ✅ Complete (11 entities)
│   │   └── enums/           ✅ Complete (6 enums)
│   ├── repository/          ✅ Complete (11 repositories)
│   ├── service/             ⏳ To implement
│   ├── controller/          ⏳ To implement
│   ├── dto/                 ⏳ To implement
│   ├── security/            ⏳ To implement
│   └── config/              ⏳ To implement
├── src/main/resources/
│   ├── db/migration/        ✅ Complete (2 scripts)
│   └── application.properties ✅ Configured
├── ops/                     ✅ Docker setup ready
├── README.md                ✅ Comprehensive docs
└── pom.xml                  ✅ All dependencies
```

## 🆘 Troubleshooting

### "Cannot connect to database"
```powershell
# Check if PostgreSQL is running
docker ps  # Should show assessment-db

# Or if using local PostgreSQL:
pg_isready -U assessment_user
```

### "Table does not exist"
```powershell
# Flyway might not have run
./mvnw flyway:info  # Check migration status
./mvnw flyway:migrate  # Run migrations manually
```

### "Port 8080 already in use"
```powershell
# Find and kill the process
netstat -ano | findstr :8080
taskkill /PID <process_id> /F

# Or change port in application.properties
server.port=8081
```

## 📞 Next Steps

You have a **solid foundation**. The data layer is complete and production-ready. 

**Recommended next action**: Start implementing the **Security Configuration** (Todo #5) to enable authentication, then build the **Services Layer** (Todo #6) for business logic.

Would you like me to continue implementing these components? I can:
1. Create the Spring Security configuration
2. Build the TransitionService with state machine logic
3. Implement DTOs and the first REST controllers
4. Add comprehensive tests

Let me know how you'd like to proceed!
