# Enterprise Inventory Management System - Interview Preparation Guide

## Table of Contents
1. [Architecture & Design Questions](#architecture--design-questions)
2. [Spring Boot & Spring Cloud Questions](#spring-boot--spring-cloud-questions)
3. [Microservices Questions](#microservices-questions)
4. [Database & JPA Questions](#database--jpa-questions)
5. [Security & Authentication Questions](#security--authentication-questions)
6. [API & REST Questions](#api--rest-questions)
7. [Testing Questions](#testing-questions)
8. [Docker & DevOps Questions](#docker--devops-questions)
9. [Performance & Scalability Questions](#performance--scalability-questions)
10. [Scenario-Based Questions](#scenario-based-questions)

---

## Architecture & Design Questions

### Q1. What is the overall architecture of this Enterprise Inventory Management System?

**Answer:** The system follows a microservices architecture pattern with the following components:
- **6 Core Microservices**: Eureka Server, API Gateway, Config Server, Inventory Service, Order Service, User Service
- **Service Discovery**: Eureka Server for dynamic service registration and discovery
- **API Gateway**: Single entry point with routing, load balancing, and cross-cutting concerns
- **Centralized Configuration**: Config Server for managing configuration across all services
- **Data Persistence**: MySQL with separate databases for each service (inventory_db, order_db, user_db)
- **Authentication**: JWT-based authentication with User Service
- **Communication**: REST APIs with Feign clients for inter-service communication

**Cross-Questions:**
- **Q:** Why did you choose microservices over monolithic architecture?
  **A:** Microservices provide better scalability, independent deployment, fault isolation, and technology diversity. For an enterprise system, this allows different teams to work on different services independently.

- **Q:** How do services communicate with each other?
  **A:** Services communicate via REST APIs using Feign clients. For example, Order Service calls Inventory Service to check stock availability and update quantities.

- **Q:** What are the advantages of having separate databases per service?
  **A:** Data isolation, independent scaling, technology flexibility, and reduced coupling between services.

### Q2. Explain the role of each microservice in detail.

**Answer:**
- **Eureka Server (8761)**: Service registry where all microservices register themselves. Enables service discovery and load balancing.
- **API Gateway (8080)**: Single entry point that routes requests to appropriate services, handles CORS, authentication, rate limiting, and circuit breaking.
- **Config Server (8888)**: Centralized configuration management using Git backend. Provides environment-specific configurations.
- **Inventory Service (8081)**: Manages product catalog, stock levels, reordering, and inventory analytics.
- **Order Service (8082)**: Handles order lifecycle, payment processing, and integrates with Inventory Service.
- **User Service (8083)**: Manages user authentication, authorization, JWT tokens, and user profiles.

**Cross-Questions:**
- **Q:** How does the API Gateway know which service to route to?
  **A:** Through configured routing rules based on URL patterns. For example, `/api/inventory/**` routes to Inventory Service.

- **Q:** What happens if Eureka Server goes down?
  **A:** Services continue to work using cached registry information, but new service discovery fails. This is why Eureka should be clustered in production.

- **Q:** How does Config Server handle different environments?
  **A:** Uses Git branches or profiles (dev, prod, test) and provides environment-specific configurations.

---

## Spring Boot & Spring Cloud Questions

### Q3. What is Spring Boot and why did you use it?

**Answer:** Spring Boot is an opinionated framework that simplifies Spring application development with auto-configuration, embedded servers, and production-ready features.

**Reasons for using Spring Boot:**
- **Rapid Development**: Auto-configuration reduces boilerplate code
- **Embedded Server**: No need for external servlet containers
- **Production Ready**: Includes health checks, metrics, and externalized configuration
- **Microservices Friendly**: Lightweight and easy to containerize
- **Rich Ecosystem**: Extensive support for databases, security, messaging, etc.

**Cross-Questions:**
- **Q:** What is auto-configuration in Spring Boot?
  **A:** Spring Boot automatically configures beans based on classpath dependencies. For example, if MySQL driver is on classpath, it configures DataSource automatically.

- **Q:** How does Spring Boot handle dependency injection?
  **A:** Uses Spring's dependency injection container with annotations like @Autowired, @Component, @Service, @Repository.

- **Q:** What are Spring Boot starters?
  **A:** Dependency descriptors that include commonly used dependencies. For example, `spring-boot-starter-web` includes Spring MVC, Tomcat, and validation.

### Q4. Explain Spring Cloud components used in this project.

**Answer:**
- **Spring Cloud Netflix Eureka**: Service discovery and registration
- **Spring Cloud Gateway**: API gateway with routing and filtering
- **Spring Cloud Config Server**: Centralized configuration management
- **Spring Cloud OpenFeign**: Declarative REST client for inter-service communication
- **Spring Cloud Circuit Breaker**: Fault tolerance with circuit breaker pattern

**Cross-Questions:**
- **Q:** How does Eureka client work?
  **A:** Each service registers with Eureka on startup, sends heartbeats every 30 seconds, and fetches registry information periodically.

- **Q:** What is the purpose of @EnableEurekaClient?
  **A:** Enables Eureka client functionality for service registration and discovery.

- **Q:** How does Feign simplify REST client development?
  **A:** Feign allows you to write REST client interfaces with annotations, eliminating boilerplate HTTP client code.

---

## Microservices Questions

### Q5. What are the key characteristics of microservices architecture?

**Answer:**
- **Single Responsibility**: Each service handles one business capability
- **Independent Deployment**: Services can be deployed independently
- **Decentralized Data Management**: Each service has its own database
- **Technology Diversity**: Services can use different technologies
- **Fault Isolation**: Failure in one service doesn't affect others
- **Scalability**: Individual services can be scaled based on demand

**Cross-Questions:**
- **Q:** How do you handle distributed transactions in microservices?
  **A:** Use saga pattern, event-driven architecture, or two-phase commit. In this system, we use compensating transactions (e.g., rollback inventory if order fails).

- **Q:** How do you ensure data consistency across services?
  **A:** Through eventual consistency using events, or by implementing distributed transaction patterns like saga.

- **Q:** What are the challenges of microservices?
  **A:** Network latency, service discovery, distributed logging, monitoring, testing complexity, and operational overhead.

### Q6. How does service discovery work in this system?

**Answer:** Service discovery works through Eureka Server:
1. Each microservice registers with Eureka on startup
2. Services send heartbeats every 30 seconds to maintain registration
3. Other services query Eureka to discover service locations
4. API Gateway and Feign clients use service names instead of hardcoded URLs

**Cross-Questions:**
- **Q:** What happens when a service instance fails?
  **A:** Eureka removes it from the registry after missed heartbeats (default 90 seconds), and traffic is routed to healthy instances.

- **Q:** How does client-side load balancing work?
  **A:** Spring Cloud LoadBalancer distributes requests across multiple instances of a service.

- **Q:** What is the difference between client-side and server-side discovery?
  **A:** Client-side: client queries registry and calls service directly. Server-side: client calls through a load balancer.

---

## Database & JPA Questions

### Q7. Explain the database design for this system.

**Answer:** The system uses separate databases for each service:
- **inventory_db**: Products table with columns for SKU, name, price, quantity, reorder level
- **order_db**: Orders table and OrderItems table with foreign key relationship
- **user_db**: Users table and UserPermissions table for role-based access

**Key Design Decisions:**
- **Database per service**: Data isolation and independent scaling
- **Foreign key constraints**: Maintain referential integrity within services
- **Indexes**: On frequently queried columns (SKU, customer_id, status)
- **ENUM types**: For fixed sets of values (status, role)

**Cross-Questions:**
- **Q:** Why did you use separate databases instead of a single database?
  **A:** To avoid tight coupling between services, allow independent scaling, and prevent single point of failure.

- **Q:** How do you handle joins across services?
  **A:** Avoid cross-service joins. Use API calls to fetch related data or implement event-driven data synchronization.

- **Q:** What is the purpose of the @PrePersist and @PreUpdate annotations?
  **A:** These are JPA lifecycle callbacks that execute before entity persistence and update, used for setting timestamps and default values.

### Q8. Explain JPA and Hibernate configuration.

**Answer:** JPA configuration includes:
- **Entity Mapping**: @Entity, @Table, @Column annotations for object-relational mapping
- **Relationships**: @OneToMany, @ManyToOne for entity relationships
- **Queries**: Custom queries using @Query annotation with JPQL
- **Transactions**: @Transactional for managing database transactions
- **Database Dialect**: MySQL8Dialect for MySQL-specific SQL generation

**Cross-Questions:**
- **Q:** What is the difference between FetchType.LAZY and FetchType.EAGER?
  **A:** LAZY loads related data on demand, EAGER loads immediately. LAZY is preferred for performance.

- **Q:** How does Hibernate handle the entity lifecycle?
  **A:** Through states: transient, persistent, detached, removed. Managed by EntityManager and session.

- **Q:** What is the N+1 query problem and how do you solve it?
  **A:** N+1 occurs when loading 1 parent + N children queries. Solve with JOIN FETCH or @EntityGraph.

---

## Security & Authentication Questions

### Q9. Explain JWT-based authentication in this system.

**Answer:** JWT authentication flow:
1. User sends credentials to /api/auth/login
2. User Service validates credentials and generates JWT token
3. Token contains user ID, roles, and expiration
4. Client includes token in Authorization header: Bearer <token>
5. JwtAuthenticationFilter validates token on each request
6. Security context is set with user authorities

**JWT Components:**
- **Header**: Algorithm and token type
- **Payload**: User claims (sub, roles, exp, iat)
- **Signature**: HMAC-SHA512 signature for integrity

**Cross-Questions:**
- **Q:** How do you handle token expiration?
  **A:** Tokens expire after 24 hours. Refresh tokens (7 days) are used to obtain new access tokens.

- **Q:** What are the advantages of JWT over session-based authentication?
  **A:** Stateless, scalable, works across domains, no server-side session storage.

- **Q:** How do you secure JWT tokens?
  **A:** Use strong secret keys, short expiration times, HTTPS, and implement token blacklisting for logout.

### Q10. Explain Spring Security configuration.

**Answer:** Spring Security configuration includes:
- **AuthenticationManager**: Handles authentication with username/password
- **JwtAuthenticationFilter**: Validates JWT tokens and sets security context
- **Password Encoding**: BCrypt for secure password hashing
- **Authorization**: Method-level security with @PreAuthorize
- **CORS**: Cross-origin resource sharing configuration
- **Exception Handling**: Custom authentication entry points

**Cross-Questions:**
- **Q:** What is the difference between authentication and authorization?
  **A:** Authentication verifies identity (who you are), authorization verifies permissions (what you can do).

- **Q:** How does BCrypt work?
  **A:** BCrypt is a hashing function that automatically salts passwords and includes a work factor to slow down brute force attacks.

- **Q:** What is the purpose of @PreAuthorize?
  **A:** Enables method-level security with expressions like hasRole('ADMIN') or #userId == authentication.principal.userId.

---

## API & REST Questions

### Q11. Explain REST API design principles used in this system.

**Answer:** REST principles implemented:
- **Resource-based URLs**: /api/products, /api/orders, /api/users
- **HTTP Methods**: GET (read), POST (create), PUT (update), DELETE (remove)
- **Status Codes**: 200 (OK), 201 (Created), 400 (Bad Request), 404 (Not Found)
- **Stateless**: Each request contains all necessary information
- **Uniform Interface**: Consistent API design across all services
- **HATEOAS**: Links to related resources (optional in this implementation)

**Cross-Questions:**
- **Q:** How do you handle API versioning?
  **A:** Can use URL versioning (/api/v1/products) or header versioning (Accept: application/vnd.api.v1+json).

- **Q:** What is the purpose of DTOs?
  **A:** Data Transfer Objects separate API representation from domain model, prevent data leakage, and enable API evolution.

- **Q:** How do you handle pagination?
  **A:** Use Pageable interface and return Page<T> with metadata (total elements, total pages, current page).

### Q12. Explain the API Gateway routing configuration.

**Answer:** API Gateway routing includes:
- **Path-based routing**: /api/inventory/** → inventory-service
- **Load balancing**: Multiple instances of same service
- **CORS handling**: Cross-origin request configuration
- **Circuit breaker**: Fault tolerance with fallback
- **Request/response filtering**: Logging, authentication, rate limiting

**Cross-Questions:**
- **Q:** How does the gateway handle service failures?
  **A:** Through circuit breaker pattern - opens circuit after failures, returns fallback responses.

- **Q:** What is the purpose of StripPrefix=2?
  **A:** Removes first 2 path segments (/api/inventory) before forwarding to service.

- **Q:** How do you implement rate limiting?
  **A:** Using Spring Cloud Gateway filters or Redis-based rate limiting.

---

## Testing Questions

### Q13. Explain the testing strategy for this microservices system.

**Answer:** Testing strategy includes:
- **Unit Tests**: JUnit 5 + Mockito for individual classes
- **Integration Tests**: @SpringBootTest for database and service integration
- **Controller Tests**: MockMvc for API endpoint testing
- **Repository Tests**: @DataJpaTest for JPA repository testing
- **Contract Tests**: Pact for inter-service API contracts
- **End-to-End Tests**: TestContainers for full system testing

**Testing Examples:**
- **Service Tests**: Mock repositories, test business logic
- **Controller Tests**: MockMvc for HTTP request/response testing
- **Security Tests**: Test authentication and authorization

**Cross-Questions:**
- **Q:** How do you test inter-service communication?
  **A:** Use @MockBean for Feign clients or WireMock for HTTP service mocking.

- **Q:** What is the purpose of @DataJpaTest?
  **A:** Slice test for JPA repositories, configures only JPA-related beans.

- **Q:** How do you test JWT authentication?
  **A:** Mock JwtTokenProvider and test security context setup.

### Q14. Explain Mockito usage in the tests.

**Answer:** Mockito features used:
- **@Mock**: Create mock objects for dependencies
- **@InjectMocks**: Inject mocks into the class under test
- **when().thenReturn()**: Configure mock behavior
- **verify()**: Verify method calls
- **ArgumentMatchers**: Flexible argument matching (any(), anyString())

**Testing Patterns:**
- **Arrange-Act-Assert**: Structure test cases
- **Given-When-Then**: Behavior-driven testing
- **Test Data Builders**: Create test objects efficiently

**Cross-Questions:**
- **Q:** What is the difference between mock and spy?
  **A:** Mock returns default values, spy calls real methods unless stubbed.

- **Q:** How do you test void methods?
  **A:** Use verify() to check method was called with specific arguments.

- **Q:** What is the purpose of @Captor?
  **A:** Captures arguments passed to mocked methods for assertion.

---

## Docker & DevOps Questions

### Q15. Explain the Docker configuration for this system.

**Answer:** Docker setup includes:
- **Multi-stage builds**: Separate build and runtime stages
- **Optimized images**: JRE-only runtime images for smaller size
- **Health checks**: curl-based health endpoints
- **Security**: Non-root users, minimal attack surface
- **Docker Compose**: Orchestrate all services with dependencies
- **Volume persistence**: MySQL data persistence
- **Networking**: Custom bridge network for service communication

**Dockerfile Features:**
- **Maven build stage**: Compile and package application
- **JRE runtime stage**: Smaller production image
- **Health checks**: Automated service monitoring
- **Environment variables**: Configuration injection

**Cross-Questions:**
- **Q:** Why use multi-stage Docker builds?
  **A:** Reduces final image size by excluding build tools and dependencies.

- **Q:** How does Docker Compose handle service dependencies?
  **A:** Using depends_on and health checks to ensure proper startup order.

- **Q:** What is the purpose of health checks?
  **A:** Enable service monitoring, load balancer health checks, and automated restarts.

### Q16. Explain the deployment architecture.

**Answer:** Deployment architecture includes:
- **Container Orchestration**: Docker Compose for development, Kubernetes for production
- **Service Discovery**: Eureka for dynamic service registration
- **Load Balancing**: Nginx reverse proxy and Spring Cloud LoadBalancer
- **Monitoring**: Prometheus metrics collection and Grafana visualization
- **Logging**: Centralized logging with ELK stack (optional)
- **CI/CD**: GitHub Actions or Jenkins for automated deployment

**Cross-Questions:**
- **Q:** How do you handle configuration in different environments?
  **A:** Spring profiles with Config Server, environment variables, and Docker secrets.

- **Q:** What is the purpose of Nginx in this architecture?
  **A:** SSL termination, load balancing, static file serving, and rate limiting.

- **Q:** How do you achieve zero-downtime deployment?
  **A:** Blue-green deployment, rolling updates, and health checks.

---

## Performance & Scalability Questions

### Q17. How do you ensure high performance in this system?

**Answer:** Performance optimization techniques:
- **Database Indexing**: Index on frequently queried columns
- **Connection Pooling**: HikariCP for database connections
- **Caching**: Redis for frequently accessed data
- **Lazy Loading**: JPA relationships loaded on demand
- **Pagination**: Large result sets split into pages
- **Asynchronous Processing**: For long-running operations
- **Circuit Breaker**: Prevent cascading failures

**Database Optimization:**
- **Query Optimization**: Use EXPLAIN to analyze queries
- **Connection Pooling**: Optimal pool size configuration
- **Read Replicas**: Separate read and write databases
- **Batch Operations**: Bulk inserts and updates

**Cross-Questions:**
- **Q:** How do you optimize database queries?
  **A:** Use proper indexes, avoid N+1 queries, use batch operations, and analyze execution plans.

- **Q:** What is the purpose of connection pooling?
  **A:** Reuse database connections to avoid connection overhead and improve performance.

- **Q:** How do you handle high traffic scenarios?
  **A:** Horizontal scaling, caching, load balancing, and rate limiting.

### Q18. Explain scalability strategies for this system.

**Answer:** Scalability approaches:
- **Horizontal Scaling**: Add more instances of services
- **Vertical Scaling**: Increase resources (CPU, memory)
- **Database Scaling**: Read replicas, sharding, partitioning
- **Caching Layer**: Redis for distributed caching
- **Load Balancing**: Distribute traffic across instances
- **Auto-scaling**: Kubernetes HPA based on metrics

**Service-specific Scaling:**
- **Stateless Services**: Easy horizontal scaling (API Gateway, Config Server)
- **Stateful Services**: Require careful planning (Database, Cache)
- **Bottleneck Identification**: Monitor and scale constrained services

**Cross-Questions:**
- **Q:** How do you identify performance bottlenecks?
  **A:** Monitoring metrics, load testing, profiling, and database query analysis.

- **Q:** What is the difference between scaling up and scaling out?
  **A:** Scaling up: increase resources of single instance. Scaling out: add more instances.

- **Q:** How do you handle session state in scaled environment?
  **A:** Use stateless design with JWT tokens or external session stores like Redis.

---

## Scenario-Based Questions

### Q19. Scenario: The Inventory Service is down. How does the system handle this?

**Answer:** System handles service failure through:
- **Circuit Breaker**: Opens circuit after failures, returns fallback response
- **Service Discovery**: Eureka removes failed instances from registry
- **API Gateway**: Routes to healthy instances or returns error
- **Retry Logic**: Configurable retry with exponential backoff
- **Graceful Degradation**: Limited functionality with cached data

**Recovery Process:**
1. Circuit breaker detects failures and opens
2. API Gateway returns 503 Service Unavailable
3. Monitoring alerts trigger incident response
4. Service is restarted or scaled up
5. Circuit breaker closes when service recovers

**Cross-Questions:**
- **Q:** What is the difference between circuit breaker and retry?
  **A:** Retry attempts to recover from transient failures, circuit breaker prevents cascading failures.

- **Q:** How do you implement graceful degradation?
  **A:** Provide fallback responses, use cached data, and disable non-critical features.

- **Q:** How do you test failure scenarios?
  **A:** Chaos engineering, fault injection, and failure testing.

### Q20. Scenario: Two users try to update the same product simultaneously. How do you handle this?

**Answer:** Handle concurrent updates through:
- **Optimistic Locking**: @Version annotation for version checking
- **Pessimistic Locking**: @Lock(LockModeType.PESSIMISTIC_WRITE)
- **Database Transactions**: @Transactional with proper isolation level
- **Business Logic**: Stock level validation before update
- **Error Handling**: Return appropriate error messages for conflicts

**Implementation Options:**
```java
@Version
private Long version;

// In service method
try {
    productRepository.save(product);
} catch (ObjectOptimisticLockingFailureException e) {
    throw new ConflictException("Product was modified by another user");
}
```

**Cross-Questions:**
- **Q:** What is the difference between optimistic and pessimistic locking?
  **A:** Optimistic: assumes no conflicts, checks at commit time. Pessimistic: locks records during transaction.

- **Q:** How do you choose isolation levels?
  **A:** READ_COMMITTED for most cases, SERIALIZABLE for critical operations.

- **Q:** What is a deadlock and how do you prevent it?
  **A:** Deadlock: two transactions waiting for each other. Prevent with consistent lock ordering and timeout.

### Q21. Scenario: Large order with 1000 items is placed. How does the system handle this?

**Answer:** Handle large orders through:
- **Batch Processing**: Process items in batches
- **Transaction Management**: Chunked transactions
- **Inventory Validation**: Check all items before processing
- **Compensating Transactions**: Rollback if any item fails
- **Asynchronous Processing**: Queue large orders for background processing
- **Performance Optimization**: Use bulk database operations

**Implementation Approach:**
```java
@Transactional
public OrderDto createLargeOrder(OrderDto orderDto) {
    // Validate all items first
    validateAllItems(orderDto.getOrderItems());
    
    // Process in batches
    for (List<OrderItemDto> batch : partition(orderDto.getOrderItems(), 100)) {
        processBatch(batch);
    }
    
    return order;
}
```

**Cross-Questions:**
- **Q:** How do you handle long-running transactions?
  **A:** Use saga pattern, break into smaller transactions, or use asynchronous processing.

- **Q:** What is the impact on database performance?
  **A:** Large transactions can lock resources, use batch operations and proper indexing.

- **Q:** How do you ensure data consistency?
  **A:** Use compensating transactions, event sourcing, or distributed transactions.

### Q22. Scenario: Database connection pool is exhausted. What do you do?

**Answer:** Handle connection pool exhaustion:
- **Monitoring**: Alert on pool usage metrics
- **Tuning**: Optimize pool size (max, min, idle)
- **Connection Leaks**: Identify and fix unclosed connections
- **Query Optimization**: Reduce connection hold time
- **Scaling**: Add more application instances
- **Database Scaling**: Increase database connection limit

**Troubleshooting Steps:**
1. Check connection pool metrics
2. Identify long-running queries
3. Review code for connection leaks
4. Optimize database queries
5. Increase pool size if necessary
6. Scale application horizontally

**Cross-Questions:**
- **Q:** What are the signs of connection pool exhaustion?
  **A:** Slow response times, connection timeout errors, high CPU usage.

- **Q:** How do you tune HikariCP connection pool?
  **A:** Set optimal maximumPoolSize, minimumIdle, connectionTimeout, and idleTimeout.

- **Q:** What causes connection leaks?
  **A:** Unclosed connections, long-running transactions, exception handling issues.

---

## Advanced Technical Questions

### Q23. Explain the saga pattern in the context of this system.

**Answer:** Saga pattern manages distributed transactions:
- **Order Saga**: Create order → Reserve inventory → Process payment → Confirm order
- **Compensation**: Rollback inventory if payment fails, cancel order if inventory fails
- **Event-Driven**: Use events to coordinate between services
- **Eventual Consistency**: System eventually reaches consistent state

**Implementation Example:**
```java
// Order Service
public void createOrder(OrderDto orderDto) {
    // Create order
    Order order = orderRepository.save(convertToEntity(orderDto));
    
    // Reserve inventory
    inventoryService.reserveProducts(order.getOrderItems());
    
    // Process payment
    paymentService.processPayment(order);
    
    // Confirm order
    order.setStatus(OrderStatus.CONFIRMED);
}
```

**Cross-Questions:**
- **Q:** How do you handle saga failures?
  **A:** Compensating transactions, retry mechanisms, and manual intervention.

- **Q:** What is the difference between saga and two-phase commit?
  **A:** Saga uses eventual consistency, 2PC provides ACID guarantees but has performance overhead.

- **Q:** How do you implement saga orchestration?
  **A:** Choreography (events) or orchestration (coordinator service).

### Q24. Explain event-driven architecture considerations.

**Answer:** Event-driven architecture benefits:
- **Loose Coupling**: Services communicate through events
- **Scalability**: Producers and consumers scale independently
- **Resilience**: Event buffering provides fault tolerance
- **Flexibility**: Easy to add new event consumers

**Implementation Options:**
- **Message Brokers**: RabbitMQ, Apache Kafka
- **Event Sourcing**: Store all events as state changes
- **CQRS**: Separate read and write models
- **Event Store**: Centralized event storage

**Cross-Questions:**
- **Q:** How do you ensure event ordering?
  **A**: Use partitioned topics, sequence numbers, or timestamp ordering.

- **Q:** What is event sourcing?
  **A**: Store all state changes as events, rebuild state by replaying events.

- **Q:** How do you handle duplicate events?
  **A**: Idempotent consumers, event deduplication, and unique event IDs.

---

## Final Preparation Tips

### Key Areas to Focus On:
1. **Architecture Patterns**: Microservices, API Gateway, Service Discovery
2. **Spring Framework**: Boot, Cloud, Security, Data
3. **Database Design**: JPA, transactions, optimization
4. **Security**: JWT, Spring Security, authentication flows
5. **Testing**: Unit testing, mocking, integration testing
6. **DevOps**: Docker, deployment, monitoring
7. **Performance**: Caching, connection pooling, scalability
8. **Problem Solving**: Scenarios, trade-offs, best practices

### Interview Strategy:
1. **Start with High-Level**: Explain architecture first
2. **Deep Dive**: Be prepared for detailed technical questions
3. **Trade-offs**: Explain why you made specific design choices
4. **Scenarios**: Practice common microservices scenarios
5. **Code Examples**: Be ready to write code snippets
6. **System Design**: Draw architecture diagrams on whiteboard

### Common Mistakes to Avoid:
1. **Don't memorize answers**: Understand concepts deeply
2. **Don't ignore trade-offs**: Every design has pros and cons
3. **Don't forget the "why"**: Explain reasoning behind decisions
4. **Don't overlook security**: Always consider security implications
5. **Don't ignore performance**: Consider scalability and performance

Remember: The best answers demonstrate deep understanding, practical experience, and the ability to explain complex concepts clearly. Good luck with your interview! 🚀
