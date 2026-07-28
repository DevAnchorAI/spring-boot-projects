package com.agentic.system.core;

/**
 * Agent status
 */
public enum AgentStatus {
    IDLE,               // Agent is idle and ready
    RUNNING,            // Agent is actively running
    PROCESSING,         // Agent is processing a task
    PAUSED,             // Agent is paused
    ERROR,              // Agent encountered an error
    COMPLETED,          // Agent completed task
    INACTIVE            // Agent is inactive
}
