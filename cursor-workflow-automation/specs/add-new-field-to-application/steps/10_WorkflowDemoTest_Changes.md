# WorkflowDemoTest Changes Specification

## File: `src/test/java/com/cursor/automation/WorkflowDemoTest.java`

## Changes Required

Update the WorkflowDemoTest class to test the UI handling of the new `dueDate` field.

### Implementation Details

1. Update existing tests to account for due date output when displaying tasks
2. Add new tests for adding tasks with due dates

### Code Example

```java
public class WorkflowDemoTest {
    // ... existing setup code ...
    
    @Test
    public void testRunDemo_OutputsExpectedMessages() {
        // Simulate user choosing to exit after viewing the menu
        String userInput = "3\n";  // Just exit
        System.setIn(new ByteArrayInputStream(userInput.getBytes()));
        
        // Run the demo
        workflowDemo.runDemo();
        
        // Get the output
        String output = outputStream.toString();
        
        // Verify expected output messages
        assertTrue(output.contains("Task Management Application"));
        assertTrue(output.contains("Adding sample tasks"));
        assertTrue(output.contains("Sample tasks added successfully"));
        assertTrue(output.contains("Main Menu"));
        assertTrue(output.contains("1. Add new task"));
        assertTrue(output.contains("2. View all tasks"));
        assertTrue(output.contains("3. Exit"));
        
        // Verify new due date column is displayed when viewing tasks
        assertTrue(output.contains("Due Date"));
    }
    
    @Test
    public void testAddNewTask_WithDueDate_OutputsExpectedMessages() {
        // Simulate user input for adding a task with a due date and then exiting
        String userInput = "1\n" +  // Choose "Add new task"
                          "Test Task\n" +  // Task title
                          "2023-12-31\n" +  // Due date
                          "3\n";  // Exit
        System.setIn(new ByteArrayInputStream(userInput.getBytes()));
        
        // Run the demo
        workflowDemo.runDemo();
        
        // Get the output
        String output = outputStream.toString();
        
        // Verify expected output messages
        assertTrue(output.contains("Add New Task"));
        assertTrue(output.contains("Enter task title"));
        assertTrue(output.contains("Enter due date"));
        assertTrue(output.contains("Task created successfully"));
        
        // When viewing tasks, should show the due date
        assertTrue(output.contains("2023-12-31"));
    }
    
    @Test
    public void testAddNewTask_InvalidDueDate_HandlesGracefully() {
        // Simulate user input with an invalid date format
        String userInput = "1\n" +  // Choose "Add new task"
                          "Test Task\n" +  // Task title
                          "invalid-date\n" +  // Invalid due date
                          "3\n";  // Exit
        System.setIn(new ByteArrayInputStream(userInput.getBytes()));
        
        // Run the demo
        workflowDemo.runDemo();
        
        // Get the output
        String output = outputStream.toString();
        
        // Verify expected output messages
        assertTrue(output.contains("Invalid date format"));
        assertTrue(output.contains("Task created successfully"));
    }
    
    @Test
    public void testAddNewTask_EmptyDueDate_HandlesGracefully() {
        // Simulate user input with an empty due date
        String userInput = "1\n" +  // Choose "Add new task"
                          "Test Task\n" +  // Task title
                          "\n" +  // Empty due date
                          "3\n";  // Exit
        System.setIn(new ByteArrayInputStream(userInput.getBytes()));
        
        // Run the demo
        workflowDemo.runDemo();
        
        // Get the output
        String output = outputStream.toString();
        
        // Verify expected output messages
        assertTrue(output.contains("Task created successfully"));
        
        // Should show "Not set" for due date when viewing tasks
        assertTrue(output.contains("Not set"));
    }
    
    // ... other existing test methods ...
}
```

### Testing Considerations

1. Test that due dates are displayed in the UI
2. Test adding tasks with valid due dates
3. Test handling of invalid date formats
4. Test handling of empty date inputs 