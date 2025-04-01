package com.cursor.automation.service;

import com.cursor.automation.dal.TaskRepository;
import com.cursor.automation.dal.model.TaskEntity;
import com.cursor.automation.dal.model.TaskStatusEntity;
import com.cursor.automation.model.dto.TaskDTO;
import com.cursor.automation.model.dto.TaskStatusDTO;
import com.cursor.automation.mapper.TaskStatusMapper;
import com.cursor.automation.service.model.TaskServiceModel;
import com.cursor.automation.service.model.TaskStatusService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests for the TaskServiceImpl class.
 */
public class TaskServiceImplTest {
    
    private TaskRepository taskRepository;
    private TaskService taskService;
    
    @BeforeEach
    void setUp() {
        taskRepository = mock(TaskRepository.class);
        taskService = new TaskServiceImpl(taskRepository);
    }
    
    @Test
    void createTask_WithValidData_ShouldReturnCreatedTask() {
        // Arrange
        String id = "123";
        String title = "Test Task";
        TaskStatusDTO status = TaskStatusDTO.TODO;
        
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setTitle(title);
        taskDTO.setStatus(status);
        
        TaskEntity savedEntity = new TaskEntity(id, title, TaskStatusEntity.TODO);
        when(taskRepository.saveTask(any(TaskEntity.class))).thenReturn(savedEntity);

        // Act
        TaskServiceModel result = taskService.createTask(taskDTO);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(title, result.getTitle());
        assertEquals(TaskStatusService.TODO, result.getStatus());
        
        // Verify repository was called
        verify(taskRepository).saveTask(any(TaskEntity.class));
    }
    
    @Test
    void createTask_WithNullTitle_ShouldThrowException() {
        // Arrange
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setTitle(null);
        taskDTO.setStatus(TaskStatusDTO.TODO);

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.createTask(taskDTO);
        });
        
        assertTrue(exception.getMessage().contains("title"));
        
        // Verify repository was not called
        verify(taskRepository, never()).saveTask(any());
    }
    
    @Test
    void createTask_WithEmptyTitle_ShouldThrowException() {
        // Arrange
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setTitle("");
        taskDTO.setStatus(TaskStatusDTO.TODO);

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.createTask(taskDTO);
        });
        
        assertTrue(exception.getMessage().contains("title"));
        
        // Verify repository was not called
        verify(taskRepository, never()).saveTask(any());
    }
    
    @Test
    void getAllTasks_ShouldReturnAllTasks() {
        // Arrange
        List<TaskEntity> taskEntities = Arrays.asList(
                new TaskEntity("1", "Task 1", TaskStatusEntity.TODO),
                new TaskEntity("2", "Task 2", TaskStatusEntity.IN_PROGRESS)
        );
        
        when(taskRepository.findAll()).thenReturn(taskEntities);

        // Act
        List<TaskServiceModel> results = taskService.getAllTasks();

        // Assert
        assertEquals(2, results.size());
        assertEquals("1", results.get(0).getId());
        assertEquals("Task 1", results.get(0).getTitle());
        assertEquals(TaskStatusService.TODO, results.get(0).getStatus());
        assertEquals("2", results.get(1).getId());
        assertEquals("Task 2", results.get(1).getTitle());
        assertEquals(TaskStatusService.IN_PROGRESS, results.get(1).getStatus());
        
        // Verify repository was called
        verify(taskRepository).findAll();
    }
    
    @Test
    void getTaskById_WithExistingId_ShouldReturnTask() {
        // Arrange
        String id = "123";
        TaskEntity taskEntity = new TaskEntity(id, "Test Task", TaskStatusEntity.IN_REVIEW);
        
        when(taskRepository.findById(id)).thenReturn(Optional.of(taskEntity));

        // Act
        Optional<TaskServiceModel> result = taskService.getTaskById(id);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        assertEquals("Test Task", result.get().getTitle());
        assertEquals(TaskStatusService.IN_REVIEW, result.get().getStatus());
        
        // Verify repository was called
        verify(taskRepository).findById(id);
    }
    
    @Test
    void getTaskById_WithNonExistingId_ShouldReturnEmptyOptional() {
        // Arrange
        String id = "non-existing";
        
        when(taskRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        Optional<TaskServiceModel> result = taskService.getTaskById(id);

        // Assert
        assertFalse(result.isPresent());
        
        // Verify repository was called
        verify(taskRepository).findById(id);
    }
    
    @Test
    public void testGetAllTasks_ReturnsAllCreatedTasks() {
        // Given
        TaskDTO task1 = new TaskDTO();
        task1.setTitle("Task 1");
        task1.setStatus(TaskStatusDTO.TODO);
        taskService.createTask(task1);
        
        TaskDTO task2 = new TaskDTO();
        task2.setTitle("Task 2");
        task2.setStatus(TaskStatusDTO.IN_PROGRESS);
        taskService.createTask(task2);
        
        TaskDTO task3 = new TaskDTO();
        task3.setTitle("Task 3");
        task3.setStatus(TaskStatusDTO.DONE);
        taskService.createTask(task3);
        
        // When
        List<TaskServiceModel> tasks = taskService.getAllTasks();
        
        // Then
        assertEquals(3, tasks.size());
        // Verify the statuses are preserved
        assertTrue(tasks.stream().anyMatch(task -> task.getStatus() == TaskStatusService.TODO));
        assertTrue(tasks.stream().anyMatch(task -> task.getStatus() == TaskStatusService.IN_PROGRESS));
        assertTrue(tasks.stream().anyMatch(task -> task.getStatus() == TaskStatusService.DONE));
    }
    
    @Test
    public void testGetTaskById_ExistingId_ReturnsTask() {
        // Given
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setTitle("Test Task");
        taskDTO.setStatus(TaskStatusDTO.BLOCKED);
        TaskServiceModel createdTask = taskService.createTask(taskDTO);
        String taskId = createdTask.getId();
        
        // When
        Optional<TaskServiceModel> foundTask = taskService.getTaskById(taskId);
        
        // Then
        assertTrue(foundTask.isPresent());
        assertEquals(taskId, foundTask.get().getId());
        assertEquals("Test Task", foundTask.get().getTitle());
        assertEquals(TaskStatusService.BLOCKED, foundTask.get().getStatus());
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
    
    @Test
    public void testCreateTask_DefaultStatus_SetsTodoStatus() {
        // Given
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setTitle("Test Task");
        // Note: not setting the status
        
        // When
        TaskServiceModel createdTask = taskService.createTask(taskDTO);
        
        // Then
        assertNotNull(createdTask);
        assertEquals("Test Task", createdTask.getTitle());
        assertEquals(TaskStatusService.TODO, createdTask.getStatus());
    }
    
    @Test
    public void testCreateTask_DifferentStatuses_PreservesStatuses() {
        // Given
        TaskStatusDTO[] statuses = TaskStatusDTO.values();
        
        for (TaskStatusDTO status : statuses) {
            TaskDTO taskDTO = new TaskDTO();
            taskDTO.setTitle("Task with status: " + status);
            taskDTO.setStatus(status);
            
            // When
            TaskServiceModel createdTask = taskService.createTask(taskDTO);
            
            // Then
            assertEquals(TaskStatusMapper.toServiceStatus(status), createdTask.getStatus());
        }
    }
} 