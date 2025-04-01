# TaskServiceModel Changes Specification

## File: `src/main/java/com/cursor/automation/service/model/TaskServiceModel.java`

## Changes Required

Add a `dueDate` field to the TaskServiceModel class to represent the date by which the task should be completed.

### Implementation Details

1. Add a new field of type `LocalDate` called `dueDate`
2. Update the constructors to include the new field (with overloads for backward compatibility)
3. Add getter and setter methods for the new field
4. Update the `toString()` method to include the new field

### Code Example

```java
// Add import
import java.time.LocalDate;

public class TaskServiceModel {
    private String id;
    private String title;
    private LocalDate dueDate; // New field
    
    // Default constructor
    public TaskServiceModel() {
    }
    
    // Constructor with title only (for backward compatibility)
    public TaskServiceModel(String title) {
        this.title = title;
    }
    
    // Constructor with id and title (for backward compatibility)
    public TaskServiceModel(String id, String title) {
        this.id = id;
        this.title = title;
    }
    
    // New constructor with all fields
    public TaskServiceModel(String id, String title, LocalDate dueDate) {
        this.id = id;
        this.title = title;
        this.dueDate = dueDate;
    }
    
    // Existing getters and setters
    // ... 
    
    // New getter and setter for dueDate
    public LocalDate getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
    
    // Update toString method
    @Override
    public String toString() {
        return "TaskServiceModel{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", dueDate=" + dueDate +
                '}';
    }
}
```

### Testing Considerations

1. Ensure that existing code that uses TaskServiceModel still works with the new field being null
2. Test serialization/deserialization if applicable
3. Test that the new field is properly set and retrieved through all service layer operations 