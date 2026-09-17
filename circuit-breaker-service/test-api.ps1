# Circuit Breaker Service - API Testing Script (PowerShell)

# Configuration
$BASE_URL = "http://localhost:8080"
$API_ENDPOINT = "/api"

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "Circuit Breaker Service - API Testing Script" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# Test 1: Health Check
Write-Host "Test 1: Health Check" -ForegroundColor Yellow
Write-Host "GET $BASE_URL$API_ENDPOINT/health"
$response = Invoke-RestMethod -Uri "$BASE_URL$API_ENDPOINT/health" -Method Get
$response | ConvertTo-Json | Write-Host
Write-Host ""

# Test 2: Get User (Async)
Write-Host "Test 2: Get User (With Circuit Breaker & Timeout)" -ForegroundColor Yellow
Write-Host "GET $BASE_URL$API_ENDPOINT/users/1"
try {
    $response = Invoke-RestMethod -Uri "$BASE_URL$API_ENDPOINT/users/1" -Method Get
    $response | ConvertTo-Json | Write-Host
}
catch {
    Write-Host "Response (as expected for fallback):" -ForegroundColor Green
    Write-Host $_.Exception.Response.Content -ForegroundColor Green
}
Write-Host ""

# Test 3: Process Payment
Write-Host "Test 3: Process Payment (With Circuit Breaker & Retry)" -ForegroundColor Yellow
Write-Host "POST $BASE_URL$API_ENDPOINT/payments/process"
$paymentData = @{
    transactionId = "TXN-$(Get-Random)"
    amount = 99.99
    currency = "USD"
    method = "CREDIT_CARD"
    description = "Test Payment"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$BASE_URL$API_ENDPOINT/payments/process" `
        -Method Post `
        -ContentType "application/json" `
        -Body $paymentData
    $response | ConvertTo-Json | Write-Host
}
catch {
    Write-Host "Payment request sent. Fallback may be triggered." -ForegroundColor Green
}
Write-Host ""

# Test 4: Send Notification
Write-Host "Test 4: Send Notification (With Circuit Breaker)" -ForegroundColor Yellow
Write-Host "POST $BASE_URL$API_ENDPOINT/notifications/send"
$notificationData = @{
    recipient = "test@example.com"
    subject = "Test Notification"
    message = "This is a test notification"
    channel = "EMAIL"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$BASE_URL$API_ENDPOINT/notifications/send" `
        -Method Post `
        -ContentType "application/json" `
        -Body $notificationData
    $response | ConvertTo-Json | Write-Host
}
catch {
    Write-Host "Notification request sent. Fallback may be triggered." -ForegroundColor Green
}
Write-Host ""

# Test 5: Actuator Health
Write-Host "Test 5: Actuator Health Details" -ForegroundColor Yellow
Write-Host "GET $BASE_URL/actuator/health"
try {
    $response = Invoke-RestMethod -Uri "$BASE_URL/actuator/health" -Method Get
    $response | ConvertTo-Json | Write-Host
}
catch {
    Write-Host "Actuator endpoint not available" -ForegroundColor Yellow
}
Write-Host ""

# Test 6: Circuit Breaker Status
Write-Host "Test 6: Circuit Breaker Status" -ForegroundColor Yellow
Write-Host "GET $BASE_URL/actuator/circuitbreakers"
try {
    $response = Invoke-RestMethod -Uri "$BASE_URL/actuator/circuitbreakers" -Method Get
    $response | ConvertTo-Json | Write-Host
}
catch {
    Write-Host "Actuator endpoint not available" -ForegroundColor Yellow
}
Write-Host ""

# Test 7: Multiple Users (Stress Test)
Write-Host "Test 7: Multiple Users (Stress Test)" -ForegroundColor Yellow
for ($i = 1; $i -le 5; $i++) {
    Write-Host "Request $i" -ForegroundColor Green
    try {
        $response = Invoke-RestMethod -Uri "$BASE_URL$API_ENDPOINT/users/$i" -Method Get
        @{
            id = $response.data.id
            name = $response.data.name
            source = $response.data.source
        } | ConvertTo-Json | Write-Host
    }
    catch {
        Write-Host "Error: $_" -ForegroundColor Red
    }
}
Write-Host ""

# Test 8: Multiple Payments
Write-Host "Test 8: Multiple Payments" -ForegroundColor Yellow
for ($i = 1; $i -le 3; $i++) {
    Write-Host "Payment $i" -ForegroundColor Green
    $paymentData = @{
        transactionId = "TXN-$(Get-Random)-$i"
        amount = (50 + (Get-Random -Maximum 100)) + 0.99
        currency = "USD"
        method = "CREDIT_CARD"
        description = "Payment #$i"
    } | ConvertTo-Json

    try {
        $response = Invoke-RestMethod -Uri "$BASE_URL$API_ENDPOINT/payments/process" `
            -Method Post `
            -ContentType "application/json" `
            -Body $paymentData
        @{
            transactionId = $response.data.transactionId
            status = $response.data.status
        } | ConvertTo-Json | Write-Host
    }
    catch {
        Write-Host "Error: $_" -ForegroundColor Red
    }
}
Write-Host ""

Write-Host "================================================" -ForegroundColor Green
Write-Host "API Testing Complete!" -ForegroundColor Green
Write-Host "================================================" -ForegroundColor Green
Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Cyan
Write-Host "1. Monitor the logs in the running application"
Write-Host "2. Check the circuit breaker state via Actuator"
Write-Host "3. Stop external services to trigger fallbacks"
Write-Host "4. Observe automatic recovery when services are restored"
