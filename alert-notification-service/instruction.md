# Alert Notification Service — Instructions

## Overview

This document describes how to build, run, test and use the Alert Notification Service microservice. This service manages alerts and notifications with features for email delivery, retry logic, and status tracking.

## Checklist

- [ ] Prerequisites installed (Java 17, Maven)
- [ ] Build the alert-notification-service module
- [ ] Run unit & integration tests
- [ ] Start the application
- [ ] Create an alert via API
- [ ] Create a notification via API
- [ ] Query alerts and notifications
- [ ] Test retry mechanism

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- (Optional) curl or Postman for API testing
- (Optional) H2 Database Console accessible at http://localhost:8083/h2-console

## Build

Run this from the repository root:

```bash
mvn -pl alert-notification-service -am clean package
```

The `-am` flag builds required modules.

## Run Tests

Execute the unit and integration tests for this module only:

```bash
mvn -pl alert-notification-service test
```

## Run the Application

Start the service locally (uses H2 in-memory database and port 8083 by default):

```bash
mvn -pl alert-notification-service spring-boot:run
```

Or run the shaded jar if packaged:

```bash
java -jar alert-notification-service/target/alert-notification-service-0.0.1-SNAPSHOT.jar
```

The application will be available at `http://localhost:8083`

## API Endpoints

### Alerts

#### Create Alert
```bash
curl -X POST http://localhost:8083/api/alerts \
  -H "Content-Type: application/json" \
  -d '{
    "title": "High Memory Usage",
    "description": "Memory usage exceeded 90%",
    "recipientEmail": "admin@example.com",
    "severity": "HIGH"
  }'
```

#### Get All Alerts
```bash
curl http://localhost:8083/api/alerts
```

#### Get Alert by ID
```bash
curl http://localhost:8083/api/alerts/{id}
```

#### Get Alerts by Email
```bash
curl http://localhost:8083/api/alerts/by-email/admin@example.com
```

#### Get Alerts by Status
```bash
curl http://localhost:8083/api/alerts/by-status/SENT
```

#### Acknowledge Alert
```bash
curl -X PUT "http://localhost:8083/api/alerts/{id}/acknowledge?acknowledgedBy=admin"
```

#### Delete Alert
```bash
curl -X DELETE http://localhost:8083/api/alerts/{id}
```

### Notifications

#### Create Notification
```bash
curl -X POST http://localhost:8083/api/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "subject": "Welcome",
    "message": "Welcome to our service",
    "recipientEmail": "user@example.com",
    "type": "EMAIL"
  }'
```

#### Get All Notifications
```bash
curl http://localhost:8083/api/notifications
```

#### Get Notification by ID
```bash
curl http://localhost:8083/api/notifications/{id}
```

#### Get Notifications by Email
```bash
curl http://localhost:8083/api/notifications/by-email/user@example.com
```

#### Get Notifications by Status
```bash
curl http://localhost:8083/api/notifications/by-status/SENT
```

#### Retry Failed Notifications
```bash
curl -X POST http://localhost:8083/api/notifications/retry-failed
```

#### Delete Notification
```bash
curl -X DELETE http://localhost:8083/api/notifications/{id}
```

## Database Schema

### Alerts Table
- `id` (BIGINT, PK, AUTO_INCREMENT)
- `title` (VARCHAR, NOT NULL)
- `description` (TEXT, NOT NULL)
- `status` (ENUM: CREATED, SENT, ACKNOWLEDGED, FAILED, CANCELLED)
- `severity` (ENUM: LOW, MEDIUM, HIGH, CRITICAL)
- `recipient_email` (VARCHAR, NOT NULL)
- `created_at` (TIMESTAMP, NOT NULL)
- `sent_at` (TIMESTAMP)
- `acknowledged_at` (TIMESTAMP)
- `acknowledged_by` (VARCHAR)

### Notifications Table
- `id` (BIGINT, PK, AUTO_INCREMENT)
- `subject` (VARCHAR, NOT NULL)
- `message` (TEXT, NOT NULL)
- `recipient_email` (VARCHAR, NOT NULL)
- `status` (ENUM: PENDING, SENT, FAILED, RETRY)
- `type` (ENUM: EMAIL, SMS, PUSH, WEBHOOK)
- `created_at` (TIMESTAMP, NOT NULL)
- `sent_at` (TIMESTAMP)
- `failure_reason` (VARCHAR)
- `retry_count` (INT)

## Configuration

Key configuration properties in `application.yaml`:

- `server.port`: 8083
- `spring.datasource.url`: H2 in-memory database
- `spring.jpa.hibernate.ddl-auto`: create-drop
- `spring.mail.host`: localhost (SMTP server)
- `logging.level.com.sks`: DEBUG

## Features

### Alert Management
- Create alerts with different severity levels
- Track alert status (CREATED, SENT, ACKNOWLEDGED, FAILED, CANCELLED)
- Acknowledge alerts with user tracking
- Retrieve alerts by email, status, or date range

### Notification Management
- Send notifications via email
- Automatic retry mechanism (max 3 retries)
- Track notification status
- Query notifications by email or status

### Email Service
- Simple email delivery via SMTP
- Failure handling and logging
- Integration with Spring Mail

### Data Persistence
- H2 in-memory database (development)
- JPA/Hibernate ORM
- Support for MySQL (update datasource in application.yaml)

## Troubleshooting

### Port Already in Use
Change the port in `application.yaml`:
```yaml
server:
  port: 8084
```

### H2 Console Not Accessible
Ensure H2 console is enabled in `application.yaml`:
```yaml
spring:
  h2:
    console:
      enabled: true
```

### Mail Delivery Issues
Check SMTP settings in `application.yaml`. For development, use MailHog or mock services.

### Database Errors
Check that JPA/Hibernate is properly configured. The schema is auto-created with `ddl-auto: create-drop`.

## Technologies Used

- Spring Boot 4.0.6
- Spring Data JPA
- Spring Security
- Spring Mail
- Hibernate
- H2 Database
- Lombok
- Maven

## Next Steps

1. Replace H2 with MySQL for production
2. Implement async email delivery using Kafka
3. Add authentication and authorization
4. Implement notification templates
5. Add metrics and monitoring with Actuator
6. Deploy with Docker


