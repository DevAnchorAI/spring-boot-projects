# JWT Service Communication - REST Service

A Spring Boot microservice that communicates with the JWT Auth Service using token-based authentication. This service demonstrates secure service-to-service communication using JWT tokens.

## Features

- **JWT Token-Based Communication**: Secure communication with JWT Auth Service
- **User Registration**: Register new users via Auth Service
- **User Login**: Obtain JWT tokens for subsequent API calls
- **Order Management**: Create, read, update, and delete orders with JWT authentication
- **Token Validation**: Automatic token validation for protected endpoints
- **Error Handling**: Comprehensive error handling and logging

## Architecture

```
┌─────────────────────────────────────────────────────┐
│         JWT Service Communication                   │
│  (Port: 8081)                                       │
│  ┌──────────────┐     ┌──────────────────────────┐ │
│  │ AuthController│────→│ JwtTokenService        │ │
│  │ - /login     │     │ - Calls remote Auth Svc│ │
│  │ - /register  │     │ - Validates tokens     │ │
│  └──────────────┘     └──────────────────────────┘ │
│  ┌──────────────┐                                   │
│  │OrderController├─ Validates JWT Token          │
│  │ - /create    │   before processing requests    │
│  │ - /get       │                                   │
│  │ - /update    │                                   │
│  │ - /delete    │                                   │
│  └──────────────┘                                   │
└────────────────────────┬──────────────────────────┘
                         │ HTTP Calls with JWT
                         ↓
            ┌────────────────────────┐
            │  JWT Auth Service      │
            │  (Port: 8080)          │
            │  ┌────────────────────┐│
            │  │ AuthController     ││
            │  │ - /register        ││
            │  │ - /login           ││
            │  │ - /validate        ││
            │  └────────────────────┘│
            └────────────────────────┘
```

## API Endpoints

### Authentication Endpoints (Proxy to Auth Service)

#### Register User
```
POST /api/auth/register
Content-Type: application/json

Request:
{
  "username": "john",
  "password": "password123"
}

Response: 201 Created
{
  "token": null,
  "message": "Registration successful",
  "success": true
}
```

#### Login
```
POST /api/auth/login
Content-Type: application/json

Request:
{
  "username": "john",
  "password": "password123"
}

Response: 200 OK
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "message": "Login successful",
  "success": true
}
```

### Order Management Endpoints (Protected with JWT)

#### Create Order
```
POST /api/order/create
Authorization: Bearer <jwt_token>
Content-Type: application/json

Request:
{
  "orderNumber": "ORD001",
  "description": "Sample Order",
  "amount": 100.50,
  "username": "john"
}

Response: 201 Created
{
  "id": 1,
  "orderNumber": "ORD001",
  "description": "Sample Order",
  "amount": 100.50,
  "status": "PENDING",
  "username": "john"
}
```

#### Get Order
```
GET /api/order/{id}
Authorization: Bearer <jwt_token>

Response: 200 OK
{
  "id": 1,
  "orderNumber": "ORD001",
  "description": "Sample Order",
  "amount": 100.50,
  "status": "PENDING",
  "username": "john"
}
```

#### Get User Orders
```
GET /api/order/user/orders
Authorization: Bearer <jwt_token>

Response: 200 OK
[
  {
    "id": 1,
    "orderNumber": "ORD001",
    "description": "Sample Order",
    "amount": 100.50,
    "status": "PENDING",
    "username": "john"
  }
]
```

#### Update Order
```
PUT /api/order/{id}
Authorization: Bearer <jwt_token>
Content-Type: application/json

Request:
{
  "orderNumber": "ORD001",
  "description": "Updated Order",
  "amount": 150.75,
  "status": "PROCESSING"
}

Response: 200 OK
{
  "id": 1,
  "orderNumber": "ORD001",
  "description": "Updated Order",
  "amount": 150.75,
  "status": "PROCESSING",
  "username": "john"
}
```

#### Get All Orders
```
GET /api/order/all
Authorization: Bearer <jwt_token>

Response: 200 OK
[
  {
    "id": 1,
    "orderNumber": "ORD001",
    "description": "Sample Order",
    "amount": 100.50,
    "status": "PENDING",
    "username": "john"
  }
]
```

#### Delete Order
```
DELETE /api/order/{id}
Authorization: Bearer <jwt_token>

Response: 200 OK
"Order deleted successfully"
```

## Configuration

### application.properties

```properties
# Server Port
server.port=8081

# JWT Auth Service URL (change based on where Auth Service runs)
auth.service.url=http://localhost:8080

# Logging
logging.level.root=INFO
logging.level.com.bs=DEBUG
```

## Setup Instructions

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- JWT Auth Service running on http://localhost:8080

### Build

```bash
cd jwt-service-communication
mvn clean install
```

### Run

```bash
mvn spring-boot:run
```

Or using Java directly:
```bash
java -jar target/jwt-service-communication-0.0.1-SNAPSHOT.jar
```

## Testing with cURL

### 1. Register a User
```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"john","password":"password123"}'
```

### 2. Login (Get JWT Token)
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"john","password":"password123"}'
```

Save the token from the response.

### 3. Create an Order (Using JWT Token)
```bash
export TOKEN="your_jwt_token_here"

curl -X POST http://localhost:8081/api/order/create \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "orderNumber":"ORD001",
    "description":"Sample Order",
    "amount":100.50,
    "username":"john"
  }'
```

### 4. Get All Orders
```bash
curl -X GET http://localhost:8081/api/order/all \
  -H "Authorization: Bearer $TOKEN"
```

### 5. Get Specific Order
```bash
curl -X GET http://localhost:8081/api/order/1 \
  -H "Authorization: Bearer $TOKEN"
```

### 6. Update Order
```bash
curl -X PUT http://localhost:8081/api/order/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "orderNumber":"ORD001",
    "description":"Updated Order",
    "amount":150.75,
    "status":"PROCESSING"
  }'
```

### 7. Delete Order
```bash
curl -X DELETE http://localhost:8081/api/order/1 \
  -H "Authorization: Bearer $TOKEN"
```

## Dependencies

- **spring-boot-starter-web**: REST API support
- **spring-boot-starter-webflux**: Reactive web client (for future enhancements)
- **jjwt**: JWT token parsing and validation
- **lombok**: Reduced boilerplate code
- **spring-boot-starter-test**: Unit testing

## Project Structure

```
jwt-service-communication/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/bs/
│   │   │   ├── JwtServiceCommunicationApplication.java
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java
│   │   │   │   └── OrderController.java
│   │   │   ├── service/
│   │   │   │   ├── JwtTokenService.java
│   │   │   │   └── OrderService.java
│   │   │   ├── dto/
│   │   │   │   ├── AuthRequest.java
│   │   │   │   ├── AuthResponse.java
│   │   │   │   ├── Order.java
│   │   │   │   └── RegisterRequest.java
│   │   │   └── config/
│   │   │       └── RestTemplateConfig.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
└── target/
```

## Key Components

### JwtTokenService
- Handles communication with JWT Auth Service
- Methods: `login()`, `validateToken()`, `register()`
- Uses RestTemplate for HTTP calls

### OrderService
- In-memory order storage (can be replaced with database)
- Order CRUD operations
- User-specific order filtering

### AuthController
- Proxy endpoints for user registration and login
- Forwards requests to JWT Auth Service

### OrderController
- Protected order management endpoints
- JWT token validation for all requests
- Comprehensive error handling

## Error Handling

The service returns appropriate HTTP status codes:
- **200 OK**: Successful operation
- **201 Created**: Resource created successfully
- **400 Bad Request**: Invalid input
- **401 Unauthorized**: Invalid or missing JWT token
- **404 Not Found**: Resource not found
- **500 Internal Server Error**: Server error

## Future Enhancements

1. **Database Integration**: Replace in-memory storage with JPA + MySQL
2. **Refresh Token Support**: Implement token refresh mechanism
3. **Rate Limiting**: Add rate limiting to prevent abuse
4. **Caching**: Cache validated tokens to reduce Auth Service calls
5. **Audit Logging**: Log all operations for audit trail
6. **API Documentation**: Add Swagger/OpenAPI documentation

## Troubleshooting

### "Invalid or missing JWT token"
- Ensure the JWT Auth Service is running on the configured URL
- Verify the token is valid (not expired)
- Check that the token is passed correctly in the Authorization header

### "Cannot connect to Auth Service"
- Check if JWT Auth Service is running
- Verify the `auth.service.url` property in application.properties
- Check network connectivity between services

### Token validation fails
- Token might be expired
- Token might be corrupted
- Auth Service might not recognize the token format

## Support

For issues or questions, refer to the JWT Auth Service documentation or contact the development team.
