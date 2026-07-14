package com.nullcheck.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Null Pointer Check Agent
 * Configurable agent for IntelliJ IDE to detect potential null pointer dereferences
 * in Spring Boot projects and Java files.
 */
public class NullCheckAgent {
    private static final Logger logger = LoggerFactory.getLogger(NullCheckAgent.class);

    private static final String[] SAFE_VARIABLES = {
            "this", "super", "System", "out", "err", "log", "env",
            "null", "true", "false", "logger"
    };

    private List<NullCheckIssue> issues;
    private NullCheckConfig config;

    public NullCheckAgent(NullCheckConfig config) {
        this.config = config;
        this.issues = new ArrayList<>();
    }

    /**
     * Analyze the entire project
     */
    public void analyzeProject(String projectRoot) {
        try {
            Files.walk(Paths.get(projectRoot))
                    .filter(path -> path.toString().endsWith(".java"))
                    .filter(this::shouldAnalyzeFile)
                    .forEach(this::analyzeFile);
        } catch (IOException e) {
            logger.error("Error analyzing project: " + e.getMessage(), e);
        }
        printReport();
    }

    /**
     * Analyze a single file
     */
    public void analyzeFile(Path filePath) {
        try {
            List<String> lines = Files.readAllLines(filePath);
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                int lineNumber = i + 1;
                analyzeLineForNullPointers(filePath.toString(), line, lineNumber);
            }
        } catch (IOException e) {
            logger.error("Error reading file: " + filePath + " - " + e.getMessage(), e);
        }
    }

    /**
     * Analyze a single line for null pointer issues
     */
    private void analyzeLineForNullPointers(String filePath, String line, int lineNumber) {
        String trimmed = line.trim();

        // Skip comments and empty lines
        if (trimmed.isEmpty() || trimmed.startsWith("//") || trimmed.startsWith("*")) {
            return;
        }

        // Pattern 1: Method calls on potentially null objects
        checkMethodCallsOnNull(filePath, line, lineNumber, trimmed);

        // Pattern 2: Field access without null check
        checkFieldAccessOnNull(filePath, line, lineNumber, trimmed);

        // Pattern 3: Array/Collection access without null check
        checkArrayAccessOnNull(filePath, line, lineNumber, trimmed);

        // Pattern 4: Method chaining without null checks
        checkMethodChaining(filePath, line, lineNumber, trimmed);
    }

    /**
     * Check for method calls on potentially null objects
     */
    private void checkMethodCallsOnNull(String filePath, String line, int lineNumber, String trimmed) {
        Pattern pattern = Pattern.compile("(\\w+)\\.(\\w+)\\(");
        Matcher matcher = pattern.matcher(line);

        while (matcher.find()) {
            String variable = matcher.group(1);
            String method = matcher.group(2);

            if (isSafeVariable(variable)) {
                continue;
            }

            if (hasNullCheck(line, variable)) {
                continue;
            }

            if (config.isCheckMethodCalls()) {
                issues.add(new NullCheckIssue(
                        filePath,
                        lineNumber,
                        "Potential null dereference: " + variable + "." + method + "()",
                        trimmed,
                        NullCheckIssue.Severity.WARNING
                ));
            }
        }
    }

    /**
     * Check for field access without null check
     */
    private void checkFieldAccessOnNull(String filePath, String line, int lineNumber, String trimmed) {
        Pattern pattern = Pattern.compile("(\\w+)\\.(\\w+)(?!\\()");
        Matcher matcher = pattern.matcher(line);

        while (matcher.find()) {
            String variable = matcher.group(1);
            String field = matcher.group(2);

            if (isSafeVariable(variable) || isInSafeContext(line)) {
                continue;
            }

            if (hasNullCheck(line, variable)) {
                continue;
            }

            if (config.isCheckFieldAccess()) {
                issues.add(new NullCheckIssue(
                        filePath,
                        lineNumber,
                        "Potential null field access: " + variable + "." + field,
                        trimmed,
                        NullCheckIssue.Severity.INFO
                ));
            }
        }
    }

    /**
     * Check for array/collection access without null check
     */
    private void checkArrayAccessOnNull(String filePath, String line, int lineNumber, String trimmed) {
        Pattern pattern = Pattern.compile("(\\w+)\\[");
        Matcher matcher = pattern.matcher(line);

        while (matcher.find()) {
            String variable = matcher.group(1);

            if (isSafeVariable(variable)) {
                continue;
            }

            if (hasNullCheck(line, variable)) {
                continue;
            }

            if (config.isCheckArrayAccess()) {
                issues.add(new NullCheckIssue(
                        filePath,
                        lineNumber,
                        "Potential null array/collection access: " + variable,
                        trimmed,
                        NullCheckIssue.Severity.WARNING
                ));
            }
        }
    }

    /**
     * Check for method chaining without null checks
     */
    private void checkMethodChaining(String filePath, String line, int lineNumber, String trimmed) {
        if (line.contains(").") && !line.contains("!= null") && !line.contains("Optional")) {
            if (config.isCheckMethodChaining()) {
                issues.add(new NullCheckIssue(
                        filePath,
                        lineNumber,
                        "Potential null on method chain",
                        trimmed,
                        NullCheckIssue.Severity.WARNING
                ));
            }
        }
    }

    /**
     * Check if a variable is safe
     */
    private boolean isSafeVariable(String variable) {
        for (String safe : SAFE_VARIABLES) {
            if (safe.equals(variable)) {
                return true;
            }
        }

        if (config.getSafeVariables() != null) {
            for (String safe : config.getSafeVariables()) {
                if (safe.equals(variable)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Check if line has null check
     */
    private boolean hasNullCheck(String line, String variable) {
        return line.contains(variable + " != null") ||
               line.contains(variable + "!= null") ||
               line.contains("null != " + variable) ||
               line.contains("Optional") ||
               line.contains(".orElse") ||
               line.contains(".ifPresent");
    }

    /**
     * Check if line is in safe context
     */
    private boolean isInSafeContext(String line) {
        return line.contains("if (") || line.contains("if(") ||
               line.contains("!= null") || line.contains("== null");
    }

    /**
     * Check if file should be analyzed
     */
    private boolean shouldAnalyzeFile(Path path) {
        String pathStr = path.toString();

        if (config.isExcludeTests() && pathStr.contains(File.separator + "test" + File.separator)) {
            return false;
        }

        if (config.getExcludePatterns() != null) {
            for (String pattern : config.getExcludePatterns()) {
                if (pathStr.contains(pattern)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Print analysis report
     */
    private void printReport() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("NULL POINTER CHECK AGENT - ANALYSIS REPORT");
        System.out.println("=".repeat(80));

        if (issues.isEmpty()) {
            System.out.println("✓ No potential null pointer issues found!");
        } else {
            System.out.println("Found " + issues.size() + " potential null pointer issue(s):\n");

            // Group by file
            Map<String, List<NullCheckIssue>> byFile = new HashMap<>();
            for (NullCheckIssue issue : issues) {
                byFile.computeIfAbsent(issue.getFilePath(), k -> new ArrayList<>()).add(issue);
            }

            // Print grouped results
            for (String filePath : byFile.keySet()) {
                System.out.println("File: " + filePath);
                System.out.println("-".repeat(80));

                for (NullCheckIssue issue : byFile.get(filePath)) {
                    System.out.println("  Line " + issue.getLineNumber() + " [" + issue.getSeverity() + "]: " + issue.getMessage());
                    System.out.println("    Code: " + issue.getCode().substring(0, Math.min(100, issue.getCode().length())));
                }
                System.out.println();
            }
        }

        System.out.println("=".repeat(80));
        System.out.println("Total Issues: " + issues.size());
        System.out.println("=".repeat(80) + "\n");
    }

    /**
     * Get all detected issues
     */
    public List<NullCheckIssue> getIssues() {
        return new ArrayList<>(issues);
    }

    /**
     * Get issues count
     */
    public int getIssueCount() {
        return issues.size();
    }

    /**
     * Main method for testing
     */
    public static void main(String[] args) {
        //String projectRoot = args.length > 0 ? args[0] : ".";
        String projectRoot ="./redis-caching";
                NullCheckConfig config = new NullCheckConfig();

        NullCheckAgent agent = new NullCheckAgent(config);
        System.out.println("Starting Null Pointer Check Agent...");
        System.out.println("Project Root: " + projectRoot);
        System.out.println("Configuration: " + config);

        agent.analyzeProject(projectRoot);
    }
}
