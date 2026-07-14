# Using null-check-agent in Your Spring Boot Project

This guide shows you exactly how to add the null-check-agent to your existing Spring Boot project.

## Prerequisites

- Java 11 or higher
- Maven project
- Basic Spring Boot knowledge

## Step-by-Step Guide

### Step 1: Update pom.xml

Add this dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.nullcheck</groupId>
    <artifactId>null-check-agent</artifactId>
    <version>1.0.0</version>
</dependency>
```

Your dependencies section should look like:

```xml
<dependencies>
    <!-- Your existing dependencies -->
    
    <!-- Add null-check-agent -->
    <dependency>
        <groupId>com.nullcheck</groupId>
        <artifactId>null-check-agent</artifactId>
        <version>1.0.0</version>
    </dependency>
    
    <!-- Spring Boot dependencies -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
</dependencies>
```

### Step 2: Create Configuration

Create a new file: `src/main/java/com/yourcompany/config/NullCheckAgentConfiguration.java`

```java
package com.yourcompany.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.nullcheck.agent.NullCheckAgent;
import com.nullcheck.agent.NullCheckConfig;

@Configuration
public class NullCheckAgentConfiguration {
    
    @Bean
    public NullCheckAgent nullCheckAgent() {
        NullCheckConfig config = new NullCheckConfig();
        
        // Configure what to check
        config.setCheckMethodCalls(true);
        config.setCheckFieldAccess(true);
        config.setCheckArrayAccess(true);
        config.setCheckMethodChaining(true);
        config.setExcludeTests(true);
        
        // Add your project-specific safe variables
        config.addSafeVariable("userRepository");
        config.addSafeVariable("accountService");
        config.addSafeVariable("logger");
        
        // Exclude generated or third-party code
        config.addExcludePattern("**/generated/**");
        config.addExcludePattern("**/dto/**");
        config.addExcludePattern("**/model/**");
        
        return new NullCheckAgent(config);
    }
}
```

### Step 3: Create Service (Optional but Recommended)

Create: `src/main/java/com/yourcompany/service/NullCheckService.java`

```java
package com.yourcompany.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.nullcheck.agent.NullCheckAgent;
import com.nullcheck.agent.NullCheckIssue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

@Service
public class NullCheckService {
    
    private static final Logger logger = LoggerFactory.getLogger(NullCheckService.class);
    
    @Autowired
    private NullCheckAgent nullCheckAgent;
    
    /**
     * Run null check analysis on the project
     */
    public NullCheckResult analyzeProject(String projectPath) {
        try {
            logger.info("Starting null check analysis for: {}", projectPath);
            
            nullCheckAgent.analyzeProject(projectPath);
            List<NullCheckIssue> issues = nullCheckAgent.getIssues();
            
            NullCheckResult result = new NullCheckResult();
            result.setTotalIssues(issues.size());
            result.setErrors((int)issues.stream()
                .filter(i -> i.getSeverity() == NullCheckIssue.Severity.ERROR)
                .count());
            result.setWarnings((int)issues.stream()
                .filter(i -> i.getSeverity() == NullCheckIssue.Severity.WARNING)
                .count());
            result.setInfos((int)issues.stream()
                .filter(i -> i.getSeverity() == NullCheckIssue.Severity.INFO)
                .count());
            result.setIssues(issues);
            
            logger.info("Analysis complete. Found {} issues", issues.size());
            return result;
            
        } catch (Exception e) {
            logger.error("Error during null check analysis", e);
            throw new RuntimeException("Failed to analyze project", e);
        }
    }
    
    /**
     * Analyze just the main code (exclude tests)
     */
    public NullCheckResult analyzeMainCode() {
        return analyzeProject("src/main/java");
    }
    
    /**
     * Get summary of issues
     */
    public NullCheckSummary getSummary(String projectPath) {
        NullCheckResult result = analyzeProject(projectPath);
        
        NullCheckSummary summary = new NullCheckSummary();
        summary.setTotal(result.getTotalIssues());
        summary.setErrors(result.getErrors());
        summary.setWarnings(result.getWarnings());
        summary.setInfos(result.getInfos());
        summary.setStatus(result.getErrors() > 0 ? "FAILED" : "PASSED");
        
        return summary;
    }
}
```

Create supporting model classes:

**NullCheckResult.java:**
```java
package com.yourcompany.service;

import com.nullcheck.agent.NullCheckIssue;
import java.util.List;

public class NullCheckResult {
    private int totalIssues;
    private int errors;
    private int warnings;
    private int infos;
    private List<NullCheckIssue> issues;
    
    // Getters and Setters
    public int getTotalIssues() { return totalIssues; }
    public void setTotalIssues(int totalIssues) { this.totalIssues = totalIssues; }
    
    public int getErrors() { return errors; }
    public void setErrors(int errors) { this.errors = errors; }
    
    public int getWarnings() { return warnings; }
    public void setWarnings(int warnings) { this.warnings = warnings; }
    
    public int getInfos() { return infos; }
    public void setInfos(int infos) { this.infos = infos; }
    
    public List<NullCheckIssue> getIssues() { return issues; }
    public void setIssues(List<NullCheckIssue> issues) { this.issues = issues; }
}
```

**NullCheckSummary.java:**
```java
package com.yourcompany.service;

public class NullCheckSummary {
    private int total;
    private int errors;
    private int warnings;
    private int infos;
    private String status;
    
    // Getters and Setters
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
    
    public int getErrors() { return errors; }
    public void setErrors(int errors) { this.errors = errors; }
    
    public int getWarnings() { return warnings; }
    public void setWarnings(int warnings) { this.warnings = warnings; }
    
    public int getInfos() { return infos; }
    public void setInfos(int infos) { this.infos = infos; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
```

### Step 4: Create REST API Endpoint (Optional)

Create: `src/main/java/com/yourcompany/controller/NullCheckController.java`

```java
package com.yourcompany.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.yourcompany.service.NullCheckService;
import com.yourcompany.service.NullCheckResult;
import com.yourcompany.service.NullCheckSummary;

@RestController
@RequestMapping("/api/nullcheck")
public class NullCheckController {
    
    @Autowired
    private NullCheckService nullCheckService;
    
    /**
     * Run analysis and get detailed issues
     * GET /api/nullcheck/analyze?path=src/main/java
     */
    @GetMapping("/analyze")
    public NullCheckResult analyze(@RequestParam(defaultValue = "src/main/java") String path) {
        return nullCheckService.analyzeProject(path);
    }
    
    /**
     * Get quick summary
     * GET /api/nullcheck/summary
     */
    @GetMapping("/summary")
    public NullCheckSummary getSummary(
        @RequestParam(defaultValue = "src/main/java") String path) {
        return nullCheckService.getSummary(path);
    }
    
    /**
     * Analyze main code (src/main/java)
     * GET /api/nullcheck/quick
     */
    @GetMapping("/quick")
    public NullCheckResult quickCheck() {
        return nullCheckService.analyzeMainCode();
    }
    
    /**
     * Health check
     * GET /api/nullcheck/health
     */
    @GetMapping("/health")
    public String health() {
        return "{\"status\": \"UP\"}";
    }
}
```

### Step 5: Add Scheduled Task (Optional)

Create: `src/main/java/com/yourcompany/scheduler/NullCheckScheduler.java`

```java
package com.yourcompany.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.yourcompany.service.NullCheckService;
import com.nullcheck.agent.NullCheckIssue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class NullCheckScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(NullCheckScheduler.class);
    
    @Autowired
    private NullCheckService nullCheckService;
    
    /**
     * Run null check analysis every 6 hours
     */
    @Scheduled(fixedDelay = 21600000) // 6 hours in milliseconds
    public void runNullCheckAnalysis() {
        try {
            logger.info("Starting scheduled null pointer check analysis...");
            
            var result = nullCheckService.analyzeMainCode();
            
            if (result.getTotalIssues() == 0) {
                logger.info("✓ No null pointer issues found!");
            } else {
                logger.warn("⚠ Found {} potential null pointer issues", result.getTotalIssues());
                
                // Log errors
                if (result.getErrors() > 0) {
                    logger.error("Found {} ERROR level issues", result.getErrors());
                }
                
                // Log warnings
                if (result.getWarnings() > 0) {
                    logger.warn("Found {} WARNING level issues", result.getWarnings());
                }
            }
        } catch (Exception e) {
            logger.error("Error running null check analysis", e);
        }
    }
}
```

### Step 6: Enable Scheduling in Main Application

Update your main Spring Boot application class:

```java
package com.yourcompany;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  // Add this line
public class YourApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(YourApplication.class, args);
    }
}
```

### Step 7: Configure application.properties (Optional)

Add to `src/main/resources/application.properties`:

```properties
# Null Check Agent Configuration
logging.level.com.nullcheck.agent=INFO
```

Or in `application.yml`:

```yaml
logging:
  level:
    com.nullcheck.agent: INFO
```

## Usage Examples

### Example 1: Call via REST API

```bash
# Get quick summary
curl http://localhost:8080/api/nullcheck/quick

# Analyze specific path
curl "http://localhost:8080/api/nullcheck/analyze?path=src/main/java"

# Get summary
curl "http://localhost:8080/api/nullcheck/summary"
```

### Example 2: Use in Code

```java
@Autowired
private NullCheckService nullCheckService;

// Run analysis
NullCheckResult result = nullCheckService.analyzeMainCode();

// Check results
if (result.getTotalIssues() > 0) {
    System.out.println("Found issues: " + result.getTotalIssues());
}
```

### Example 3: Call During Tests

```java
@RunWith(SpringRunner.class)
@SpringBootTest
public class NullCheckTest {
    
    @Autowired
    private NullCheckService nullCheckService;
    
    @Test
    public void testNullCheck() {
        NullCheckSummary summary = nullCheckService.getSummary("src/main/java");
        assertEquals(0, summary.getErrors());
    }
}
```

## Build and Run

```bash
# Clean install
mvn clean install

# Start application
mvn spring-boot:run

# Or run as JAR
java -jar target/your-app.jar
```

## Testing the Integration

```bash
# Check if service started
curl http://localhost:8080/api/nullcheck/health

# Run first analysis
curl http://localhost:8080/api/nullcheck/quick

# Check logs
tail -f logs/application.log
```

## Troubleshooting

### Issue: Dependency not found

```bash
# Make sure you built null-check-agent first
cd null-check-agent
mvn clean install
cd ../your-project
mvn clean install
```

### Issue: Too many false positives

Adjust configuration in `NullCheckAgentConfiguration`:

```java
config.addSafeVariable("yourVariable");
config.addExcludePattern("**/yourPackage/**");
config.setCheckFieldAccess(false); // Disable less important checks
```

### Issue: Agent runs slow

Exclude more patterns:

```java
config.addExcludePattern("target/");
config.addExcludePattern("build/");
config.setExcludeTests(true);
```

## Next Steps

1. Build and run your application
2. Test REST endpoints
3. Monitor logs for analysis results
4. Adjust configuration as needed
5. Integrate with CI/CD pipeline

See INTEGRATION_GUIDE.md for advanced configurations like CI/CD integration.
