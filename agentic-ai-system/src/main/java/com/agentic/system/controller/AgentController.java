package com.agentic.system.controller;

import com.agentic.system.core.*;
import com.agentic.system.dto.ApiResponse;
import com.agentic.system.dto.CreateAgentRequest;
import com.agentic.system.service.AgentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * REST Controller for Agent management
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/agents")
@CrossOrigin(origins = "*")
public class AgentController {
    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    /**
     * Create a new agent
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Agent>> createAgent(@Valid @RequestBody CreateAgentRequest request) {
        try {
            Agent agent = agentService.createAgent(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Agent created successfully", agent));
        } catch (Exception e) {
            log.error("Error creating agent", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.failure("Failed to create agent", e.getMessage()));
        }
    }

    /**
     * Get agent by ID
     */
    @GetMapping("/{agentId}")
    public ResponseEntity<ApiResponse<Agent>> getAgent(@PathVariable String agentId) {
        try {
            Agent agent = agentService.getAgent(agentId);
            if (agent == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.failure("Agent not found", "Agent ID: " + agentId));
            }
            return ResponseEntity.ok(ApiResponse.success("Agent retrieved successfully", agent));
        } catch (Exception e) {
            log.error("Error retrieving agent", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure("Error retrieving agent", e.getMessage()));
        }
    }

    /**
     * Get all agents
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Agent>>> getAllAgents() {
        try {
            List<Agent> agents = agentService.getAllAgents();
            return ResponseEntity.ok(ApiResponse.success("Agents retrieved successfully", agents));
        } catch (Exception e) {
            log.error("Error retrieving agents", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure("Error retrieving agents", e.getMessage()));
        }
    }

    /**
     * Get agents by type
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<List<Agent>>> getAgentsByType(@PathVariable String type) {
        try {
            AgentType agentType = AgentType.valueOf(type.toUpperCase());
            List<Agent> agents = agentService.getAgentsByType(agentType);
            return ResponseEntity.ok(ApiResponse.success("Agents retrieved successfully", agents));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.failure("Invalid agent type", e.getMessage()));
        } catch (Exception e) {
            log.error("Error retrieving agents by type", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure("Error retrieving agents", e.getMessage()));
        }
    }

    /**
     * Get agent status
     */
    @GetMapping("/{agentId}/status")
    public ResponseEntity<ApiResponse<AgentStatus>> getAgentStatus(@PathVariable String agentId) {
        try {
            AgentStatus status = agentService.getAgentStatus(agentId);
            if (status == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.failure("Agent not found", "Agent ID: " + agentId));
            }
            return ResponseEntity.ok(ApiResponse.success("Agent status retrieved", status));
        } catch (Exception e) {
            log.error("Error retrieving agent status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure("Error retrieving status", e.getMessage()));
        }
    }

    /**
     * Update agent status
     */
    @PutMapping("/{agentId}/status")
    public ResponseEntity<ApiResponse<Agent>> updateAgentStatus(@PathVariable String agentId,
                                                                 @RequestParam AgentStatus status) {
        try {
            Agent agent = agentService.updateAgentStatus(agentId, status);
            if (agent == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.failure("Agent not found", "Agent ID: " + agentId));
            }
            return ResponseEntity.ok(ApiResponse.success("Agent status updated", agent));
        } catch (Exception e) {
            log.error("Error updating agent status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure("Error updating status", e.getMessage()));
        }
    }

    /**
     * Get active agents
     */
    @GetMapping("/status/active")
    public ResponseEntity<ApiResponse<List<Agent>>> getActiveAgents() {
        try {
            List<Agent> agents = agentService.getActiveAgents();
            return ResponseEntity.ok(ApiResponse.success("Active agents retrieved", agents));
        } catch (Exception e) {
            log.error("Error retrieving active agents", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure("Error retrieving active agents", e.getMessage()));
        }
    }

    /**
     * Delete agent
     */
    @DeleteMapping("/{agentId}")
    public ResponseEntity<ApiResponse<Void>> deleteAgent(@PathVariable String agentId) {
        try {
            boolean deleted = agentService.deleteAgent(agentId);
            if (!deleted) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.failure("Agent not found", "Agent ID: " + agentId));
            }
            return ResponseEntity.ok(ApiResponse.success("Agent deleted successfully", null));
        } catch (Exception e) {
            log.error("Error deleting agent", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure("Error deleting agent", e.getMessage()));
        }
    }
}
