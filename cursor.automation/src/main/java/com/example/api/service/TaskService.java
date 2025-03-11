package com.example.api.service;

import com.example.api.model.Task;
import com.example.api.model.TaskSearchParams;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TaskService {
    
    private final Map<String, Task> tasks = new HashMap<>();
    
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }
    
    public Task getTaskById(String id) {
        return tasks.get(id);
    }
    
    public Task createTask(Task task) {
        tasks.put(task.getId(), task);
        return task;
    }
    
    public Task updateTask(String id, Task taskDetails) {
        Task task = tasks.get(id);
        if (task != null) {
            if (taskDetails.getTitle() != null) {
                task.setTitle(taskDetails.getTitle());
            }
            if (taskDetails.getDescription() != null) {
                task.setDescription(taskDetails.getDescription());
            }
            task.setCompleted(taskDetails.isCompleted());
            tasks.put(id, task);
        }
        return task;
    }
    
    public boolean deleteTask(String id) {
        Task removed = tasks.remove(id);
        return removed != null;
    }
    
    public List<Task> searchTasks(TaskSearchParams searchParams) {
        return tasks.values().stream()
            .filter(task -> {
                // Match by ID if provided
                if (searchParams.id() != null && !searchParams.id().isEmpty() && 
                    !task.getId().contains(searchParams.id())) {
                    return false;
                }
                
                // Match by title if provided
                if (searchParams.title() != null && !searchParams.title().isEmpty() && 
                    (task.getTitle() == null || !task.getTitle().toLowerCase().contains(searchParams.title().toLowerCase()))) {
                    return false;
                }
                
                // Match by description if provided
                if (searchParams.description() != null && !searchParams.description().isEmpty() && 
                    (task.getDescription() == null || !task.getDescription().toLowerCase().contains(searchParams.description().toLowerCase()))) {
                    return false;
                }
                
                // Match by completed status if provided
                if (searchParams.completed() != null && 
                    task.isCompleted() != searchParams.completed()) {
                    return false;
                }
                
                // Match by createdAt range if provided
                if (searchParams.createdAtStart() != null && 
                    task.getCreatedAt().isBefore(searchParams.createdAtStart())) {
                    return false;
                }
                
                if (searchParams.createdAtEnd() != null && 
                    task.getCreatedAt().isAfter(searchParams.createdAtEnd())) {
                    return false;
                }
                
                // Match by updatedAt range if provided
                if (searchParams.updatedAtStart() != null && 
                    task.getUpdatedAt().isBefore(searchParams.updatedAtStart())) {
                    return false;
                }
                
                if (searchParams.updatedAtEnd() != null && 
                    task.getUpdatedAt().isAfter(searchParams.updatedAtEnd())) {
                    return false;
                }
                
                return true;
            })
            .collect(Collectors.toList());
    }
} 