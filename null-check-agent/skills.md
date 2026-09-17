# Null-Check-Agent Skills

## Overview
The null-check-agent provides comprehensive capabilities for detecting and analyzing potential null pointer exceptions in Java Spring Boot projects.

## Core Skills

### 1. Method Call Null Checking
- **Enabled by default**: true
- **Configuration key**: `checkMethodCalls`
- **Description**: Detects and warns about null checks required before calling methods on objects
- **Example**: 
  ```java
  // Warns: obj could be null before calling method()
  obj.method();
  ```

### 2. Field Access Validation
- **Enabled by default**: true
- **Configuration key**: `checkFieldAccess`
- **Description**: Validates null checks before accessing object fields/properties
- **Example**:
  ```java
  // Warns: obj could be null before accessing field
  String value = obj.field;
  ```

### 3. Array/Collection Access Detection
- **Enabled by default**: true
- **Configuration key**: `checkArrayAccess`
- **Description**: Detects potential null pointer issues when accessing arrays or collections
- **Example**:
  ```java
  // Warns: list could be null before accessing element
  Object item = list.get(0);
  ```

### 4. Method Chaining Analysis
- **Enabled by default**: true
- **Configuration key**: `checkMethodChaining`
- **Description**: Analyzes method chaining patterns for null safety issues
- **Example**:
  ```java
  // Warns: obj or obj.getChild() could be null
  String value = obj.getChild().getValue();
  ```

### 5. Test Exclusion
- **Enabled by default**: true
- **Configuration key**: `excludeTests`
- **Description**: Optionally excludes test files from null pointer analysis
- **Benefit**: Reduces false positives in test code

## Safe Variables Configuration

Default safe variables (no null checks required):
- `System` - Java System class
- `log` - Logger instances
- `logger` - Logger field
- `this` - Current object reference
- `super` - Parent class reference

**Custom safe variables** can be added via configuration:
```java
config.addSafeVariable("myRepository");
config.addSafeVariable("myService");
```

## Exclude Patterns

Default patterns to exclude from checks:
- `.generated.` - Generated source files
- `target/` - Maven build output directory

**Custom exclude patterns** can be added:
```java
config.addExcludePattern("**/generated/**");
config.addExcludePattern("**/model/**");
```

## Configuration Example

To customize skills in code:

```java
NullCheckConfig config = new NullCheckConfig();

// Enable/disable specific checks
config.setCheckMethodCalls(true);
config.setCheckFieldAccess(true);
config.setCheckArrayAccess(true);
config.setCheckMethodChaining(true);
config.setExcludeTests(true);

// Add safe variables
config.addSafeVariable("myRepository");
config.addSafeVariable("myService");

// Add exclude patterns
config.addExcludePattern("**/generated/**");
config.addExcludePattern("**/model/**");
```

## IntelliJ IDE Integration

To configure null-check-agent in IntelliJ IDE:
1. Open **Settings** → **Tools** → **Null Check Agent**
2. Enable/disable individual skills
3. Add safe variables in the custom variables section
4. Configure exclude patterns
5. Click **Apply** to save configuration

## Usage

The agent runs automatically on:
- Java source files in your project
- Spring Boot configuration classes
- REST controllers and services
- Any monitored Java files

Violations are reported as:
- **Warnings**: Potential null pointer issues requiring investigation
- **Inspections**: IntelliJ IDE highlights with quick fixes available
