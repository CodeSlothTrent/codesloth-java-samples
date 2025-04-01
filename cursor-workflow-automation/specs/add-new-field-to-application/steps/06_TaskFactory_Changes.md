# TaskFactory Changes Specification

## File: `src/main/java/com/cursor/automation/factory/TaskFactory.java`

## Changes Required

Update the TaskFactory to support creating tasks with the new field.

### Implementation Details

1. Add new factory methods that accept the new field parameter
2. Update existing factory methods to maintain backward compatibility
3. Add methods to generate sample tasks with the new field

### Code Example

```java
// Add any necessary imports for the field type
// import java.time.LocalDate; // Example import for date type
// import java.math.BigDecimal; // Example import for decimal type

public class TaskFactory {

    // Existing methods for backward compatibility
    public static TaskDTO createTaskDTO(String title) {
        return new TaskDTO(title);
    }
    
    public static TaskServiceModel createTaskServiceModel(String title) {
        return new TaskServiceModel(title);
    }
    
    public static TaskEntity createTaskEntity(String title) {
        return new TaskEntity(title);
    }
    
    // New methods with new field parameter
    public static TaskDTO createTaskDTO(String title, [FieldType] newField) {
        TaskDTO task = new TaskDTO(title);
        task.setNewField(newField);
        return task;
    }
    
    public static TaskServiceModel createTaskServiceModel(String title, [FieldType] newField) {
        TaskServiceModel task = new TaskServiceModel(title);
        task.setNewField(newField);
        return task;
    }
    
    public static TaskEntity createTaskEntity(String title, [FieldType] newField) {
        TaskEntity task = new TaskEntity(title);
        task.setNewField(newField);
        return task;
    }
    
    // Methods for creating sample tasks
    public static List<TaskDTO> createSampleTaskDTOs() {
        List<TaskDTO> tasks = new ArrayList<>();
        
        // Add tasks with various values for the new field
        TaskDTO task1 = createTaskDTO("Complete project documentation");
        task1.setNewField([SampleValue1]); // Example value
        tasks.add(task1);
        
        TaskDTO task2 = createTaskDTO("Prepare presentation");
        task2.setNewField([SampleValue2]); // Example value
        tasks.add(task2);
        
        TaskDTO task3 = createTaskDTO("Submit report");
        task3.setNewField([SampleValue3]); // Example value
        tasks.add(task3);
        
        // Include a task with no value for the new field for backward compatibility testing
        tasks.add(createTaskDTO("Review code"));
        
        return tasks;
    }
    
    // Similar methods for service models and entities
    // ...
}
```

### Testing Considerations

1. Test creating tasks with and without the new field
2. Verify that sample tasks have appropriate values for the new field
3. Test backward compatibility with code that doesn't use the new field
4. Ensure factory methods correctly initialize the new field
5. For reference types (non-primitives), test null handling
6. For primitive types, test default values 