#!/bin/bash

# Performance Testing Script
# Monitors app performance metrics during test execution

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
PACKAGE_NAME="com.example.doannt118"
TEST_DURATION=300  # 5 minutes
SAMPLE_INTERVAL=5  # 5 seconds
REPORTS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/test-reports"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

echo -e "${BLUE}⚡ Starting Performance Testing${NC}"
echo -e "${BLUE}Package: $PACKAGE_NAME${NC}"
echo -e "${BLUE}Duration: $TEST_DURATION seconds${NC}"
echo -e "${BLUE}Sample Interval: $SAMPLE_INTERVAL seconds${NC}"
echo "=================================================="

# Create reports directory
mkdir -p "$REPORTS_DIR/performance-$TIMESTAMP"
PERF_DIR="$REPORTS_DIR/performance-$TIMESTAMP"

# Function to check if app is running
is_app_running() {
    adb shell pidof "$PACKAGE_NAME" >/dev/null 2>&1
}

# Function to get app PID
get_app_pid() {
    adb shell pidof "$PACKAGE_NAME" 2>/dev/null || echo ""
}

# Function to get memory usage
get_memory_usage() {
    local pid=$1
    if [ -n "$pid" ]; then
        # Get PSS memory in KB
        adb shell dumpsys meminfo "$PACKAGE_NAME" | grep "TOTAL PSS:" | awk '{print $3}' | tr -d ','
    else
        echo "0"
    fi
}

# Function to get CPU usage
get_cpu_usage() {
    local pid=$1
    if [ -n "$pid" ]; then
        # Get CPU percentage
        adb shell top -p "$pid" -n 1 | grep "$pid" | awk '{print $9}' | head -1
    else
        echo "0"
    fi
}

# Function to get battery level
get_battery_level() {
    adb shell dumpsys battery | grep "level:" | awk '{print $2}'
}

# Function to get battery temperature
get_battery_temperature() {
    local temp=$(adb shell dumpsys battery | grep "temperature:" | awk '{print $2}')
    # Convert from tenths of degrees to degrees
    echo "scale=1; $temp / 10" | bc -l 2>/dev/null || echo "0"
}

# Function to get network usage
get_network_usage() {
    # Get network stats for the app (simplified)
    local uid=$(adb shell dumpsys package "$PACKAGE_NAME" | grep "userId=" | head -1 | cut -d= -f2)
    if [ -n "$uid" ]; then
        # This is a simplified approach - in practice, you'd parse /proc/net/xt_qtaguid/stats
        echo "0"  # Placeholder
    else
        echo "0"
    fi
}

# Function to monitor performance
monitor_performance() {
    echo -e "${YELLOW}Starting performance monitoring...${NC}"
    
    # Initialize CSV files
    echo "timestamp,memory_kb,cpu_percent,battery_level,battery_temp_c" > "$PERF_DIR/performance_metrics.csv"
    echo "timestamp,event,details" > "$PERF_DIR/performance_events.csv"
    
    local start_time=$(date +%s)
    local end_time=$((start_time + TEST_DURATION))
    local sample_count=0
    
    # Initial battery level
    local initial_battery=$(get_battery_level)
    echo "$(date '+%Y-%m-%d %H:%M:%S'),test_start,initial_battery=$initial_battery" >> "$PERF_DIR/performance_events.csv"
    
    echo -e "${BLUE}Initial battery level: $initial_battery%${NC}"
    
    while [ $(date +%s) -lt $end_time ]; do
        local current_time=$(date +%s)
        local timestamp=$(date '+%Y-%m-%d %H:%M:%S')
        
        # Get app PID
        local pid=$(get_app_pid)
        
        if [ -n "$pid" ] && [ "$pid" != "0" ]; then
            # Get performance metrics
            local memory_kb=$(get_memory_usage "$pid")
            local cpu_percent=$(get_cpu_usage "$pid")
            local battery_level=$(get_battery_level)
            local battery_temp=$(get_battery_temperature)
            
            # Log metrics
            echo "$timestamp,$memory_kb,$cpu_percent,$battery_level,$battery_temp" >> "$PERF_DIR/performance_metrics.csv"
            
            # Display current metrics
            echo -e "${GREEN}[$timestamp] Memory: ${memory_kb}KB, CPU: ${cpu_percent}%, Battery: ${battery_level}%, Temp: ${battery_temp}°C${NC}"
            
            # Check for performance issues
            if [ -n "$memory_kb" ] && [ "$memory_kb" -gt 200000 ]; then  # > 200MB
                echo "$(date '+%Y-%m-%d %H:%M:%S'),high_memory,memory_usage=${memory_kb}KB" >> "$PERF_DIR/performance_events.csv"
                echo -e "${YELLOW}⚠️ High memory usage detected: ${memory_kb}KB${NC}"
            fi
            
            if [ -n "$cpu_percent" ] && [ "${cpu_percent%.*}" -gt 80 ]; then  # > 80%
                echo "$(date '+%Y-%m-%d %H:%M:%S'),high_cpu,cpu_usage=${cpu_percent}%" >> "$PERF_DIR/performance_events.csv"
                echo -e "${YELLOW}⚠️ High CPU usage detected: ${cpu_percent}%${NC}"
            fi
            
            if [ -n "$battery_temp" ] && [ "${battery_temp%.*}" -gt 40 ]; then  # > 40°C
                echo "$(date '+%Y-%m-%d %H:%M:%S'),high_temperature,temperature=${battery_temp}°C" >> "$PERF_DIR/performance_events.csv"
                echo -e "${YELLOW}⚠️ High temperature detected: ${battery_temp}°C${NC}"
            fi
            
        else
            echo -e "${RED}❌ App not running (PID: $pid)${NC}"
            echo "$(date '+%Y-%m-%d %H:%M:%S'),app_not_running,pid=$pid" >> "$PERF_DIR/performance_events.csv"
        fi
        
        sample_count=$((sample_count + 1))
        sleep $SAMPLE_INTERVAL
    done
    
    # Final battery level
    local final_battery=$(get_battery_level)
    local battery_drain=$((initial_battery - final_battery))
    
    echo "$(date '+%Y-%m-%d %H:%M:%S'),test_end,final_battery=$final_battery,drain=$battery_drain" >> "$PERF_DIR/performance_events.csv"
    
    echo -e "${BLUE}Final battery level: $final_battery%${NC}"
    echo -e "${BLUE}Battery drain: $battery_drain%${NC}"
    echo -e "${BLUE}Total samples collected: $sample_count${NC}"
}

# Function to analyze performance data
analyze_performance() {
    echo -e "${YELLOW}Analyzing performance data...${NC}"
    
    if [ ! -f "$PERF_DIR/performance_metrics.csv" ]; then
        echo -e "${RED}❌ No performance data found${NC}"
        return 1
    fi
    
    # Create analysis report
    local analysis_file="$PERF_DIR/performance_analysis.txt"
    
    cat > "$analysis_file" << EOF
Performance Analysis Report
===========================
Test Duration: $TEST_DURATION seconds
Sample Interval: $SAMPLE_INTERVAL seconds
Package: $PACKAGE_NAME
Timestamp: $TIMESTAMP

EOF
    
    # Analyze memory usage
    echo "Memory Usage Analysis:" >> "$analysis_file"
    echo "=====================" >> "$analysis_file"
    
    # Get memory statistics (skip header line)
    local memory_stats=$(tail -n +2 "$PERF_DIR/performance_metrics.csv" | cut -d, -f2 | grep -v "^$" | sort -n)
    
    if [ -n "$memory_stats" ]; then
        local min_memory=$(echo "$memory_stats" | head -1)
        local max_memory=$(echo "$memory_stats" | tail -1)
        local avg_memory=$(echo "$memory_stats" | awk '{sum+=$1} END {print sum/NR}')
        
        echo "Min Memory: ${min_memory}KB" >> "$analysis_file"
        echo "Max Memory: ${max_memory}KB" >> "$analysis_file"
        echo "Avg Memory: ${avg_memory}KB" >> "$analysis_file"
        
        # Memory usage assessment
        if [ "$max_memory" -gt 300000 ]; then
            echo "Status: ❌ HIGH MEMORY USAGE" >> "$analysis_file"
        elif [ "$max_memory" -gt 200000 ]; then
            echo "Status: ⚠️ MODERATE MEMORY USAGE" >> "$analysis_file"
        else
            echo "Status: ✅ GOOD MEMORY USAGE" >> "$analysis_file"
        fi
    else
        echo "No memory data available" >> "$analysis_file"
    fi
    
    echo "" >> "$analysis_file"
    
    # Analyze CPU usage
    echo "CPU Usage Analysis:" >> "$analysis_file"
    echo "==================" >> "$analysis_file"
    
    local cpu_stats=$(tail -n +2 "$PERF_DIR/performance_metrics.csv" | cut -d, -f3 | grep -v "^$" | grep -v "^0$" | sort -n)
    
    if [ -n "$cpu_stats" ]; then
        local min_cpu=$(echo "$cpu_stats" | head -1)
        local max_cpu=$(echo "$cpu_stats" | tail -1)
        local avg_cpu=$(echo "$cpu_stats" | awk '{sum+=$1} END {print sum/NR}')
        
        echo "Min CPU: ${min_cpu}%" >> "$analysis_file"
        echo "Max CPU: ${max_cpu}%" >> "$analysis_file"
        echo "Avg CPU: ${avg_cpu}%" >> "$analysis_file"
        
        # CPU usage assessment
        if [ "${max_cpu%.*}" -gt 80 ]; then
            echo "Status: ❌ HIGH CPU USAGE" >> "$analysis_file"
        elif [ "${max_cpu%.*}" -gt 50 ]; then
            echo "Status: ⚠️ MODERATE CPU USAGE" >> "$analysis_file"
        else
            echo "Status: ✅ GOOD CPU USAGE" >> "$analysis_file"
        fi
    else
        echo "No CPU data available" >> "$analysis_file"
    fi
    
    echo "" >> "$analysis_file"
    
    # Analyze battery drain
    echo "Battery Analysis:" >> "$analysis_file"
    echo "=================" >> "$analysis_file"
    
    local initial_battery=$(grep "test_start" "$PERF_DIR/performance_events.csv" | cut -d= -f2)
    local final_battery=$(grep "test_end" "$PERF_DIR/performance_events.csv" | cut -d= -f2 | cut -d, -f1)
    
    if [ -n "$initial_battery" ] && [ -n "$final_battery" ]; then
        local battery_drain=$((initial_battery - final_battery))
        local drain_rate=$(echo "scale=2; $battery_drain * 3600 / $TEST_DURATION" | bc -l)
        
        echo "Initial Battery: ${initial_battery}%" >> "$analysis_file"
        echo "Final Battery: ${final_battery}%" >> "$analysis_file"
        echo "Battery Drain: ${battery_drain}%" >> "$analysis_file"
        echo "Drain Rate: ${drain_rate}%/hour" >> "$analysis_file"
        
        # Battery drain assessment
        if [ "$battery_drain" -gt 10 ]; then
            echo "Status: ❌ HIGH BATTERY DRAIN" >> "$analysis_file"
        elif [ "$battery_drain" -gt 5 ]; then
            echo "Status: ⚠️ MODERATE BATTERY DRAIN" >> "$analysis_file"
        else
            echo "Status: ✅ GOOD BATTERY EFFICIENCY" >> "$analysis_file"
        fi
    else
        echo "No battery data available" >> "$analysis_file"
    fi
    
    echo "" >> "$analysis_file"
    
    # Performance events summary
    echo "Performance Events:" >> "$analysis_file"
    echo "==================" >> "$analysis_file"
    
    local high_memory_events=$(grep "high_memory" "$PERF_DIR/performance_events.csv" | wc -l)
    local high_cpu_events=$(grep "high_cpu" "$PERF_DIR/performance_events.csv" | wc -l)
    local high_temp_events=$(grep "high_temperature" "$PERF_DIR/performance_events.csv" | wc -l)
    local app_not_running_events=$(grep "app_not_running" "$PERF_DIR/performance_events.csv" | wc -l)
    
    echo "High Memory Events: $high_memory_events" >> "$analysis_file"
    echo "High CPU Events: $high_cpu_events" >> "$analysis_file"
    echo "High Temperature Events: $high_temp_events" >> "$analysis_file"
    echo "App Not Running Events: $app_not_running_events" >> "$analysis_file"
    
    echo "" >> "$analysis_file"
    
    # Overall assessment
    echo "Overall Assessment:" >> "$analysis_file"
    echo "==================" >> "$analysis_file"
    
    local total_issues=$((high_memory_events + high_cpu_events + high_temp_events + app_not_running_events))
    
    if [ "$total_issues" -eq 0 ]; then
        echo "Status: ✅ EXCELLENT PERFORMANCE" >> "$analysis_file"
        echo "No performance issues detected during testing." >> "$analysis_file"
    elif [ "$total_issues" -lt 5 ]; then
        echo "Status: ⚠️ GOOD PERFORMANCE WITH MINOR ISSUES" >> "$analysis_file"
        echo "Minor performance issues detected. Consider optimization." >> "$analysis_file"
    else
        echo "Status: ❌ PERFORMANCE ISSUES DETECTED" >> "$analysis_file"
        echo "Multiple performance issues detected. Optimization required." >> "$analysis_file"
    fi
    
    echo -e "${GREEN}Performance analysis completed${NC}"
    echo -e "${BLUE}Analysis report saved to: $analysis_file${NC}"
    
    # Display summary
    echo ""
    echo -e "${BLUE}PERFORMANCE SUMMARY${NC}"
    echo "======================"
    cat "$analysis_file" | grep "Status:" | while read line; do
        echo -e "${BLUE}$line${NC}"
    done
}

# Function to generate performance charts (requires Python)
generate_charts() {
    echo -e "${YELLOW}Generating performance charts...${NC}"
    
    # Create Python script for chart generation
    cat > "$PERF_DIR/generate_charts.py" << 'EOF'
import csv
import matplotlib.pyplot as plt
import matplotlib.dates as mdates
from datetime import datetime
import sys
import os

def read_performance_data(csv_file):
    timestamps = []
    memory = []
    cpu = []
    battery = []
    temperature = []
    
    try:
        with open(csv_file, 'r') as f:
            reader = csv.DictReader(f)
            for row in reader:
                try:
                    timestamp = datetime.strptime(row['timestamp'], '%Y-%m-%d %H:%M:%S')
                    timestamps.append(timestamp)
                    memory.append(float(row['memory_kb']) / 1024)  # Convert to MB
                    cpu.append(float(row['cpu_percent']) if row['cpu_percent'] else 0)
                    battery.append(float(row['battery_level']) if row['battery_level'] else 0)
                    temperature.append(float(row['battery_temp_c']) if row['battery_temp_c'] else 0)
                except (ValueError, KeyError) as e:
                    continue
    except FileNotFoundError:
        print(f"Error: {csv_file} not found")
        return None, None, None, None, None
    
    return timestamps, memory, cpu, battery, temperature

def create_charts(data_dir):
    csv_file = os.path.join(data_dir, 'performance_metrics.csv')
    timestamps, memory, cpu, battery, temperature = read_performance_data(csv_file)
    
    if not timestamps:
        print("No data to plot")
        return
    
    # Create subplots
    fig, ((ax1, ax2), (ax3, ax4)) = plt.subplots(2, 2, figsize=(15, 10))
    fig.suptitle('Performance Monitoring Results', fontsize=16)
    
    # Memory usage chart
    ax1.plot(timestamps, memory, 'b-', linewidth=2)
    ax1.set_title('Memory Usage Over Time')
    ax1.set_ylabel('Memory (MB)')
    ax1.grid(True, alpha=0.3)
    ax1.xaxis.set_major_formatter(mdates.DateFormatter('%H:%M'))
    
    # CPU usage chart
    ax2.plot(timestamps, cpu, 'r-', linewidth=2)
    ax2.set_title('CPU Usage Over Time')
    ax2.set_ylabel('CPU (%)')
    ax2.grid(True, alpha=0.3)
    ax2.xaxis.set_major_formatter(mdates.DateFormatter('%H:%M'))
    
    # Battery level chart
    ax3.plot(timestamps, battery, 'g-', linewidth=2)
    ax3.set_title('Battery Level Over Time')
    ax3.set_ylabel('Battery (%)')
    ax3.grid(True, alpha=0.3)
    ax3.xaxis.set_major_formatter(mdates.DateFormatter('%H:%M'))
    
    # Temperature chart
    ax4.plot(timestamps, temperature, 'orange', linewidth=2)
    ax4.set_title('Battery Temperature Over Time')
    ax4.set_ylabel('Temperature (°C)')
    ax4.grid(True, alpha=0.3)
    ax4.xaxis.set_major_formatter(mdates.DateFormatter('%H:%M'))
    
    # Adjust layout and save
    plt.tight_layout()
    chart_file = os.path.join(data_dir, 'performance_charts.png')
    plt.savefig(chart_file, dpi=300, bbox_inches='tight')
    print(f"Charts saved to: {chart_file}")

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage: python generate_charts.py <data_directory>")
        sys.exit(1)
    
    create_charts(sys.argv[1])
EOF
    
    # Try to generate charts if Python and matplotlib are available
    if command -v python3 >/dev/null 2>&1; then
        if python3 -c "import matplotlib" >/dev/null 2>&1; then
            python3 "$PERF_DIR/generate_charts.py" "$PERF_DIR"
            if [ $? -eq 0 ]; then
                echo -e "${GREEN}✅ Performance charts generated${NC}"
            else
                echo -e "${YELLOW}⚠️ Chart generation failed${NC}"
            fi
        else
            echo -e "${YELLOW}⚠️ matplotlib not available, skipping chart generation${NC}"
        fi
    else
        echo -e "${YELLOW}⚠️ Python3 not available, skipping chart generation${NC}"
    fi
}

# Main execution
main() {
    # Check if device is connected
    if ! adb devices | grep -q "device$"; then
        echo -e "${RED}❌ No Android device connected${NC}"
        exit 1
    fi
    
    # Check if app is installed
    if ! adb shell pm list packages | grep -q "$PACKAGE_NAME"; then
        echo -e "${RED}❌ App $PACKAGE_NAME is not installed${NC}"
        exit 1
    fi
    
    # Start performance monitoring
    monitor_performance
    
    # Analyze results
    analyze_performance
    
    # Generate charts
    generate_charts
    
    echo ""
    echo -e "${GREEN}Performance testing completed!${NC}"
    echo -e "${BLUE}All reports saved to: $PERF_DIR${NC}"
    echo ""
    echo -e "${BLUE}Generated files:${NC}"
    echo "- performance_metrics.csv (raw data)"
    echo "- performance_events.csv (events log)"
    echo "- performance_analysis.txt (analysis report)"
    echo "- performance_charts.png (charts, if generated)"
}

# Run main function
main "$@"