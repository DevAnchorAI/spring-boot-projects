# Deployment Guide

## Overview

This guide covers deploying the Agentic AI System in various environments.

## Prerequisites

- Java 11 or higher
- Maven 3.6+
- Target Database (H2 for dev, PostgreSQL/MySQL for production)
- (Optional) Docker and Docker Compose for containerized deployment

## Development Deployment

### Local Development

```bash
# 1. Clone and navigate to project
cd agentic-ai-system

# 2. Build the project
mvn clean install

# 3. Run the application
mvn spring-boot:run

# 4. Access the API
# API Base URL: http://localhost:8080/api/v1
# Health Check: http://localhost:8080/actuator/health
```

### Development Configuration

Create `application-dev.properties`:

```properties
spring.profiles.active=dev
server.port=8080
logging.level.com.agentic=DEBUG
spring.jpa.show-sql=true
spring.h2.console.enabled=true
```

## Production Deployment

### Prerequisites

- PostgreSQL 12+ or MySQL 8+
- Java 11+ runtime
- Reverse proxy (Nginx/Apache)
- SSL certificate

### Production Configuration

Create `application-prod.properties`:

```properties
spring.profiles.active=prod
server.port=8080
server.servlet.context-path=/api

# Database Configuration
spring.datasource.url=jdbc:postgresql://db-host:5432/agentic_db
spring.datasource.username=agentic_user
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQL10Dialect

# Logging
logging.level.root=WARN
logging.level.com.agentic=INFO
logging.file.name=/var/log/agentic-ai-system/application.log
logging.file.max-size=10MB
logging.file.max-history=30

# Performance
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.task.execution.pool.core-size=20
spring.task.execution.pool.max-size=40

# Actuator
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=when-authorized
management.metrics.export.prometheus.enabled=true
```

### Step-by-Step Production Deployment

#### 1. Database Setup

```sql
-- PostgreSQL
CREATE DATABASE agentic_db;
CREATE USER agentic_user WITH PASSWORD 'strong_password';
GRANT ALL PRIVILEGES ON DATABASE agentic_db TO agentic_user;
```

#### 2. Build Application

```bash
# Build with production profile
mvn clean package -P prod

# JAR location: target/agentic-ai-system-1.0.0.jar
```

#### 3. Create Systemd Service

Create `/etc/systemd/system/agentic-ai-system.service`:

```ini
[Unit]
Description=Agentic AI System
After=network.target

[Service]
Type=simple
User=agentic
WorkingDirectory=/opt/agentic-ai-system
Environment="SPRING_PROFILES_ACTIVE=prod"
Environment="DB_PASSWORD=strong_password"
ExecStart=/usr/bin/java -jar /opt/agentic-ai-system/agentic-ai-system-1.0.0.jar
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

#### 4. Start Service

```bash
# Reload systemd daemon
sudo systemctl daemon-reload

# Enable service to start on boot
sudo systemctl enable agentic-ai-system

# Start the service
sudo systemctl start agentic-ai-system

# Check status
sudo systemctl status agentic-ai-system
```

#### 5. Configure Nginx Reverse Proxy

Create `/etc/nginx/sites-available/agentic-ai-system`:

```nginx
upstream agentic_backend {
    server localhost:8080;
}

server {
    listen 80;
    server_name api.example.com;
    
    # Redirect to HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name api.example.com;
    
    ssl_certificate /etc/letsencrypt/live/api.example.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/api.example.com/privkey.pem;
    
    # Security headers
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-Frame-Options "DENY" always;
    
    # Proxy settings
    location / {
        proxy_pass http://agentic_backend;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_cache_bypass $http_upgrade;
        
        # Timeouts
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }
}
```

Enable the site:
```bash
sudo ln -s /etc/nginx/sites-available/agentic-ai-system /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

## Docker Deployment

### Dockerfile

Create `Dockerfile`:

```dockerfile
FROM maven:3.8-openjdk-11 AS builder
WORKDIR /build
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:11-jre-slim
WORKDIR /app
COPY --from=builder /build/target/agentic-ai-system-1.0.0.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Docker Compose

Create `docker-compose.yml`:

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:14
    container_name: agentic-postgres
    environment:
      POSTGRES_DB: agentic_db
      POSTGRES_USER: agentic_user
      POSTGRES_PASSWORD: strong_password
    volumes:
      - postgres_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U agentic_user"]
      interval: 10s
      timeout: 5s
      retries: 5

  agentic-ai-system:
    build: .
    container_name: agentic-ai-system
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/agentic_db
      SPRING_DATASOURCE_USERNAME: agentic_user
      SPRING_DATASOURCE_PASSWORD: strong_password
    ports:
      - "8080:8080"
    depends_on:
      postgres:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 3s
      retries: 3
    restart: unless-stopped

volumes:
  postgres_data:
```

Deploy with Docker Compose:

```bash
# Build and start services
docker-compose up -d

# View logs
docker-compose logs -f agentic-ai-system

# Stop services
docker-compose down
```

## Kubernetes Deployment

### Deployment Manifest

Create `k8s-deployment.yaml`:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: agentic-ai-system
spec:
  replicas: 3
  selector:
    matchLabels:
      app: agentic-ai-system
  template:
    metadata:
      labels:
        app: agentic-ai-system
    spec:
      containers:
      - name: agentic-ai-system
        image: agentic-ai-system:1.0.0
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: prod
        - name: SPRING_DATASOURCE_URL
          valueFrom:
            configMapKeyRef:
              name: agentic-config
              key: db_url
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 20
          periodSeconds: 5
        resources:
          requests:
            cpu: 500m
            memory: 512Mi
          limits:
            cpu: 1000m
            memory: 1024Mi

---
apiVersion: v1
kind: Service
metadata:
  name: agentic-ai-system-service
spec:
  type: LoadBalancer
  selector:
    app: agentic-ai-system
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8080
```

Deploy to Kubernetes:

```bash
kubectl apply -f k8s-deployment.yaml
kubectl get pods
kubectl logs -f deployment/agentic-ai-system
```

## Scaling Considerations

### Horizontal Scaling

- Use load balancer (Nginx, HAProxy, AWS ELB)
- Deploy multiple instances
- Use shared database
- Consider message queue for task distribution

### Vertical Scaling

- Increase thread pool size
- Increase JVM heap size
- Use faster database
- Optimize queries

### Database Tuning

```sql
-- PostgreSQL indexes
CREATE INDEX idx_tasks_agent_id ON tasks(agent_id);
CREATE INDEX idx_tasks_status ON tasks(status);
CREATE INDEX idx_tasks_created_at ON tasks(created_at);
CREATE INDEX idx_agents_type ON agents(type);
CREATE INDEX idx_agents_status ON agents(status);
```

## Monitoring and Logging

### Prometheus Integration

Add to `application.properties`:

```properties
management.metrics.export.prometheus.enabled=true
management.endpoints.web.exposure.include=prometheus
```

Access metrics: `http://localhost:8080/actuator/prometheus`

### ELK Stack Integration

- Logback configuration for JSON logging
- Filebeat for log collection
- Elasticsearch for storage
- Kibana for visualization

### Application Performance Monitoring (APM)

Consider using:
- New Relic
- Datadog
- Elastic APM
- Dynatrace

## Backup and Recovery

### Database Backup

```bash
# PostgreSQL backup
pg_dump -U agentic_user agentic_db > backup.sql

# Restore from backup
psql -U agentic_user agentic_db < backup.sql
```

### Application Backup

```bash
# Backup JAR and configuration
tar -czf agentic-ai-system-backup.tar.gz /opt/agentic-ai-system/
```

## Security Checklist

- [ ] Change default passwords
- [ ] Enable SSL/TLS
- [ ] Configure firewall rules
- [ ] Enable authentication
- [ ] Set up rate limiting
- [ ] Enable audit logging
- [ ] Regular security updates
- [ ] Backup and recovery testing

## Troubleshooting

### Application won't start

```bash
# Check logs
tail -f /var/log/agentic-ai-system/application.log

# Check port availability
netstat -tlnp | grep 8080

# Check database connection
psql -h localhost -U agentic_user -d agentic_db
```

### Performance issues

- Monitor CPU and memory usage
- Check database query performance
- Review application logs for errors
- Increase thread pool size if needed

### Database issues

- Check database connectivity
- Verify database permissions
- Review slow query logs
- Optimize indexes

## Rollback Procedure

```bash
# Stop current version
sudo systemctl stop agentic-ai-system

# Restore previous version
cp /backups/agentic-ai-system-1.0.0.jar /opt/agentic-ai-system/

# Start service
sudo systemctl start agentic-ai-system

# Verify
curl http://localhost:8080/actuator/health
```

## Support and Documentation

- Application Logs: `/var/log/agentic-ai-system/`
- Configuration: `/opt/agentic-ai-system/application-prod.properties`
- Database: PostgreSQL on localhost:5432
- API Documentation: See README.md
