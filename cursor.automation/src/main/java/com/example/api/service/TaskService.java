package com.example.api.service;

import com.example.api.model.Task;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
} 