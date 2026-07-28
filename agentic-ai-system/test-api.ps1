# Agentic AI System - API Test Scripts (PowerShell)
# This file contains useful curl commands for testing the API on Windows

$BASE_URL = "http://localhost:8080/api/v1"

Write-Host "Agentic AI System - API Test Suite" -ForegroundColor Blue
Write-Host ""

# 1. Create an ANALYZER agent
Write-Host "1. Creating ANALYZER agent..." -ForegroundColor Green
$analyzerJson = @{
    name = "Code Analyzer Agent"
    description = "Analyzes Java code for issues"
    type = "ANALYZER"
    capabilities = @{
        language = "java"
        framework = "spring-boot"
        analysis_depth = "deep"
    }
    config = @{
        timeout = "30000"
        max_file_size = "1048576"
    }
} | ConvertTo-Json

$analyzerResponse = Invoke-WebRequest -Uri "$BASE_URL/agents" `
    -Method POST `
    -Headers @{"Content-Type"="application/json"} `
    -Body $analyzerJson `
    -UseBasicParsing

$analyzerId = ($analyzerResponse.Content | ConvertFrom-Json).data.id
Write-Host "Analyzer Agent ID: $analyzerId"
Write-Host ""

# 2. Create a PROCESSOR agent
Write-Host "2. Creating PROCESSOR agent..." -ForegroundColor Green
$processorJson = @{
    name = "Data Processor Agent"
    description = "Processes data in batches"
    type = "PROCESSOR"
    capabilities = @{
        format = "json"
        batch_processing = $true
    }
    config = @{
        batch_size = "1000"
    }
} | ConvertTo-Json

$processorResponse = Invoke-WebRequest -Uri "$BASE_URL/agents" `
    -Method POST `
    -Headers @{"Content-Type"="application/json"} `
    -Body $processorJson `
    -UseBasicParsing

$processorId = ($processorResponse.Content | ConvertFrom-Json).data.id
Write-Host "Processor Agent ID: $processorId"
Write-Host ""

# 3. Create a VALIDATOR agent
Write-Host "3. Creating VALIDATOR agent..." -ForegroundColor Green
$validatorJson = @{
    name = "Data Validator Agent"
    description = "Validates data against rules"
    type = "VALIDATOR"
    capabilities = @{
        rules = "standard"
        schema_validation = $true
    }
} | ConvertTo-Json

$validatorResponse = Invoke-WebRequest -Uri "$BASE_URL/agents" `
    -Method POST `
    -Headers @{"Content-Type"="application/json"} `
    -Body $validatorJson `
    -UseBasicParsing

$validatorId = ($validatorResponse.Content | ConvertFrom-Json).data.id
Write-Host "Validator Agent ID: $validatorId"
Write-Host ""

# 4. Get all agents
Write-Host "4. Retrieving all agents..." -ForegroundColor Green
$allAgents = Invoke-WebRequest -Uri "$BASE_URL/agents" -UseBasicParsing
$allAgents.Content | ConvertFrom-Json | ConvertTo-Json -Depth 10
Write-Host ""

# 5. Create an ANALYSIS task
Write-Host "5. Creating ANALYSIS task..." -ForegroundColor Green
$taskJson = @{
    name = "Analyze Spring Boot Project"
    description = "Perform static analysis on spring-boot projects"
    type = "ANALYSIS"
    agentId = $analyzerId
    input = @{
        project_path = "spring-boot-projects"
        include_tests = $false
        depth = "deep"
    }
    priority = 9
    maxRetries = 3
} | ConvertTo-Json

$taskResponse = Invoke-WebRequest -Uri "$BASE_URL/tasks" `
    -Method POST `
    -Headers @{"Content-Type"="application/json"} `
    -Body $taskJson `
    -UseBasicParsing

$taskId = ($taskResponse.Content | ConvertFrom-Json).data.id
Write-Host "Task ID: $taskId"
Write-Host ""

# 6. Get task status
Write-Host "6. Retrieving task status..." -ForegroundColor Green
$taskStatus = Invoke-WebRequest -Uri "$BASE_URL/tasks/$taskId" -UseBasicParsing
$taskStatus.Content | ConvertFrom-Json | ConvertTo-Json -Depth 10
Write-Host ""

# 7. Get queue statistics
Write-Host "7. Getting queue statistics..." -ForegroundColor Green
$queueStats = Invoke-WebRequest -Uri "$BASE_URL/tasks/stats/queue" -UseBasicParsing
$queueStats.Content | ConvertFrom-Json | ConvertTo-Json -Depth 10
Write-Host ""

# 8. Create a PROCESSING task
Write-Host "8. Creating PROCESSING task..." -ForegroundColor Green
$procTaskJson = @{
    name = "Process User Data"
    type = "PROCESSING"
    agentId = $processorId
    input = @{
        batch_size = 100
        format = "json"
    }
    priority = 5
} | ConvertTo-Json

$procTaskResponse = Invoke-WebRequest -Uri "$BASE_URL/tasks" `
    -Method POST `
    -Headers @{"Content-Type"="application/json"} `
    -Body $procTaskJson `
    -UseBasicParsing

$procTaskId = ($procTaskResponse.Content | ConvertFrom-Json).data.id
Write-Host "Processing Task ID: $procTaskId"
Write-Host ""

# 9. Get tasks by agent
Write-Host "9. Getting tasks by agent..." -ForegroundColor Green
$tasksByAgent = Invoke-WebRequest -Uri "$BASE_URL/tasks/agent/$analyzerId" -UseBasicParsing
$tasksByAgent.Content | ConvertFrom-Json | ConvertTo-Json -Depth 10
Write-Host ""

# 10. Get execution results
Write-Host "10. Getting execution results..." -ForegroundColor Green
$execResults = Invoke-WebRequest -Uri "$BASE_URL/executions" -UseBasicParsing
$execResults.Content | ConvertFrom-Json | ConvertTo-Json -Depth 10
Write-Host ""

# 11. Get execution statistics
Write-Host "11. Getting execution statistics..." -ForegroundColor Green
$execStats = Invoke-WebRequest -Uri "$BASE_URL/executions/stats/all" -UseBasicParsing
$execStats.Content | ConvertFrom-Json | ConvertTo-Json -Depth 10
Write-Host ""

# 12. Get agent status
Write-Host "12. Getting agent status..." -ForegroundColor Green
$agentStatus = Invoke-WebRequest -Uri "$BASE_URL/agents/$analyzerId/status" -UseBasicParsing
$agentStatus.Content | ConvertFrom-Json | ConvertTo-Json -Depth 10
Write-Host ""

# 13. Get agents by type
Write-Host "13. Getting agents by type (ANALYZER)..." -ForegroundColor Green
$agentsByType = Invoke-WebRequest -Uri "$BASE_URL/agents/type/ANALYZER" -UseBasicParsing
$agentsByType.Content | ConvertFrom-Json | ConvertTo-Json -Depth 10
Write-Host ""

# 14. Get active agents
Write-Host "14. Getting active agents..." -ForegroundColor Green
$activeAgents = Invoke-WebRequest -Uri "$BASE_URL/agents/status/active" -UseBasicParsing
$activeAgents.Content | ConvertFrom-Json | ConvertTo-Json -Depth 10
Write-Host ""

# 15. Get tasks by status
Write-Host "15. Getting tasks by status (RUNNING)..." -ForegroundColor Green
$tasksByStatus = Invoke-WebRequest -Uri "$BASE_URL/tasks/status/RUNNING" -UseBasicParsing
$tasksByStatus.Content | ConvertFrom-Json | ConvertTo-Json -Depth 10
Write-Host ""

Write-Host "Test suite completed!" -ForegroundColor Blue
