# TaskFactory Changes Specification

## File: `src/main/java/com/cursor/automation/factory/TaskFactory.java`

## Changes Required

Update the TaskFactory to support creating tasks with the new `dueDate` field.

### Implementation Details

1. Add new factory methods that accept a dueDate parameter
2. Update existing factory methods to maintain backward compatibility
3. Add methods to generate sample tasks with due dates

### Code Example

```java
import java.time.LocalDate;
import java.util.UUID;

public class TaskFactory {

    /**
     * Creates a new TaskServiceModel with a generated ID.
     * (kept for backward compatibility)
     * 
     * @param title the title for the task
     * @return a new TaskServiceModel with a generated ID
     */
    public static TaskServiceModel createServiceModel(String title) {
        String id = generateId();
        return new TaskServiceModel(id, title);
    }
    
    /**
     * Creates a new TaskServiceModel with a generated ID and due date.
     * 
     * @param title the title for the task
     * @param dueDate the due date for the task
     * @return a new TaskServiceModel with a generated ID
     */
    public static TaskServiceModel createServiceModel(String title, LocalDate dueDate) {
        String id = generateId();
        return new TaskServiceModel(id, title, dueDate);
    }

    /**
     * Creates a new TaskEntity with a generated ID.
     * (kept for backward compatibility)
     * 
     * @param title the title for the task
     * @return a new TaskEntity with a generated ID
     */
    public static TaskEntity createEntity(String title) {
        String id = generateId();
        return new TaskEntity(id, title);
    }
    
    /**
     * Creates a new TaskEntity with a generated ID and due date.
     * 
     * @param title the title for the task
     * @param dueDate the due date for the task
     * @return a new TaskEntity with a generated ID
     */
    public static TaskEntity createEntity(String title, LocalDate dueDate) {
        String id = generateId();
        return new TaskEntity(id, title, dueDate);
    }

    /**
     * Generates a unique ID for a task.
     * 
     * @return a unique ID
     */
    private static String generateId() {
        return UUID.randomUUID().toString();
    }
}
```

### Testing Considerations

1. Test creating tasks with and without due dates
2. Verify that sample tasks have appropriate due dates
3. Test backward compatibility with code that doesn't use due dates
4. Ensure factory methods correctly initialize the due date field 