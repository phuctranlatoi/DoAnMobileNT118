#!/bin/bash

# Unit Tests Runner Script
# Chạy unit tests và tạo báo cáo

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}Starting Unit Tests${NC}"
echo "=================================================="

# Clean previous build
echo -e "${YELLOW}Cleaning previous build...${NC}"
./gradlew clean

# Run unit tests
echo -e "${YELLOW}Running unit tests...${NC}"
./gradlew test

if [ $? -eq 0 ]; then
    echo -e "${GREEN}Unit tests passed!${NC}"
    
    # Create reports directory
    REPORTS_DIR="test-reports/unit-tests-$(date +%Y%m%d_%H%M%S)"
    mkdir -p "$REPORTS_DIR"
    
    # Copy test reports
    if [ -d "app/build/reports/tests/testDebugUnitTest" ]; then
        cp -r app/build/reports/tests/testDebugUnitTest/* "$REPORTS_DIR/"
        echo -e "${BLUE}Test reports saved to: $REPORTS_DIR${NC}"
        echo -e "${BLUE}Open: $REPORTS_DIR/index.html${NC}"
    fi
    
    # Show test summary
    echo ""
    echo -e "${GREEN}UNIT TESTS COMPLETED SUCCESSFULLY!${NC}"
    echo "=================================================="
    
else
    echo -e "${RED}Unit tests failed!${NC}"
    echo "Check the test reports for details."
    exit 1
fi