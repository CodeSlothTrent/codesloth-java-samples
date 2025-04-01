package com.cursor.automation.mapper;

import com.cursor.automation.dal.model.TaskEntity;
import com.cursor.automation.service.model.TaskServiceModel;
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
        TaskServiceModel serviceModel = new TaskServiceModel("test-id", "Test Task");
        
        // When
        TaskEntity entity = TaskEntityMapper.toEntity(serviceModel);
        
        // Then
        assertNotNull(entity);
        assertEquals(serviceModel.getId(), entity.getId());
        assertEquals(serviceModel.getTitle(), entity.getTitle());
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
        TaskEntity entity = new TaskEntity("test-id", "Test Task");
        
        // When
        TaskServiceModel serviceModel = TaskEntityMapper.toServiceModel(entity);
        
        // Then
        assertNotNull(serviceModel);
        assertEquals(entity.getId(), serviceModel.getId());
        assertEquals(entity.getTitle(), serviceModel.getTitle());
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
            new TaskEntity("id-1", "Task 1"),
            new TaskEntity("id-2", "Task 2"),
            new TaskEntity("id-3", "Task 3")
        );
        
        // When
        List<TaskServiceModel> serviceModels = TaskEntityMapper.toServiceModelList(entities);
        
        // Then
        assertNotNull(serviceModels);
        assertEquals(3, serviceModels.size());
        assertEquals("id-1", serviceModels.get(0).getId());
        assertEquals("Task 1", serviceModels.get(0).getTitle());
        assertEquals("id-2", serviceModels.get(1).getId());
        assertEquals("Task 2", serviceModels.get(1).getTitle());
        assertEquals("id-3", serviceModels.get(2).getId());
        assertEquals("Task 3", serviceModels.get(2).getTitle());
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
} 