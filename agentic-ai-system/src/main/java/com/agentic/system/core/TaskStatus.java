package com.agentic.system.core;

/**
 * Task status
 */
public enum TaskStatus {
    PENDING,            // Task is pending
    QUEUED,             // Task is queued
    RUNNING,            // Task is running
    COMPLETED,          // Task completed successfully
    FAILED,             // Task failed
    RETRY,              // Task retry
    CANCELLED           // Task cancelled
}
