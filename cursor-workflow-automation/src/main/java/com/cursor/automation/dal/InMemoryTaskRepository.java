package com.cursor.automation.dal;

import com.cursor.automation.dal.model.TaskEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of the TaskRepository interface.
 * This is a simple implementation that stores tasks in memory.
 */
public class InMemoryTaskRepository implements TaskRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(InMemoryTaskRepository.class);
    private final Map<String, TaskEntity> taskStore = new ConcurrentHashMap<>();
    
    @Override
    public TaskEntity saveTask(TaskEntity taskEntity) {
        logger.debug("Saving task entity: {}", taskEntity);
        if (taskEntity == null) {
            throw new IllegalArgumentException("Task entity cannot be null");
        }
        
        if (taskEntity.getId() == null) {
            throw new IllegalArgumentException("Task entity ID cannot be null");
        }
        
        taskStore.put(taskEntity.getId(), taskEntity);
        return taskEntity;
    }
    
    @Override
    public Optional<TaskEntity> findById(String id) {
        logger.debug("Finding task entity by ID: {}", id);
        return Optional.ofNullable(taskStore.get(id));
    }
    
    @Override
    public List<TaskEntity> findAll() {
        logger.debug("Finding all task entities, current count: {}", taskStore.size());
        return new ArrayList<>(taskStore.values());
    }
} 