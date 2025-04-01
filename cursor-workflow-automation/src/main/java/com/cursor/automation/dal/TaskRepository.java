package com.cursor.automation.dal;

import com.cursor.automation.dal.model.TaskEntity;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Task data access operations.
 */
public interface TaskRepository {
    
    /**
     * Saves a new task to the repository.
     * 
     * @param taskEntity the task entity to save
     * @return the saved task entity
     */
    TaskEntity saveTask(TaskEntity taskEntity);
    
    /**
     * Retrieves a task by its ID.
     * 
     * @param id the task ID
     * @return an Optional containing the task entity if found, or empty if not found
     */
    Optional<TaskEntity> findById(String id);
    
    /**
     * Retrieves all tasks from the repository.
     * 
     * @return a list of all task entities
     */
    List<TaskEntity> findAll();
} 