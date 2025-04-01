package com.cursor.automation.service;

import com.cursor.automation.dal.InMemoryTaskRepository;
import com.cursor.automation.dal.TaskRepository;
import com.cursor.automation.model.dto.TaskDTO;
import com.cursor.automation.service.model.TaskServiceModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the TaskServiceImpl class.
 */
public class TaskServiceImplTest {
    
    private TaskService taskService;
    private TaskRepository taskRepository;
    
    @BeforeEach
    public void setUp() {
        taskRepository = new InMemoryTaskRepository();
        taskService = new TaskServiceImpl(taskRepository);
    }
    
    @Test
    public void testCreateTask_ValidDTO_ReturnsCreatedTask() {
        // Given
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setTitle("Test Task");
        
        // When
        TaskServiceModel createdTask = taskService.createTask(taskDTO);
        
        // Then
        assertNotNull(createdTask);
        assertNotNull(createdTask.getId());
        assertEquals(taskDTO.getTitle(), createdTask.getTitle());
    }
    
    @Test
    public void testCreateTask_DTOWithId_PreservesId() {
        // Given
        TaskDTO taskDTO = new TaskDTO("custom-id", "Test Task");
        
        // When
        TaskServiceModel createdTask = taskService.createTask(taskDTO);
        
        // Then
        assertNotNull(createdTask);
        assertEquals("custom-id", createdTask.getId());
        assertEquals(taskDTO.getTitle(), createdTask.getTitle());
    }
    
    @Test
    public void testCreateTask_NullDTO_ThrowsException() {
        // Given
        TaskDTO taskDTO = null;
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            taskService.createTask(taskDTO);
        });
    }
    
    @Test
    public void testCreateTask_EmptyTitle_ThrowsException() {
        // Given
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setTitle("");
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            taskService.createTask(taskDTO);
        });
    }
    
    @Test
    public void testGetAllTasks_ReturnsAllCreatedTasks() {
        // Given
        TaskDTO task1 = new TaskDTO();
        task1.setTitle("Task 1");
        taskService.createTask(task1);
        
        TaskDTO task2 = new TaskDTO();
        task2.setTitle("Task 2");
        taskService.createTask(task2);
        
        TaskDTO task3 = new TaskDTO();
        task3.setTitle("Task 3");
        taskService.createTask(task3);
        
        // When
        List<TaskServiceModel> tasks = taskService.getAllTasks();
        
        // Then
        assertEquals(3, tasks.size());
    }
    
    @Test
    public void testGetTaskById_ExistingId_ReturnsTask() {
        // Given
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setTitle("Test Task");
        TaskServiceModel createdTask = taskService.createTask(taskDTO);
        String taskId = createdTask.getId();
        
        // When
        Optional<TaskServiceModel> foundTask = taskService.getTaskById(taskId);
        
        // Then
        assertTrue(foundTask.isPresent());
        assertEquals(taskId, foundTask.get().getId());
        assertEquals("Test Task", foundTask.get().getTitle());
    }
    
    @Test
    public void testGetTaskById_NonExistingId_ReturnsEmptyOptional() {
        // Given
        String nonExistingId = "non-existing-id";
        
        // When
        Optional<TaskServiceModel> foundTask = taskService.getTaskById(nonExistingId);
        
        // Then
        assertFalse(foundTask.isPresent());
    }
} 