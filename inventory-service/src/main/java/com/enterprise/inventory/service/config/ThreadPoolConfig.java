package com.enterprise.inventory.service.config;

import lombok.extern.slf4j.Slf4j; // Import Lombok's SLF4J annotation for logging
import org.springframework.context.annotation.Bean; // Import Bean annotation for Spring component definition
import org.springframework.context.annotation.Configuration; // Import Configuration annotation for Spring configuration class
import org.springframework.scheduling.annotation.EnableAsync; // Import EnableAsync annotation for asynchronous processing
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor; // Import Spring's thread pool task executor

import java.util.concurrent.Executor; // Import Executor interface
import java.util.concurrent.ThreadPoolExecutor; // Import Java's thread pool executor

/**
 * Configuration class for Thread Pool and Multithreading setup
 * This class configures custom thread pools for asynchronous processing
 * and optimizes concurrent operations in the inventory management system
 * 
 * @Configuration: Marks this class as a Spring configuration class
 * @EnableAsync: Enables Spring's asynchronous method execution capability
 * @Slf4j: Lombok annotation to add SLF4J logging capabilities
 */
@Configuration
@EnableAsync
@Slf4j
public class ThreadPoolConfig {

    /**
     * Bean definition for custom thread pool task executor
     * This executor is used for asynchronous operations like inventory updates,
     * report generation, and bulk data processing
     * 
     * @return Configured ThreadPoolTaskExecutor bean
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        log.info("Configuring custom thread pool for inventory service");
        
        // Create Spring's ThreadPoolTaskExecutor with optimized settings
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Core thread pool size - number of threads that are always kept alive
        // Set to number of CPU cores for optimal performance
        executor.setCorePoolSize(Runtime.getRuntime().availableProcessors());
        
        // Maximum thread pool size - maximum number of threads that can be created
        // Set to double the core pool size to handle burst loads
        executor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 2);
        
        // Queue capacity - number of tasks that can be queued when all threads are busy
        // Set to 100 to buffer tasks during high load periods
        executor.setQueueCapacity(100);
        
        // Thread name prefix - helps with debugging and monitoring
        executor.setThreadNamePrefix("Inventory-Async-");
        
        // Rejected execution policy - what to do when queue is full and max threads reached
        // CallerRunsPolicy: the task runs in the calling thread (provides backpressure)
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // Keep threads alive for 60 seconds after completion
        // Reduces thread creation overhead for recurring tasks
        executor.setKeepAliveSeconds(60);
        
        // Allow core threads to timeout - helps resource management
        executor.setAllowCoreThreadTimeOut(true);
        
        // Wait for tasks to complete on shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);
        
        // Await termination for 30 seconds on shutdown
        executor.setAwaitTerminationSeconds(30);
        
        log.info("Thread pool configured with core size: {}, max size: {}, queue capacity: {}", 
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());
        
        return executor;
    }

    /**
     * Bean definition for file processing thread pool
     * This executor is specifically optimized for file I/O operations
     * like importing/exporting inventory data, generating reports, and batch processing
     * 
     * @return Configured ThreadPoolTaskExecutor for file operations
     */
    @Bean(name = "fileProcessingExecutor")
    public Executor fileProcessingExecutor() {
        log.info("Configuring file processing thread pool");
        
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // File I/O is typically I/O bound, so we can use more threads
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("File-Processing-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setKeepAliveSeconds(120); // Keep longer for file operations
        executor.setAllowCoreThreadTimeOut(true);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        
        log.info("File processing thread pool configured with core size: {}, max size: {}", 
                executor.getCorePoolSize(), executor.getMaxPoolSize());
        
        return executor;
    }

    /**
     * Bean definition for scheduled task executor
     * This executor is used for periodic tasks like inventory reconciliation,
     * low stock alerts, and data cleanup operations
     * 
     * @return Configured ThreadPoolTaskExecutor for scheduled tasks
     */
    @Bean(name = "scheduledTaskExecutor")
    public Executor scheduledTaskExecutor() {
        log.info("Configuring scheduled task thread pool");
        
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Scheduled tasks are typically lightweight, use smaller pool
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("Scheduled-Task-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setKeepAliveSeconds(30);
        executor.setAllowCoreThreadTimeOut(true);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        
        log.info("Scheduled task thread pool configured with core size: {}, max size: {}", 
                executor.getCorePoolSize(), executor.getMaxPoolSize());
        
        return executor;
    }

    /**
     * Bean definition for notification thread pool
     * This executor handles sending notifications, emails, and alerts
     * Ensures notifications don't block main business operations
     * 
     * @return Configured ThreadPoolTaskExecutor for notifications
     */
    @Bean(name = "notificationExecutor")
    public Executor notificationExecutor() {
        log.info("Configuring notification thread pool");
        
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Notifications are typically I/O bound but lightweight
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(6);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("Notification-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy()); // Discard old notifications
        executor.setKeepAliveSeconds(60);
        executor.setAllowCoreThreadTimeOut(true);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        
        log.info("Notification thread pool configured with core size: {}, max size: {}", 
                executor.getCorePoolSize(), executor.getMaxPoolSize());
        
        return executor;
    }
}
