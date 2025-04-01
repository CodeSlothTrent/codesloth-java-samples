# WorkflowDemoTest Changes Specification

## File: `src/test/java/com/cursor/automation/WorkflowDemoTest.java`

## Changes Required

Update the WorkflowDemoTest class to test the UI handling of the new field.

### Implementation Details

1. Update existing tests to account for new field output when displaying tasks
2. Add new tests for adding tasks with the new field

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
        
        // Verify new field column is displayed when viewing tasks
        assertTrue(output.contains("New Field"));
    }
    
    @Test
    public void testAddNewTask_WithNewField_OutputsExpectedMessages() {
        // Simulate user input for adding a task with a value for the new field
        String userInput = "1\n" +  // Choose "Add new task"
                          "Test Task\n" +  // Task title
                          "SampleValue\n" +  // Value for the new field
                          "3\n";  // Exit
        System.setIn(new ByteArrayInputStream(userInput.getBytes()));
        
        // Run the demo
        workflowDemo.runDemo();
        
        // Get the output
        String output = outputStream.toString();
        
        // Verify expected output messages
        assertTrue(output.contains("Add New Task"));
        assertTrue(output.contains("Enter task title"));
        assertTrue(output.contains("Enter [field description]"));
        assertTrue(output.contains("Task created successfully"));
        
        // When viewing tasks, should show the new field value
        // Note: The actual output verification will depend on how the field is displayed
        // assertTrue(output.contains("SampleValue")); // Uncomment and adjust as needed
    }
    
    @Test
    public void testAddNewTask_InvalidNewFieldValue_HandlesGracefully() {
        // Simulate user input with an invalid format for the new field
        String userInput = "1\n" +  // Choose "Add new task"
                          "Test Task\n" +  // Task title
                          "invalid-value\n" +  // Invalid value format
                          "3\n";  // Exit
        System.setIn(new ByteArrayInputStream(userInput.getBytes()));
        
        // Run the demo
        workflowDemo.runDemo();
        
        // Get the output
        String output = outputStream.toString();
        
        // Verify expected output messages
        // Note: This assertion depends on the actual validation message used
        // assertTrue(output.contains("Invalid format")); // Uncomment and adjust as needed
        assertTrue(output.contains("Task created successfully"));
    }
    
    @Test
    public void testAddNewTask_EmptyNewFieldValue_HandlesGracefully() {
        // Simulate user input with an empty value for the new field
        String userInput = "1\n" +  // Choose "Add new task"
                          "Test Task\n" +  // Task title
                          "\n" +  // Empty value
                          "3\n";  // Exit
        System.setIn(new ByteArrayInputStream(userInput.getBytes()));
        
        // Run the demo
        workflowDemo.runDemo();
        
        // Get the output
        String output = outputStream.toString();
        
        // Verify expected output messages
        assertTrue(output.contains("Task created successfully"));
        
        // Should show "Not set" for the new field when viewing tasks
        // Note: This depends on how null/empty values are displayed
        // assertTrue(output.contains("Not set")); // Uncomment and adjust as needed
    }
    
    // ... other existing test methods ...
}
```

### Testing Considerations

1. Test that the new field is displayed in the UI
2. Test adding tasks with valid values for the new field
3. Test handling of invalid input formats (if applicable)
4. Test handling of empty inputs
5. Test that existing functionality still works correctly
6. Consider specific edge cases related to the field type being added 