package com.cursor.automation.service.model;

/**
 * Service layer model for Task business logic.
 */
public class TaskServiceModel {
    private String id;
    private String title;
    private TaskStatusService status;

    public TaskServiceModel() {
    }

    public TaskServiceModel(String id, String title) {
        this.id = id;
        this.title = title;
        this.status = TaskStatusService.TODO; // Default status
    }

    public TaskServiceModel(String id, String title, TaskStatusService status) {
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

    public TaskStatusService getStatus() {
        return status;
    }

    public void setStatus(TaskStatusService status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "TaskServiceModel{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", status=" + status +
                '}';
    }
} 