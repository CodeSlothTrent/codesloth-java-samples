package com.cursor.automation.factory;

import com.cursor.automation.dal.model.TaskEntity;
import com.cursor.automation.dal.model.TaskStatusEntity;
import com.cursor.automation.service.model.TaskServiceModel;
import com.cursor.automation.service.model.TaskStatusService;

import java.util.UUID;

/**
 * Factory class for creating Task models.
 */
public class TaskFactory {

    /**
     * Creates a new TaskServiceModel with a generated ID and default TODO status.
     * 
     * @param title the title for the task
     * @return a new TaskServiceModel with a generated ID
     */
    public static TaskServiceModel createServiceModel(String title) {
        String id = generateId();
        return new TaskServiceModel(id, title, TaskStatusService.TODO);
    }

    /**
     * Creates a new TaskServiceModel with a generated ID and specified status.
     * 
     * @param title the title for the task
     * @param status the status for the task
     * @return a new TaskServiceModel with a generated ID and specified status
     */
    public static TaskServiceModel createServiceModel(String title, TaskStatusService status) {
        String id = generateId();
        return new TaskServiceModel(id, title, status);
    }

    /**
     * Creates a new TaskEntity with a generated ID and default TODO status.
     * 
     * @param title the title for the task
     * @return a new TaskEntity with a generated ID
     */
    public static TaskEntity createEntity(String title) {
        String id = generateId();
        return new TaskEntity(id, title, TaskStatusEntity.TODO);
    }

    /**
     * Creates a new TaskEntity with a generated ID and specified status.
     * 
     * @param title the title for the task
     * @param status the status for the task
     * @return a new TaskEntity with a generated ID and specified status
     */
    public static TaskEntity createEntity(String title, TaskStatusEntity status) {
        String id = generateId();
        return new TaskEntity(id, title, status);
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