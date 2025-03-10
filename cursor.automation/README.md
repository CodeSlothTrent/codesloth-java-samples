# Cursor Automation Spring Web API

A simple Spring Boot Web API for task management.

## Features

- RESTful API for managing tasks
- CRUD operations (Create, Read, Update, Delete)
- In-memory storage (no database required)

## API Endpoints

### Hello Endpoint

- `GET /api/hello` - Returns a simple greeting message

### Task Endpoints

- `GET /api/tasks` - Get all tasks
- `GET /api/tasks/{id}` - Get a specific task by ID
- `POST /api/tasks` - Create a new task
- `PUT /api/tasks/{id}` - Update an existing task
- `DELETE /api/tasks/{id}` - Delete a task

## Running the Application

### Prerequisites

- Java 17 or higher
- Maven

### Steps

1. Clone the repository
2. Navigate to the project directory
3. Run the following command:

```bash
mvn spring-boot:run
```

The application will start on port 8080.

## Example API Usage

### Creating a Task

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"Learn Spring Boot","description":"Complete the Spring Boot tutorial"}'
```

### Getting All Tasks

```bash
curl -X GET http://localhost:8080/api/tasks
```

### Getting a Specific Task

```bash
curl -X GET http://localhost:8080/api/tasks/{task-id}
```

### Updating a Task

```bash
curl -X PUT http://localhost:8080/api/tasks/{task-id} \
  -H "Content-Type: application/json" \
  -d '{"title":"Updated Task Title","completed":true}'
```

### Deleting a Task

```bash
curl -X DELETE http://localhost:8080/api/tasks/{task-id}
``` 