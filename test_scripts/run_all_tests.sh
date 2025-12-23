#!/bin/bash

# Automated Test Runner Script
# Runs all unit tests, integration tests, and UI tests

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
REPORTS_DIR="$PROJECT_DIR/test-reports"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

echo -e "${BLUE}Starting Automated Test Suite${NC}"
echo -e "${BLUE}Project Directory: $PROJECT_DIR${NC}"
echo -e "${BLUE}Timestamp: $TIMESTAMP${NC}"
echo "=================================================="

# Create reports directory
mkdir -p "$REPORTS_DIR"

# Function to print section headers
print_section() {
    echo ""
    echo -e "${YELLOW}$1${NC}"
    echo "=================================================="
}

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Function to check if emulator is running
is_emulator_running() {
    adb devices | grep -q "emulator"
}

# Function to wait for device
wait_for_device() {
    echo -e "${YELLOW}Waiting for device to be ready...${NC}"
    adb wait-for-device
    
    # Wait for boot to complete
    while [ "$(adb shell getprop sys.boot_completed 2>/dev/null)" != "1" ]; do
        echo -e "${YELLOW}Waiting for device boot to complete...${NC}"
        sleep 2
    done
    
    # Unlock screen
    adb shell input keyevent 82
    echo -e "${GREEN}Device is ready${NC}"
}

# Function to start emulator if needed
start_emulator_if_needed() {
    if ! is_emulator_running; then
        echo -e "${YELLOW}No emulator running, attempting to start one...${NC}"
        
        # Try to start default AVD
        if command_exists emulator; then
            # List available AVDs
            avd_list=$(emulator -list-avds 2>/dev/null | head -1)
            if [ -n "$avd_list" ]; then
                echo -e "${YELLOW}Starting emulator: $avd_list${NC}"
                emulator -avd "$avd_list" -no-window -no-audio &
                EMULATOR_PID=$!
                
                wait_for_device
            else
                echo -e "${RED}No AVDs found. Please create an AVD first.${NC}"
                exit 1
            fi
        else
            echo -e "${RED}Emulator command not found. Please ensure Android SDK is properly installed.${NC}"
            exit 1
        fi
    else
        echo -e "${GREEN}Emulator is already running${NC}"
        wait_for_device
    fi
}

# Function to run unit tests
run_unit_tests() {
    print_section "🧪 Running Unit Tests"
    
    cd "$PROJECT_DIR"
    
    echo -e "${BLUE}Running unit tests...${NC}"
    ./gradlew test --continue
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ Unit tests passed${NC}"
        
        # Copy test reports
        if [ -d "app/build/reports/tests/testDebugUnitTest" ]; then
            cp -r app/build/reports/tests/testDebugUnitTest "$REPORTS_DIR/unit-tests-$TIMESTAMP"
            echo -e "${BLUE}Unit test reports saved to: $REPORTS_DIR/unit-tests-$TIMESTAMP${NC}"
        fi
        
        return 0
    else
        echo -e "${RED}❌ Unit tests failed${NC}"
        return 1
    fi
}

# Function to run instrumented tests
run_instrumented_tests() {
    print_section "Running Instrumented Tests"
    
    cd "$PROJECT_DIR"
    
    # Ensure device is ready
    start_emulator_if_needed
    
    echo -e "${BLUE}Installing app for testing...${NC}"
    ./gradlew installDebug installDebugAndroidTest
    
    echo -e "${BLUE}Running instrumented tests...${NC}"
    ./gradlew connectedDebugAndroidTest --continue
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ Instrumented tests passed${NC}"
        
        # Copy test reports
        if [ -d "app/build/reports/androidTests/connected" ]; then
            cp -r app/build/reports/androidTests/connected "$REPORTS_DIR/instrumented-tests-$TIMESTAMP"
            echo -e "${BLUE}Instrumented test reports saved to: $REPORTS_DIR/instrumented-tests-$TIMESTAMP${NC}"
        fi
        
        return 0
    else
        echo -e "${RED}❌ Instrumented tests failed${NC}"
        return 1
    fi
}

# Function to run lint checks
run_lint_checks() {
    print_section "Running Lint Checks"
    
    cd "$PROJECT_DIR"
    
    echo -e "${BLUE}Running lint analysis...${NC}"
    ./gradlew lint
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ Lint checks passed${NC}"
        
        # Copy lint reports
        if [ -d "app/build/reports/lint-results-debug.html" ]; then
            cp app/build/reports/lint-results-debug.html "$REPORTS_DIR/lint-report-$TIMESTAMP.html"
            echo -e "${BLUE}Lint report saved to: $REPORTS_DIR/lint-report-$TIMESTAMP.html${NC}"
        fi
        
        return 0
    else
        echo -e "${YELLOW}⚠️ Lint checks found issues (non-blocking)${NC}"
        return 0  # Don't fail the entire test suite for lint issues
    fi
}

# Function to generate test coverage
generate_coverage() {
    print_section "Generating Test Coverage"
    
    cd "$PROJECT_DIR"
    
    echo -e "${BLUE}Generating coverage report...${NC}"
    ./gradlew jacocoTestReport
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ Coverage report generated${NC}"
        
        # Copy coverage reports
        if [ -d "app/build/reports/jacoco/jacocoTestReport" ]; then
            cp -r app/build/reports/jacoco/jacocoTestReport "$REPORTS_DIR/coverage-$TIMESTAMP"
            echo -e "${BLUE}Coverage report saved to: $REPORTS_DIR/coverage-$TIMESTAMP${NC}"
        fi
        
        return 0
    else
        echo -e "${YELLOW}⚠️ Coverage report generation failed (non-blocking)${NC}"
        return 0
    fi
}

# Function to run performance tests
run_performance_tests() {
    print_section "⚡ Running Performance Tests"
    
    if [ -f "$PROJECT_DIR/test_scripts/performance_test.sh" ]; then
        echo -e "${BLUE}Running performance tests...${NC}"
        bash "$PROJECT_DIR/test_scripts/performance_test.sh"
        
        if [ $? -eq 0 ]; then
            echo -e "${GREEN}✅ Performance tests completed${NC}"
        else
            echo -e "${YELLOW}⚠️ Performance tests had issues (non-blocking)${NC}"
        fi
    else
        echo -e "${YELLOW}⚠️ Performance test script not found, skipping...${NC}"
    fi
}

# Function to cleanup
cleanup() {
    print_section "🧹 Cleanup"
    
    # Kill emulator if we started it
    if [ -n "$EMULATOR_PID" ]; then
        echo -e "${YELLOW}Stopping emulator...${NC}"
        kill $EMULATOR_PID 2>/dev/null || true
    fi
    
    # Clean build artifacts if requested
    if [ "$CLEAN_AFTER" = "true" ]; then
        echo -e "${BLUE}Cleaning build artifacts...${NC}"
        cd "$PROJECT_DIR"
        ./gradlew clean
    fi
}

# Function to generate summary report
generate_summary() {
    print_section "Test Summary"
    
    local summary_file="$REPORTS_DIR/test-summary-$TIMESTAMP.txt"
    
    cat > "$summary_file" << EOF
Test Execution Summary
======================
Timestamp: $TIMESTAMP
Project: DoAn Mobile NT118

Test Results:
- Unit Tests: $UNIT_TEST_RESULT
- Instrumented Tests: $INSTRUMENTED_TEST_RESULT
- Lint Checks: $LINT_RESULT
- Coverage Generation: $COVERAGE_RESULT

Reports Location: $REPORTS_DIR

EOF
    
    echo -e "${BLUE}Summary report saved to: $summary_file${NC}"
    
    # Display summary
    echo ""
    echo -e "${BLUE}TEST EXECUTION SUMMARY${NC}"
    echo "=========================="
    echo -e "Unit Tests: $UNIT_TEST_RESULT"
    echo -e "Instrumented Tests: $INSTRUMENTED_TEST_RESULT"
    echo -e "Lint Checks: $LINT_RESULT"
    echo -e "Coverage Generation: $COVERAGE_RESULT"
    echo ""
    
    # Calculate overall result
    if [[ "$UNIT_TEST_RESULT" == *"PASS"* && "$INSTRUMENTED_TEST_RESULT" == *"PASS"* ]]; then
        echo -e "${GREEN}ALL TESTS PASSED!${NC}"
        return 0
    else
        echo -e "${RED}SOME TESTS FAILED${NC}"
        return 1
    fi
}

# Main execution
main() {
    # Parse command line arguments
    CLEAN_AFTER=false
    SKIP_INSTRUMENTED=false
    
    while [[ $# -gt 0 ]]; do
        case $1 in
            --clean)
                CLEAN_AFTER=true
                shift
                ;;
            --skip-instrumented)
                SKIP_INSTRUMENTED=true
                shift
                ;;
            --help)
                echo "Usage: $0 [OPTIONS]"
                echo "Options:"
                echo "  --clean              Clean build artifacts after tests"
                echo "  --skip-instrumented  Skip instrumented tests (useful for CI without emulator)"
                echo "  --help               Show this help message"
                exit 0
                ;;
            *)
                echo "Unknown option: $1"
                exit 1
                ;;
        esac
    done
    
    # Trap cleanup on exit
    trap cleanup EXIT
    
    # Check prerequisites
    if ! command_exists adb; then
        echo -e "${RED}❌ ADB not found. Please install Android SDK.${NC}"
        exit 1
    fi
    
    if [ ! -f "$PROJECT_DIR/gradlew" ]; then
        echo -e "${RED}❌ gradlew not found. Please run from project root.${NC}"
        exit 1
    fi
    
    # Make gradlew executable
    chmod +x "$PROJECT_DIR/gradlew"
    
    # Initialize result variables
    UNIT_TEST_RESULT="FAILED"
    INSTRUMENTED_TEST_RESULT="FAILED"
    LINT_RESULT="FAILED"
    COVERAGE_RESULT="FAILED"
    
    # Run tests
    echo -e "${GREEN}Starting test execution...${NC}"
    
    # 1. Unit Tests
    if run_unit_tests; then
        UNIT_TEST_RESULT="PASSED"
    fi
    
    # 2. Lint Checks
    if run_lint_checks; then
        LINT_RESULT="PASSED"
    fi
    
    # 3. Instrumented Tests (if not skipped)
    if [ "$SKIP_INSTRUMENTED" = "false" ]; then
        if run_instrumented_tests; then
            INSTRUMENTED_TEST_RESULT="PASSED"
        fi
    else
        INSTRUMENTED_TEST_RESULT="SKIPPED"
    fi
    
    # 4. Coverage Report
    if generate_coverage; then
        COVERAGE_RESULT="GENERATED"
    fi
    
    # 5. Performance Tests (optional)
    run_performance_tests
    
    # 6. Generate Summary
    generate_summary
    
    local exit_code=$?
    
    echo ""
    echo -e "${BLUE}Test execution completed!${NC}"
    echo -e "${BLUE}All reports saved to: $REPORTS_DIR${NC}"
    
    exit $exit_code
}

# Run main function
main "$@"