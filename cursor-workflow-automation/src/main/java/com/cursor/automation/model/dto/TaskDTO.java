package com.cursor.automation.model.dto;

/**
 * Data Transfer Object for Task to be used with presentation layer.
 */
public class TaskDTO {
    private String id;
    private String title;

    public TaskDTO() {
    }

    public TaskDTO(String id, String title) {
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
        return "TaskDTO{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
} 