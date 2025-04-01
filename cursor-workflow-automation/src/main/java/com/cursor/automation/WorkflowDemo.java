package com.cursor.automation;

import com.cursor.automation.dal.InMemoryTaskRepository;
import com.cursor.automation.dal.TaskRepository;
import com.cursor.automation.mapper.TaskDTOMapper;
import com.cursor.automation.model.dto.TaskDTO;
import com.cursor.automation.model.dto.TaskStatusDTO;
import com.cursor.automation.service.TaskService;
import com.cursor.automation.service.TaskServiceImpl;
import com.cursor.automation.service.model.TaskServiceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Scanner;

/**
 * Task Management Application Demo.
 * Demonstrates the task management functionality with an n-tier architecture.
 */
public class WorkflowDemo {
    
    private static final Logger logger = LoggerFactory.getLogger(WorkflowDemo.class);
    private final TaskService taskService;
    private final Scanner scanner;
    
    /**
     * Initializes the WorkflowDemo with necessary dependencies.
     */
    public WorkflowDemo() {
        // Initialize the repository
        TaskRepository taskRepository = new InMemoryTaskRepository();
        
        // Initialize the service with the repository
        taskService = new TaskServiceImpl(taskRepository);
        
        // Initialize scanner for user input
        scanner = new Scanner(System.in);
        
        logger.info("Task Management Application initialized");
    }
    
    /**
     * Runs the task management demo.
     */
    public void runDemo() {
        System.out.println("\n--- Task Management Application ---");
        
        // Add some sample tasks
        addSampleTasks();
        
        // Show the main menu
        showMainMenu();
        
        System.out.println("--- Application Closed ---\n");
    }
    
    private void addSampleTasks() {
        System.out.println("\nAdding sample tasks...");
        
        try {
            // Create some sample tasks - now using DTOs
            TaskDTO task1 = new TaskDTO();
            task1.setTitle("Setup development environment");
            task1.setStatus(TaskStatusDTO.DONE);
            taskService.createTask(task1);
            
            TaskDTO task2 = new TaskDTO();
            task2.setTitle("Design database schema");
            task2.setStatus(TaskStatusDTO.IN_PROGRESS);
            taskService.createTask(task2);
            
            TaskDTO task3 = new TaskDTO();
            task3.setTitle("Write documentation");
            task3.setStatus(TaskStatusDTO.TODO);
            taskService.createTask(task3);
            
            System.out.println("Sample tasks added successfully");
        } catch (Exception e) {
            logger.error("Error adding sample tasks", e);
            System.out.println("Error adding sample tasks: " + e.getMessage());
        }
    }
    
    private void showMainMenu() {
        boolean exit = false;
        
        while (!exit) {
            System.out.println("\n----- Main Menu -----");
            System.out.println("1. Add new task");
            System.out.println("2. View all tasks");
            System.out.println("3. Exit");
            System.out.print("Select an option: ");
            
            String choice = scanner.nextLine();
            
            switch (choice) {
                case "1":
                    addNewTask();
                    break;
                case "2":
                    viewAllTasks();
                    break;
                case "3":
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }
    
    private void addNewTask() {
        try {
            System.out.println("\n----- Add New Task -----");
            
            // Get task details from user - working at DTO level
            System.out.print("Enter task title: ");
            String title = scanner.nextLine();
            
            // Get task status from user
            TaskStatusDTO status = promptForTaskStatus();
            
            // Create a DTO with the input data
            TaskDTO taskDTO = new TaskDTO();
            taskDTO.setTitle(title);
            taskDTO.setStatus(status);
            
            // Call service to create task directly with DTO
            TaskServiceModel createdTask = taskService.createTask(taskDTO);
            
            // Convert created task back to DTO
            TaskDTO createdTaskDTO = TaskDTOMapper.toDTO(createdTask);
            
            System.out.println("Task created successfully with ID: " + createdTaskDTO.getId());
            
        } catch (Exception e) {
            logger.error("Error creating task", e);
            System.out.println("Error creating task: " + e.getMessage());
        }
    }
    
    private TaskStatusDTO promptForTaskStatus() {
        while (true) {
            System.out.println("\nSelect task status:");
            System.out.println("1. TODO");
            System.out.println("2. IN_PROGRESS");
            System.out.println("3. BLOCKED");
            System.out.println("4. IN_REVIEW");
            System.out.println("5. DONE");
            System.out.print("Enter choice (1-5): ");
            
            String choice = scanner.nextLine();
            
            switch (choice) {
                case "1": return TaskStatusDTO.TODO;
                case "2": return TaskStatusDTO.IN_PROGRESS;
                case "3": return TaskStatusDTO.BLOCKED;
                case "4": return TaskStatusDTO.IN_REVIEW;
                case "5": return TaskStatusDTO.DONE;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    
    private void viewAllTasks() {
        System.out.println("\n----- All Tasks -----");
        
        // Get all tasks from service - returns service models
        List<TaskServiceModel> taskServiceModels = taskService.getAllTasks();
        
        // Convert to DTOs for presentation
        List<TaskDTO> taskDTOs = TaskDTOMapper.toDTOList(taskServiceModels);
        
        if (taskDTOs.isEmpty()) {
            System.out.println("No tasks found.");
            return;
        }
        
        System.out.println(String.format("%-36s %-40s %-15s", 
                "ID", "Title", "Status"));
        System.out.println("-".repeat(95));
        
        for (TaskDTO taskDTO : taskDTOs) {
            System.out.println(String.format("%-36s %-40s %-15s",
                    taskDTO.getId(),
                    taskDTO.getTitle(),
                    taskDTO.getStatus()
            ));
        }
    }
} 