package com.enterprise.inventory.service.config;

import lombok.extern.slf4j.Slf4j; // Import Lombok's SLF4J annotation for logging
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler; // Import AsyncUncaughtExceptionHandler
import org.springframework.context.annotation.Bean; // Import Bean annotation for Spring component definition
import org.springframework.context.annotation.Configuration; // Import Configuration annotation for Spring configuration class
import org.springframework.scheduling.annotation.EnableAsync; // Import EnableAsync annotation for asynchronous processing
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor; // Import Spring's thread pool task executor

import java.util.concurrent.Executor; // Import Executor interface
import java.util.concurrent.ThreadPoolExecutor; // Import Java's thread pool executor

/**
 * Configuration class for asynchronous processing
 * This class configures global async processing settings
 * and handles uncaught exceptions in async methods
 * 
 * @Configuration: Marks this class as a Spring configuration class
 * @EnableAsync: Enables Spring's @Async annotation processing
 * @Slf4j: Lombok annotation to add SLF4J logging capabilities
 */
@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig {

    /**
     * Bean for asynchronous exception handler
     * This method handles exceptions that occur in async methods
     * 
     * @return AsyncUncaughtExceptionHandler bean
     */
    @Bean(name = "asyncExceptionHandler")
    public AsyncUncaughtExceptionHandler asyncExceptionHandler() {
        return new AsyncUncaughtExceptionHandler();
    }

    /**
     * Custom async exception handler
     * This class handles exceptions thrown by async methods
     * Provides centralized logging for async operation failures
     */
    public static class AsyncUncaughtExceptionHandler implements AsyncUncaughtExceptionHandler {

        /**
         * Handle uncaught exceptions from async methods
         * This method logs the exception and provides a fallback response
         * 
         * @param throwable the exception that was thrown
         * @return fallback response object
         */
        @Override
        public Object handleUncaughtException(Throwable throwable) {
            log.error("Async operation failed: {}", throwable.getMessage(), throwable);
            
            // In a real implementation, you might:
            // - Send alert to monitoring system
            // - Log to database for analysis
            // - Return appropriate fallback response
            // - Notify operations team
            
            return "Async operation failed: " + throwable.getMessage();
        }
    }

    /**
     * Bean for global task executor
     * This provides a default thread pool for async operations
     * Used when no specific executor is specified
     * 
     * @return ThreadPoolTaskExecutor bean
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        log.info("Configuring global task executor for async operations");
        
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Core thread pool size - number of threads that are always kept alive
        executor.setCorePoolSize(2);
        
        // Maximum thread pool size - maximum number of threads that can be created
        executor.setMaxPoolSize(5);
        
        // Queue capacity - number of tasks that can be queued when all threads are busy
        executor.setQueueCapacity(100);
        
        // Thread name prefix - helps with debugging and monitoring
        executor.setThreadNamePrefix("Global-Async-");
        
        // Rejected execution policy - what to do when queue is full and max threads reached
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // Keep threads alive for 60 seconds after completion
        executor.setKeepAliveSeconds(60);
        
        // Allow core threads to timeout - helps resource management
        executor.setAllowCoreThreadTimeOut(true);
        
        // Wait for tasks to complete on shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);
        
        // Await termination for 30 seconds on shutdown
        executor.setAwaitTerminationSeconds(30);
        
        log.info("Global task executor configured with core size: {}, max size: {}, queue capacity: {}", 
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());
        
        return executor;
    }

    /**
     * Bean for file processing task executor
     * This provides optimized thread pool for I/O-intensive operations
     * 
     * @return ThreadPoolTaskExecutor bean
     */
    @Bean(name = "fileProcessingExecutor")
    public Executor fileProcessingExecutor() {
        log.info("Configuring file processing task executor");
        
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
        
        log.info("File processing executor configured with core size: {}, max size: {}", 
                executor.getCorePoolSize(), executor.getMaxPoolSize());
        
        return executor;
    }

    /**
     * Bean for notification task executor
     * This provides thread pool for non-critical notification operations
     * 
     * @return ThreadPoolTaskExecutor bean
     */
    @Bean(name = "notificationExecutor")
    public Executor notificationExecutor() {
        log.info("Configuring notification task executor");
        
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Notifications are typically lightweight, use smaller pool
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(6);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("Notification-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy()); // Discard old notifications
        executor.setKeepAliveSeconds(60);
        executor.setAllowCoreThreadTimeOut(true);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        
        log.info("Notification executor configured with core size: {}, max size: {}", 
                executor.getCorePoolSize(), executor.getMaxPoolSize());
        
        return executor;
    }

    /**
     * Bean for scheduled task executor
     * This provides thread pool for periodic maintenance operations
     * 
     * @return ThreadPoolTaskExecutor bean
     */
    @Bean(name = "scheduledTaskExecutor")
    public Executor scheduledTaskExecutor() {
        log.info("Configuring scheduled task executor");
        
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Scheduled tasks are typically lightweight, use small pool
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("Scheduled-Task-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setKeepAliveSeconds(30);
        executor.setAllowCoreThreadTimeOut(true);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        
        log.info("Scheduled task executor configured with core size: {}, max size: {}", 
                executor.getCorePoolSize(), executor.getMaxPoolSize());
        
        return executor;
    }
}
