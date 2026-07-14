# Installation and Integration Guide

## Quick Start

The Null Pointer Check Agent can be easily integrated into any Spring Boot project as a plugin.

## Step 1: Add Dependency to pom.xml

Add the following dependency to your Spring Boot project's `pom.xml`:

```xml
<dependency>
    <groupId>com.nullcheck</groupId>
    <artifactId>null-check-agent</artifactId>
    <version>1.0.0</version>
</dependency>
```

Then run:
```bash
mvn clean install
```

## Step 2: Create a Configuration Class

Create a `NullCheckAgentConfig.java` in your project:

```java
package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.nullcheck.agent.NullCheckAgent;
import com.nullcheck.agent.NullCheckConfig;

@Configuration
public class NullCheckAgentConfig {
    
    @Bean
    public NullCheckAgent nullCheckAgent() {
        NullCheckConfig config = new NullCheckConfig();
        config.setCheckMethodCalls(true);
        config.setCheckFieldAccess(true);
        config.setCheckArrayAccess(true);
        config.setCheckMethodChaining(true);
        config.setExcludeTests(true);
        
        // Add custom safe variables for your project
        config.addSafeVariable("userRepository");
        config.addSafeVariable("accountService");
        
        // Add exclusion patterns
        config.addExcludePattern("**/generated/**");
        
        return new NullCheckAgent(config);
    }
}
```

## Step 3: Create a Service to Run Checks

Create `NullCheckService.java`:

```java
package com.example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.nullcheck.agent.NullCheckAgent;
import com.nullcheck.agent.NullCheckIssue;
import java.util.List;

@Service
public class NullCheckService {
    
    @Autowired
    private NullCheckAgent nullCheckAgent;
    
    /**
     * Analyze project and return results
     */
    public List<NullCheckIssue> analyzeProject(String projectPath) {
        nullCheckAgent.analyzeProject(projectPath);
        return nullCheckAgent.getIssues();
    }
    
    /**
     * Get count of issues by severity
     */
    public NullCheckSummary getIssueSummary(String projectPath) {
        nullCheckAgent.analyzeProject(projectPath);
        List<NullCheckIssue> issues = nullCheckAgent.getIssues();
        
        NullCheckSummary summary = new NullCheckSummary();
        summary.setTotalIssues(issues.size());
        summary.setErrors((int)issues.stream()
            .filter(i -> i.getSeverity() == NullCheckIssue.Severity.ERROR)
            .count());
        summary.setWarnings((int)issues.stream()
            .filter(i -> i.getSeverity() == NullCheckIssue.Severity.WARNING)
            .count());
        summary.setInfos((int)issues.stream()
            .filter(i -> i.getSeverity() == NullCheckIssue.Severity.INFO)
            .count());
        
        return summary;
    }
}
```

## Step 4: Create a REST Controller (Optional)

Create `NullCheckController.java` to expose API endpoints:

```java
package com.example.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.example.service.NullCheckService;
import com.nullcheck.agent.NullCheckIssue;
import java.util.List;

@RestController
@RequestMapping("/api/nullcheck")
public class NullCheckController {
    
    @Autowired
    private NullCheckService nullCheckService;
    
    /**
     * Analyze project and get all issues
     */
    @PostMapping("/analyze")
    public List<NullCheckIssue> analyzeProject(@RequestParam String projectPath) {
        return nullCheckService.analyzeProject(projectPath);
    }
    
    /**
     * Get null check summary
     */
    @PostMapping("/summary")
    public NullCheckSummary getSummary(@RequestParam String projectPath) {
        return nullCheckService.getIssueSummary(projectPath);
    }
}
```

## Step 5: Create Scheduled Task (Optional)

Create `NullCheckScheduler.java` to run checks periodically:

```java
package com.example.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.example.service.NullCheckService;
import com.nullcheck.agent.NullCheckIssue;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class NullCheckScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(NullCheckScheduler.class);
    
    @Autowired
    private NullCheckService nullCheckService;
    
    /**
     * Run null check analysis every hour
     */
    @Scheduled(fixedDelay = 3600000)
    public void runNullCheck() {
        try {
            logger.info("Starting scheduled null pointer check analysis...");
            
            List<NullCheckIssue> issues = nullCheckService.analyzeProject("src/main/java");
            
            if (issues.isEmpty()) {
                logger.info("✓ No null pointer issues found!");
            } else {
                logger.warn("⚠ Found {} potential null pointer issues", issues.size());
                
                // Log warnings
                issues.stream()
                    .filter(i -> i.getSeverity() == NullCheckIssue.Severity.WARNING)
                    .forEach(issue -> logger.warn(
                        "{}:{} - {}", 
                        issue.getFilePath(), 
                        issue.getLineNumber(), 
                        issue.getMessage()
                    ));
            }
        } catch (Exception e) {
            logger.error("Error running null check analysis", e);
        }
    }
}
```

## Step 6: Enable Scheduling in Application

Update your main `@SpringBootApplication` class:

```java
package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MySpringBootApp {
    
    public static void main(String[] args) {
        SpringApplication.run(MySpringBootApp.class, args);
    }
}
```

## Step 7: Configure in application.properties

Add to `application.properties`:

```properties
# Null Check Agent Configuration
nullcheck.enabled=true
nullcheck.check-method-calls=true
nullcheck.check-field-access=true
nullcheck.check-array-access=true
nullcheck.check-method-chaining=true
nullcheck.exclude-tests=true
```

## Manual Invocation

If you prefer manual invocation without scheduling:

```java
@GetMapping("/run-check")
public ResponseEntity<Map<String, Object>> runNullCheck() {
    NullCheckConfig config = new NullCheckConfig();
    NullCheckAgent agent = new NullCheckAgent(config);
    agent.analyzeProject("src/main/java");
    
    List<NullCheckIssue> issues = agent.getIssues();
    
    Map<String, Object> response = new HashMap<>();
    response.put("totalIssues", issues.size());
    response.put("issues", issues);
    
    return ResponseEntity.ok(response);
}
```

## Command Line Usage

You can also run the agent from command line:

```bash
# Build the agent
mvn clean package

# Run the agent
java -cp target/null-check-agent-1.0.0.jar com.nullcheck.agent.NullCheckAgent "C:\path\to\your\project\src"
```

## Integration with CI/CD Pipeline

### Maven Build

Add to your pom.xml:

```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>exec-maven-plugin</artifactId>
    <version>3.0.0</version>
    <executions>
        <execution>
            <phase>test</phase>
            <goals>
                <goal>java</goal>
            </goals>
            <configuration>
                <mainClass>com.nullcheck.agent.NullCheckAgent</mainClass>
                <arguments>
                    <argument>src/main/java</argument>
                </arguments>
            </configuration>
        </execution>
    </executions>
</plugin>
```

Run with:
```bash
mvn clean verify
```

### GitHub Actions

Create `.github/workflows/nullcheck.yml`:

```yaml
name: Null Check Agent

on: [push, pull_request]

jobs:
  nullcheck:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - uses: actions/setup-java@v2
        with:
          java-version: '11'
      - run: mvn clean package
      - run: java -cp target/null-check-agent-1.0.0.jar com.nullcheck.agent.NullCheckAgent src/main/java
```

### GitLab CI

Create `.gitlab-ci.yml`:

```yaml
nullcheck:
  image: maven:3.8.1-openjdk-11
  script:
    - mvn clean package
    - java -cp target/null-check-agent-1.0.0.jar com.nullcheck.agent.NullCheckAgent src/main/java
```

## Troubleshooting

### Issue: Dependency not found

**Solution:** Make sure you have the artifact deployed to your Maven repository or build from source:

```bash
cd null-check-agent
mvn clean install
```

### Issue: Too many false positives

**Solution:** Configure safe variables and exclude patterns for your project:

```java
config.addSafeVariable("yourVariable");
config.addExcludePattern("**/yourPackage/**");
```

### Issue: ClassNotFoundException

**Solution:** Ensure the JAR is on the classpath:

```bash
java -cp ".:./lib/*" com.nullcheck.agent.NullCheckAgent src
```

## Next Steps

1. Configure for your project's specific needs
2. Run initial analysis to identify issues
3. Fix critical issues first (ERROR severity)
4. Add to CI/CD pipeline for automated checks
5. Set up alerts for new issues

For more examples, see `NullCheckAgentExample.java`.
