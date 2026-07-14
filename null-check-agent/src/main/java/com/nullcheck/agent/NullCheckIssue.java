package com.nullcheck.agent;

/**
 * Represents a null pointer check issue found by NullCheckAgent
 */
public class NullCheckIssue {

    public enum Severity {
        INFO, WARNING, ERROR
    }

    private String filePath;
    private int lineNumber;
    private String message;
    private String code;
    private Severity severity;

    public NullCheckIssue(String filePath, int lineNumber, String message, String code, Severity severity) {
        this.filePath = filePath;
        this.lineNumber = lineNumber;
        this.message = message;
        this.code = code;
        this.severity = severity;
    }

    // Getters
    public String getFilePath() {
        return filePath;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getMessage() {
        return message;
    }

    public String getCode() {
        return code;
    }

    public Severity getSeverity() {
        return severity;
    }

    @Override
    public String toString() {
        return "NullCheckIssue{" +
                "filePath='" + filePath + '\'' +
                ", lineNumber=" + lineNumber +
                ", message='" + message + '\'' +
                ", severity=" + severity +
                '}';
    }
}
