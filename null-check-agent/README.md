# Null Pointer Check Agent

A lightweight, configurable Java agent for detecting potential null pointer dereferences in Spring Boot projects and Java files.

## Overview

The Null Pointer Check Agent is a standalone Java library that can be integrated into any Spring Boot project as a plugin to automatically scan for potential null pointer exceptions (NPE) at compile time or runtime.

### Key Features

✓ **Configurable Detection** - Enable/disable specific checks (method calls, field access, array access, method chaining)  
✓ **Easy Integration** - Add as a Maven dependency to any Spring Boot project  
✓ **IntelliJ IDE Support** - Configure directly in IntelliJ IDEA  
✓ **Flexible Exclusions** - Exclude test files, generated code, or specific patterns  
✓ **Safe Variables** - Define trusted variables that won't trigger warnings  
✓ **Detailed Reports** - Get detailed analysis reports grouped by file and severity  

## Installation

### Option 1: Maven Dependency

Add to your Spring Boot project's `pom.xml`:

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

### Option 2: Manual Installation

1. Clone or download the project
2. Build the JAR:
   ```bash
   mvn clean package
   ```
3. Copy the JAR to your project's lib directory
4. Add to your classpath

## Usage

### Option 1: Command Line

Scan a project from the command line:

```bash
java -cp null-check-agent-1.0.0.jar com.nullcheck.agent.NullCheckAgent "C:\path\to\spring-boot-project"
```

**Output Example:**
```
================================================================================
NULL POINTER CHECK AGENT - ANALYSIS REPORT
================================================================================
Found 5 potential null pointer issue(s):

File: C:\path\to\UserService.java
--------------------------------------------------------------------------------
  Line 45 [WARNING]: Potential null dereference: user.getName()
    Code: String name = user.getName().toUpperCase();
  Line 67 [INFO]: Potential null field access: response.data
    Code: int id = response.data.id;

================================================================================
Total Issues: 5
================================================================================
```

### Option 2: Programmatically in Java

Use the agent in your Spring Boot application:

```java
import com.nullcheck.agent.NullCheckAgent;
import com.nullcheck.agent.NullCheckConfig;
import com.nullcheck.agent.NullCheckIssue;
import java.util.List;

public class MyApplication {
    public static void main(String[] args) {
        // Create configuration
        NullCheckConfig config = new NullCheckConfig();
        config.setCheckMethodCalls(true);
        config.setCheckFieldAccess(true);
        config.setCheckArrayAccess(true);
        config.setCheckMethodChaining(true);
        config.setExcludeTests(true);
        
        // Create agent
        NullCheckAgent agent = new NullCheckAgent(config);
        
        // Analyze project
        agent.analyzeProject("C:\\path\\to\\project");
        
        // Get results
        List<NullCheckIssue> issues = agent.getIssues();
        for (NullCheckIssue issue : issues) {
            System.out.println(issue);
        }
    }
}
```

### Option 3: Spring Boot Application

Create a scheduled task in your Spring Boot application:

```java
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.nullcheck.agent.NullCheckAgent;
import com.nullcheck.agent.NullCheckConfig;

@Component
public class NullCheckScheduler {
    
    @Scheduled(fixedDelay = 3600000) // Run hourly
    public void runNullCheck() {
        NullCheckConfig config = new NullCheckConfig();
        NullCheckAgent agent = new NullCheckAgent(config);
        agent.analyzeProject("src/main/java");
    }
}
```

## Configuration

### Basic Configuration

```java
NullCheckConfig config = new NullCheckConfig();

// Enable/disable specific checks
config.setCheckMethodCalls(true);       // obj.method() calls
config.setCheckFieldAccess(true);       // obj.field accesses
config.setCheckArrayAccess(true);       // array[index] accesses
config.setCheckMethodChaining(true);    // obj.method1().method2() chains
config.setExcludeTests(true);           // Skip test files
```

### Custom Safe Variables

Add variables that won't trigger warnings:

```java
config.addSafeVariable("applicationProperties");
config.addSafeVariable("configService");
config.addSafeVariable("environment");
```

### Custom Exclude Patterns

Exclude files/folders from analysis:

```java
config.addExcludePattern("**/generated/**");
config.addExcludePattern("**/model/**");
config.addExcludePattern("**/dto/**");
```

### Configuration from File

Create `nullcheck-config.properties`:

```properties
check.method.calls=true
check.field.access=true
check.array.access=true
check.method.chaining=true
exclude.tests=true
exclude.patterns=.generated.,target/
safe.variables=logger,log,System
```

Load in your application:

```java
NullCheckConfig config = NullCheckConfig.loadFromProperties("nullcheck-config.properties");
```

## What It Detects

The agent identifies these patterns:

### 1. Method Calls on Potentially Null Objects
```java
String name = user.getName();  // user might be null
```

### 2. Field Access Without Null Check
```java
int id = response.data.id;  // response might be null
```

### 3. Array/Collection Access Without Null Check
```java
String first = items[0];  // items might be null
```

### 4. Method Chaining Without Null Checks
```java
String result = service.getUser().getName();  // service.getUser() might return null
```

## What It Ignores

The agent is smart and doesn't flag:

✓ Variables in null check contexts:
```java
if (user != null) {
    String name = user.getName();  // OK - already checked
}
```

✓ Variables with null checks using Optional:
```java
String name = Optional.ofNullable(user).map(User::getName).orElse("N/A");  // OK
```

✓ Safe variables (System, log, logger, this, super)
```java
System.out.println("test");  // OK - System is safe
```

✓ Generated code and test files (configurable)

## Issue Severity Levels

- **INFO** - Field access on potentially null objects (low risk)
- **WARNING** - Method calls and array access on potentially null objects (medium risk)
- **ERROR** - Critical null dereference patterns (can be customized)

## IntelliJ IDE Integration

### Step 1: Import Configuration

1. Open IntelliJ IDEA
2. Go to **Settings** > **Editor** > **Inspections**
3. Import `intellij-null-check-config.xml`
4. Apply settings

### Step 2: Run as IntelliJ Inspection

```
Analyze > Run Inspection by Name > Search "Null Check"
```

## Maven Plugin Configuration

Add to your `pom.xml` to run checks during build:

```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>exec-maven-plugin</artifactId>
    <version>3.0.0</version>
    <executions>
        <execution>
            <phase>verify</phase>
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

## Building from Source

```bash
# Clone repository
git clone https://github.com/your-org/null-check-agent.git
cd null-check-agent

# Build JAR
mvn clean package

# Build with sources and javadoc
mvn clean package javadoc:jar source:jar
```

## API Reference

### NullCheckAgent

Main agent class:

```java
public class NullCheckAgent {
    // Constructor
    public NullCheckAgent(NullCheckConfig config)
    
    // Methods
    public void analyzeProject(String projectRoot)
    public void analyzeFile(Path filePath)
    public List<NullCheckIssue> getIssues()
    public int getIssueCount()
}
```

### NullCheckConfig

Configuration class:

```java
public class NullCheckConfig {
    public void setCheckMethodCalls(boolean value)
    public void setCheckFieldAccess(boolean value)
    public void setCheckArrayAccess(boolean value)
    public void setCheckMethodChaining(boolean value)
    public void setExcludeTests(boolean value)
    public void addExcludePattern(String pattern)
    public void addSafeVariable(String variable)
}
```

### NullCheckIssue

Issue representation:

```java
public class NullCheckIssue {
    public enum Severity { INFO, WARNING, ERROR }
    
    public String getFilePath()
    public int getLineNumber()
    public String getMessage()
    public String getCode()
    public Severity getSeverity()
}
```

## Examples

### Example 1: Basic Project Scan

```java
NullCheckConfig config = new NullCheckConfig();
NullCheckAgent agent = new NullCheckAgent(config);
agent.analyzeProject("C:\\my-spring-boot-app\\src");
```

### Example 2: Custom Configuration

```java
NullCheckConfig config = new NullCheckConfig();
config.setCheckMethodCalls(true);
config.setCheckFieldAccess(false);  // Skip field access checks
config.addExcludePattern("**/generated/**");
config.addSafeVariable("myRepository");
config.addSafeVariable("myService");

NullCheckAgent agent = new NullCheckAgent(config);
agent.analyzeProject("src/main/java");
```

### Example 3: Process Results

```java
NullCheckConfig config = new NullCheckConfig();
NullCheckAgent agent = new NullCheckAgent(config);
agent.analyzeProject("src");

List<NullCheckIssue> issues = agent.getIssues();

// Count by severity
long errors = issues.stream().filter(i -> i.getSeverity() == NullCheckIssue.Severity.ERROR).count();
long warnings = issues.stream().filter(i -> i.getSeverity() == NullCheckIssue.Severity.WARNING).count();
long infos = issues.stream().filter(i -> i.getSeverity() == NullCheckIssue.Severity.INFO).count();

System.out.println("Errors: " + errors + ", Warnings: " + warnings + ", Infos: " + infos);
```

## Troubleshooting

### Issue: Agent not detecting null checks

**Solution:** Ensure your code follows standard null check patterns:
- Use `if (variable != null)`
- Use Optional with `.ifPresent()` or `.orElse()`

### Issue: Too many false positives

**Solution:** Add safe variables and exclude patterns:
```java
config.addSafeVariable("yourVariable");
config.addExcludePattern("**/yourPackage/**");
```

### Issue: Agent runs slow on large projects

**Solution:** Exclude unnecessary directories:
```java
config.addExcludePattern("target/");
config.addExcludePattern("build/");
config.setExcludeTests(true);
```

## Version History

- **v1.0.0** - Initial release
  - Basic null pointer detection
  - Configurable checks
  - IntelliJ IDE integration

## License

MIT License - See LICENSE file for details

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## Support

For issues, questions, or feature requests, please create an issue on GitHub.

---

**Agent Name:** null-check-agent  
**Version:** 1.0.0  
**Language:** Java 11+  
**Configurable in:** IntelliJ IDE, Command Line, Programmatically  
