## 1. Introduction

This design document details the technical architecture and implementation approach for the Excel-Integrated Project Management System. The system enables Project Managers to create and manage projects and tasks through a web interface, while team members interact with the system through Excel spreadsheets. This document builds on the project specification and provides specific technical guidance for the development team.

## 2. System Architecture Design

### 2.1 Overall Architecture

The system follows a layered architecture pattern with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────┐
│                  Presentation Layer                      │
│                                                         │
│  ┌─────────────────┐              ┌─────────────────┐   │
│  │  Web Interface  │              │  Excel Add-In   │   │
│  │  (Thymeleaf)    │              │  (Office JS)    │   │
│  └─────────────────┘              └─────────────────┘   │
└─────────────────────────────────────────────────────────┘
                         │                  │
                         ▼                  ▼
┌─────────────────────────────────────────────────────────┐
│                      API Layer                          │
│                                                         │
│  ┌─────────────────────────────────────────────────┐    │
│  │               REST Controllers                   │    │
│  └─────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────┐
│                   Service Layer                         │
│                                                         │
│  ┌───────────┐   ┌───────────┐   ┌───────────────┐      │
│  │  Project  │   │   Task    │   │  Timesheet    │      │
│  │  Service  │   │  Service  │   │   Service     │      │
│  └───────────┘   └───────────┘   └───────────────┘      │
└─────────────────────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────┐
│                  Persistence Layer                      │
│                                                         │
│  ┌───────────┐   ┌───────────┐   ┌───────────────┐      │
│  │  Project  │   │   Task    │   │  Timesheet    │      │
│  │Repository │   │Repository │   │  Repository   │      │
│  └───────────┘   └───────────┘   └───────────────┘      │
└─────────────────────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────┐
│                   Database Layer                        │
│                                                         │
│  ┌─────────────────────────────────────────┐            │
│  │              PostgreSQL                 │            │
│  └─────────────────────────────────────────┘            │
└─────────────────────────────────────────────────────────┘

```

### 2.2 Component Interactions

The system uses a request-response pattern for synchronous operations:

1. Web Interface or Excel Add-In sends HTTP requests to REST Controllers
2. Controllers delegate to appropriate Service layer components
3. Services implement business logic and use Repositories for data access
4. Repositories interact with the database through JPA/Hibernate

## 3. Detailed Component Design

### 3.1 Web Application (PM Interface)

The web application is built using Spring Boot with Thymeleaf for server-side rendering:

```kotlin
@Controller
@RequestMapping("/projects")
class ProjectController(private val projectService: ProjectService) {

    @GetMapping
    fun listProjects(model: Model): String {
        // Add projects to the model for rendering
        model.addAttribute("projects", projectService.getAllProjects())
        return "projects/list"
    }

    @GetMapping("/create")
    fun createProjectForm(model: Model): String {
        // Add empty project object to the model
        model.addAttribute("project", ProjectDto())
        return "projects/create"
    }

    @PostMapping("/create")
    fun createProject(@Valid @ModelAttribute("project") projectDto: ProjectDto,
                     bindingResult: BindingResult): String {
        if (bindingResult.hasErrors()) {
            return "projects/create"
        }

        // Create project and redirect to project list
        projectService.createProject(projectDto)
        return "redirect:/projects"
    }

    // Additional controller methods...
}

```

The controller methods map directly to view templates in the Thymeleaf structure:

```
src/main/resources/templates/
├── fragments/
│   ├── header.html
│   ├── footer.html
│   └── sidebar.html
├── projects/
│   ├── list.html
│   ├── create.html
│   ├── edit.html
│   ├── view.html
│   └── tasks.html
├── tasks/
│   ├── create.html
│   └── edit.html
└── timesheets/
    └── view.html

```

### 3.2 Excel Integration Layer

### 3.2.1 Office JS Add-In Structure

The Excel Add-In follows the Microsoft Office Add-In architecture:

```
excel-addin/
├── assets/
│   ├── logo-16.png
│   ├── logo-32.png
│   └── logo-80.png
├── src/
│   ├── taskpane/
│   │   ├── taskpane.html
│   │   ├── taskpane.css
│   │   └── taskpane.js
│   ├── auth/
│   │   ├── auth.js
│   │   └── token-storage.js
│   ├── api/
│   │   ├── api-client.js
│   │   ├── project-api.js
│   │   ├── task-api.js
│   │   └── timesheet-api.js
│   └── helpers/
│       ├── excel-utilities.js
│       └── validation.js
├── manifest.xml
└── package.json

```

The add-in provides these main capabilities:

1. **Authentication**: Handles login and token management
2. **Project Selection**: Allows users to select an active project
3. **Task Dropdowns**: Populates Excel cells with task options
4. **Data Submission**: Sends completed timesheet data to the server

Key code for populating the task dropdowns:

```jsx
/**
 * Populates task dropdown lists in designated cells
 * @param {string} projectId - The ID of the selected project
 */
async function populateTaskDropdowns(projectId) {
    try {
        // First, fetch tasks for the selected project
        const tasks = await taskApi.getTasksForProject(projectId);

        // Format tasks for dropdown (ID + Name format)
        const taskOptions = tasks.map(task => `${task.id} - ${task.name}`);

        // Now apply to Excel
        await Excel.run(async (context) => {
            // Get the active worksheet
            const sheet = context.workbook.worksheets.getActiveWorksheet();

            // Define the cells that should have task dropdowns (C5:C19)
            const taskCells = sheet.getRange("C5:C19");

            // Apply validation with dropdown list
            taskCells.dataValidation.clear();
            taskCells.dataValidation.rule = {
                list: {
                    inCellDropDown: true,
                    source: taskOptions.join(",")
                }
            };

            // Apply special formatting to these cells
            taskCells.format.fill.color = "#e6f2ff";
            taskCells.format.font.color = "#000000";

            await context.sync();
            console.log("Task dropdowns created successfully");
        });
    } catch (error) {
        console.error("Error creating task dropdowns:", error);
        showErrorNotification("Failed to load tasks. Please try again.");
    }
}

```

### 3.3 Backend Services Design

### 3.3.1 Service Layer

The service layer implements the core business logic of the application:

```kotlin
@Service
class ProjectServiceImpl(
    private val projectRepository: ProjectRepository,
    private val userRepository: UserRepository
) : ProjectService {

    @Transactional
    override fun createProject(projectDto: ProjectDto): Project {
        // Validate project data
        if (projectDto.name.isBlank()) {
            throw InvalidDataException("Project name cannot be empty")
        }

        if (projectDto.endDate != null && projectDto.startDate != null) {
            if (projectDto.endDate.isBefore(projectDto.startDate)) {
                throw InvalidDataException("End date cannot be before start date")
            }
        }

        // Create project entity
        val project = Project(
            name = projectDto.name,
            description = projectDto.description,
            startDate = projectDto.startDate,
            endDate = projectDto.endDate
        )

        // Associate project manager if provided
        projectDto.projectManagerId?.let { pmId ->
            userRepository.findById(pmId).ifPresent { pm ->
                project.projectManager = pm
            }
        }

        // Save and return the project
        return projectRepository.save(project)
    }

    // Additional service methods...
}

```

### 3.3.2 REST API Design

The REST API provides endpoints for both the web interface and Excel add-in:

```kotlin
@RestController
@RequestMapping("/api/tasks")
class TaskApiController(private val taskService: TaskService) {

    @GetMapping("/project/{projectId}")
    fun getTasksByProject(@PathVariable projectId: Long): ResponseEntity<List<TaskDto>> {
        val tasks = taskService.getTasksByProject(projectId)
        return ResponseEntity.ok(tasks)
    }

    @GetMapping("/{id}")
    fun getTask(@PathVariable id: Long): ResponseEntity<TaskDto> {
        return taskService.getTaskById(id)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
    }

    @PostMapping
    fun createTask(@Valid @RequestBody taskDto: TaskCreateDto): ResponseEntity<TaskDto> {
        val created = taskService.createTask(taskDto)
        return ResponseEntity
            .created(URI.create("/api/tasks/${created.id}"))
            .body(created)
    }

    @PutMapping("/{id}")
    fun updateTask(
        @PathVariable id: Long,
        @Valid @RequestBody taskDto: TaskUpdateDto
    ): ResponseEntity<TaskDto> {
        return taskService.updateTask(id, taskDto)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
    }

    @DeleteMapping("/{id}")
    fun deleteTask(@PathVariable id: Long): ResponseEntity<Void> {
        val deleted = taskService.deleteTask(id)
        return if (deleted) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
}

```

### 3.3.3 API Specifications

The system exposes the following key APIs:

### Project API

| Endpoint | Method | Description | Request | Response |
| --- | --- | --- | --- | --- |
| /api/projects | GET | Get all projects | - | Array of ProjectDto |
| /api/projects | POST | Create new project | ProjectCreateDto | ProjectDto |
| /api/projects/{id} | GET | Get project by ID | - | ProjectDto |
| /api/projects/{id} | PUT | Update project | ProjectUpdateDto | ProjectDto |
| /api/projects/{id} | DELETE | Delete project | - | 204 No Content |

### Task API

| Endpoint | Method | Description | Request | Response |
| --- | --- | --- | --- | --- |
| /api/tasks/project/{projectId} | GET | Get tasks for project | - | Array of TaskDto |
| /api/tasks | POST | Create new task | TaskCreateDto | TaskDto |
| /api/tasks/{id} | GET | Get task by ID | - | TaskDto |
| /api/tasks/{id} | PUT | Update task | TaskUpdateDto | TaskDto |
| /api/tasks/{id} | DELETE | Delete task | - | 204 No Content |

### Timesheet API

| Endpoint | Method | Description | Request | Response |
| --- | --- | --- | --- | --- |
| /api/timesheets | POST | Submit timesheet entry | TimesheetEntryDto | TimesheetEntryDto |
| /api/timesheets/project/{projectId} | GET | Get timesheet entries for project | - | Array of TimesheetEntryDto |
| /api/timesheets/task/{taskId} | GET | Get timesheet entries for task | - | Array of TimesheetEntryDto |

## 4. Data Model Design

### 4.1 Entity Relationship Diagram

```
┌───────────────┐       ┌───────────────┐       ┌───────────────┐
│     User      │       │    Project    │       │     Task      │
├───────────────┤       ├───────────────┤       ├───────────────┤
│ id            │       │ id            │       │ id            │
│ username      │◄──────┤ projectManagerId    │◄──────┤ projectId     │
│ password      │       │ name          │       │ name          │
│ email         │       │ description   │       │ description   │
│ role          │       │ startDate     │       │ estimatedHours│
│ createdAt     │       │ endDate       │       │ createdAt     │
└───────────────┘       │ createdAt     │       └───────────────┘
                        └───────────────┘              │
                                                       │
                                                       ▼
                                              ┌───────────────┐
                                              │  Timesheet    │
                                              │    Entry      │
                                              ├───────────────┤
                                              │ id            │
                                              │ taskId        │
                                              │ employeeName  │
                                              │ hoursWorked   │
                                              │ workDate      │
                                              │ submittedAt   │
                                              │ notes         │
                                              └───────────────┘

```

### 4.2 Database Schema Definition

Here's the complete database schema definition:

```sql
-- Users table for Project Managers
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Projects table
CREATE TABLE projects (
    id SERIAL PRIMARY KEY,
    project_manager_id INTEGER REFERENCES users(id),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    start_date DATE,
    end_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Tasks table
CREATE TABLE tasks (
    id SERIAL PRIMARY KEY,
    project_id INTEGER NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    estimated_hours NUMERIC(6,2),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Timesheet entries table
CREATE TABLE timesheet_entries (
    id SERIAL PRIMARY KEY,
    task_id INTEGER NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    employee_name VARCHAR(100) NOT NULL,
    hours_worked NUMERIC(5,2) NOT NULL CHECK (hours_worked > 0),
    work_date DATE NOT NULL,
    submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notes TEXT
);

-- Indexes for performance
CREATE INDEX idx_tasks_project_id ON tasks(project_id);
CREATE INDEX idx_timesheet_entries_task_id ON timesheet_entries(task_id);
CREATE INDEX idx_timesheet_entries_work_date ON timesheet_entries(work_date);

```

### 4.3 Entity Classes

The Java entity classes with JPA annotations:

```java
@Entity
@Table(name = "projects")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_manager_id")
    private User projectManager;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasks = new ArrayList<>();

    // Getters, setters, and utility methods
    public void addTask(Task task) {
        tasks.add(task);
        task.setProject(this);
    }

    public void removeTask(Task task) {
        tasks.remove(task);
        task.setProject(null);
    }
}

```

For comparison, the same entity in Kotlin:

```kotlin
@Entity
@Table(name = "projects")
class Project(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var name: String,

    @Column
    var description: String? = null,

    @Column(name = "start_date")
    var startDate: LocalDate? = null,

    @Column(name = "end_date")
    var endDate: LocalDate? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_manager_id")
    var projectManager: User? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @OneToMany(mappedBy = "project", cascade = [CascadeType.ALL], orphanRemoval = true)
    val tasks: MutableList<Task> = mutableListOf()
) {
    // Utility methods
    fun addTask(task: Task) {
        tasks.add(task)
        task.project = this
    }

    fun removeTask(task: Task) {
        tasks.remove(task)
        task.project = null
    }
}

```

### 4.4 DTO (Data Transfer Object) Classes

DTOs provide a clean separation between the internal entity model and the external API:

```kotlin
data class ProjectDto(
    val id: Long? = null,
    val name: String,
    val description: String? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val projectManagerId: Long? = null,
    val taskCount: Int = 0,
    val createdAt: LocalDateTime? = null
)

data class TaskDto(
    val id: Long? = null,
    val projectId: Long,
    val name: String,
    val description: String? = null,
    val estimatedHours: Double? = null,
    val createdAt: LocalDateTime? = null
)

data class TimesheetEntryDto(
    val id: Long? = null,
    val taskId: Long,
    val employeeName: String,
    val hoursWorked: Double,
    val workDate: LocalDate,
    val notes: String? = null,
    val submittedAt: LocalDateTime? = null
)

```

## 5. User Interface Design

### 5.1 Web Interface Wireframes

The web interface follows a clean, responsive design with these key screens:

1. **Project List Screen**
  - Grid of project cards with key metrics
  - Create project button
  - Search and filter options
2. **Project Detail Screen**
  - Project information section
  - Task list with progress indicators
  - Add task button
  - Timesheet submission summary
3. **Task Management Screen**
  - Form for adding/editing tasks
  - Task properties (name, description, estimated hours)
  - Task assignment options

### 5.2 Excel Template Design

The Excel template is designed for clarity and ease of use:

```
┌───────────────────────────────────────────────────────────────┐
│ Project Timesheet                                     Week: 25 │
├───────────────────────────────────────────────────────────────┤
│ Employee Name: _________________                               │
│ Project: [DROPDOWN - Populated from API]                       │
├───────┬───────────────────────┬───────┬───────┬───────────────┤
│ Date  │ Task                  │ Hours │ Notes │ Status        │
├───────┼───────────────────────┼───────┼───────┼───────────────┤
│ Mon   │ [DROPDOWN - Tasks]    │       │       │ Not Submitted │
├───────┼───────────────────────┼───────┼───────┼───────────────┤
│ Tue   │ [DROPDOWN - Tasks]    │       │       │ Not Submitted │
├───────┼───────────────────────┼───────┼───────┼───────────────┤
│ Wed   │ [DROPDOWN - Tasks]    │       │       │ Not Submitted │
├───────┼───────────────────────┼───────┼───────┼───────────────┤
│ Thu   │ [DROPDOWN - Tasks]    │       │       │ Not Submitted │
├───────┼───────────────────────┼───────┼───────┼───────────────┤
│ Fri   │ [DROPDOWN - Tasks]    │       │       │ Not Submitted │
├───────┼───────────────────────┼───────┼───────┼───────────────┤
│                           Total Hours: [SUM]                   │
└───────────────────────────────────────────────────────────────┘

```

## 6. Security Design

### 6.1 Authentication and Authorization

The system implements Spring Security with JWT tokens:

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtTokenFilter jwtTokenFilter;

    public SecurityConfig(UserDetailsService userDetailsService, JwtTokenFilter jwtTokenFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtTokenFilter = jwtTokenFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
                // Public endpoints
                .antMatchers("/api/auth/**").permitAll()
                .antMatchers("/api/excel-auth/**").permitAll()
                // Protected endpoints
                .antMatchers("/api/projects/**").hasRole("PROJECT_MANAGER")
                .antMatchers("/api/tasks/**").hasRole("PROJECT_MANAGER")
                .antMatchers("/api/timesheets/submit").permitAll() // Excel submissions
                .antMatchers("/api/timesheets/**").hasRole("PROJECT_MANAGER")
                .anyRequest().authenticated()
            .and()
            .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}

```

### 6.2 JWT Token Management

For Excel integration, a simplified token approach is used:

```kotlin
@Service
class JwtTokenService {

    @Value("\\${app.jwt.secret}")
    private lateinit var jwtSecret: String

    @Value("\\${app.jwt.expiration-ms}")
    private var jwtExpirationMs: Long = 0

    @Value("\\${app.jwt.excel-expiration-ms}")
    private var jwtExcelExpirationMs: Long = 0

    fun generateToken(authentication: Authentication): String {
        val userPrincipal = authentication.principal as UserDetailsImpl
        return createToken(userPrincipal.username, userPrincipal.authorities, jwtExpirationMs)
    }

    fun generateExcelToken(projectId: Long): String {
        // Generate a special token for Excel with project scope
        return Jwts.builder()
            .setSubject("excel-client")
            .claim("projectId", projectId)
            .claim("type", "excel")
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + jwtExcelExpirationMs))
            .signWith(Keys.hmacShaKeyFor(jwtSecret.toByteArray()), SignatureAlgorithm.HS512)
            .compact()
    }

    fun validateToken(token: String): Boolean {
        try {
            Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.toByteArray()))
                .build()
                .parseClaimsJws(token)
            return true
        } catch (e: Exception) {
            return false
        }
    }

    // Private helper methods
    private fun createToken(
        username: String,
        authorities: Collection<GrantedAuthority>,
        expirationMs: Long
    ): String {
        val claims = Jwts.claims().setSubject(username)
        claims["roles"] = authorities.map { it.authority }

        val now = Date()
        val expiration = Date(now.time + expirationMs)

        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(expiration)
            .signWith(Keys.hmacShaKeyFor(jwtSecret.toByteArray()), SignatureAlgorithm.HS512)
            .compact()
    }
}

```

### 6.3 Excel Add-In Authentication Flow

The Excel add-in uses a simplified authentication approach:

1. **Project Selection**: User selects a project in the web interface
2. **Token Generation**: System generates a project-specific token
3. **Token Sharing**: Token is displayed or can be copied to clipboard
4. **Excel Authentication**: User enters token in Excel add-in
5. **Token Storage**: Token is securely stored in Excel document settings
6. **API Access**: Token is used for all API calls from Excel

```jsx
// Excel Add-In Authentication Module
const authModule = (function() {
    let currentToken = null;

    /**
     * Authenticate with the application server using a project token
     * @param {string} token - The project-specific JWT token
     * @returns {Promise<boolean>} - Whether authentication was successful
     */
    async function authenticate(token) {
        try {
            // Validate the token with the server
            const response = await fetch(`${API_BASE_URL}/api/excel-auth/validate`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ token })
            });

            if (!response.ok) {
                throw new Error('Invalid token');
            }

            // Store the token
            currentToken = token;
            await storeTokenInSettings(token);

            return true;
        } catch (error) {
            console.error('Authentication error:', error);
            return false;
        }
    }

    /**
     * Store the token in Excel document settings
     * @param {string} token - The JWT token to store
     */
    async function storeTokenInSettings(token) {
        await Office.context.document.settings.set('authToken', token);
        await Office.context.document.settings.saveAsync();
    }

    /**
     * Load a previously stored token from Excel document settings
     * @returns {Promise<string|null>} - The stored token or null if not found
     */
    async function loadStoredToken() {
        return new Promise((resolve) => {
            const token = Office.context.document.settings.get('authToken');
            currentToken = token || null;
            resolve(currentToken);
        });
    }

    /**
     * Get the current token for API requests
     * @returns {string|null} - The current JWT token
     */
    function getToken() {
        return currentToken;
    }

    /**
     * Clear the stored authentication token
     */
    async function logout() {
        currentToken = null;
        await Office.context.document.settings.remove('authToken');
        await Office.context.document.settings.saveAsync();
    }

    // Public API
    return {
        authenticate,
        getToken,
        loadStoredToken,
        logout
    };
})();

// Export the module
export default authModule;

```

## 7. Implementation Details

### 7.1 Spring Boot Application Configuration

### 7.1.1 Application Properties

```
# Server configuration
server.port=8080
server.servlet.context-path=/

# Database configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/timesheet_db
spring.datasource.username=postgres
spring.datasource.password=password
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA/Hibernate configuration
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.show-sql=false

# Flyway migration
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true

# JWT configuration
app.jwt.secret=your-secret-key-should-be-at-least-256-bits-long
app.jwt.expiration-ms=86400000
app.jwt.excel-expiration-ms=604800000

# CORS configuration
app.cors.allowed-origins=http://localhost:3000,<https://your-production-domain.com>

# Logging
logging.level.org.springframework.web=INFO
logging.level.com.yourcompany.timesheet=DEBUG

```

### 7.1.2 Database Migration with Flyway

Using Flyway for versioned database migrations:

```
src/main/resources/db/migration/
├── V1__create_base_schema.sql
├── V2__add_task_status_column.sql
└── V3__add_indexes.sql

```

Example migration script:

```sql
-- V1__create_base_schema.sql
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- More table definitions...

```

### 7.2 Repository Layer Implementation

Using Spring Data JPA for data access:

```kotlin
interface ProjectRepository : JpaRepository<Project, Long> {
    fun findByProjectManagerId(projectManagerId: Long): List<Project>

    fun findByNameContainingIgnoreCase(name: String): List<Project>

    @Query("SELECT p FROM Project p WHERE p.startDate <= :date AND (p.endDate IS NULL OR p.endDate >= :date)")
    fun findActiveProjects(@Param("date") date: LocalDate): List<Project>
}

interface TaskRepository : JpaRepository<Task, Long> {
    fun findByProjectId(projectId: Long): List<Task>

    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id = :projectId")
    fun countTasksByProjectId(@Param("projectId") projectId: Long): Long
}

interface TimesheetEntryRepository : JpaRepository<TimesheetEntry, Long> {
    fun findByTaskId(taskId: Long): List<TimesheetEntry>

    fun findByTaskProjectId(projectId: Long): List<TimesheetEntry>

    fun findByWorkDateBetween(startDate: LocalDate, endDate: LocalDate): List<TimesheetEntry>

    @Query("SELECT NEW com.yourcompany.timesheet.dto.TaskHoursDto(t.id, t.name, SUM(te.hoursWorked)) " +
           "FROM Task t JOIN t.timesheetEntries te " +
           "WHERE t.project.id = :projectId " +
           "GROUP BY t.id, t.name")
    fun getTaskHoursByProject(@Param("projectId") projectId: Long): List<TaskHoursDto>
}

```

### 7.3 Service Layer Implementation

The Task Service with business logic implementation:

```kotlin
@Service
class TaskServiceImpl(
    private val taskRepository: TaskRepository,
    private val projectRepository: ProjectRepository
) : TaskService {

    override fun getTasksByProject(projectId: Long): List<TaskDto> {
        return taskRepository.findByProjectId(projectId)
            .map { it.toDto() }
    }

    override fun getTaskById(id: Long): TaskDto? {
        return taskRepository.findById(id)
            .map { it.toDto() }
            .orElse(null)
    }

    @Transactional
    override fun createTask(taskDto: TaskCreateDto): TaskDto {
        // Find the project
        val project = projectRepository.findById(taskDto.projectId)
            .orElseThrow { ResourceNotFoundException("Project not found with id: ${taskDto.projectId}") }

        // Create and save the task
        val task = Task(
            project = project,
            name = taskDto.name,
            description = taskDto.description,
            estimatedHours = taskDto.estimatedHours
        )

        val savedTask = taskRepository.save(task)
        return savedTask.toDto()
    }

    @Transactional
    override fun updateTask(id: Long, taskDto: TaskUpdateDto): TaskDto? {
        return taskRepository.findById(id).map { task ->
            // Update task properties
            task.name = taskDto.name
            task.description = taskDto.description
            task.estimatedHours = taskDto.estimatedHours

            // Save and convert to DTO
            taskRepository.save(task).toDto()
        }.orElse(null)
    }

    @Transactional
    override fun deleteTask(id: Long): Boolean {
        return if (taskRepository.existsById(id)) {
            taskRepository.deleteById(id)
            true
        } else {
            false
        }
    }

    override fun getTasksForDropdown(projectId: Long): List<String> {
        return getTasksByProject(projectId)
            .map { "${it.id} - ${it.name}" }
    }
}

```

### 7.4 Excel Add-In Implementation

The task submission functionality in the Excel add-in:

```jsx
/**
 * Collects timesheet data from the Excel sheet and submits to the server
 */
async function submitTimesheetData() {
    try {
        const token = authModule.getToken();
        if (!token) {
            showErrorNotification("Please authenticate first");
            return;
        }

        await Excel.run(async (context) => {
            // Get the active worksheet
            const sheet = context.workbook.worksheets.getActiveWorksheet();

            // Load employee name
            const employeeNameRange = sheet.getRange("B3");
            employeeNameRange.load("values");

            // Load date range
            const dateRanges = sheet.getRange("A5:A9");
            dateRanges.load("values");

            // Load task selections
            const taskRanges = sheet.getRange("C5:C9");
            taskRanges.load("values");

            // Load hours worked
            const hoursRanges = sheet.getRange("D5:D9");
            hoursRanges.load("values");

            // Load notes
            const notesRanges = sheet.getRange("E5:E9");
            notesRanges.load("values");

            await context.sync();

            // Get employee name
            const employeeName = employeeNameRange.values[0][0];
            if (!employeeName) {
                throw new Error("Employee name is required");
            }

            // Prepare timesheet entries
            const entries = [];

            for (let i = 0; i < 5; i++) {
                // Extract day of week and convert to date
                const dayOfWeek = dateRanges.values[i][0];
                const hours = hoursRanges.values[i][0];
                const taskCell = taskRanges.values[i][0];
                const notes = notesRanges.values[i][0];

                // Skip empty rows
                if (!taskCell || !hours) {
                    continue;
                }

                // Extract task ID from the format "123 - Task Name"
                const taskId = parseInt(taskCell.split(' - ')[0], 10);
                if (isNaN(taskId)) {
                    throw new Error(`Invalid task format in row ${i + 5}`);
                }

                // Calculate the actual date based on day of week
                const workDate = calculateDateFromDayOfWeek(dayOfWeek);

                entries.push({
                    taskId,
                    employeeName,
                    hoursWorked: parseFloat(hours),
                    workDate: workDate.toISOString().split('T')[0], // YYYY-MM-DD format
                    notes: notes || null
                });
            }

            // Submit entries to the server
            if (entries.length > 0) {
                const response = await fetch(`${API_BASE_URL}/api/timesheets/batch`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${token}`
                    },
                    body: JSON.stringify(entries)
                });

                if (!response.ok) {
                    throw new Error(`Server error: ${response.status}`);
                }

                // Update status cells
                const statusRanges = sheet.getRange("F5:F9");
                const statusValues = [];

                for (let i = 0; i < 5; i++) {
                    if (i < entries.length) {
                        statusValues.push(["Submitted"]);
                    } else {
                        statusValues.push(["Not Submitted"]);
                    }
                }

                statusRanges.values = statusValues;
                statusRanges.format.font.color = "#007700";

                await context.sync();
                showSuccessNotification(`${entries.length} timesheet entries submitted`);
            } else {
                showWarningNotification("No timesheet entries to submit");
            }
        });
    } catch (error) {
        console.error("Error submitting timesheet:", error);
        showErrorNotification(`Submission failed: ${error.message}`);
    }
}

/**
 * Helper function to calculate date from day of week
 * @param {string} dayOfWeek - The day of week (e.g., "Mon", "Tue")
 * @returns {Date} - The date object for that day in the current week
 */
function calculateDateFromDayOfWeek(dayOfWeek) {
    const days = { "Mon": 1, "Tue": 2, "Wed": 3, "Thu": 4, "Fri": 5, "Sat": 6, "Sun": 0 };
    const dayNumber = days[dayOfWeek];

    if (dayNumber === undefined) {
        throw new Error(`Invalid day of week: ${dayOfWeek}`);
    }

    const today = new Date();
    const currentDayNumber = today.getDay(); // 0 = Sunday, 1 = Monday, etc.
    const diff = dayNumber - currentDayNumber;

    // Calculate the date for the specified day in the current week
    const targetDate = new Date(today);
    targetDate.setDate(today.getDate() + diff + (diff < 0 ? 7 : 0));

    return targetDate;
}

```

## 8. Testing Strategy

### 8.1 Unit Testing

Using JUnit 5 and Mockito for unit tests:

```kotlin
@ExtendWith(MockitoExtension::class)
class TaskServiceTest {

    @Mock
    private lateinit var taskRepository: TaskRepository

    @Mock
    private lateinit var projectRepository: ProjectRepository

    @InjectMocks
    private lateinit var taskService: TaskServiceImpl

    @Test
    fun `should get tasks by project id`() {
        // Given
        val projectId = 1L
        val task1 = createTask(1L, "Task 1", projectId)
        val task2 = createTask(2L, "Task 2", projectId)
        val tasks = listOf(task1, task2)

        // When
        whenever(taskRepository.findByProjectId(projectId)).thenReturn(tasks)
        val result = taskService.getTasksByProject(projectId)

        // Then
        assertEquals(2, result.size)
        assertEquals("Task 1", result[0].name)
        assertEquals("Task 2", result[1].name)

        verify(taskRepository).findByProjectId(projectId)
    }

    @Test
    fun `should create task successfully`() {
        // Given
        val projectId = 1L
        val project = Project(id = projectId, name = "Test Project")
        val taskDto = TaskCreateDto(
            name = "New Task",
            description = "Task description",
            projectId = projectId,
            estimatedHours = 8.0
        )

        // When
        whenever(projectRepository.findById(projectId)).thenReturn(Optional.of(project))
        whenever(taskRepository.save(any<Task>())).thenAnswer { invocation ->
            val savedTask = invocation.getArgument<Task>(0)
            savedTask.id = 3L
            savedTask
        }

        val result = taskService.createTask(taskDto)

        // Then
        assertEquals("New Task", result.name)
        assertEquals("Task description", result.description)
        assertEquals(projectId, result.projectId)
        assertEquals(8.0, result.estimatedHours)

        verify(projectRepository).findById(projectId)
        verify(taskRepository).save(any<Task>())
    }

    @Test
    fun `should throw exception when project not found`() {
        // Given
        val projectId = 999L
        val taskDto = TaskCreateDto(
            name = "New Task",
            description = "Task description",
            projectId = projectId,
            estimatedHours = 8.0
        )

        // When
        whenever(projectRepository.findById(projectId)).thenReturn(Optional.empty())

        // Then
        assertThrows<ResourceNotFoundException> {
            taskService.createTask(taskDto)
        }

        verify(projectRepository).findById(projectId)
        verify(taskRepository, never()).save(any<Task>())
    }

    // Helper methods
    private fun createTask(id: Long, name: String, projectId: Long): Task {
        val project = Project(id = projectId, name = "Test Project")
        return Task(
            id = id,
            name = name,
            project = project,
            description = "Description for $name",
            estimatedHours = 4.0
        )
    }
}

```

### 8.2 Integration Testing

Using Spring Boot Test for integration tests:

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProjectControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenService jwtTokenService;

    private User testUser;
    private String authToken;

    @BeforeAll
    public void setup() {
        // Create test user
        testUser = new User();
        testUser.setUsername("test-pm");
        testUser.setPassword(new BCryptPasswordEncoder().encode("password"));
        testUser.setEmail("test-pm@example.com");
        testUser.setRole("PROJECT_MANAGER");

        userRepository.save(testUser);

        // Create auth token
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        new UserDetailsImpl(testUser),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_PROJECT_MANAGER"))
                );

        authToken = jwtTokenService.generateToken(authentication);
    }

    @AfterAll
    public void cleanup() {
        projectRepository.deleteAll();
        userRepository.delete(testUser);
    }

    @Test
    public void testCreateProject() throws Exception {
        // Create project DTO
        ProjectDto projectDto = new ProjectDto();
        projectDto.setName("Test Project");
        projectDto.setDescription("Project for testing");
        projectDto.setStartDate(LocalDate.now());
        projectDto.setEndDate(LocalDate.now().plusMonths(1));

        // Perform request
        mockMvc.perform(post("/api/projects")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Project"))
                .andExpect(jsonPath("$.description").value("Project for testing"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    public void testGetProjects() throws Exception {
        // Create test project
        Project project = new Project();
        project.setName("Another Test Project");
        project.setDescription("For testing GET endpoint");
        project.setProjectManager(testUser);
        projectRepository.save(project);

        // Perform request
        mockMvc.perform(get("/api/projects")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].name", hasItem("Another Test Project")));
    }
}

```

### 8.3 Excel Add-In Testing

Example Jest test for Excel add-in functionality:

```jsx
// Mock the Office.js API
global.Office = {
    context: {
        document: {
            settings: {
                get: jest.fn(),
                set: jest.fn(),
                saveAsync: jest.fn((callback) => callback()),
                remove: jest.fn()
            }
        }
    }
};

global.Excel = {
    run: jest.fn((callback) => {
        const context = {
            sync: jest.fn().mockResolvedValue(undefined),
            workbook: {
                worksheets: {
                    getActiveWorksheet: jest.fn().mockReturnValue({
                        getRange: jest.fn().mockReturnValue({
                            load: jest.fn(),
                            values: [["Test Value"]],
                            dataValidation: {
                                rule: {},
                                clear: jest.fn()
                            },
                            format: {
                                fill: { color: "" },
                                font: { color: "" }
                            }
                        })
                    })
                }
            }
        };
        return callback(context);
    })
};

// Import the module to test
import authModule from '../src/auth/auth';

// Mock fetch API
global.fetch = jest.fn(() =>
    Promise.resolve({
        ok: true,
        json: () => Promise.resolve({ success: true })
    })
);

describe('Authentication Module', () => {
    beforeEach(() => {
        jest.clearAllMocks();
    });

    test('authenticate should store token when successful', async () => {
        // Arrange
        const testToken = 'test-token-123';

        // Act
        const result = await authModule.authenticate(testToken);

        // Assert
        expect(result).toBe(true);
        expect(global.Office.context.document.settings.set).toHaveBeenCalledWith('authToken', testToken);
        expect(global.Office.context.document.settings.saveAsync).toHaveBeenCalled();
        expect(global.fetch).toHaveBeenCalledWith(
            expect.stringContaining('/api/excel-auth/validate'),
            expect.objectContaining({
                method: 'POST',
                body: JSON.stringify({ token: testToken })
            })
        );
    });

    test('getToken should return the current token', async () => {
        // Arrange
        const testToken = 'test-token-123';
        await authModule.authenticate(testToken);

        // Act
        const token = authModule.getToken();

        // Assert
        expect(token).toBe(testToken);
    });

    test('logout should clear the token', async () => {
        // Arrange
        const testToken = 'test-token-123';
        await authModule.authenticate(testToken);

        // Act
        await authModule.logout();

        // Assert
        expect(authModule.getToken()).toBeNull();
        expect(global.Office.context.document.settings.remove).toHaveBeenCalledWith('authToken');
        expect(global.Office.context.document.settings.saveAsync).toHaveBeenCalled();
    });

    test('loadStoredToken should load token from settings', async () => {
        // Arrange
        const testToken = 'stored-token-456';
        global.Office.context.document.settings.get.mockReturnValue(testToken);

        // Act
        const token = await authModule.loadStoredToken();

        // Assert
        expect(token).toBe(testToken);
        expect(global.Office.context.document.settings.get).toHaveBeenCalledWith('authToken');
        expect(authModule.getToken()).toBe(testToken);
    });
});

```

## 9. Deployment Considerations

### 9.1 CI/CD Pipeline with GitHub Actions

```yaml
name: Build and Deploy

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest

    services:
      postgres:
        image: postgres:14
        env:
          POSTGRES_USER: postgres
          POSTGRES_PASSWORD: postgres
          POSTGRES_DB: timesheet_test
        ports:
          - 5432:5432
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5

    steps:
    - uses: actions/checkout@v3

    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
        cache: gradle

    - name: Grant execute permission for gradlew
      run: chmod +x gradlew

    - name: Build with Gradle
      run: ./gradlew build

    - name: Run tests
      run: ./gradlew test

    - name: Build Excel Add-In
      working-directory: ./excel-addin
      run: |
        npm install
        npm run build

    - name: Run Excel Add-In tests
      working-directory: ./excel-addin
      run: npm test

    - name: Build Docker image
      run: ./gradlew bootBuildImage --imageName=timesheet-app:latest

    - name: Save Docker image
      run: docker save timesheet-app:latest > timesheet-app.tar

    - name: Upload Docker image
      uses: actions/upload-artifact@v3
      with:
        name: docker-image
        path: timesheet-app.tar

```

### 9.2 Docker Compose Configuration

```yaml
version: '3.8'

services:
  app:
    image: timesheet-app:latest
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/timesheet_db
      - SPRING_DATASOURCE_USERNAME=postgres
      - SPRING_DATASOURCE_PASSWORD=postgres
    depends_on:
      - db

  db:
    image: postgres:14
    ports:
      - "5432:5432"
    environment:
      - POSTGRES_DB=timesheet_db
      - POSTGRES_USER=postgres
      - POSTGRES_PASSWORD=postgres
    volumes:
      - postgres_data:/var/lib/postgresql/data

  nginx:
    image: nginx:1.21
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/conf.d:/etc/nginx/conf.d
      - ./nginx/ssl:/etc/nginx/ssl
    depends_on:
      - app

volumes:
  postgres_data:

```

## 10. Conclusion

This design document provides a comprehensive blueprint for implementing the Excel-Integrated Project Management System. By following this design, the development team will be able to create a system that effectively bridges the gap between project management needs and the familiar Excel interface for timesheet reporting.

Key aspects of the design include:

1. A clean separation of concerns with well-defined layers
2. Secure authentication for both web and Excel interfaces
3. Seamless integration between Excel and the backend using Office JS
4. Efficient data storage and retrieval with optimized queries
5. Comprehensive testing strategy to ensure robustness

The implementation can begin with the core backend services, followed by the web interface and Excel integration components. By adopting a phased approach, the team can deliver incremental value while ensuring high quality throughout the development process.