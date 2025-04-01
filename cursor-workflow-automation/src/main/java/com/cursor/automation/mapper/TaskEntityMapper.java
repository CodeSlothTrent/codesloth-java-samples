package com.cursor.automation.mapper;

import com.cursor.automation.dal.model.TaskEntity;
import com.cursor.automation.service.model.TaskServiceModel;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between TaskEntity and TaskServiceModel.
 */
public class TaskEntityMapper {
    
    /**
     * Converts a TaskServiceModel to a TaskEntity.
     * 
     * @param serviceModel the TaskServiceModel to convert
     * @return the converted TaskEntity
     */
    public static TaskEntity toEntity(TaskServiceModel serviceModel) {
        if (serviceModel == null) {
            return null;
        }
        
        return new TaskEntity(
            serviceModel.getId(),
            serviceModel.getTitle()
        );
    }
    
    /**
     * Converts a TaskEntity to a TaskServiceModel.
     * 
     * @param entity the TaskEntity to convert
     * @return the converted TaskServiceModel
     */
    public static TaskServiceModel toServiceModel(TaskEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return new TaskServiceModel(
            entity.getId(),
            entity.getTitle()
        );
    }
    
    /**
     * Converts a list of TaskEntities to a list of TaskServiceModels.
     * 
     * @param entities the list of TaskEntities to convert
     * @return the list of converted TaskServiceModels
     */
    public static List<TaskServiceModel> toServiceModelList(List<TaskEntity> entities) {
        if (entities == null) {
            return null;
        }
        
        return entities.stream()
            .map(TaskEntityMapper::toServiceModel)
            .collect(Collectors.toList());
    }
} 