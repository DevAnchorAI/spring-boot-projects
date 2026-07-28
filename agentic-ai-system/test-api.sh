#!/bin/bash

# Agentic AI System - API Test Scripts
# This file contains useful curl commands for testing the API

BASE_URL="http://localhost:8080/api/v1"

# Color codes for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}Agentic AI System - API Test Suite${NC}\n"

# 1. Create an ANALYZER agent
echo -e "${GREEN}1. Creating ANALYZER agent...${NC}"
ANALYZER_RESPONSE=$(curl -s -X POST ${BASE_URL}/agents \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Code Analyzer Agent",
    "description": "Analyzes Java code for issues",
    "type": "ANALYZER",
    "capabilities": {
      "language": "java",
      "framework": "spring-boot",
      "analysis_depth": "deep"
    },
    "config": {
      "timeout": "30000",
      "max_file_size": "1048576"
    }
  }')

ANALYZER_ID=$(echo $ANALYZER_RESPONSE | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
echo "Analyzer Agent ID: $ANALYZER_ID"
echo ""

# 2. Create a PROCESSOR agent
echo -e "${GREEN}2. Creating PROCESSOR agent...${NC}"
PROCESSOR_RESPONSE=$(curl -s -X POST ${BASE_URL}/agents \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Data Processor Agent",
    "description": "Processes data in batches",
    "type": "PROCESSOR",
    "capabilities": {
      "format": "json",
      "batch_processing": true
    },
    "config": {
      "batch_size": "1000"
    }
  }')

PROCESSOR_ID=$(echo $PROCESSOR_RESPONSE | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
echo "Processor Agent ID: $PROCESSOR_ID"
echo ""

# 3. Create a VALIDATOR agent
echo -e "${GREEN}3. Creating VALIDATOR agent...${NC}"
VALIDATOR_RESPONSE=$(curl -s -X POST ${BASE_URL}/agents \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Data Validator Agent",
    "description": "Validates data against rules",
    "type": "VALIDATOR",
    "capabilities": {
      "rules": "standard",
      "schema_validation": true
    }
  }')

VALIDATOR_ID=$(echo $VALIDATOR_RESPONSE | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
echo "Validator Agent ID: $VALIDATOR_ID"
echo ""

# 4. Get all agents
echo -e "${GREEN}4. Retrieving all agents...${NC}"
curl -s -X GET ${BASE_URL}/agents | jq '.'
echo ""

# 5. Create an ANALYSIS task
echo -e "${GREEN}5. Creating ANALYSIS task...${NC}"
TASK_RESPONSE=$(curl -s -X POST ${BASE_URL}/tasks \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"Analyze Spring Boot Project\",
    \"description\": \"Perform static analysis on spring-boot projects\",
    \"type\": \"ANALYSIS\",
    \"agentId\": \"${ANALYZER_ID}\",
    \"input\": {
      \"project_path\": \"spring-boot-projects\",
      \"include_tests\": false,
      \"depth\": \"deep\"
    },
    \"priority\": 9,
    \"maxRetries\": 3
  }")

TASK_ID=$(echo $TASK_RESPONSE | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
echo "Task ID: $TASK_ID"
echo ""

# 6. Get task status
echo -e "${GREEN}6. Retrieving task status...${NC}"
curl -s -X GET ${BASE_URL}/tasks/${TASK_ID} | jq '.'
echo ""

# 7. Get queue statistics
echo -e "${GREEN}7. Getting queue statistics...${NC}"
curl -s -X GET ${BASE_URL}/tasks/stats/queue | jq '.'
echo ""

# 8. Create a PROCESSING task
echo -e "${GREEN}8. Creating PROCESSING task...${NC}"
PROC_TASK=$(curl -s -X POST ${BASE_URL}/tasks \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"Process User Data\",
    \"type\": \"PROCESSING\",
    \"agentId\": \"${PROCESSOR_ID}\",
    \"input\": {
      \"batch_size\": 100,
      \"format\": \"json\"
    },
    \"priority\": 5
  }")

PROC_TASK_ID=$(echo $PROC_TASK | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
echo "Processing Task ID: $PROC_TASK_ID"
echo ""

# 9. Get tasks by agent
echo -e "${GREEN}9. Getting tasks by agent...${NC}"
curl -s -X GET ${BASE_URL}/tasks/agent/${ANALYZER_ID} | jq '.'
echo ""

# 10. Get execution results
echo -e "${GREEN}10. Getting execution results...${NC}"
curl -s -X GET ${BASE_URL}/executions | jq '.'
echo ""

# 11. Get execution statistics
echo -e "${GREEN}11. Getting execution statistics...${NC}"
curl -s -X GET ${BASE_URL}/executions/stats/all | jq '.'
echo ""

# 12. Get agent status
echo -e "${GREEN}12. Getting agent status...${NC}"
curl -s -X GET ${BASE_URL}/agents/${ANALYZER_ID}/status | jq '.'
echo ""

# 13. Get agents by type
echo -e "${GREEN}13. Getting agents by type (ANALYZER)...${NC}"
curl -s -X GET ${BASE_URL}/agents/type/ANALYZER | jq '.'
echo ""

# 14. Get active agents
echo -e "${GREEN}14. Getting active agents...${NC}"
curl -s -X GET ${BASE_URL}/agents/status/active | jq '.'
echo ""

# 15. Get tasks by status
echo -e "${GREEN}15. Getting tasks by status (RUNNING)...${NC}"
curl -s -X GET ${BASE_URL}/tasks/status/RUNNING | jq '.'
echo ""

echo -e "${BLUE}Test suite completed!${NC}"
