package com.cursor.automation.mapper;

import com.cursor.automation.dal.model.TaskEntity;
import com.cursor.automation.dal.model.TaskStatusEntity;
import com.cursor.automation.service.model.TaskServiceModel;
import com.cursor.automation.service.model.TaskStatusService;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TaskEntityMapper.
 */
public class TaskEntityMapperTest {

    @Test
    public void testToEntity_ValidServiceModel_ReturnsEntity() {
        // Given
        TaskServiceModel serviceModel = new TaskServiceModel("test-id", "Test Task", TaskStatusService.IN_PROGRESS);
        
        // When
        TaskEntity entity = TaskEntityMapper.toEntity(serviceModel);
        
        // Then
        assertNotNull(entity);
        assertEquals(serviceModel.getId(), entity.getId());
        assertEquals(serviceModel.getTitle(), entity.getTitle());
        assertEquals(TaskStatusEntity.IN_PROGRESS, entity.getStatus());
    }
    
    @Test
    public void testToEntity_NullServiceModel_ReturnsNull() {
        // Given
        TaskServiceModel serviceModel = null;
        
        // When
        TaskEntity entity = TaskEntityMapper.toEntity(serviceModel);
        
        // Then
        assertNull(entity);
    }
    
    @Test
    public void testToServiceModel_ValidEntity_ReturnsServiceModel() {
        // Given
        TaskEntity entity = new TaskEntity("test-id", "Test Task", TaskStatusEntity.TODO);
        
        // When
        TaskServiceModel serviceModel = TaskEntityMapper.toServiceModel(entity);
        
        // Then
        assertNotNull(serviceModel);
        assertEquals(entity.getId(), serviceModel.getId());
        assertEquals(entity.getTitle(), serviceModel.getTitle());
        assertEquals(TaskStatusService.TODO, serviceModel.getStatus());
    }
    
    @Test
    public void testToServiceModel_NullEntity_ReturnsNull() {
        // Given
        TaskEntity entity = null;
        
        // When
        TaskServiceModel serviceModel = TaskEntityMapper.toServiceModel(entity);
        
        // Then
        assertNull(serviceModel);
    }
    
    @Test
    public void testToServiceModelList_ValidEntities_ReturnsServiceModelList() {
        // Given
        List<TaskEntity> entities = Arrays.asList(
            new TaskEntity("id-1", "Task 1", TaskStatusEntity.TODO),
            new TaskEntity("id-2", "Task 2", TaskStatusEntity.IN_PROGRESS)
        );
        
        // When
        List<TaskServiceModel> serviceModels = TaskEntityMapper.toServiceModelList(entities);
        
        // Then
        assertNotNull(serviceModels);
        assertEquals(2, serviceModels.size());
        assertEquals("id-1", serviceModels.get(0).getId());
        assertEquals("Task 1", serviceModels.get(0).getTitle());
        assertEquals(TaskStatusService.TODO, serviceModels.get(0).getStatus());
        assertEquals("id-2", serviceModels.get(1).getId());
        assertEquals("Task 2", serviceModels.get(1).getTitle());
        assertEquals(TaskStatusService.IN_PROGRESS, serviceModels.get(1).getStatus());
    }
    
    @Test
    public void testToServiceModelList_NullEntities_ReturnsNull() {
        // Given
        List<TaskEntity> entities = null;
        
        // When
        List<TaskServiceModel> serviceModels = TaskEntityMapper.toServiceModelList(entities);
        
        // Then
        assertNull(serviceModels);
    }
    
    @Test
    public void testToEntity_AllStatusValues_MapsCorrectly() {
        // Test for all status values
        for (var status : TaskStatusService.values()) {
            // Given
            TaskServiceModel serviceModel = new TaskServiceModel("test-id", "Test Task", status);
            
            // When
            TaskEntity entity = TaskEntityMapper.toEntity(serviceModel);
            
            // Then
            assertEquals(status, entity.getStatus());
        }
    }
    
    @Test
    public void testToServiceModel_AllStatusValues_MapsCorrectly() {
        // Test for all status values
        for (TaskStatusEntity status : TaskStatusEntity.values()) {
            // Given
            TaskEntity entity = new TaskEntity("test-id", "Test Task", status);
            
            // When
            TaskServiceModel serviceModel = TaskEntityMapper.toServiceModel(entity);
            
            // Then
            assertEquals(status, serviceModel.getStatus());
        }
    }
} 