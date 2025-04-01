# Task Management Application

A Java console application demonstrating n-tier architecture with a task management system.

## Architecture

This application implements a clean n-tier architecture with isolated models for each layer:

1. **Presentation Layer**: 
   - Uses DTOs (Data Transfer Objects) for data exchange with the UI
   - Converts between DTOs and Service Models using mappers
   - Represented by TaskDTO objects

2. **Business Logic Layer**: 
   - Service layer that implements business rules and logic
   - Uses Service Models isolated from both presentation and data access layers
   - Maps between Service Models and Data Entities
   - Represented by TaskServiceModel objects

3. **Data Access Layer**: 
   - Repository pattern for data storage and retrieval
   - Uses Entity models specific to the data storage mechanism
   - Represented by TaskEntity objects

## Project Structure

```
cursor-workflow-automation/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── cursor/
│   │   │           └── automation/
│   │   │               ├── Application.java              # Main entry point
│   │   │               ├── WorkflowDemo.java             # Console UI (presentation layer)
│   │   │               ├── model/                        # Models
│   │   │               │   └── dto/                      # DTOs for presentation layer
│   │   │               │       └── TaskDTO.java          # DTO for Task
│   │   │               ├── mapper/                       # Object mappers
│   │   │               │   ├── TaskDTOMapper.java        # Maps between DTOs and Service Models
│   │   │               │   └── TaskEntityMapper.java     # Maps between Service Models and Entities
│   │   │               ├── factory/                      # Object factories
│   │   │               │   └── TaskFactory.java          # Creates Service Models and Entities
│   │   │               ├── service/                      # Business logic layer
│   │   │               │   ├── TaskService.java          # Service interface
│   │   │               │   ├── TaskServiceImpl.java      # Service implementation
│   │   │               │   └── model/                    # Service layer models
│   │   │               │       └── TaskServiceModel.java # Service model for Task
│   │   │               └── dal/                          # Data access layer
│   │   │                   ├── TaskRepository.java       # Repository interface
│   │   │                   ├── InMemoryTaskRepository.java # Repository implementation
│   │   │                   └── model/                     # Data access layer models
│   │   │                       └── TaskEntity.java       # Entity for Task
│   │   └── resources/
│   │       └── logback.xml                             # Logging configuration
│   └── test/                                           # Test directory
│       └── java/
│           └── com/
│               └── cursor/
│                   └── automation/
│                       ├── service/
│                       │   └── TaskServiceImplTest.java  # Service tests
│                       ├── mapper/
│                       │   ├── TaskDTOMapperTest.java    # DTO mapper tests
│                       │   └── TaskEntityMapperTest.java # Entity mapper tests
│                       └── WorkflowDemoTest.java         # UI integration tests
└── pom.xml                                             # Maven project configuration
```

## Data Flow Architecture

The application follows this data flow pattern:

1. **UI Layer**:
   - Collects user input and creates TaskDTO objects
   - Passes DTOs directly to the Service Layer
   - Receives Service Models from the Service Layer
   - Converts Service Models to DTOs for display using TaskDTOMapper

2. **Service Layer**:
   - Receives DTOs from the UI Layer
   - Converts DTOs to Service Models using TaskDTOMapper
   - Implements business logic using Service Models
   - Converts Service Models to Entities using TaskEntityMapper
   - Passes Entities to the Data Access Layer
   - Receives Entities from the Data Access Layer
   - Converts Entities back to Service Models to return to UI

3. **Data Access Layer**:
   - Receives Entities from the Service Layer
   - Persists Entities (in memory for this implementation)
   - Returns Entities to the Service Layer

## Data Transformation Flow

The following diagram shows how data is transformed as it flows through the application layers:

```
+-------------------+                   +----------------------+                 +-------------------+
|                   |                   |                      |                 |                   |
| Presentation      |                   | Business Logic       |                 | Data Access       |
| Layer             |                   | Layer                |                 | Layer             |
|                   |                   |                      |                 |                   |
| +-------------+   |  TaskDTOMapper    | +----------------+   | TaskEntityMapper| +-------------+   |
| |             |   |                   | |                |   |                 | |             |   |
| | TaskDTO     |---+------------------>| | TaskService    |---+---------------->| | TaskEntity  |   |
| | {id, title} |   |  toServiceModel() | | Model          |   | toEntity()      | | {id, title} |   |
| |             |   |                   | | {id, title}    |   |                 | |             |   |
| +-------------+   |                   | +----------------+   |                 | +------+------+   |
|        ^          |                   |         ^           |                 |        |          |
|        |          |                   |         |           |                 |        v          |
|        |          |                   |         |           |                 |                   |
| +-------------+   |  TaskDTOMapper    | +----------------+   | TaskEntityMapper| +-------------+   |
| |             |   |                   | |                |   |                 | |             |   |
| | TaskDTO     |<--+-------------------+ | TaskService    |<--+-----------------+ | In-Memory   |   |
| | {id, title} |   |  toDTO()          | | Model          |   | toServiceModel()| | Storage     |   |
| |             |   |                   | | {id, title}    |   |                 | |             |   |
| +-------------+   |                   | +----------------+   |                 | +-------------+   |
|                   |                   |                      |                 |                   |
+-------------------+                   +----------------------+                 +-------------------+
       ^  |                                                                               
       |  |                                                                               
       |  v                                                                               
 +------------+                                                                           
 |            |                                                                           
 |    User    |                                                                           
 |            |                                                                           
 +------------+                                                                           
```

The diagram shows:

1. User interacts with the Presentation Layer, inputting data that becomes TaskDTO objects
2. TaskDTOs are converted to TaskServiceModels using TaskDTOMapper
3. Business logic is applied in the Service Layer using the TaskServiceModel
4. The TaskServiceModel is converted to a TaskEntity for storage using TaskEntityMapper
5. The TaskEntity is stored and retrieved from the Data Access Layer
6. When retrieving data, the flow is reversed:
   - TaskEntity → TaskServiceModel → TaskDTO → User display

## Requirements

- Java 11 or higher
- Maven 3.6 or higher

## Building and Running the Project

### Building with Maven

To build the project, run:

```bash
cd cursor-workflow-automation
mvn clean package
```

This will create an executable JAR file in the `target` directory.

### Running Tests with Maven

To run the tests:

```bash
mvn test
```

### Running the Application with Maven

You can run the application directly with Maven using the `exec` plugin:

```bash
mvn exec:java -Dexec.mainClass="com.cursor.automation.Application"
```

### Running the Packaged JAR

After building the project, you can run the JAR file:

```bash
java -jar target/workflow-automation-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## Features

- Add new tasks with title
- View all existing tasks
- In-memory data storage (no persistence between runs)

## Architecture Benefits

This layered architecture with isolated models provides several benefits:

1. **Separation of Concerns**: Each layer has a single responsibility and is isolated from the others
2. **Enhanced Testability**: Each layer can be tested independently with mock objects
3. **Flexibility**: Changes to one layer don't affect the others as long as the interfaces remain the same
4. **Scalability**: Each layer can be scaled independently
5. **Maintainability**: The code is more maintainable as changes in one layer don't cascade to others
6. **Clear Data Flow**: The application has a clear and unidirectional data flow between layers

## Future Enhancements

- Add more task properties (description, status, priority, due date)
- Persistent data storage (database)
- Update and delete task functionality
- Task filtering and sorting
- User authentication and authorization
- REST API for integration with other systems 