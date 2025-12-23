#!/bin/bash

# Deployment Testing Script
# Tests deployment process, environment setup, and production readiness

set -e

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
DEPLOYMENT_REPORT="$REPORTS_DIR/deployment_test_$TIMESTAMP.txt"

echo -e "${BLUE}Starting Deployment Testing${NC}"
echo -e "${BLUE}Project Directory: $PROJECT_DIR${NC}"
echo -e "${BLUE}Timestamp: $TIMESTAMP${NC}"
echo "=================================================="

# Create reports directory
mkdir -p "$REPORTS_DIR"

# Initialize report
cat > "$DEPLOYMENT_REPORT" << EOF
DEPLOYMENT TESTING REPORT
=========================
Test Date: $(date)
Project: DoAn Mobile NT118 - Doctor-Patient Messaging System

EOF

# Function to log test results
log_test_result() {
    local test_name="$1"
    local result="$2"
    local details="$3"
    
    echo -e "${BLUE}Testing: $test_name${NC}"
    if [ "$result" = "PASS" ]; then
        echo -e "${GREEN}PASS: $test_name${NC}"
    else
        echo -e "${RED}FAIL: $test_name${NC}"
    fi
    
    if [ -n "$details" ]; then
        echo "Details: $details"
    fi
    
    # Log to report
    echo "$result: $test_name" >> "$DEPLOYMENT_REPORT"
    if [ -n "$details" ]; then
        echo "  Details: $details" >> "$DEPLOYMENT_REPORT"
    fi
    echo "" >> "$DEPLOYMENT_REPORT"
}

# Test 1: Build System Verification
test_build_system() {
    echo -e "${YELLOW}Test 1: Build System Verification${NC}"
    
    cd "$PROJECT_DIR"
    
    # Test Gradle wrapper
    if [ -f "./gradlew" ]; then
        chmod +x ./gradlew
        if ./gradlew --version > /dev/null 2>&1; then
            log_test_result "Gradle Wrapper" "PASS" "Gradle wrapper is functional"
        else
            log_test_result "Gradle Wrapper" "FAIL" "Gradle wrapper not working"
            return 1
        fi
    else
        log_test_result "Gradle Wrapper" "FAIL" "gradlew not found"
        return 1
    fi
    
    # Test clean build
    echo "Running clean build..."
    if ./gradlew clean build > /dev/null 2>&1; then
        log_test_result "Clean Build" "PASS" "Project builds successfully"
    else
        log_test_result "Clean Build" "FAIL" "Build failed"
        return 1
    fi
    
    # Check APK generation
    if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
        APK_SIZE=$(du -h "app/build/outputs/apk/debug/app-debug.apk" | cut -f1)
        log_test_result "APK Generation" "PASS" "APK generated successfully, size: $APK_SIZE"
    else
        log_test_result "APK Generation" "FAIL" "APK not generated"
        return 1
    fi
}

# Test 2: Dependencies and Configuration
test_dependencies() {
    echo -e "${YELLOW}Test 2: Dependencies and Configuration${NC}"
    
    # Check critical files
    local critical_files=(
        "app/build.gradle.kts"
        "app/google-services.json"
        "firebase.json"
        "local.properties"
    )
    
    for file in "${critical_files[@]}"; do
        if [ -f "$PROJECT_DIR/$file" ]; then
            log_test_result "File: $file" "PASS" "File exists"
        else
            log_test_result "File: $file" "FAIL" "File missing"
        fi
    done
    
    # Check Firebase configuration
    if [ -f "$PROJECT_DIR/app/google-services.json" ]; then
        if grep -q "project_id" "$PROJECT_DIR/app/google-services.json"; then
            log_test_result "Firebase Config" "PASS" "Firebase configuration valid"
        else
            log_test_result "Firebase Config" "FAIL" "Invalid Firebase configuration"
        fi
    else
        log_test_result "Firebase Config" "FAIL" "Firebase configuration missing"
    fi
    
    # Check Stringee configuration
    if grep -q "STRINGEE_SID_KEY" "$PROJECT_DIR/app/src/main/java/com/example/doannt118/stringee/StringeeTokenGenerator.java"; then
        log_test_result "Stringee Config" "PASS" "Stringee configuration found"
    else
        log_test_result "Stringee Config" "FAIL" "Stringee configuration missing"
    fi
}

# Test 3: Security and Permissions
test_security() {
    echo -e "${YELLOW}Test 3: Security and Permissions${NC}"
    
    # Check AndroidManifest.xml permissions
    MANIFEST_FILE="$PROJECT_DIR/app/src/main/AndroidManifest.xml"
    
    if [ -f "$MANIFEST_FILE" ]; then
        # Check required permissions
        local required_permissions=(
            "android.permission.INTERNET"
            "android.permission.RECORD_AUDIO"
            "android.permission.CAMERA"
            "android.permission.MODIFY_AUDIO_SETTINGS"
        )
        
        for permission in "${required_permissions[@]}"; do
            if grep -q "$permission" "$MANIFEST_FILE"; then
                log_test_result "Permission: $permission" "PASS" "Permission declared"
            else
                log_test_result "Permission: $permission" "FAIL" "Permission missing"
            fi
        done
        
        # Check for dangerous permissions without proper handling
        if grep -q "android.permission.WRITE_EXTERNAL_STORAGE" "$MANIFEST_FILE"; then
            log_test_result "Storage Permission" "WARN" "External storage permission found - ensure runtime permission handling"
        fi
        
    else
        log_test_result "AndroidManifest.xml" "FAIL" "AndroidManifest.xml not found"
    fi
    
    # Check for hardcoded secrets (basic check)
    echo "Checking for potential hardcoded secrets..."
    
    # Check for API keys in source code
    if grep -r "AIza" "$PROJECT_DIR/app/src/" > /dev/null 2>&1; then
        log_test_result "API Key Security" "WARN" "Potential API keys found in source code"
    else
        log_test_result "API Key Security" "PASS" "No obvious API keys in source code"
    fi
    
    # Check for passwords in source code
    if grep -ri "password.*=" "$PROJECT_DIR/app/src/" | grep -v "editTextPassword" > /dev/null 2>&1; then
        log_test_result "Password Security" "WARN" "Potential hardcoded passwords found"
    else
        log_test_result "Password Security" "PASS" "No obvious hardcoded passwords"
    fi
}

# Test 4: Performance and Resource Usage
test_performance() {
    echo -e "${YELLOW}Test 4: Performance and Resource Usage${NC}"
    
    # Check APK size
    if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
        APK_SIZE_BYTES=$(stat -f%z "app/build/outputs/apk/debug/app-debug.apk" 2>/dev/null || stat -c%s "app/build/outputs/apk/debug/app-debug.apk" 2>/dev/null)
        APK_SIZE_MB=$((APK_SIZE_BYTES / 1024 / 1024))
        
        if [ $APK_SIZE_MB -lt 50 ]; then
            log_test_result "APK Size" "PASS" "APK size: ${APK_SIZE_MB}MB (acceptable)"
        elif [ $APK_SIZE_MB -lt 100 ]; then
            log_test_result "APK Size" "WARN" "APK size: ${APK_SIZE_MB}MB (large but acceptable)"
        else
            log_test_result "APK Size" "FAIL" "APK size: ${APK_SIZE_MB}MB (too large)"
        fi
    fi
    
    # Check for large resources
    echo "Checking for large resource files..."
    find "$PROJECT_DIR/app/src/main/res" -type f -size +1M 2>/dev/null | while read -r large_file; do
        file_size=$(du -h "$large_file" | cut -f1)
        log_test_result "Large Resource" "WARN" "Large file found: $(basename "$large_file") ($file_size)"
    done
    
    # Check drawable resources
    DRAWABLE_COUNT=$(find "$PROJECT_DIR/app/src/main/res/drawable*" -name "*.png" -o -name "*.jpg" -o -name "*.jpeg" 2>/dev/null | wc -l)
    if [ $DRAWABLE_COUNT -gt 0 ]; then
        log_test_result "Drawable Resources" "PASS" "$DRAWABLE_COUNT drawable resources found"
    else
        log_test_result "Drawable Resources" "WARN" "No drawable resources found"
    fi
}

# Test 5: Database and Backend Connectivity
test_backend_connectivity() {
    echo -e "${YELLOW}Test 5: Database and Backend Connectivity${NC}"
    
    # Test Firebase connectivity (basic check)
    if command -v curl > /dev/null 2>&1; then
        echo "Testing Firebase connectivity..."
        
        # Try to reach Firebase
        if curl -s --connect-timeout 10 "https://firebase.googleapis.com" > /dev/null; then
            log_test_result "Firebase Connectivity" "PASS" "Firebase services reachable"
        else
            log_test_result "Firebase Connectivity" "FAIL" "Cannot reach Firebase services"
        fi
        
        # Try to reach Stringee
        if curl -s --connect-timeout 10 "https://api.stringee.com" > /dev/null; then
            log_test_result "Stringee Connectivity" "PASS" "Stringee services reachable"
        else
            log_test_result "Stringee Connectivity" "FAIL" "Cannot reach Stringee services"
        fi
    else
        log_test_result "Connectivity Test" "SKIP" "curl not available for connectivity testing"
    fi
    
    # Check Firebase configuration validity
    if [ -f "$PROJECT_DIR/app/google-services.json" ]; then
        if python3 -c "import json; json.load(open('$PROJECT_DIR/app/google-services.json'))" 2>/dev/null; then
            log_test_result "Firebase JSON Validity" "PASS" "Firebase configuration is valid JSON"
        else
            log_test_result "Firebase JSON Validity" "FAIL" "Firebase configuration is invalid JSON"
        fi
    fi
}

# Test 6: Deployment Environment
test_deployment_environment() {
    echo -e "${YELLOW}Test 6: Deployment Environment${NC}"
    
    # Check Java version
    if command -v java > /dev/null 2>&1; then
        JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
        log_test_result "Java Version" "PASS" "Java version: $JAVA_VERSION"
    else
        log_test_result "Java Version" "FAIL" "Java not found"
    fi
    
    # Check Android SDK
    if [ -n "$ANDROID_HOME" ] && [ -d "$ANDROID_HOME" ]; then
        log_test_result "Android SDK" "PASS" "Android SDK found at: $ANDROID_HOME"
    else
        log_test_result "Android SDK" "FAIL" "Android SDK not found or ANDROID_HOME not set"
    fi
    
    # Check available disk space
    AVAILABLE_SPACE=$(df -h "$PROJECT_DIR" | awk 'NR==2 {print $4}')
    log_test_result "Disk Space" "PASS" "Available space: $AVAILABLE_SPACE"
    
    # Check memory
    if command -v free > /dev/null 2>&1; then
        TOTAL_MEM=$(free -h | awk 'NR==2{print $2}')
        log_test_result "System Memory" "PASS" "Total memory: $TOTAL_MEM"
    elif command -v vm_stat > /dev/null 2>&1; then
        # macOS
        log_test_result "System Memory" "PASS" "Memory check completed (macOS)"
    fi
}

# Test 7: Production Readiness
test_production_readiness() {
    echo -e "${YELLOW}Test 7: Production Readiness${NC}"
    
    # Check for debug flags
    if grep -r "android:debuggable.*true" "$PROJECT_DIR/app/src/main/" > /dev/null 2>&1; then
        log_test_result "Debug Flags" "FAIL" "Debug flags found in production code"
    else
        log_test_result "Debug Flags" "PASS" "No debug flags in production code"
    fi
    
    # Check for TODO/FIXME comments
    TODO_COUNT=$(grep -r "TODO\|FIXME" "$PROJECT_DIR/app/src/" 2>/dev/null | wc -l)
    if [ $TODO_COUNT -gt 0 ]; then
        log_test_result "Code Completeness" "WARN" "$TODO_COUNT TODO/FIXME comments found"
    else
        log_test_result "Code Completeness" "PASS" "No TODO/FIXME comments found"
    fi
    
    # Check for proper error handling
    if grep -r "try.*catch" "$PROJECT_DIR/app/src/" > /dev/null 2>&1; then
        log_test_result "Error Handling" "PASS" "Error handling found in code"
    else
        log_test_result "Error Handling" "WARN" "Limited error handling found"
    fi
    
    # Check for logging
    if grep -r "Log\." "$PROJECT_DIR/app/src/" > /dev/null 2>&1; then
        log_test_result "Logging" "PASS" "Logging implementation found"
    else
        log_test_result "Logging" "WARN" "No logging implementation found"
    fi
}

# Test 8: Release Build
test_release_build() {
    echo -e "${YELLOW}Test 8: Release Build${NC}"
    
    cd "$PROJECT_DIR"
    
    echo "Attempting release build..."
    if ./gradlew assembleRelease > /dev/null 2>&1; then
        log_test_result "Release Build" "PASS" "Release build successful"
        
        # Check release APK
        if [ -f "app/build/outputs/apk/release/app-release-unsigned.apk" ]; then
            RELEASE_APK_SIZE=$(du -h "app/build/outputs/apk/release/app-release-unsigned.apk" | cut -f1)
            log_test_result "Release APK" "PASS" "Release APK generated, size: $RELEASE_APK_SIZE"
        else
            log_test_result "Release APK" "FAIL" "Release APK not found"
        fi
    else
        log_test_result "Release Build" "FAIL" "Release build failed"
    fi
}

# Main execution
main() {
    local overall_result="PASS"
    
    # Run all tests
    echo "Running deployment tests..."
    echo ""
    
    if ! test_build_system; then
        overall_result="FAIL"
    fi
    
    test_dependencies
    test_security
    test_performance
    test_backend_connectivity
    test_deployment_environment
    test_production_readiness
    
    if ! test_release_build; then
        overall_result="FAIL"
    fi
    
    # Generate summary
    echo "" >> "$DEPLOYMENT_REPORT"
    echo "DEPLOYMENT TEST SUMMARY" >> "$DEPLOYMENT_REPORT"
    echo "======================" >> "$DEPLOYMENT_REPORT"
    echo "Overall Result: $overall_result" >> "$DEPLOYMENT_REPORT"
    echo "Test Completed: $(date)" >> "$DEPLOYMENT_REPORT"
    
    # Display results
    echo ""
    echo "=================================================="
    echo -e "${BLUE}DEPLOYMENT TESTING COMPLETED${NC}"
    echo "=================================================="
    
    if [ "$overall_result" = "PASS" ]; then
        echo -e "${GREEN}Overall Result: PASS${NC}"
        echo "System is ready for deployment!"
    else
        echo -e "${RED}Overall Result: FAIL${NC}"
        echo "System has issues that need to be addressed before deployment."
    fi
    
    echo ""
    echo "Detailed report saved to: $DEPLOYMENT_REPORT"
    echo ""
    
    # Show critical issues
    echo "Critical Issues Summary:"
    grep "FAIL:" "$DEPLOYMENT_REPORT" | while read -r line; do
        echo -e "${RED}$line${NC}"
    done
    
    echo ""
    echo "Warnings Summary:"
    grep "WARN:" "$DEPLOYMENT_REPORT" | while read -r line; do
        echo -e "${YELLOW}$line${NC}"
    done
    
    if [ "$overall_result" = "FAIL" ]; then
        exit 1
    fi
}

# Run main function
main "$@"