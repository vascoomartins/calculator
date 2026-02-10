#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Counters
PASSED=0
FAILED=0

# Base URL
BASE_URL="${BASE_URL:-http://localhost:8080}"

# Function to test an endpoint
test_endpoint() {
    local description="$1"
    local endpoint="$2"
    local expected="$3"

    echo -n "  Testing: $description... "

    response=$(curl -s "${BASE_URL}${endpoint}")

    if [ -z "$expected" ]; then
        # No expected value, just show result
        echo -e "${BLUE}${response}${NC}"
        ((PASSED++))
    elif echo "$response" | grep -q "$expected"; then
        echo -e "${GREEN}PASS${NC} - $response"
        ((PASSED++))
    else
        echo -e "${RED}FAIL${NC}"
        echo "    Expected: $expected"
        echo "    Got:      $response"
        ((FAILED++))
    fi
}

# Function to test header
test_header() {
    local description="$1"
    local endpoint="$2"
    local header="$3"

    echo -n "  Testing: $description... "

    response=$(curl -sI "${BASE_URL}${endpoint}" | grep -i "$header")

    if [ -n "$response" ]; then
        echo -e "${GREEN}PASS${NC} - $response"
        ((PASSED++))
    else
        echo -e "${RED}FAIL${NC} - Header '$header' not found"
        ((FAILED++))
    fi
}

echo ""
echo -e "${BLUE}=============================================="
echo "       CALCULATOR API TEST SUITE"
echo "==============================================${NC}"
echo ""
echo "Base URL: $BASE_URL"
echo ""

# Check if server is up
echo -e "${YELLOW}Checking if server is available...${NC}"
if ! curl -s "${BASE_URL}/health" > /dev/null 2>&1; then
    echo -e "${RED}ERROR: Server is not responding at ${BASE_URL}${NC}"
    echo "Make sure the application is running with: docker-compose up"
    exit 1
fi
echo -e "${GREEN}Server is UP!${NC}"
echo ""

echo -e "${YELLOW}=== 1. BASIC OPERATIONS ===${NC}"
test_endpoint "Sum 10 + 5 = 15" "/sum?a=10&b=5" '"result":15'
test_endpoint "Subtract 20 - 8 = 12" "/subtract?a=20&b=8" '"result":12'
test_endpoint "Multiply 6 * 7 = 42" "/multiply?a=6&b=7" '"result":42'
test_endpoint "Divide 100 / 4 = 25" "/divide?a=100&b=4" '"result":25'
echo ""

echo -e "${YELLOW}=== 2. DECIMAL NUMBERS ===${NC}"
test_endpoint "Sum 1.5 + 2.5 = 4.0" "/sum?a=1.5&b=2.5" '"result":4'
test_endpoint "Divide 10 / 3 (repeating)" "/divide?a=10&b=3" '"result":3.333333'
test_endpoint "Multiply 0.1 * 0.2 = 0.02" "/multiply?a=0.1&b=0.2" '"result":0.02'
echo ""

echo -e "${YELLOW}=== 3. NEGATIVE NUMBERS ===${NC}"
test_endpoint "Sum -5 + 3 = -2" "/sum?a=-5&b=3" '"result":-2'
test_endpoint "Subtract -10 - (-5) = -5" "/subtract?a=-10&b=-5" '"result":-5'
test_endpoint "Multiply -6 * 7 = -42" "/multiply?a=-6&b=7" '"result":-42'
echo ""

echo -e "${YELLOW}=== 4. LARGE NUMBERS (Arbitrary Precision) ===${NC}"
test_endpoint "Large multiply" "/multiply?a=999999999999&b=999999999999" '"result":999999999998000000000001'
test_endpoint "Large sum" "/sum?a=12345678901234567890&b=98765432109876543210" '"result":111111111011111111100'
echo ""

echo -e "${YELLOW}=== 5. ZERO OPERATIONS ===${NC}"
test_endpoint "Sum 0 + 5 = 5" "/sum?a=0&b=5" '"result":5'
test_endpoint "Multiply 100 * 0 = 0" "/multiply?a=100&b=0" '"result":0'
echo ""

echo -e "${YELLOW}=== 6. ERROR CASES ===${NC}"
test_endpoint "Divide by zero" "/divide?a=10&b=0" '"error":"Division by zero is not allowed"'
echo ""

echo -e "${YELLOW}=== 7. HEALTH ENDPOINTS ===${NC}"
test_endpoint "Basic health" "/health" '"status":"UP"'
test_endpoint "Detailed health info" "/health/info" '"status":"UP"'
echo ""

echo -e "${YELLOW}=== 8. REQUEST TRACING ===${NC}"
test_header "X-Request-ID header present" "/sum?a=1&b=1" "X-Request-ID"
echo ""


echo -e "${BLUE}=============================================="
echo "              TEST SUMMARY"
echo "==============================================${NC}"
echo ""
echo -e "  ${GREEN}Passed: $PASSED${NC}"
echo -e "  ${RED}Failed: $FAILED${NC}"
echo ""

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}All tests passed! ✓${NC}"
    exit 0
else
    echo -e "${RED}Some tests failed! ✗${NC}"
    exit 1
fi

