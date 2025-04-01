# WorkflowDemo Changes Specification

## File: `src/main/java/com/cursor/automation/WorkflowDemo.java`

## Changes Required

Update the WorkflowDemo class to handle the collection, display, and management of the new field in the user interface.

### Implementation Details

1. Update the menu options and input handling to collect values for the new field when adding tasks
2. Modify the task display format to show the new field's values
3. Add validation for the new field input (if applicable)
4. Update UI messages to reference the new field

### Code Example

```java
// Add any necessary imports for the field type
// import java.time.LocalDate; // Example import for date type
// import java.time.format.DateTimeFormatter; // Example formatter for date
// import java.time.format.DateTimeParseException; // Example exception handling

public class WorkflowDemo {
    private final TaskService taskService;
    private final Scanner scanner;
    // Add any formatters or utility variables needed for handling the new field
    // private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    // Constructor and other members...

    private void displayMenu() {
        System.out.println("\nMain Menu:");
        System.out.println("1. Add new task");
        System.out.println("2. View all tasks");
        System.out.println("3. Exit");
        System.out.print("Enter your choice: ");
    }
    
    private void addTask() {
        System.out.println("\nAdd New Task");
        System.out.print("Enter task title: ");
        String title = scanner.nextLine();
        
        // Add new field input with validation if needed
        System.out.print("Enter [field description] or leave empty if none: ");
        String inputValue = scanner.nextLine();
        
        [FieldType] newFieldValue = null; // Or appropriate default for primitive types
        if (!inputValue.trim().isEmpty()) {
            try {
                // Parse or convert the input string to the appropriate type
                // Example for different types:
                // For String: newFieldValue = inputValue;
                // For Integer: newFieldValue = Integer.parseInt(inputValue);
                // For LocalDate: newFieldValue = LocalDate.parse(inputValue, DATE_FORMATTER);
                
                // Add appropriate error handling for the field type
            } catch (Exception e) {
                System.out.println("Invalid format. The new field will not be set.");
            }
        }
        
        // Use the factory to create a task with or without the new field
        TaskDTO taskDTO = newFieldValue == null 
            ? TaskFactory.createTaskDTO(title)
            : TaskFactory.createTaskDTO(title, newFieldValue);
        
        taskService.addTask(taskDTO);
        System.out.println("Task created successfully!");
    }
    
    private void displayTasks() {
        System.out.println("\nAll Tasks:");
        List<TaskDTO> tasks = taskService.getAllTasks();
        
        if (tasks.isEmpty()) {
            System.out.println("No tasks found.");
            return;
        }
        
        // Update the display format to include the new field
        System.out.printf("%-5s | %-30s | %-20s%n", "ID", "Title", "New Field");
        System.out.println("------------------------------------------------------------------------");
        
        for (TaskDTO task : tasks) {
            // Format the new field value appropriately
            String fieldValueStr = formatFieldValue(task.getNewField());
            
            System.out.printf("%-5s | %-30s | %-20s%n", 
                              task.getId(), 
                              task.getTitle(), 
                              fieldValueStr);
        }
    }
    
    // Helper method to format the new field value for display
    private String formatFieldValue([FieldType] value) {
        if (value == null) {
            return "Not set";
        }
        
        // Format the value appropriately for its type
        // Examples:
        // For String: return value;
        // For LocalDate: return value.format(DATE_FORMATTER);
        // For numeric types: return String.valueOf(value);
        
        return value.toString(); // Default implementation
    }
    
    // Other methods...
}
```

### Testing Considerations

1. Test adding tasks with valid values for the new field
2. Test adding tasks with invalid input formats (if applicable)
3. Test adding tasks with empty values for the new field
4. Verify that the new field is correctly displayed in the task list
5. Test existing functionality to ensure it's not disrupted by these changes
6. Test handling of different value formats and edge cases specific to the field type
</rewritten_file> 