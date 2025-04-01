package com.cursor.automation.mapper;

import com.cursor.automation.model.dto.TaskDTO;
import com.cursor.automation.model.dto.TaskStatusDTO;
import com.cursor.automation.service.model.TaskServiceModel;
import com.cursor.automation.service.model.TaskStatusService;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TaskDTOMapper.
 */
public class TaskDTOMapperTest {

    @Test
    void toServiceModel_ValidDTO_ReturnsServiceModel() {
        // Arrange
        TaskDTO dto = new TaskDTO("123", "Test Task", TaskStatusDTO.TODO);
        
        // Act
        TaskServiceModel serviceModel = TaskDTOMapper.toServiceModel(dto);
        
        // Assert
        assertNotNull(serviceModel);
        assertEquals(dto.getId(), serviceModel.getId());
        assertEquals(dto.getTitle(), serviceModel.getTitle());
        assertEquals(TaskStatusService.TODO, serviceModel.getStatus());
    }
    
    @Test
    void toDTO_ValidServiceModel_ReturnsDTO() {
        // Arrange
        TaskServiceModel serviceModel = new TaskServiceModel("123", "Test Task", TaskStatusService.IN_PROGRESS);
        
        // Act
        TaskDTO dto = TaskDTOMapper.toDTO(serviceModel);
        
        // Assert
        assertNotNull(dto);
        assertEquals(serviceModel.getId(), dto.getId());
        assertEquals(serviceModel.getTitle(), dto.getTitle());
        assertEquals(TaskStatusDTO.IN_PROGRESS, dto.getStatus());
    }
    
    @Test
    void toDTOList_ValidServiceModelList_ReturnsDTOList() {
        // Arrange
        List<TaskServiceModel> serviceModels = Arrays.asList(
                new TaskServiceModel("1", "Task 1", TaskStatusService.TODO),
                new TaskServiceModel("2", "Task 2", TaskStatusService.IN_PROGRESS)
        );
        
        // Act
        List<TaskDTO> dtos = TaskDTOMapper.toDTOList(serviceModels);
        
        // Assert
        assertNotNull(dtos);
        assertEquals(2, dtos.size());
        
        assertEquals("1", dtos.get(0).getId());
        assertEquals("Task 1", dtos.get(0).getTitle());
        assertEquals(TaskStatusDTO.TODO, dtos.get(0).getStatus());
        
        assertEquals("2", dtos.get(1).getId());
        assertEquals("Task 2", dtos.get(1).getTitle());
        assertEquals(TaskStatusDTO.IN_PROGRESS, dtos.get(1).getStatus());
    }
    
    @Test
    void toServiceModel_NullDTO_ReturnsNull() {
        // Act & Assert
        assertNull(TaskDTOMapper.toServiceModel(null));
    }
    
    @Test
    void toDTO_NullServiceModel_ReturnsNull() {
        // Act & Assert
        assertNull(TaskDTOMapper.toDTO(null));
    }
    
    @Test
    void toServiceModel_AllStatusValues_MapsCorrectly() {
        // Test mapping for all status values
        for (TaskStatusDTO status : TaskStatusDTO.values()) {
            // Arrange
            TaskDTO dto = new TaskDTO("123", "Test Task", status);
            
            // Act
            TaskServiceModel serviceModel = TaskDTOMapper.toServiceModel(dto);
            
            // Assert
            assertNotNull(serviceModel);
            TaskStatusService expectedStatus = TaskStatusMapper.toServiceStatus(status);
            assertEquals(expectedStatus, serviceModel.getStatus());
        }
    }
    
    @Test
    void toDTO_AllStatusValues_MapsCorrectly() {
        // Test mapping for all status values
        for (TaskStatusService status : TaskStatusService.values()) {
            // Arrange
            TaskServiceModel serviceModel = new TaskServiceModel("123", "Test Task", status);
            
            // Act
            TaskDTO dto = TaskDTOMapper.toDTO(serviceModel);
            
            // Assert
            assertNotNull(dto);
            TaskStatusDTO expectedStatus = TaskStatusMapper.toDTOStatus(status);
            assertEquals(expectedStatus, dto.getStatus());
        }
    }
} 