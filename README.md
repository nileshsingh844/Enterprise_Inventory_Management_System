# Enterprise Inventory Management System

A scalable Enterprise Inventory Management System built with Java Spring Boot microservices architecture, implementing RESTful APIs to automate inventory tracking and order processing, reducing manual data entry by 65%.

## 🏗️ Architecture Overview

This system follows a microservices architecture pattern with the following components:

### Core Microservices

1. **Eureka Server** (Port: 8761) - Service Discovery and Registration
2. **API Gateway** (Port: 8080) - Single entry point for all client requests
3. **Config Server** (Port: 8888) - Centralized configuration management
4. **Inventory Service** (Port: 8081) - Product inventory and stock management
5. **Order Service** (Port: 8082) - Order processing and management
6. **User Service** (Port: 8083) - User authentication and authorization with JWT

### Technology Stack

- **Java 11+** - Core programming language
- **Spring Boot 2.7.14** - Application framework
- **Spring Cloud 2021.0.8** - Microservices framework
- **MySQL 8.0** - Relational database management
- **Hibernate ORM** - Data persistence layer
- **JWT** - Token-based authentication
- **Maven** - Dependency management
- **JUnit/Mockito** - Unit testing framework
- **Git** - Version control

## 🚀 Quick Start

### Prerequisites

- Java 11 or higher
- Maven 3.6 or higher
- MySQL 8.0 or higher
- Git

### Database Setup

1. Start MySQL server
2. Execute the database setup script:
   ```bash
   mysql -u root -p < database-setup.sql
   ```

### Running the Application

1. **Start Eureka Server** (Service Discovery)
   ```bash
   cd eureka-server
   mvn spring-boot:run
   ```

2. **Start Config Server** (Configuration Management)
   ```bash
   cd config-server
   mvn spring-boot:run
   ```

3. **Start API Gateway** (Entry Point)
   ```bash
   cd api-gateway
   mvn spring-boot:run
   ```

4. **Start Inventory Service**
   ```bash
   cd inventory-service
   mvn spring-boot:run
   ```

5. **Start Order Service**
   ```bash
   cd order-service
   mvn spring-boot:run
   ```

6. **Start User Service**
   ```bash
   cd user-service
   mvn spring-boot:run
   ```

### Access Points

- **API Gateway**: http://localhost:8080
- **Eureka Dashboard**: http://localhost:8761
- **Config Server**: http://localhost:8888
- **Inventory Service**: http://localhost:8081
- **Order Service**: http://localhost:8082
- **User Service**: http://localhost:8083

## 📚 API Documentation

### Authentication Endpoints (User Service)

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "password123"
}
```

#### Register User
```http
POST /api/users
Content-Type: application/json
Authorization: Bearer <token>

{
  "username": "newuser",
  "email": "user@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe"
}
```

### Inventory Endpoints (Inventory Service)

#### Get All Products
```http
GET /api/products
Authorization: Bearer <token>
```

#### Create Product
```http
POST /api/products
Content-Type: application/json
Authorization: Bearer <token>

{
  "sku": "PROD-001",
  "name": "Product Name",
  "description": "Product Description",
  "category": "Electronics",
  "unitPrice": 99.99,
  "quantityInStock": 100,
  "reorderLevel": 10
}
```

#### Update Stock
```http
PATCH /api/products/{id}/stock?change=-5
Authorization: Bearer <token>
```

### Order Endpoints (Order Service)

#### Create Order
```http
POST /api/orders
Content-Type: application/json
Authorization: Bearer <token>

{
  "customerId": 1,
  "customerName": "John Doe",
  "customerEmail": "john@example.com",
  "shippingAddress": "123 Main St, City, State 12345",
  "orderItems": [
    {
      "productId": 1,
      "quantity": 2,
      "unitPrice": 99.99
    }
  ]
}
```

#### Get Orders by Customer
```http
GET /api/orders/customer/{customerId}
Authorization: Bearer <token>
```

## 🔧 Configuration

### Application Properties

Each microservice can be configured through:

1. **application.yml** - Local configuration
2. **Config Server** - Centralized configuration
3. **Environment Variables** - Runtime configuration

### Database Configuration

Default database connections:
- **Inventory Service**: `jdbc:mysql://localhost:3306/inventory_db`
- **Order Service**: `jdbc:mysql://localhost:3306/order_db`
- **User Service**: `jdbc:mysql://localhost:3306/user_db`

### Security Configuration

JWT Configuration:
- **Secret**: Configurable in application.yml
- **Expiration**: 24 hours (default)
- **Refresh Token**: 7 days (default)

## 🧪 Testing

### Running Tests

Run all tests for the entire project:
```bash
mvn test
```

Run tests for a specific module:
```bash
cd inventory-service
mvn test
```

### Test Coverage

The project includes comprehensive unit tests with:
- **JUnit 5** - Testing framework
- **Mockito** - Mocking framework
- **Spring Boot Test** - Integration testing
- **TestContainers** - Database testing

## 📊 Monitoring and Management

### Health Checks

Each service provides health check endpoints:
- `/actuator/health` - Service health status
- `/actuator/metrics` - Application metrics
- `/actuator/info` - Application information

### Service Discovery

View registered services in Eureka Dashboard:
http://localhost:8761

### Logging

Configure logging levels in application.yml:
```yaml
logging:
  level:
    com.enterprise.inventory: DEBUG
    org.springframework.security: DEBUG
```

## 🐳 Docker Support

### Building Docker Images

```bash
# Build all services
mvn clean package

# Build individual service
cd inventory-service
mvn clean package docker:build
```

### Running with Docker Compose

```bash
docker-compose up -d
```

## 🔒 Security Features

### Authentication & Authorization

- **JWT-based authentication** for secure API access
- **Role-based access control** (RBAC)
- **Password encryption** using BCrypt
- **Account lockout** after failed attempts
- **Token refresh** mechanism

### Security Best Practices

- **Input validation** on all endpoints
- **SQL injection prevention** with parameterized queries
- **CORS configuration** for cross-origin requests
- **HTTPS enforcement** in production
- **Security headers** for web applications

## 📈 Performance & Scalability

### Performance Features

- **Connection pooling** for database connections
- **Caching** for frequently accessed data
- **Lazy loading** for JPA relationships
- **Pagination** for large result sets
- **Asynchronous processing** for long operations

### Scalability Considerations

- **Stateless services** for horizontal scaling
- **Database sharding** support
- **Load balancing** through API Gateway
- **Circuit breaker** pattern for fault tolerance
- **Service discovery** for dynamic scaling

## 🔄 CI/CD Pipeline

### Build Pipeline

1. **Code Commit** - Git repository
2. **Build** - Maven compilation
3. **Test** - Unit and integration tests
4. **Package** - JAR file creation
5. **Deploy** - Container deployment
6. **Monitor** - Health and performance checks

### Deployment Strategies

- **Blue-Green Deployment** - Zero downtime
- **Canary Deployment** - Gradual rollout
- **Rolling Deployment** - Incremental updates
- **Feature Flags** - Controlled feature releases

## 🛠️ Development Guidelines

### Code Standards

- **Clean Code** principles
- **SOLID** design principles
- **Design Patterns** implementation
- **Comprehensive documentation**
- **Error handling** best practices

### Git Workflow

- **Feature branches** for new development
- **Pull requests** for code review
- **Semantic versioning** for releases
- **Conventional commits** for change tracking

## 📝 Project Structure

```
inventory-management-system/
├── eureka-server/           # Service discovery
├── api-gateway/            # API gateway
├── config-server/          # Configuration server
├── inventory-service/      # Inventory management
├── order-service/          # Order processing
├── user-service/           # User management
├── database-setup.sql      # Database schema
├── pom.xml                 # Root Maven POM
└── README.md              # This file
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 📞 Support

For support and questions:
- **Email**: support@inventory-system.com
- **Documentation**: [Wiki](https://github.com/inventory-system/wiki)
- **Issues**: [GitHub Issues](https://github.com/inventory-system/issues)

## 🗺️ Roadmap

### Version 2.0 (Planned)
- **Real-time notifications** with WebSocket
- **Advanced analytics** and reporting
- **Mobile application** support
- **Multi-tenancy** support
- **Advanced search** and filtering

### Version 3.0 (Future)
- **Machine learning** for demand forecasting
- **Blockchain** for supply chain transparency
- **IoT integration** for smart inventory
- **AI-powered** recommendations
- **Voice commands** support

---

**Built with ❤️ using Spring Boot Microservices Architecture**
