package com.cursor.automation.service;

import com.cursor.automation.dal.TaskRepository;
import com.cursor.automation.dal.model.TaskEntity;
import com.cursor.automation.factory.TaskFactory;
import com.cursor.automation.mapper.TaskDTOMapper;
import com.cursor.automation.mapper.TaskEntityMapper;
import com.cursor.automation.model.dto.TaskDTO;
import com.cursor.automation.service.model.TaskServiceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of the TaskService interface.
 */
public class TaskServiceImpl implements TaskService {
    
    private static final Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);
    private final TaskRepository taskRepository;
    
    public TaskServiceImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }
    
    @Override
    public TaskServiceModel createTask(TaskDTO taskDTO) {
        logger.info("Creating new task from DTO: {}", taskDTO);
        
        // Validate inputs
        if (taskDTO == null) {
            throw new IllegalArgumentException("Task DTO cannot be null");
        }
        
        String title = taskDTO.getTitle();
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Task title cannot be empty");
        }
        
        // Convert DTO to service model
        TaskServiceModel taskServiceModel = TaskDTOMapper.toServiceModel(taskDTO);
        
        // If there's no ID, generate one
        if (taskServiceModel.getId() == null || taskServiceModel.getId().trim().isEmpty()) {
            taskServiceModel = TaskFactory.createServiceModel(taskServiceModel.getTitle());
        }
        
        logger.debug("Created task service model with ID: {}", taskServiceModel.getId());
        
        // Convert to entity and save
        TaskEntity taskEntity = TaskEntityMapper.toEntity(taskServiceModel);
        TaskEntity savedEntity = taskRepository.saveTask(taskEntity);
        
        // Convert back to service model and return
        return TaskEntityMapper.toServiceModel(savedEntity);
    }
    
    @Override
    public Optional<TaskServiceModel> getTaskById(String id) {
        logger.info("Getting task by ID: {}", id);
        
        // Get entity from repository
        Optional<TaskEntity> taskEntityOptional = taskRepository.findById(id);
        
        // Convert to service model if present
        return taskEntityOptional.map(TaskEntityMapper::toServiceModel);
    }
    
    @Override
    public List<TaskServiceModel> getAllTasks() {
        logger.info("Getting all tasks");
        
        // Get all entities from repository
        List<TaskEntity> taskEntities = taskRepository.findAll();
        
        // Convert to service models and return
        return TaskEntityMapper.toServiceModelList(taskEntities);
    }
} 