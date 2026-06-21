# JWT Auth Service - Implementation Guide

## Quick Start

### 1. Database Setup
Create MySQL database and user:
```sql
CREATE DATABASE jwt_auth_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'jwtuser'@'%' IDENTIFIED BY 'jwtpassword';
GRANT ALL PRIVILEGES ON jwt_auth_db.* TO 'jwtuser'@'%';
FLUSH PRIVILEGES;
```

Or use existing root user (update application.yml credentials if using different credentials).

### 2. Build the Module
From repo root:
```powershell
cd C:\WORK\CODE\spring-boot-projects
.\mvnw -DskipTests=true -pl jwt-auth-service package
```

### 3. Run the Service
```powershell
.\mvnw -pl jwt-auth-service spring-boot:run
```
Service runs on port 9100.

## Features Implemented

- **JWT-based Authentication**: Token generation and validation using JJWT library
- **Persistent User Store**: JPA + MySQL users table with Flyway migrations
- **User Registration**: `/api/auth/register` endpoint to create new users
- **User Login**: `/api/auth/login` endpoint to obtain JWT tokens
- **Token Validation**: `/api/auth/validate` endpoint to verify token validity
- **Role-based Authorization**: Endpoints secured by roles (ROLE_USER, ROLE_ADMIN)
  - `/api/user/**` - accessible to ROLE_USER and ROLE_ADMIN
  - `/api/admin/**` - accessible to ROLE_ADMIN only
  - `/api/auth/**` - public, no authentication needed

## API Endpoints

### 1. Register User
```
POST /api/auth/register
Content-Type: application/json

{
  "username": "john",
  "password": "password123"
}

Response: 201 Created
{
  "id": 1,
  "username": "john",
  "password": "$2a$10$...",
  "role": "ROLE_USER"
}
```

### 2. Login
```
POST /api/auth/login
Content-Type: application/json

{
  "username": "john",
  "password": "password123"
}

Response: 200 OK
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### 3. Validate Token
```
GET /api/auth/validate
Authorization: Bearer <token>

Response: 200 OK
true
```

### 4. User Endpoint (Secured)
```
GET /api/user/me
Authorization: Bearer <token>

Response: 200 OK
{
  "username": "john",
  "role": "ROLE_USER"
}
```

### 5. Admin Endpoint (Secured)
```
GET /api/admin/info
Authorization: Bearer <token>

Response: 200 OK (if ROLE_ADMIN)
{
  "username": "admin",
  "role": "ROLE_ADMIN"
}

Response: 403 Forbidden (if ROLE_USER)
```

## Testing with curl/PowerShell

```powershell
# 1. Register
$body = '{"username":"testuser","password":"test123"}'
Invoke-WebRequest -Uri "http://localhost:9100/api/auth/register" `
  -Method POST -ContentType "application/json" -Body $body

# 2. Login
Invoke-WebRequest -Uri "http://localhost:9100/api/auth/login" `
  -Method POST -ContentType "application/json" -Body $body

# 3. Call secured endpoint (replace TOKEN with actual JWT)
$headers = @{ Authorization = "Bearer TOKEN" }
Invoke-WebRequest -Uri "http://localhost:9100/api/user/me" `
  -Method GET -Headers $headers
```

## Configuration

Edit `application.yml` to customize:
- Database credentials (datasource.username/password)
- JWT secret (jwt.secret) - **change this in production**
- JWT expiration (jwt.expirationMs) - defaults to 1 hour
- Port (server.port)

## Database Schema (Auto-created by Flyway)

```sql
CREATE TABLE users (
  id BIGINT NOT NULL AUTO_INCREMENT,
  username VARCHAR(100) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  role VARCHAR(50) NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB;
```

## Integration with Other Microservices

To use this as a centralized auth server for other services:

1. **Share JWT Secret**: Configure same `jwt.secret` in all services
2. **Add JWT Filter**: Each service reuses `JwtAuthenticationFilter` logic
3. **Protect Endpoints**: Use Spring Security `@PreAuthorize` or filter chain
4. **Example** (in other service):
```java
http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
http.authorizeRequests()
    .antMatchers("/api/auth/**").permitAll()
    .antMatchers("/admin/**").hasRole("ADMIN")
    .anyRequest().authenticated();
```

## Troubleshooting

**Flyway Error: "Unsupported Database: MySQL 8.0"**
- Solution: Flyway 8.5.13 is pinned in pom.xml for MySQL 8 compatibility
- Ensure `flyway-mysql` is NOT in dependencies (use only `flyway-core`)

**Connection Refused on Startup**
- Check MySQL is running: `mysql -u root -p` (should connect)
- Verify database exists: `SHOW DATABASES;` (should list `jwt_auth_db`)
- Verify credentials in `application.yml`

**"User already exists" on Registration**
- Username is unique constraint; use a different username

**Token Validation Fails**
- Ensure token hasn't expired (check jwt.expirationMs)
- Verify Authorization header format: `Bearer <token>` (note space after Bearer)
- Check token was signed with same `jwt.secret`

## Files Modified/Created

- `pom.xml` - Added JPA, MySQL, Flyway, JWT dependencies
- `src/main/java/com/example/jwtauth/entity/AppUser.java` - User entity (JPA)
- `src/main/java/com/example/jwtauth/repository/UserRepository.java` - User repository
- `src/main/java/com/example/jwtauth/service/UserService.java` - User registration/lookup
- `src/main/java/com/example/jwtauth/service/JpaUserDetailsService.java` - JPA-backed UserDetailsService
- `src/main/java/com/example/jwtauth/security/JwtUtil.java` - JWT generation/validation
- `src/main/java/com/example/jwtauth/security/JwtAuthenticationFilter.java` - JWT filter
- `src/main/java/com/example/jwtauth/config/SecurityConfig.java` - Security configuration
- `src/main/java/com/example/jwtauth/controller/AuthController.java` - Auth endpoints (login, register, validate)
- `src/main/java/com/example/jwtauth/controller/UserApiController.java` - User-role endpoint
- `src/main/java/com/example/jwtauth/controller/AdminApiController.java` - Admin-role endpoint
- `src/main/resources/application.yml` - Spring Boot config (DB, JWT, Flyway)
- `src/main/resources/db/migration/V1__create_users_table.sql` - Flyway migration (create users table)
- `instruction.md` - This file

## Next Steps

1. **Optional**: Create an admin user manually:
   ```sql
   INSERT INTO users (username, password, role) 
   VALUES ('admin', '$2a$10$...bcrypt_hash...', 'ROLE_ADMIN');
   ```
2. **Optional**: Integrate JWT auth into other microservices by sharing the security filter and JWT secret
3. **Production**: Use environment variables for secrets (jwt.secret), datasource credentials
