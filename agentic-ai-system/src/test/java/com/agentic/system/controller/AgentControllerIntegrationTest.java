package com.agentic.system.controller;

import com.agentic.system.core.*;
import com.agentic.system.dto.CreateAgentRequest;
import com.agentic.system.dto.CreateTaskRequest;
import com.agentic.system.service.AgentService;
import com.agentic.system.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Agentic AI System
 */
@SpringBootTest
@AutoConfigureMockMvc
public class AgentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AgentService agentService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;

    private String agentId;

    @BeforeEach
    public void setUp() {
        // Get first available agent
        var agents = agentService.getAllAgents();
        if (!agents.isEmpty()) {
            agentId = agents.get(0).getId();
        }
    }

    @Test
    public void testCreateAgent() throws Exception {
        CreateAgentRequest request = new CreateAgentRequest();
        request.setName("Test Analyzer");
        request.setDescription("Test analyzer agent");
        request.setType("ANALYZER");

        mockMvc.perform(post("/api/v1/agents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Test Analyzer"));
    }

    @Test
    public void testGetAllAgents() throws Exception {
        mockMvc.perform(get("/api/v1/agents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    public void testGetAgentById() throws Exception {
        if (agentId != null) {
            mockMvc.perform(get("/api/v1/agents/" + agentId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(agentId));
        }
    }

    @Test
    public void testGetAgentsByType() throws Exception {
        mockMvc.perform(get("/api/v1/agents/type/ANALYZER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    public void testCreateTask() throws Exception {
        if (agentId != null) {
            CreateTaskRequest request = new CreateTaskRequest();
            request.setName("Test Task");
            request.setDescription("Test task description");
            request.setType("ANALYSIS");
            request.setAgentId(agentId);
            request.setInput(new HashMap<>());

            mockMvc.perform(post("/api/v1/tasks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("Test Task"));
        }
    }

    @Test
    public void testGetAllTasks() throws Exception {
        mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    public void testGetQueueStats() throws Exception {
        mockMvc.perform(get("/api/v1/tasks/stats/queue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.queue_size").isNumber());
    }

    @Test
    public void testGetExecutionStats() throws Exception {
        mockMvc.perform(get("/api/v1/executions/stats/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total_executions").isNumber());
    }
}
