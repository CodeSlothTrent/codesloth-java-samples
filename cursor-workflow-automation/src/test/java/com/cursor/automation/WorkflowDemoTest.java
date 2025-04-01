package com.cursor.automation;

import com.cursor.automation.mapper.TaskStatusMapper;
import com.cursor.automation.model.dto.TaskDTO;
import com.cursor.automation.model.dto.TaskStatusDTO;
import com.cursor.automation.service.TaskService;
import com.cursor.automation.service.model.TaskServiceModel;
import com.cursor.automation.service.model.TaskStatusService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for the WorkflowDemo class.
 */
public class WorkflowDemoTest {
    
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final InputStream originalIn = System.in;
    
    private TaskService mockTaskService;
    private WorkflowDemo workflowDemo;
    
    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outContent));
        
        mockTaskService = mock(TaskService.class);
        
        // Use a static mock to intercept TaskServiceImpl constructor calls
        MockedStatic<com.cursor.automation.service.TaskServiceImpl> mockedStatic = 
                mockStatic(com.cursor.automation.service.TaskServiceImpl.class);
        
        mockedStatic.when(() -> new com.cursor.automation.service.TaskServiceImpl(any()))
                .thenReturn((com.cursor.automation.service.TaskServiceImpl) mockTaskService);
        
        workflowDemo = new WorkflowDemo();
        
        mockedStatic.close();
    }
    
    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setIn(originalIn);
    }
    
    @Test
    void testMainMenu_ExitOption() {
        // Arrange
        String input = "3\n"; // Select "Exit" option
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        // Act
        workflowDemo.runDemo();
        
        // Assert
        String output = outContent.toString();
        assertTrue(output.contains("--- Task Management Application ---"));
        assertTrue(output.contains("----- Main Menu -----"));
        assertTrue(output.contains("--- Application Closed ---"));
    }
    
    @Test
    void testAddNewTask_OutputsExpectedMessages() {
        // Arrange
        String input = "1\nTest Task\n1\n3\n"; // Add task -> Enter title -> Select status (TODO) -> Exit
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        TaskServiceModel createdTask = new TaskServiceModel("new-id", "Test Task", TaskStatusService.TODO);
        when(mockTaskService.createTask(any(TaskDTO.class))).thenReturn(createdTask);
        
        // Act
        workflowDemo.runDemo();
        
        // Assert
        String output = outContent.toString();
        assertTrue(output.contains("----- Add New Task -----"));
        assertTrue(output.contains("Enter task title:"));
        assertTrue(output.contains("Select task status:"));
        assertTrue(output.contains("1. TODO"));
        assertTrue(output.contains("Task created successfully with ID: new-id"));
        
        // Verify TaskDTO was passed to service
        ArgumentCaptor<TaskDTO> taskCaptor = ArgumentCaptor.forClass(TaskDTO.class);
        verify(mockTaskService).createTask(taskCaptor.capture());
        
        TaskDTO capturedTask = taskCaptor.getValue();
        assertEquals("Test Task", capturedTask.getTitle());
        assertEquals(TaskStatusDTO.TODO, capturedTask.getStatus());
    }
    
    @Test
    void testAddNewTask_InvalidStatusInput_PromptsTryAgain() {
        // Arrange
        String input = "1\nTest Task\n9\n1\n3\n"; // Add task -> Enter title -> Invalid status -> Valid status -> Exit
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        TaskServiceModel createdTask = new TaskServiceModel("new-id", "Test Task", TaskStatusService.TODO);
        when(mockTaskService.createTask(any(TaskDTO.class))).thenReturn(createdTask);
        
        // Act
        workflowDemo.runDemo();
        
        // Assert
        String output = outContent.toString();
        assertTrue(output.contains("Invalid choice. Please try again."));
        
        // Verify TaskDTO was passed to service with correct status (after retry)
        ArgumentCaptor<TaskDTO> taskCaptor = ArgumentCaptor.forClass(TaskDTO.class);
        verify(mockTaskService).createTask(taskCaptor.capture());
        
        TaskDTO capturedTask = taskCaptor.getValue();
        assertEquals(TaskStatusDTO.TODO, capturedTask.getStatus());
    }
    
    @Test
    void testViewAllTasks_DisplaysTasks() {
        // Arrange
        String input = "2\n3\n"; // View tasks -> Exit
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        List<TaskServiceModel> tasks = Arrays.asList(
                new TaskServiceModel("1", "Task 1", TaskStatusService.TODO),
                new TaskServiceModel("2", "Task 2", TaskStatusService.IN_PROGRESS),
                new TaskServiceModel("3", "Task 3", TaskStatusService.DONE)
        );
        
        when(mockTaskService.getAllTasks()).thenReturn(tasks);
        
        // Act
        workflowDemo.runDemo();
        
        // Assert
        String output = outContent.toString();
        assertTrue(output.contains("----- All Tasks -----"));
        assertTrue(output.contains("Task 1"));
        assertTrue(output.contains("Task 2"));
        assertTrue(output.contains("Task 3"));
        assertTrue(output.contains("TODO"));
        assertTrue(output.contains("IN_PROGRESS"));
        assertTrue(output.contains("DONE"));
    }
    
    @Test
    void testViewAllTasks_NoTasks_DisplaysMessage() {
        // Arrange
        String input = "2\n3\n"; // View tasks -> Exit
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        when(mockTaskService.getAllTasks()).thenReturn(Collections.emptyList());
        
        // Act
        workflowDemo.runDemo();
        
        // Assert
        String output = outContent.toString();
        assertTrue(output.contains("----- All Tasks -----"));
        assertTrue(output.contains("No tasks found."));
    }
    
    @Test
    void testViewAllTasks_DisplaysStatus() {
        // Arrange
        String input = "2\n3\n"; // View tasks -> Exit
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        List<TaskServiceModel> tasks = Arrays.asList(
                new TaskServiceModel("1", "Task 1", TaskStatusService.TODO),
                new TaskServiceModel("2", "Task 2", TaskStatusService.IN_PROGRESS),
                new TaskServiceModel("3", "Task 3", TaskStatusService.BLOCKED),
                new TaskServiceModel("4", "Task 4", TaskStatusService.IN_REVIEW),
                new TaskServiceModel("5", "Task 5", TaskStatusService.DONE)
        );
        
        when(mockTaskService.getAllTasks()).thenReturn(tasks);
        
        // Act
        workflowDemo.runDemo();
        
        // Assert
        String output = outContent.toString();
        assertTrue(output.contains("Status"));
        assertTrue(output.contains("TODO"));
        assertTrue(output.contains("IN_PROGRESS"));
        assertTrue(output.contains("BLOCKED"));
        assertTrue(output.contains("IN_REVIEW"));
        assertTrue(output.contains("DONE"));
    }
    
    @Test
    void testAddNewTask_AllStatuses_WorkCorrectly() {
        // Test each status option
        String[] statusOptions = {"1", "2", "3", "4", "5"}; // TODO, IN_PROGRESS, BLOCKED, IN_REVIEW, DONE
        TaskStatusDTO[] expectedStatuses = {
                TaskStatusDTO.TODO, 
                TaskStatusDTO.IN_PROGRESS, 
                TaskStatusDTO.BLOCKED, 
                TaskStatusDTO.IN_REVIEW, 
                TaskStatusDTO.DONE
        };
        
        for (int i = 0; i < statusOptions.length; i++) {
            // Reset output stream
            outContent.reset();
            
            // Arrange
            String statusOption = statusOptions[i];
            TaskStatusDTO expectedStatus = expectedStatuses[i];
            
            String input = "1\nTest Task\n" + statusOption + "\n3\n"; 
            System.setIn(new ByteArrayInputStream(input.getBytes()));
            
            TaskServiceModel createdTask = new TaskServiceModel(
                    "new-id", "Test Task", TaskStatusMapper.toServiceStatus(expectedStatus));
            when(mockTaskService.createTask(any(TaskDTO.class))).thenReturn(createdTask);
            
            // Act
            workflowDemo.runDemo();
            
            // Verify TaskDTO was passed to service with correct status
            ArgumentCaptor<TaskDTO> taskCaptor = ArgumentCaptor.forClass(TaskDTO.class);
            verify(mockTaskService, atLeastOnce()).createTask(taskCaptor.capture());
            
            // Get the last captured value (in case there were multiple calls)
            List<TaskDTO> capturedTasks = taskCaptor.getAllValues();
            TaskDTO capturedTask = capturedTasks.get(capturedTasks.size() - 1);
            
            assertEquals(expectedStatus, capturedTask.getStatus());
            
            // Reset mocks for next iteration
            reset(mockTaskService);
        }
    }
} 