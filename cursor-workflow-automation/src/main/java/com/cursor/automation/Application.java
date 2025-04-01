package com.cursor.automation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main application class for the Task Management Application.
 */
public class Application {
    
    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    
    public static void main(String[] args) {
        logger.info("Starting Task Management Application");
        
        System.out.println("========================================");
        System.out.println("    TASK MANAGEMENT APPLICATION");
        System.out.println("========================================");
        System.out.println("Java version: " + System.getProperty("java.version"));
        System.out.println("Operating system: " + System.getProperty("os.name"));
        
        try {
            // Initialize and run the task management workflow
            WorkflowDemo taskManager = new WorkflowDemo();
            taskManager.runDemo();
            
            logger.info("Application completed successfully");
        } catch (Exception e) {
            logger.error("Error running application", e);
            System.err.println("An error occurred: " + e.getMessage());
        }
    }
} 