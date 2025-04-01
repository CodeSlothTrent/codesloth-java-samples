package com.cursor.automation.mapper;

import com.cursor.automation.model.dto.TaskDTO;
import com.cursor.automation.service.model.TaskServiceModel;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between Task DTOs and Service models.
 */
public class TaskDTOMapper {
    
    /**
     * Converts a TaskDTO to a TaskServiceModel.
     * 
     * @param dto the TaskDTO to convert
     * @return the converted TaskServiceModel
     */
    public static TaskServiceModel toServiceModel(TaskDTO dto) {
        if (dto == null) {
            return null;
        }
        
        return new TaskServiceModel(
            dto.getId(),
            dto.getTitle()
        );
    }
    
    /**
     * Converts a TaskServiceModel to a TaskDTO.
     * 
     * @param serviceModel the TaskServiceModel to convert
     * @return the converted TaskDTO
     */
    public static TaskDTO toDTO(TaskServiceModel serviceModel) {
        if (serviceModel == null) {
            return null;
        }
        
        return new TaskDTO(
            serviceModel.getId(),
            serviceModel.getTitle()
        );
    }
    
    /**
     * Converts a list of TaskServiceModels to a list of TaskDTOs.
     * 
     * @param serviceModels the list of TaskServiceModels to convert
     * @return the list of converted TaskDTOs
     */
    public static List<TaskDTO> toDTOList(List<TaskServiceModel> serviceModels) {
        if (serviceModels == null) {
            return null;
        }
        
        return serviceModels.stream()
            .map(TaskDTOMapper::toDTO)
            .collect(Collectors.toList());
    }
} 