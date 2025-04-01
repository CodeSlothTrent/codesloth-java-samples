package com.cursor.automation.factory;

import com.cursor.automation.dal.model.TaskEntity;
import com.cursor.automation.service.model.TaskServiceModel;

import java.util.UUID;

/**
 * Factory class for creating Task models.
 */
public class TaskFactory {

    /**
     * Creates a new TaskServiceModel with a generated ID.
     * 
     * @param title the title for the task
     * @return a new TaskServiceModel with a generated ID
     */
    public static TaskServiceModel createServiceModel(String title) {
        String id = generateId();
        return new TaskServiceModel(id, title);
    }

    /**
     * Creates a new TaskEntity with a generated ID.
     * 
     * @param title the title for the task
     * @return a new TaskEntity with a generated ID
     */
    public static TaskEntity createEntity(String title) {
        String id = generateId();
        return new TaskEntity(id, title);
    }

    /**
     * Generates a unique ID for a task.
     * 
     * @return a unique ID
     */
    private static String generateId() {
        return UUID.randomUUID().toString();
    }
} 