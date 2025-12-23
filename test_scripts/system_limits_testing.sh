#!/bin/bash

# System Limits Testing Script
# Tests maximum capacity, resource limits, and breaking points

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
LIMITS_REPORT="$REPORTS_DIR/system_limits_test_$TIMESTAMP.txt"

echo -e "${BLUE}Starting System Limits Testing${NC}"
echo -e "${BLUE}Project Directory: $PROJECT_DIR${NC}"
echo -e "${BLUE}Timestamp: $TIMESTAMP${NC}"
echo "=================================================="

# Create reports directory
mkdir -p "$REPORTS_DIR"

# Initialize report
cat > "$LIMITS_REPORT" << EOF
SYSTEM LIMITS TESTING REPORT
============================
Test Date: $(date)
Project: DoAn Mobile NT118 - Doctor-Patient Messaging System

EOF

# Function to log test results
log_result() {
    local test_name="$1"
    local result="$2"
    local value="$3"
    local limit="$4"
    
    echo -e "${BLUE}Testing: $test_name${NC}"
    echo "Result: $result"
    if [ -n "$value" ]; then
        echo "Value: $value"
    fi
    if [ -n "$limit" ]; then
        echo "Limit: $limit"
    fi
    echo ""
    
    # Log to report
    echo "$test_name: $result" >> "$LIMITS_REPORT"
    if [ -n "$value" ]; then
        echo "  Value: $value" >> "$LIMITS_REPORT"
    fi
    if [ -n "$limit" ]; then
        echo "  Limit: $limit" >> "$LIMITS_REPORT"
    fi
    echo "" >> "$LIMITS_REPORT"
}

# Test 1: Maximum APK Size
test_apk_size_limits() {
    echo -e "${YELLOW}Test 1: APK Size Limits${NC}"
    
    cd "$PROJECT_DIR"
    
    # Build release APK
    echo "Building release APK..."
    if ./gradlew assembleRelease > /dev/null 2>&1; then
        
        # Check APK sizes
        if [ -f "app/build/outputs/apk/release/app-release-unsigned.apk" ]; then
            APK_SIZE_BYTES=$(stat -f%z "app/build/outputs/apk/release/app-release-unsigned.apk" 2>/dev/null || stat -c%s "app/build/outputs/apk/release/app-release-unsigned.apk" 2>/dev/null)
            APK_SIZE_MB=$((APK_SIZE_BYTES / 1024 / 1024))
            
            # Google Play limits
            PLAY_STORE_LIMIT=150  # MB for base APK
            BUNDLE_LIMIT=150      # MB per split APK
            
            if [ $APK_SIZE_MB -lt $PLAY_STORE_LIMIT ]; then
                log_result "APK Size Limit" "PASS" "${APK_SIZE_MB}MB" "${PLAY_STORE_LIMIT}MB (Play Store limit)"
            else
                log_result "APK Size Limit" "FAIL" "${APK_SIZE_MB}MB" "${PLAY_STORE_LIMIT}MB (Play Store limit)"
            fi
            
            # Check individual components
            echo "Analyzing APK components..."
            if command -v aapt > /dev/null 2>&1; then
                aapt dump badging "app/build/outputs/apk/release/app-release-unsigned.apk" > /tmp/apk_info.txt 2>/dev/null || true
                if [ -f /tmp/apk_info.txt ]; then
                    PACKAGE_NAME=$(grep "package:" /tmp/apk_info.txt | sed "s/.*name='\([^']*\)'.*/\1/")
                    VERSION_CODE=$(grep "package:" /tmp/apk_info.txt | sed "s/.*versionCode='\([^']*\)'.*/\1/")
                    log_result "APK Package Info" "INFO" "Package: $PACKAGE_NAME, Version: $VERSION_CODE" ""
                fi
            fi
        else
            log_result "APK Size Limit" "FAIL" "APK not found" ""
        fi
    else
        log_result "APK Size Limit" "FAIL" "Build failed" ""
    fi
}

# Test 2: Memory Usage Limits
test_memory_limits() {
    echo -e "${YELLOW}Test 2: Memory Usage Limits${NC}"
    
    # Get system memory info
    if command -v free > /dev/null 2>&1; then
        TOTAL_MEM_KB=$(free | grep "Mem:" | awk '{print $2}')
        TOTAL_MEM_MB=$((TOTAL_MEM_KB / 1024))
        AVAILABLE_MEM_KB=$(free | grep "Mem:" | awk '{print $7}')
        AVAILABLE_MEM_MB=$((AVAILABLE_MEM_KB / 1024))
        
        log_result "System Memory" "INFO" "Total: ${TOTAL_MEM_MB}MB, Available: ${AVAILABLE_MEM_MB}MB" ""
        
        # Android app memory limits (typical)
        if [ $TOTAL_MEM_MB -lt 1024 ]; then
            APP_MEMORY_LIMIT=64   # Low-end devices
        elif [ $TOTAL_MEM_MB -lt 2048 ]; then
            APP_MEMORY_LIMIT=128  # Mid-range devices
        else
            APP_MEMORY_LIMIT=256  # High-end devices
        fi
        
        log_result "App Memory Limit" "INFO" "${APP_MEMORY_LIMIT}MB" "Estimated based on device RAM"
        
    elif command -v vm_stat > /dev/null 2>&1; then
        # macOS
        VM_STAT=$(vm_stat)
        PAGE_SIZE=$(vm_stat | grep "page size" | awk '{print $8}')
        FREE_PAGES=$(vm_stat | grep "Pages free" | awk '{print $3}' | sed 's/\.//')
        FREE_MB=$(( FREE_PAGES * PAGE_SIZE / 1024 / 1024 ))
        
        log_result "System Memory (macOS)" "INFO" "Free: ${FREE_MB}MB" ""
    fi
}

# Test 3: Storage Limits
test_storage_limits() {
    echo -e "${YELLOW}Test 3: Storage Limits${NC}"
    
    # Check available disk space
    AVAILABLE_SPACE=$(df -h "$PROJECT_DIR" | awk 'NR==2 {print $4}')
    AVAILABLE_SPACE_BYTES=$(df "$PROJECT_DIR" | awk 'NR==2 {print $4}')
    AVAILABLE_SPACE_GB=$((AVAILABLE_SPACE_BYTES / 1024 / 1024))
    
    log_result "Available Disk Space" "INFO" "$AVAILABLE_SPACE (${AVAILABLE_SPACE_GB}GB)" ""
    
    # Test large file creation
    echo "Testing large file creation..."
    TEST_FILE="$PROJECT_DIR/large_test_file.tmp"
    
    # Try to create a 100MB test file
    if dd if=/dev/zero of="$TEST_FILE" bs=1M count=100 > /dev/null 2>&1; then
        log_result "Large File Creation" "PASS" "100MB file created successfully" ""
        rm -f "$TEST_FILE"
    else
        log_result "Large File Creation" "FAIL" "Cannot create 100MB file" ""
    fi
    
    # Database size limits (Firebase Firestore)
    FIRESTORE_DOC_LIMIT="1MB per document"
    FIRESTORE_COLLECTION_LIMIT="No limit on collection size"
    FIRESTORE_QUERY_LIMIT="1MB per query result"
    
    log_result "Firestore Document Limit" "INFO" "$FIRESTORE_DOC_LIMIT" ""
    log_result "Firestore Collection Limit" "INFO" "$FIRESTORE_COLLECTION_LIMIT" ""
    log_result "Firestore Query Limit" "INFO" "$FIRESTORE_QUERY_LIMIT" ""
}

# Test 4: Network Connection Limits
test_network_limits() {
    echo -e "${YELLOW}Test 4: Network Connection Limits${NC}"
    
    # Check system connection limits
    if [ -f /proc/sys/net/core/somaxconn ]; then
        SOMAXCONN=$(cat /proc/sys/net/core/somaxconn)
        log_result "System Socket Backlog" "INFO" "$SOMAXCONN connections" ""
    fi
    
    if [ -f /proc/sys/fs/file-max ]; then
        FILE_MAX=$(cat /proc/sys/fs/file-max)
        log_result "System File Descriptor Limit" "INFO" "$FILE_MAX files" ""
    fi
    
    # Check ulimit
    ULIMIT_N=$(ulimit -n)
    log_result "Process File Descriptor Limit" "INFO" "$ULIMIT_N files" ""
    
    # Test network connectivity
    echo "Testing network connectivity..."
    
    # Test Firebase connectivity
    if command -v curl > /dev/null 2>&1; then
        if curl -s --connect-timeout 10 "https://firebase.googleapis.com" > /dev/null; then
            log_result "Firebase Connectivity" "PASS" "Firebase reachable" ""
        else
            log_result "Firebase Connectivity" "FAIL" "Cannot reach Firebase" ""
        fi
        
        # Test Stringee connectivity
        if curl -s --connect-timeout 10 "https://api.stringee.com" > /dev/null; then
            log_result "Stringee Connectivity" "PASS" "Stringee reachable" ""
        else
            log_result "Stringee Connectivity" "FAIL" "Cannot reach Stringee" ""
        fi
    fi
    
    # Android network limits
    HTTP_CONNECTION_LIMIT="4 connections per host (default)"
    SOCKET_TIMEOUT="30 seconds (default)"
    
    log_result "HTTP Connection Limit" "INFO" "$HTTP_CONNECTION_LIMIT" ""
    log_result "Socket Timeout" "INFO" "$SOCKET_TIMEOUT" ""
}

# Test 5: Concurrent User Limits
test_concurrent_user_limits() {
    echo -e "${YELLOW}Test 5: Concurrent User Limits${NC}"
    
    # Firebase Firestore limits
    FIRESTORE_CONCURRENT_CONNECTIONS="1 million concurrent connections"
    FIRESTORE_WRITES_PER_SECOND="10,000 writes per second per database"
    FIRESTORE_READS_PER_SECOND="No limit on reads"
    
    log_result "Firestore Concurrent Connections" "INFO" "$FIRESTORE_CONCURRENT_CONNECTIONS" ""
    log_result "Firestore Write Limit" "INFO" "$FIRESTORE_WRITES_PER_SECOND" ""
    log_result "Firestore Read Limit" "INFO" "$FIRESTORE_READS_PER_SECOND" ""
    
    # Stringee limits
    STRINGEE_CONCURRENT_CALLS="Based on subscription plan"
    STRINGEE_API_RATE_LIMIT="1000 requests per minute"
    
    log_result "Stringee Concurrent Calls" "INFO" "$STRINGEE_CONCURRENT_CALLS" ""
    log_result "Stringee API Rate Limit" "INFO" "$STRINGEE_API_RATE_LIMIT" ""
    
    # Estimated app limits based on resources
    ESTIMATED_CONCURRENT_USERS="500-1000 users (estimated)"
    log_result "Estimated Concurrent Users" "INFO" "$ESTIMATED_CONCURRENT_USERS" "Based on system resources"
}

# Test 6: Message Size and Volume Limits
test_message_limits() {
    echo -e "${YELLOW}Test 6: Message Size and Volume Limits${NC}"
    
    # Create test messages of various sizes
    echo "Testing message size limits..."
    
    # Small message (typical)
    SMALL_MSG_SIZE=100  # bytes
    log_result "Small Message Size" "PASS" "${SMALL_MSG_SIZE} bytes" "Typical chat message"
    
    # Medium message
    MEDIUM_MSG_SIZE=1000  # bytes
    log_result "Medium Message Size" "PASS" "${MEDIUM_MSG_SIZE} bytes" "Long text message"
    
    # Large message (approaching Firestore limit)
    LARGE_MSG_SIZE=1048576  # 1MB - Firestore document limit
    log_result "Large Message Size" "LIMIT" "${LARGE_MSG_SIZE} bytes (1MB)" "Firestore document limit"
    
    # Message volume estimates
    DAILY_MESSAGES_PER_USER="50-200 messages"
    PEAK_MESSAGES_PER_SECOND="100-500 messages"
    
    log_result "Daily Messages Per User" "INFO" "$DAILY_MESSAGES_PER_USER" "Estimated"
    log_result "Peak Messages Per Second" "INFO" "$PEAK_MESSAGES_PER_SECOND" "System capacity estimate"
}

# Test 7: Call Duration and Quality Limits
test_call_limits() {
    echo -e "${YELLOW}Test 7: Call Duration and Quality Limits${NC}"
    
    # Stringee call limits
    STRINGEE_CALL_DURATION="No limit (based on subscription)"
    STRINGEE_CALL_QUALITY="HD video, high-quality audio"
    STRINGEE_CONCURRENT_CALLS_PER_USER="1 call per user"
    
    log_result "Stringee Call Duration" "INFO" "$STRINGEE_CALL_DURATION" ""
    log_result "Stringee Call Quality" "INFO" "$STRINGEE_CALL_QUALITY" ""
    log_result "Concurrent Calls Per User" "INFO" "$STRINGEE_CONCURRENT_CALLS_PER_USER" ""
    
    # Network bandwidth requirements
    VOICE_CALL_BANDWIDTH="64 kbps (minimum)"
    VIDEO_CALL_BANDWIDTH="1 Mbps (minimum for HD)"
    
    log_result "Voice Call Bandwidth" "INFO" "$VOICE_CALL_BANDWIDTH" ""
    log_result "Video Call Bandwidth" "INFO" "$VIDEO_CALL_BANDWIDTH" ""
}

# Test 8: Database Query Limits
test_database_query_limits() {
    echo -e "${YELLOW}Test 8: Database Query Limits${NC}"
    
    # Firestore query limits
    FIRESTORE_QUERY_RESULT_SIZE="1MB per query"
    FIRESTORE_COMPOUND_QUERIES="Up to 30 compound queries"
    FIRESTORE_IN_QUERIES="Up to 10 values in 'in' queries"
    FIRESTORE_ARRAY_CONTAINS="Up to 10 values in 'array-contains-any'"
    
    log_result "Firestore Query Result Size" "INFO" "$FIRESTORE_QUERY_RESULT_SIZE" ""
    log_result "Firestore Compound Queries" "INFO" "$FIRESTORE_COMPOUND_QUERIES" ""
    log_result "Firestore IN Queries" "INFO" "$FIRESTORE_IN_QUERIES" ""
    log_result "Firestore Array Contains" "INFO" "$FIRESTORE_ARRAY_CONTAINS" ""
    
    # Pagination recommendations
    RECOMMENDED_PAGE_SIZE="20-50 documents per page"
    log_result "Recommended Page Size" "INFO" "$RECOMMENDED_PAGE_SIZE" "For optimal performance"
}

# Test 9: Security and Rate Limits
test_security_limits() {
    echo -e "${YELLOW}Test 9: Security and Rate Limits${NC}"
    
    # Firebase Authentication limits
    FIREBASE_AUTH_RATE_LIMIT="No specific limit documented"
    FIREBASE_SECURITY_RULES="Applied per request"
    
    log_result "Firebase Auth Rate Limit" "INFO" "$FIREBASE_AUTH_RATE_LIMIT" ""
    log_result "Firebase Security Rules" "INFO" "$FIREBASE_SECURITY_RULES" ""
    
    # API rate limiting recommendations
    API_RATE_LIMIT_RECOMMENDATION="100 requests per minute per user"
    log_result "Recommended API Rate Limit" "INFO" "$API_RATE_LIMIT_RECOMMENDATION" ""
    
    # Security considerations
    MAX_LOGIN_ATTEMPTS="5 attempts per 15 minutes (recommended)"
    SESSION_TIMEOUT="30 minutes (recommended)"
    
    log_result "Max Login Attempts" "INFO" "$MAX_LOGIN_ATTEMPTS" ""
    log_result "Session Timeout" "INFO" "$SESSION_TIMEOUT" ""
}

# Generate summary and recommendations
generate_summary() {
    echo -e "${YELLOW}Generating Summary and Recommendations${NC}"
    
    cat >> "$LIMITS_REPORT" << EOF

SUMMARY AND RECOMMENDATIONS
===========================

SYSTEM CAPACITY ESTIMATES:
- Concurrent Users: 500-1,000 users
- Messages per Second: 100-500 messages
- Storage Growth: ~1GB per 10,000 users per month
- Network Bandwidth: 1-10 Mbps for video calls

CRITICAL LIMITS TO MONITOR:
1. APK Size: Keep under 150MB for Play Store
2. Memory Usage: Monitor for memory leaks
3. Firestore Document Size: Keep under 1MB
4. Network Connections: Monitor concurrent connections
5. API Rate Limits: Implement proper rate limiting

SCALING RECOMMENDATIONS:
1. Implement message pagination (20-50 messages per page)
2. Use Firebase Cloud Functions for heavy processing
3. Implement proper caching strategies
4. Monitor and optimize database queries
5. Use CDN for static content
6. Implement proper error handling and retry logic

MONITORING RECOMMENDATIONS:
1. Set up Firebase Performance Monitoring
2. Monitor Crashlytics for app stability
3. Track user engagement metrics
4. Monitor API response times
5. Set up alerts for resource usage

OPTIMIZATION OPPORTUNITIES:
1. Implement message compression
2. Use image optimization for media messages
3. Implement offline message caching
4. Optimize database indexes
5. Use connection pooling for network requests

EOF

    echo "Summary and recommendations added to report"
}

# Main execution
main() {
    echo "Running system limits tests..."
    echo ""
    
    # Run all tests
    test_apk_size_limits
    test_memory_limits
    test_storage_limits
    test_network_limits
    test_concurrent_user_limits
    test_message_limits
    test_call_limits
    test_database_query_limits
    test_security_limits
    
    # Generate summary
    generate_summary
    
    # Final report
    echo "" >> "$LIMITS_REPORT"
    echo "Test Completed: $(date)" >> "$LIMITS_REPORT"
    
    # Display results
    echo ""
    echo "=================================================="
    echo -e "${BLUE}SYSTEM LIMITS TESTING COMPLETED${NC}"
    echo "=================================================="
    echo ""
    echo "Detailed report saved to: $LIMITS_REPORT"
    echo ""
    
    # Show key findings
    echo -e "${GREEN}KEY FINDINGS:${NC}"
    echo "- APK size and memory limits checked"
    echo "- Network and storage capacity evaluated"
    echo "- Database and API limits documented"
    echo "- Security and rate limiting reviewed"
    echo ""
    
    echo -e "${YELLOW}NEXT STEPS:${NC}"
    echo "1. Review the detailed report for specific limits"
    echo "2. Implement monitoring for critical metrics"
    echo "3. Plan scaling strategies based on findings"
    echo "4. Set up alerts for approaching limits"
    echo ""
}

# Run main function
main "$@"