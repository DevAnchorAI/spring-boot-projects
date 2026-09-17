#!/bin/bash

# Circuit Breaker Service - API Testing Script

# Color codes
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
BASE_URL="http://localhost:8080"
API_ENDPOINT="/api"

echo -e "${BLUE}================================================${NC}"
echo -e "${BLUE}Circuit Breaker Service - API Testing Script${NC}"
echo -e "${BLUE}================================================${NC}"
echo ""

# Test 1: Health Check
echo -e "${YELLOW}Test 1: Health Check${NC}"
echo "GET $BASE_URL$API_ENDPOINT/health"
curl -s -X GET "$BASE_URL$API_ENDPOINT/health" | jq '.'
echo ""

# Test 2: Get User (Async)
echo -e "${YELLOW}Test 2: Get User (With Circuit Breaker & Timeout)${NC}"
echo "GET $BASE_URL$API_ENDPOINT/users/1"
curl -s -X GET "$BASE_URL$API_ENDPOINT/users/1" | jq '.'
echo ""

# Test 3: Process Payment
echo -e "${YELLOW}Test 3: Process Payment (With Circuit Breaker & Retry)${NC}"
echo "POST $BASE_URL$API_ENDPOINT/payments/process"
curl -s -X POST "$BASE_URL$API_ENDPOINT/payments/process" \
  -H "Content-Type: application/json" \
  -d '{
    "transactionId": "TXN-'$(date +%s)'",
    "amount": 99.99,
    "currency": "USD",
    "method": "CREDIT_CARD",
    "description": "Test Payment"
  }' | jq '.'
echo ""

# Test 4: Send Notification
echo -e "${YELLOW}Test 4: Send Notification (With Circuit Breaker)${NC}"
echo "POST $BASE_URL$API_ENDPOINT/notifications/send"
curl -s -X POST "$BASE_URL$API_ENDPOINT/notifications/send" \
  -H "Content-Type: application/json" \
  -d '{
    "recipient": "test@example.com",
    "subject": "Test Notification",
    "message": "This is a test notification",
    "channel": "EMAIL"
  }' | jq '.'
echo ""

# Test 5: Actuator Health
echo -e "${YELLOW}Test 5: Actuator Health Details${NC}"
echo "GET $BASE_URL/actuator/health"
curl -s -X GET "$BASE_URL/actuator/health" | jq '.'
echo ""

# Test 6: Circuit Breaker Status
echo -e "${YELLOW}Test 6: Circuit Breaker Status${NC}"
echo "GET $BASE_URL/actuator/circuitbreakers"
curl -s -X GET "$BASE_URL/actuator/circuitbreakers" | jq '.'
echo ""

# Test 7: Metrics
echo -e "${YELLOW}Test 7: Available Metrics${NC}"
echo "GET $BASE_URL/actuator/metrics"
curl -s -X GET "$BASE_URL/actuator/metrics" | jq '.names | .[] | select(contains("resilience4j"))' | head -20
echo ""

# Test 8: Multiple Users (Stress Test)
echo -e "${YELLOW}Test 8: Multiple Users (Stress Test)${NC}"
for i in {1..5}; do
  echo -e "${GREEN}Request $i${NC}"
  curl -s -X GET "$BASE_URL$API_ENDPOINT/users/$i" | jq '.data | {id, name, source}'
done
echo ""

# Test 9: Multiple Payments
echo -e "${YELLOW}Test 9: Multiple Payments${NC}"
for i in {1..3}; do
  echo -e "${GREEN}Payment $i${NC}"
  curl -s -X POST "$BASE_URL$API_ENDPOINT/payments/process" \
    -H "Content-Type: application/json" \
    -d '{
      "transactionId": "TXN-'$(date +%s%N | cut -c1-13)'-'$i'",
      "amount": '$((50 + RANDOM % 100))'.99,
      "currency": "USD",
      "method": "CREDIT_CARD",
      "description": "Payment #'$i'"
    }' | jq '.data | {transactionId, status}'
done
echo ""

# Test 10: Monitoring Circuit Breaker State
echo -e "${YELLOW}Test 10: Check Circuit Breaker States${NC}"
echo "Checking User Service Breaker..."
curl -s -X GET "$BASE_URL/actuator/metrics/resilience4j.circuitbreaker.state?tag=name:userServiceBreaker" | jq '.measurements'
echo ""

echo -e "${GREEN}================================================${NC}"
echo -e "${GREEN}API Testing Complete!${NC}"
echo -e "${GREEN}================================================${NC}"
echo ""
echo "Next Steps:"
echo "1. Monitor the logs in the running application"
echo "2. Check the circuit breaker state via Actuator"
echo "3. Stop external services to trigger fallbacks"
echo "4. Observe automatic recovery when services are restored"
