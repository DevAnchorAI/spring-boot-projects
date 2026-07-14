# Null Pointer Check Agent - Quick Start

## What is This?

A lightweight Java plugin that automatically scans your Spring Boot project for potential null pointer exceptions (NPE) in the code.

## How to Use in Your Spring Boot Project

### 1. Add to Maven pom.xml

```xml
<dependency>
    <groupId>com.nullcheck</groupId>
    <artifactId>null-check-agent</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. Create Configuration Class

```java
@Configuration
public class NullCheckConfig {
    
    @Bean
    public NullCheckAgent nullCheckAgent() {
        NullCheckConfig config = new NullCheckConfig();
        config.setCheckMethodCalls(true);
        config.setCheckFieldAccess(true);
        config.addSafeVariable("repository");  // Add your safe variables
        return new NullCheckAgent(config);
    }
}
```

### 3. Inject and Use

```java
@RestController
public class MyController {
    
    @Autowired
    private NullCheckAgent agent;
    
    @PostMapping("/scan")
    public List<NullCheckIssue> scanProject() {
        agent.analyzeProject("src/main/java");
        return agent.getIssues();
    }
}
```

## Command Line Usage

```bash
# Build
mvn clean package

# Run
java -cp target/null-check-agent-1.0.0.jar com.nullcheck.agent.NullCheckAgent "C:\path\to\project"
```

## Example Output

```
================================================================================
NULL POINTER CHECK AGENT - ANALYSIS REPORT
================================================================================
Found 3 potential null pointer issue(s):

File: C:\project\UserService.java
--------------------------------------------------------------------------------
  Line 45 [WARNING]: Potential null dereference: user.getName()
    Code: String name = user.getName().toUpperCase();

  Line 67 [INFO]: Potential null field access: response.data
    Code: int id = response.data.id;

================================================================================
Total Issues: 3
================================================================================
```

## What It Detects

✓ Method calls on null objects: `user.getName()` when `user` might be null  
✓ Field access: `response.data` when `response` might be null  
✓ Array/collection access: `items[0]` when `items` might be null  
✓ Method chaining: `service.getUser().getName()` without null checks  

## What It Ignores

✓ Code with null checks: `if (user != null) { ... }`  
✓ Optional usage: `Optional.ofNullable(...)`  
✓ Safe variables: System, log, logger, this, super  
✓ Test files and generated code (configurable)  

## Configuration

```java
NullCheckConfig config = new NullCheckConfig();

// Enable/disable checks
config.setCheckMethodCalls(true);
config.setCheckFieldAccess(true);
config.setCheckArrayAccess(true);
config.setCheckMethodChaining(true);

// Add safe variables that won't trigger warnings
config.addSafeVariable("myRepository");
config.addSafeVariable("myService");

// Exclude patterns
config.addExcludePattern("**/generated/**");
config.addExcludePattern("**/model/**");
```

## Files Included

- **NullCheckAgent.java** - Main scanning engine
- **NullCheckConfig.java** - Configuration class
- **NullCheckIssue.java** - Issue model
- **NullCheckAgentExample.java** - Usage examples
- **pom.xml** - Maven configuration
- **README.md** - Full documentation
- **INTEGRATION_GUIDE.md** - Step-by-step integration guide

## Next Steps

1. See `README.md` for detailed documentation
2. See `INTEGRATION_GUIDE.md` for Spring Boot integration steps
3. See `NullCheckAgentExample.java` for code examples
4. Run `mvn clean package` to build
5. Use in your project as a Maven dependency

## Support

For issues or questions, refer to the documentation files or modify the configuration to suit your project's needs.

---

**Simple. Fast. Effective.**

Detect null pointer exceptions before they happen!
