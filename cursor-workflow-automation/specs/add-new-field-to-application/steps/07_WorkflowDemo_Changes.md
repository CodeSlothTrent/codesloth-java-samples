# WorkflowDemo Changes Specification

## File: `src/main/java/com/cursor/automation/WorkflowDemo.java`

## Changes Required

Update the WorkflowDemo class to handle the collection, display, and management of task due dates in the user interface.

### Implementation Details

1. Update the menu options and input handling to collect due dates when adding tasks
2. Modify the task display format to show due dates
3. Add validation for date input
4. Update UI messages to reference due dates

### Code Example

```java
// Add import
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class WorkflowDemo {
    private final TaskService taskService;
    private final Scanner scanner;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
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
        
        // Add due date input with validation
        System.out.print("Enter due date (yyyy-MM-dd) or leave empty if none: ");
        String dueDateStr = scanner.nextLine();
        
        LocalDate dueDate = null;
        if (!dueDateStr.trim().isEmpty()) {
            try {
                dueDate = LocalDate.parse(dueDateStr, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Due date will not be set.");
            }
        }
        
        // Use the factory to create a task with or without a due date
        TaskDTO taskDTO = dueDate == null 
            ? TaskFactory.createTaskDTO(title)
            : TaskFactory.createTaskDTO(title, dueDate);
        
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
        
        // Update the display format to include due date
        System.out.printf("%-5s | %-30s | %-12s%n", "ID", "Title", "Due Date");
        System.out.println("------------------------------------------------------------------------");
        
        for (TaskDTO task : tasks) {
            String dueDateStr = task.getDueDate() == null ? "Not set" : 
                                task.getDueDate().format(DATE_FORMATTER);
            
            System.out.printf("%-5s | %-30s | %-12s%n", 
                              task.getId(), 
                              task.getTitle(), 
                              dueDateStr);
        }
    }
    
    // Other methods...
}
```

### Testing Considerations

1. Test adding tasks with valid due dates
2. Test adding tasks with invalid date formats
3. Test adding tasks with empty due dates
4. Verify that due dates are correctly displayed in the task list
5. Test existing functionality to ensure it's not disrupted by these changes
</rewritten_file> 