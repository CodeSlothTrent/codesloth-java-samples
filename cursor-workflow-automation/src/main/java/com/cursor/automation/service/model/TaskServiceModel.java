package com.cursor.automation.service.model;

/**
 * Service layer model for Task business logic.
 */
public class TaskServiceModel {
    private String id;
    private String title;

    public TaskServiceModel() {
    }

    public TaskServiceModel(String id, String title) {
        this.id = id;
        this.title = title;
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

    @Override
    public String toString() {
        return "TaskServiceModel{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
} 