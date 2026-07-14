package com.nullcheck.agent;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration class for NullCheckAgent
 * Can be configured in IntelliJ IDE and programmatically
 */
public class NullCheckConfig {

    private boolean checkMethodCalls = true;
    private boolean checkFieldAccess = true;
    private boolean checkArrayAccess = true;
    private boolean checkMethodChaining = true;
    private boolean excludeTests = true;
    private List<String> excludePatterns;
    private List<String> safeVariables;

    public NullCheckConfig() {
        this.excludePatterns = new ArrayList<>();
        this.safeVariables = new ArrayList<>();
        initializeDefaults();
    }

    private void initializeDefaults() {
        // Default safe variables
        safeVariables.add("System");
        safeVariables.add("log");
        safeVariables.add("logger");
        safeVariables.add("this");
        safeVariables.add("super");

        // Default exclude patterns
        excludePatterns.add(".generated.");
        excludePatterns.add("target/");
    }

    // Getters and Setters
    public boolean isCheckMethodCalls() {
        return checkMethodCalls;
    }

    public void setCheckMethodCalls(boolean checkMethodCalls) {
        this.checkMethodCalls = checkMethodCalls;
    }

    public boolean isCheckFieldAccess() {
        return checkFieldAccess;
    }

    public void setCheckFieldAccess(boolean checkFieldAccess) {
        this.checkFieldAccess = checkFieldAccess;
    }

    public boolean isCheckArrayAccess() {
        return checkArrayAccess;
    }

    public void setCheckArrayAccess(boolean checkArrayAccess) {
        this.checkArrayAccess = checkArrayAccess;
    }

    public boolean isCheckMethodChaining() {
        return checkMethodChaining;
    }

    public void setCheckMethodChaining(boolean checkMethodChaining) {
        this.checkMethodChaining = checkMethodChaining;
    }

    public boolean isExcludeTests() {
        return excludeTests;
    }

    public void setExcludeTests(boolean excludeTests) {
        this.excludeTests = excludeTests;
    }

    public List<String> getExcludePatterns() {
        return new ArrayList<>(excludePatterns);
    }

    public void setExcludePatterns(List<String> excludePatterns) {
        this.excludePatterns = new ArrayList<>(excludePatterns);
    }

    public void addExcludePattern(String pattern) {
        this.excludePatterns.add(pattern);
    }

    public List<String> getSafeVariables() {
        return new ArrayList<>(safeVariables);
    }

    public void setSafeVariables(List<String> safeVariables) {
        this.safeVariables = new ArrayList<>(safeVariables);
    }

    public void addSafeVariable(String variable) {
        this.safeVariables.add(variable);
    }

    @Override
    public String toString() {
        return "NullCheckConfig{" +
                "checkMethodCalls=" + checkMethodCalls +
                ", checkFieldAccess=" + checkFieldAccess +
                ", checkArrayAccess=" + checkArrayAccess +
                ", checkMethodChaining=" + checkMethodChaining +
                ", excludeTests=" + excludeTests +
                ", excludePatterns=" + excludePatterns +
                ", safeVariables=" + safeVariables +
                '}';
    }
}
