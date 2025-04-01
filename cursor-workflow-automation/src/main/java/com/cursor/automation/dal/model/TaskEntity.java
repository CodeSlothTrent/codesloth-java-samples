package com.cursor.automation.dal.model;

/**
 * Entity class for Task to be used with data access layer.
 */
public class TaskEntity {
    private String id;
    private String title;
    private TaskStatusEntity status;

    public TaskEntity() {
    }

    public TaskEntity(String id, String title) {
        this.id = id;
        this.title = title;
        this.status = TaskStatusEntity.TODO; // Default status
    }

    public TaskEntity(String id, String title, TaskStatusEntity status) {
        this.id = id;
        this.title = title;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public TaskStatusEntity getStatus() {
        return status;
    }

    public void setStatus(TaskStatusEntity status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "TaskEntity{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", status=" + status +
                '}';
    }
} 