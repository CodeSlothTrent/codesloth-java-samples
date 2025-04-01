package com.cursor.automation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for the WorkflowDemo class.
 */
public class WorkflowDemoTest {
    
    private WorkflowDemo workflowDemo;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final InputStream originalIn = System.in;
    
    @BeforeEach
    public void setUp() {
        // Redirect System.out to our stream
        System.setOut(new PrintStream(outputStreamCaptor));
        
        // Create a simulated user input to exit immediately after sample tasks are added
        ByteArrayInputStream testIn = new ByteArrayInputStream("3\n".getBytes());
        System.setIn(testIn);
        
        workflowDemo = new WorkflowDemo();
    }
    
    @AfterEach
    public void tearDown() {
        // Restore original System.out and System.in
        System.setOut(originalOut);
        System.setIn(originalIn);
    }
    
    @Test
    public void testRunDemo_OutputsExpectedMessages() {
        // When
        workflowDemo.runDemo();
        
        // Then
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("Task Management Application"));
        assertTrue(output.contains("Adding sample tasks"));
        assertTrue(output.contains("Sample tasks added successfully"));
        assertTrue(output.contains("Main Menu"));
    }
    
    @Test
    public void testAddNewTask_OutputsExpectedMessages() {
        // Given
        // Simulate user input for adding a task and then exiting
        String userInput = "1\n" +  // Choose "Add new task"
                           "Test Task\n" +  // Task title
                           "3\n";   // Exit
                           
        ByteArrayInputStream testIn = new ByteArrayInputStream(userInput.getBytes());
        System.setIn(testIn);
        
        // When
        workflowDemo = new WorkflowDemo(); // Re-initialize with new input
        workflowDemo.runDemo();
        
        // Then
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("Add New Task"));
        assertTrue(output.contains("Enter task title"));
        assertTrue(output.contains("Task created successfully"));
    }
} 