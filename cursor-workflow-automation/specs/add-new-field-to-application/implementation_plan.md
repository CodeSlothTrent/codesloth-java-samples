# Implementation Plan for Adding a New Field

This document outlines the step-by-step process for adding a new field to the task management application, ensuring it flows through all layers of the architecture.

## Implementation Steps

Follow these steps in order to ensure a smooth implementation:

### 1. Model Updates

1. **Update TaskDTO**
   - File: `src/main/java/com/cursor/automation/model/dto/TaskDTO.java`
   - Changes: Add the new field, getters, setters, and constructors
   - Reference: [01_TaskDTO_Changes.md](steps/01_TaskDTO_Changes.md)

2. **Update TaskServiceModel**
   - File: `src/main/java/com/cursor/automation/service/model/TaskServiceModel.java`
   - Changes: Add the new field, getters, setters, and constructors
   - Reference: [02_TaskServiceModel_Changes.md](steps/02_TaskServiceModel_Changes.md)

3. **Update TaskEntity**
   - File: `src/main/java/com/cursor/automation/dal/model/TaskEntity.java`
   - Changes: Add the new field, getters, setters, and constructors
   - Reference: [03_TaskEntity_Changes.md](steps/03_TaskEntity_Changes.md)

### 2. Mapper Updates

4. **Update TaskDTOMapper**
   - File: `src/main/java/com/cursor/automation/mapper/TaskDTOMapper.java`
   - Changes: Modify mapping methods to include the new field
   - Reference: [04_TaskDTOMapper_Changes.md](steps/04_TaskDTOMapper_Changes.md)

5. **Update TaskEntityMapper**
   - File: `src/main/java/com/cursor/automation/mapper/TaskEntityMapper.java`
   - Changes: Modify mapping methods to include the new field
   - Reference: [05_TaskEntityMapper_Changes.md](steps/05_TaskEntityMapper_Changes.md)

### 3. Factory Updates

6. **Update TaskFactory**
   - File: `src/main/java/com/cursor/automation/factory/TaskFactory.java`
   - Changes: Add new factory methods that accept the new field parameter
   - Reference: [06_TaskFactory_Changes.md](steps/06_TaskFactory_Changes.md)

### 4. UI Updates

7. **Update WorkflowDemo**
   - File: `src/main/java/com/cursor/automation/WorkflowDemo.java`
   - Changes: Update UI to collect, display, and handle the new field
   - Reference: [07_WorkflowDemo_Changes.md](steps/07_WorkflowDemo_Changes.md)

### 5. Test Updates

8. **Update TaskServiceImplTest**
   - File: `src/test/java/com/cursor/automation/service/TaskServiceImplTest.java`
   - Changes: Add tests for the new field in service operations
   - Reference: [08_TaskServiceImplTest_Changes.md](steps/08_TaskServiceImplTest_Changes.md)

9. **Update Mapper Tests**
   - Files: 
     - `src/test/java/com/cursor/automation/mapper/TaskDTOMapperTest.java`
     - `src/test/java/com/cursor/automation/mapper/TaskEntityMapperTest.java`
   - Changes: Add tests for mapping the new field
   - Reference: [09_Mapper_Tests_Changes.md](steps/09_Mapper_Tests_Changes.md)

10. **Update WorkflowDemoTest**
    - File: `src/test/java/com/cursor/automation/WorkflowDemoTest.java`
    - Changes: Add tests for UI handling of the new field
    - Reference: [10_WorkflowDemoTest_Changes.md](steps/10_WorkflowDemoTest_Changes.md)

## Verification Steps

After implementing the changes:

1. Run all tests to ensure they pass
2. Run the application manually and test the following scenarios:
   - Adding a task with the new field populated
   - Adding a task without the new field populated
   - Entering invalid values for the new field (if applicable)
   - Viewing tasks with different values for the new field

## Rollback Plan

If issues are encountered:

1. Revert the changes to each file in reverse order
2. Run tests to ensure the application is back to its original state

## Script Execution

A script could be created to apply these changes if desired:

```bash
#!/bin/bash

# Implementation script for adding a new field
# This script applies all the changes defined in the spec files

echo "Implementing new field in the Task Management Application..."

# 1. Update model classes
echo "Updating model classes..."
# Add implementation commands here

# 2. Update mappers
echo "Updating mappers..."
# Add implementation commands here

# 3. Update factory
echo "Updating factory..."
# Add implementation commands here

# 4. Update UI
echo "Updating UI..."
# Add implementation commands here

# 5. Update tests
echo "Updating tests..."
# Add implementation commands here

echo "Implementation complete. Running tests..."
cd cursor-workflow-automation
mvn test

echo "Done!"
``` 