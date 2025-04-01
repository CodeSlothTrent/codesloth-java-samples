package com.cursor.automation.dal.model;

/**
 * Entity class for Task to be used with data access layer.
 */
public class TaskEntity {
    private String id;
    private String title;

    public TaskEntity() {
    }

    public TaskEntity(String id, String title) {
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
        return "TaskEntity{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
} 