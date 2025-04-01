package com.cursor.automation.mapper;

import com.cursor.automation.model.dto.TaskDTO;
import com.cursor.automation.service.model.TaskServiceModel;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TaskDTOMapper.
 */
public class TaskDTOMapperTest {

    @Test
    public void testToServiceModel_ValidDTO_ReturnsServiceModel() {
        // Given
        TaskDTO dto = new TaskDTO("test-id", "Test Task");
        
        // When
        TaskServiceModel serviceModel = TaskDTOMapper.toServiceModel(dto);
        
        // Then
        assertNotNull(serviceModel);
        assertEquals(dto.getId(), serviceModel.getId());
        assertEquals(dto.getTitle(), serviceModel.getTitle());
    }
    
    @Test
    public void testToServiceModel_NullDTO_ReturnsNull() {
        // Given
        TaskDTO dto = null;
        
        // When
        TaskServiceModel serviceModel = TaskDTOMapper.toServiceModel(dto);
        
        // Then
        assertNull(serviceModel);
    }
    
    @Test
    public void testToDTO_ValidServiceModel_ReturnsDTO() {
        // Given
        TaskServiceModel serviceModel = new TaskServiceModel("test-id", "Test Task");
        
        // When
        TaskDTO dto = TaskDTOMapper.toDTO(serviceModel);
        
        // Then
        assertNotNull(dto);
        assertEquals(serviceModel.getId(), dto.getId());
        assertEquals(serviceModel.getTitle(), dto.getTitle());
    }
    
    @Test
    public void testToDTO_NullServiceModel_ReturnsNull() {
        // Given
        TaskServiceModel serviceModel = null;
        
        // When
        TaskDTO dto = TaskDTOMapper.toDTO(serviceModel);
        
        // Then
        assertNull(dto);
    }
    
    @Test
    public void testToDTOList_ValidServiceModels_ReturnsDTOList() {
        // Given
        List<TaskServiceModel> serviceModels = Arrays.asList(
            new TaskServiceModel("id-1", "Task 1"),
            new TaskServiceModel("id-2", "Task 2"),
            new TaskServiceModel("id-3", "Task 3")
        );
        
        // When
        List<TaskDTO> dtos = TaskDTOMapper.toDTOList(serviceModels);
        
        // Then
        assertNotNull(dtos);
        assertEquals(3, dtos.size());
        assertEquals("id-1", dtos.get(0).getId());
        assertEquals("Task 1", dtos.get(0).getTitle());
        assertEquals("id-2", dtos.get(1).getId());
        assertEquals("Task 2", dtos.get(1).getTitle());
        assertEquals("id-3", dtos.get(2).getId());
        assertEquals("Task 3", dtos.get(2).getTitle());
    }
    
    @Test
    public void testToDTOList_NullServiceModels_ReturnsNull() {
        // Given
        List<TaskServiceModel> serviceModels = null;
        
        // When
        List<TaskDTO> dtos = TaskDTOMapper.toDTOList(serviceModels);
        
        // Then
        assertNull(dtos);
    }
} 