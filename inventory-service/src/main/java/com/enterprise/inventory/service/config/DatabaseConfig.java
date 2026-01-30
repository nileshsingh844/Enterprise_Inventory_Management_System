package com.enterprise.inventory.service.config;

import com.zaxxer.hikari.HikariDataSource; // Import HikariDataSource for connection pooling
import lombok.extern.slf4j.Slf4j; // Import Lombok's SLF4J annotation for logging
import org.springframework.boot.context.properties.ConfigurationProperties; // Import ConfigurationProperties annotation
import org.springframework.context.annotation.Bean; // Import Bean annotation for Spring component definition
import org.springframework.context.annotation.Configuration; // Import Configuration annotation for Spring configuration class
import org.springframework.context.annotation.Primary; // Import Primary annotation for primary bean definition

import javax.sql.DataSource; // Import DataSource interface
import java.util.Properties; // Import Properties class for configuration properties

/**
 * Database configuration class for connection pooling and optimization
 * This class configures HikariCP connection pool with optimal settings
 * for the inventory management system's database operations
 * 
 * @Configuration: Marks this class as a Spring configuration class
 * @ConfigurationProperties: Enables binding of external configuration properties
 * @Slf4j: Lombok annotation to add SLF4J logging capabilities
 */
@Configuration
@ConfigurationProperties(prefix = "app.datasource")
@Slf4j
public class DatabaseConfig {

    private String url;
    private String username;
    private String password;
    private String driverClassName;

    // HikariCP specific configuration properties
    private HikariProperties hikari = new HikariProperties();

    /**
     * Getter for database URL
     * 
     * @return the database URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * Setter for database URL
     * 
     * @param url the database URL to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Getter for database username
     * 
     * @return the database username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Setter for database username
     * 
     * @param username the database username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Getter for database password
     * 
     * @return the database password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Setter for database password
     * 
     * @param password the database password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Getter for driver class name
     * 
     * @return the driver class name
     */
    public String getDriverClassName() {
        return driverClassName;
    }

    /**
     * Setter for driver class name
     * 
     * @param driverClassName the driver class name to set
     */
    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    /**
     * Getter for HikariCP properties
     * 
     * @return the HikariCP properties
     */
    public HikariProperties getHikari() {
        return hikari;
    }

    /**
     * Setter for HikariCP properties
     * 
     * @param hikari the HikariCP properties to set
     */
    public void setHikari(HikariProperties hikari) {
        this.hikari = hikari;
    }

    /**
     * Primary DataSource bean configuration
     * This method creates and configures the HikariCP connection pool
     * with optimized settings for the inventory management system
     * 
     * @return Configured DataSource bean
     */
    @Bean
    @Primary
    public DataSource dataSource() {
        log.info("Configuring HikariCP connection pool for inventory service");
        
        HikariDataSource dataSource = new HikariDataSource();
        
        // Basic database connection settings
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(driverClassName);
        
        // Connection pool size configuration
        // Core pool size: number of connections maintained even when idle
        dataSource.setMinimumIdle(hikari.getMinimumIdle());
        
        // Maximum pool size: maximum number of connections that can be created
        dataSource.setMaximumPoolSize(hikari.getMaximumPoolSize());
        
        // Connection timeout: maximum time to wait for a connection from the pool
        dataSource.setConnectionTimeout(hikari.getConnectionTimeout());
        
        // Idle timeout: how long connections can remain idle before being closed
        dataSource.setIdleTimeout(hikari.getIdleTimeout());
        
        // Maximum lifetime: maximum lifetime of a connection before it's closed
        dataSource.setMaxLifetime(hikari.getMaxLifetime());
        
        // Leak detection: detect and log connection leaks
        dataSource.setLeakDetectionThreshold(hikari.getLeakDetectionThreshold());
        
        // Connection testing: test connections before giving them out from the pool
        dataSource.setConnectionTestQuery(hikari.getConnectionTestQuery());
        dataSource.setConnectionTestTimeout(hikari.getConnectionTestTimeout());
        
        // Validation timeout: timeout for connection validation
        dataSource.setValidationTimeout(hikari.getValidationTimeout());
        
        // Pool name: name of the connection pool for logging and monitoring
        dataSource.setPoolName(hikari.getPoolName());
        
        // Allow pool suspension: suspend pool when all connections are in use
        dataSource.setAllowPoolSuspension(hikari.isAllowPoolSuspension());
        
        // Custom properties for MySQL optimization
        Properties properties = new Properties();
        
        // MySQL specific optimizations
        properties.setProperty("cachePrepStmts", "true");                    // Enable prepared statement caching
        properties.setProperty("prepStmtCacheSize", "250");                    // Prepared statement cache size
        properties.setProperty("prepStmtCacheSqlLimit", "2048");                 // Maximum SQL length for cached statements
        properties.setProperty("useServerPrepStmts", "true");                   // Use server-side prepared statements
        properties.setProperty("useLocalSessionState", "true");                    // Use local session state
        properties.setProperty("rewriteBatchedStatements", "true");            // Rewrite batch statements
        properties.setProperty("cacheResultSetMetadata", "true");               // Cache result set metadata
        properties.setProperty("cacheServerConfiguration", "true");               // Cache server configuration
        properties.setProperty("elideSetAutoCommits", "false");                     // Don't auto-commit empty result sets
        properties.setProperty("maintainTimeStats", "false");                        // Don't maintain time stats
        properties.setProperty("net.sf.jsql.expand_sql", "false");                   // Don't expand SQL for logging
        
        // Connection pool tuning for MySQL
        properties.setProperty("dataSource.logAbandonedConnections", "false");       // Log abandoned connections
        properties.setProperty("dataSource.removeAbandoned", "false");               // Remove abandoned connections
        properties.setProperty("dataSource.logAbandonedOnTimeout", "false");         // Log abandoned connections on timeout
        
        dataSource.setDataSourceProperties(properties);
        
        log.info("HikariCP connection pool configured:");
        log.info("  - Minimum Idle Connections: {}", dataSource.getMinimumIdle());
        log.info("  - Maximum Pool Size: {}", dataSource.getMaximumPoolSize());
        log.info("  - Connection Timeout: {}ms", dataSource.getConnectionTimeout());
        log.info("  - Idle Timeout: {}ms", dataSource.getIdleTimeout());
        log.info("  - Max Lifetime: {}ms", dataSource.getMaxLifetime());
        log.info("  - Pool Name: {}", dataSource.getPoolName());
        
        return dataSource;
    }

    /**
     * Inner class for HikariCP specific properties
     * This class contains all the configuration options for the connection pool
     */
    public static class HikariProperties {
        private int minimumIdle = 5;                    // Minimum number of idle connections
        private int maximumPoolSize = 20;                 // Maximum number of connections
        private long connectionTimeout = 30000;            // Connection timeout in milliseconds (30 seconds)
        private long idleTimeout = 600000;                  // Idle timeout in milliseconds (10 minutes)
        private long maxLifetime = 1800000;                 // Max lifetime in milliseconds (30 minutes)
        private int leakDetectionThreshold = 60000;           // Leak detection threshold in milliseconds (1 minute)
        private String connectionTestQuery = "SELECT 1";    // SQL query for connection testing
        private long connectionTestTimeout = 5000;           // Connection test timeout in milliseconds (5 seconds)
        private long validationTimeout = 5000;               // Validation timeout in milliseconds (5 seconds)
        private String poolName = "InventoryHikariCP";       // Pool name for identification
        private boolean allowPoolSuspension = true;          // Allow pool suspension when all connections are in use

        // Getters and setters
        public int getMinimumIdle() { return minimumIdle; }
        public void setMinimumIdle(int minimumIdle) { this.minimumIdle = minimumIdle; }
        
        public int getMaximumPoolSize() { return maximumPoolSize; }
        public void setMaximumPoolSize(int maximumPoolSize) { this.maximumPoolSize = maximumPoolSize; }
        
        public long getConnectionTimeout() { return connectionTimeout; }
        public void setConnectionTimeout(long connectionTimeout) { this.connectionTimeout = connectionTimeout; }
        
        public long getIdleTimeout() { return idleTimeout; }
        public void setIdleTimeout(long idleTimeout) { this.idleTimeout = idleTimeout; }
        
        public long getMaxLifetime() { return maxLifetime; }
        public void setMaxLifetime(long maxLifetime) { this.maxLifetime = maxLifetime; }
        
        public int getLeakDetectionThreshold() { return leakDetectionThreshold; }
        public void setLeakDetectionThreshold(int leakDetectionThreshold) { this.leakDetectionThreshold = leakDetectionThreshold; }
        
        public String getConnectionTestQuery() { return connectionTestQuery; }
        public void setConnectionTestQuery(String connectionTestQuery) { this.connectionTestQuery = connectionTestQuery; }
        
        public long getConnectionTestTimeout() { return connectionTestTimeout; }
        public void setConnectionTestTimeout(long connectionTestTimeout) { this.connectionTestTimeout = connectionTestTimeout; }
        
        public long getValidationTimeout() { return validationTimeout; }
        public void setValidationTimeout(long validationTimeout) { this.validationTimeout = validationTimeout; }
        
        public String getPoolName() { return poolName; }
        public void setPoolName(String poolName) { this.poolName = poolName; }
        
        public boolean isAllowPoolSuspension() { return allowPoolSuspension; }
        public void setAllowPoolSuspension(boolean allowPoolSuspension) { this.allowPoolSuspension = allowPoolSuspension; }
    }
}
