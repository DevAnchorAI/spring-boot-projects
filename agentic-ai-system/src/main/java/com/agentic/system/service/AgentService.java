package com.agentic.system.service;

import com.agentic.system.core.*;
import com.agentic.system.dto.CreateAgentRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service for managing agents
 */
@Slf4j
@Service
public class AgentService {
    private final Map<String, Agent> agentRegistry = new ConcurrentHashMap<>();

    public AgentService() {
        initializeDefaultAgents();
    }

    /**
     * Create a new agent
     */
    public Agent createAgent(CreateAgentRequest request) {
        String agentId = UUID.randomUUID().toString();
        AgentType type = AgentType.valueOf(request.getType().toUpperCase());

        Agent agent = new Agent(agentId, request.getName(), request.getDescription(), type);
        agent.setCapabilities(request.getCapabilities() != null ? request.getCapabilities() : new HashMap<>());
        agent.setConfig(request.getConfig() != null ? request.getConfig() : new HashMap<>());
        agent.setCreatedBy("system");

        agentRegistry.put(agentId, agent);
        log.info("Agent created: {} with ID: {}", request.getName(), agentId);

        return agent;
    }

    /**
     * Get agent by ID
     */
    public Agent getAgent(String agentId) {
        return agentRegistry.get(agentId);
    }

    /**
     * Get all agents
     */
    public List<Agent> getAllAgents() {
        return new ArrayList<>(agentRegistry.values());
    }

    /**
     * Get agents by type
     */
    public List<Agent> getAgentsByType(AgentType type) {
        return agentRegistry.values().stream()
                .filter(a -> a.getType() == type)
                .collect(Collectors.toList());
    }

    /**
     * Update agent status
     */
    public Agent updateAgentStatus(String agentId, AgentStatus status) {
        Agent agent = agentRegistry.get(agentId);
        if (agent != null) {
            agent.setStatus(status);
            agent.setUpdatedAt(java.time.LocalDateTime.now());
            log.info("Agent {} status updated to {}", agentId, status);
        }
        return agent;
    }

    /**
     * Delete agent
     */
    public boolean deleteAgent(String agentId) {
        boolean removed = agentRegistry.remove(agentId) != null;
        if (removed) {
            log.info("Agent {} deleted", agentId);
        }
        return removed;
    }

    /**
     * Initialize default agents
     */
    private void initializeDefaultAgents() {
        // Create default analyzer agent
        Agent analyzer = new Agent(
                UUID.randomUUID().toString(),
                "Default Analyzer",
                "Default code analysis agent",
                AgentType.ANALYZER
        );
        analyzer.getCapabilities().put("language", "java");
        analyzer.getCapabilities().put("framework", "spring-boot");
        agentRegistry.put(analyzer.getId(), analyzer);

        // Create default processor agent
        Agent processor = new Agent(
                UUID.randomUUID().toString(),
                "Default Processor",
                "Default data processing agent",
                AgentType.PROCESSOR
        );
        processor.getCapabilities().put("format", "json");
        processor.getCapabilities().put("batch_size", 1000);
        agentRegistry.put(processor.getId(), processor);

        // Create default validator agent
        Agent validator = new Agent(
                UUID.randomUUID().toString(),
                "Default Validator",
                "Default validation agent",
                AgentType.VALIDATOR
        );
        validator.getCapabilities().put("rules", "standard");
        agentRegistry.put(validator.getId(), validator);

        log.info("Initialized {} default agents", agentRegistry.size());
    }

    /**
     * Get agent status
     */
    public AgentStatus getAgentStatus(String agentId) {
        Agent agent = agentRegistry.get(agentId);
        return agent != null ? agent.getStatus() : null;
    }

    /**
     * List active agents
     */
    public List<Agent> getActiveAgents() {
        return agentRegistry.values().stream()
                .filter(a -> a.getStatus() != AgentStatus.INACTIVE)
                .collect(Collectors.toList());
    }
}
