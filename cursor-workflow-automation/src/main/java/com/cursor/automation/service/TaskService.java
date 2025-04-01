package com.cursor.automation.service;

import com.cursor.automation.model.dto.TaskDTO;
import com.cursor.automation.service.model.TaskServiceModel;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for task management business logic.
 */
public interface TaskService {
    
    /**
     * Creates a new task from the provided task DTO.
     * 
     * @param taskDTO the task DTO containing task information
     * @return the created task service model
     */
    TaskServiceModel createTask(TaskDTO taskDTO);
    
    /**
     * Retrieves a task by its ID.
     * 
     * @param id the task ID
     * @return an Optional containing the task service model if found, or empty if not found
     */
    Optional<TaskServiceModel> getTaskById(String id);
    
    /**
     * Retrieves all tasks.
     * 
     * @return a list of all task service models
     */
    List<TaskServiceModel> getAllTasks();
} 