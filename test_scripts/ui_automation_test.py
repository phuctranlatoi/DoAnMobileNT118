#!/usr/bin/env python3
"""
🤖 UI Automation Test Script
Advanced UI testing using Python and ADB commands
"""

import subprocess
import time
import json
import os
import sys
from datetime import datetime
from typing import List, Dict, Optional, Tuple

class UIAutomationTester:
    def __init__(self, package_name: str = "com.example.doannt118"):
        self.package_name = package_name
        self.device_id = None
        self.test_results = []
        self.screenshots_dir = None
        self.setup_directories()
    
    def setup_directories(self):
        """Setup test directories"""
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        base_dir = os.path.join(os.path.dirname(__file__), "..", "test-reports")
        self.test_dir = os.path.join(base_dir, f"ui-automation-{timestamp}")
        self.screenshots_dir = os.path.join(self.test_dir, "screenshots")
        
        os.makedirs(self.test_dir, exist_ok=True)
        os.makedirs(self.screenshots_dir, exist_ok=True)
        
        print(f"Test directory: {self.test_dir}")
    
    def run_adb_command(self, command: str) -> Tuple[str, int]:
        """Run ADB command and return output and exit code"""
        try:
            result = subprocess.run(
                f"adb {command}",
                shell=True,
                capture_output=True,
                text=True,
                timeout=30
            )
            return result.stdout.strip(), result.returncode
        except subprocess.TimeoutExpired:
            return "", 1
        except Exception as e:
            print(f"❌ ADB command failed: {e}")
            return "", 1
    
    def check_device_connection(self) -> bool:
        """Check if Android device is connected"""
        output, code = self.run_adb_command("devices")
        if code != 0:
            return False
        
        devices = [line for line in output.split('\n') if '\tdevice' in line]
        if not devices:
            print("❌ No Android device connected")
            return False
        
        self.device_id = devices[0].split('\t')[0]
        print(f"✅ Connected to device: {self.device_id}")
        return True
    
    def is_app_installed(self) -> bool:
        """Check if the app is installed"""
        output, code = self.run_adb_command(f"shell pm list packages | grep {self.package_name}")
        return code == 0 and self.package_name in output
    
    def launch_app(self) -> bool:
        """Launch the app"""
        print("Launching app...")
        output, code = self.run_adb_command(f"shell monkey -p {self.package_name} -c android.intent.category.LAUNCHER 1")
        time.sleep(3)  # Wait for app to launch
        return code == 0
    
    def take_screenshot(self, name: str) -> str:
        """Take screenshot and save it"""
        timestamp = datetime.now().strftime("%H%M%S")
        filename = f"{name}_{timestamp}.png"
        filepath = os.path.join(self.screenshots_dir, filename)
        
        # Take screenshot on device
        self.run_adb_command("shell screencap -p /sdcard/screenshot.png")
        
        # Pull screenshot to local
        output, code = self.run_adb_command(f"pull /sdcard/screenshot.png {filepath}")
        
        if code == 0:
            print(f"Screenshot saved: {filename}")
            return filepath
        else:
            print(f"❌ Failed to take screenshot: {name}")
            return ""
    
    def tap_coordinates(self, x: int, y: int) -> bool:
        """Tap at specific coordinates"""
        output, code = self.run_adb_command(f"shell input tap {x} {y}")
        time.sleep(1)  # Wait for tap to register
        return code == 0
    
    def input_text(self, text: str) -> bool:
        """Input text (requires focused text field)"""
        # Escape special characters
        escaped_text = text.replace(' ', '%s').replace('&', '\\&')
        output, code = self.run_adb_command(f"shell input text '{escaped_text}'")
        time.sleep(1)
        return code == 0
    
    def press_key(self, keycode: int) -> bool:
        """Press a key (e.g., KEYCODE_BACK = 4, KEYCODE_HOME = 3)"""
        output, code = self.run_adb_command(f"shell input keyevent {keycode}")
        time.sleep(1)
        return code == 0
    
    def swipe(self, x1: int, y1: int, x2: int, y2: int, duration: int = 300) -> bool:
        """Swipe from (x1,y1) to (x2,y2)"""
        output, code = self.run_adb_command(f"shell input swipe {x1} {y1} {x2} {y2} {duration}")
        time.sleep(1)
        return code == 0
    
    def get_screen_size(self) -> Tuple[int, int]:
        """Get screen dimensions"""
        output, code = self.run_adb_command("shell wm size")
        if code == 0 and "Physical size:" in output:
            size_str = output.split("Physical size: ")[1]
            width, height = map(int, size_str.split('x'))
            return width, height
        return 1080, 1920  # Default fallback
    
    def find_text_on_screen(self, text: str) -> bool:
        """Check if text exists on current screen using UI dump"""
        output, code = self.run_adb_command("shell uiautomator dump /sdcard/ui_dump.xml")
        if code != 0:
            return False
        
        output, code = self.run_adb_command("shell cat /sdcard/ui_dump.xml")
        return code == 0 and text in output
    
    def wait_for_text(self, text: str, timeout: int = 10) -> bool:
        """Wait for text to appear on screen"""
        start_time = time.time()
        while time.time() - start_time < timeout:
            if self.find_text_on_screen(text):
                return True
            time.sleep(1)
        return False
    
    def record_test_result(self, test_name: str, passed: bool, details: str = ""):
        """Record test result"""
        result = {
            "test_name": test_name,
            "passed": passed,
            "timestamp": datetime.now().isoformat(),
            "details": details
        }
        self.test_results.append(result)
        
        status = "✅ PASS" if passed else "❌ FAIL"
        print(f"{status} {test_name}")
        if details:
            print(f"   Details: {details}")
    
    def test_app_launch(self) -> bool:
        """Test: App launches successfully"""
        print("\n🧪 Testing app launch...")
        
        success = self.launch_app()
        screenshot = self.take_screenshot("app_launch")
        
        # Check if app is running
        output, code = self.run_adb_command(f"shell pidof {self.package_name}")
        app_running = code == 0 and output.strip() != ""
        
        self.record_test_result(
            "App Launch",
            success and app_running,
            f"App PID: {output.strip() if app_running else 'Not running'}"
        )
        
        return success and app_running
    
    def test_login_screen(self) -> bool:
        """Test: Login screen elements are present"""
        print("\n🧪 Testing login screen...")
        
        time.sleep(2)  # Wait for screen to load
        screenshot = self.take_screenshot("login_screen")
        
        # Check for login elements (adjust based on your UI)
        elements_found = 0
        expected_elements = ["Bệnh nhân", "Bác sĩ", "Đăng nhập"]
        
        for element in expected_elements:
            if self.find_text_on_screen(element):
                elements_found += 1
                print(f"   ✅ Found: {element}")
            else:
                print(f"   ❌ Missing: {element}")
        
        success = elements_found >= 2  # At least 2 out of 3 elements
        
        self.record_test_result(
            "Login Screen Elements",
            success,
            f"Found {elements_found}/{len(expected_elements)} elements"
        )
        
        return success
    
    def test_patient_login_flow(self) -> bool:
        """Test: Patient login flow"""
        print("\n🧪 Testing patient login flow...")
        
        width, height = self.get_screen_size()
        
        # Take initial screenshot
        self.take_screenshot("before_patient_login")
        
        # Try to find and tap "Bệnh nhân" radio button
        # These coordinates are estimates - adjust based on your layout
        patient_radio_x = width // 4
        patient_radio_y = height // 3
        
        print("   Tapping patient radio button...")
        self.tap_coordinates(patient_radio_x, patient_radio_y)
        time.sleep(1)
        
        # Try to find phone input field
        phone_input_x = width // 2
        phone_input_y = height // 2
        
        print("   Tapping phone input field...")
        self.tap_coordinates(phone_input_x, phone_input_y)
        time.sleep(1)
        
        # Input test phone number
        test_phone = "0123456789"
        print(f"   Entering phone number: {test_phone}")
        self.input_text(test_phone)
        
        # Try to find password input field
        password_input_x = width // 2
        password_input_y = height // 2 + 100
        
        print("   Tapping password input field...")
        self.tap_coordinates(password_input_x, password_input_y)
        time.sleep(1)
        
        # Input test password
        test_password = "test123"
        print(f"   Entering password: {test_password}")
        self.input_text(test_password)
        
        # Take screenshot after filling form
        self.take_screenshot("patient_login_filled")
        
        # Try to find and tap login button
        login_button_x = width // 2
        login_button_y = height // 2 + 200
        
        print("   Tapping login button...")
        self.tap_coordinates(login_button_x, login_button_y)
        
        # Wait for response (either success or error)
        time.sleep(3)
        self.take_screenshot("patient_login_result")
        
        # Check if login was successful (look for main screen elements)
        success = (self.find_text_on_screen("Xin chào") or 
                  self.find_text_on_screen("Trang chủ") or
                  self.find_text_on_screen("Chat"))
        
        self.record_test_result(
            "Patient Login Flow",
            success,
            "Login form filled and submitted"
        )
        
        return success
    
    def test_navigation_elements(self) -> bool:
        """Test: Navigation elements are present"""
        print("\n🧪 Testing navigation elements...")
        
        self.take_screenshot("navigation_test")
        
        # Check for navigation elements
        nav_elements = ["Trang chủ", "Lịch hẹn", "Nhắn tin", "Hồ sơ"]
        elements_found = 0
        
        for element in nav_elements:
            if self.find_text_on_screen(element):
                elements_found += 1
                print(f"   ✅ Found navigation: {element}")
            else:
                print(f"   ❌ Missing navigation: {element}")
        
        success = elements_found >= 2
        
        self.record_test_result(
            "Navigation Elements",
            success,
            f"Found {elements_found}/{len(nav_elements)} navigation elements"
        )
        
        return success
    
    def test_chat_functionality(self) -> bool:
        """Test: Chat functionality"""
        print("\n🧪 Testing chat functionality...")
        
        width, height = self.get_screen_size()
        
        # Try to navigate to chat
        if self.find_text_on_screen("Nhắn tin"):
            # Find and tap messages/chat button
            messages_x = width // 2
            messages_y = height - 200  # Bottom navigation area
            
            print("   Tapping messages navigation...")
            self.tap_coordinates(messages_x, messages_y)
            time.sleep(2)
            
            self.take_screenshot("chat_navigation")
            
            # Check if chat screen opened
            chat_opened = (self.find_text_on_screen("Chat") or 
                          self.find_text_on_screen("Chọn bác sĩ") or
                          self.find_text_on_screen("Tin nhắn"))
            
            if chat_opened:
                print("   ✅ Chat screen opened")
                
                # Try to test message input if available
                if self.find_text_on_screen("Nhập tin nhắn") or self.find_text_on_screen("Gửi"):
                    # Try to find message input field
                    input_x = width // 2
                    input_y = height - 100
                    
                    print("   Testing message input...")
                    self.tap_coordinates(input_x, input_y)
                    time.sleep(1)
                    
                    test_message = "Hello test message"
                    self.input_text(test_message)
                    
                    self.take_screenshot("message_input_test")
                    
                    success = True
                else:
                    success = True  # Chat screen opened but no input field visible
            else:
                print("   ❌ Chat screen did not open")
                success = False
        else:
            print("   ❌ Messages navigation not found")
            success = False
        
        self.record_test_result(
            "Chat Functionality",
            success,
            "Chat navigation and basic functionality tested"
        )
        
        return success
    
    def test_app_stability(self) -> bool:
        """Test: App stability during navigation"""
        print("\n🧪 Testing app stability...")
        
        width, height = self.get_screen_size()
        
        # Perform various navigation actions
        actions = [
            ("Swipe up", lambda: self.swipe(width//2, height//2, width//2, height//4)),
            ("Swipe down", lambda: self.swipe(width//2, height//4, width//2, height//2)),
            ("Tap center", lambda: self.tap_coordinates(width//2, height//2)),
            ("Back button", lambda: self.press_key(4)),  # KEYCODE_BACK
        ]
        
        crashes = 0
        for action_name, action_func in actions:
            print(f"   Performing: {action_name}")
            
            # Check if app is still running before action
            output, code = self.run_adb_command(f"shell pidof {self.package_name}")
            if code != 0 or not output.strip():
                crashes += 1
                print(f"   ❌ App crashed before {action_name}")
                continue
            
            # Perform action
            action_func()
            time.sleep(2)
            
            # Check if app is still running after action
            output, code = self.run_adb_command(f"shell pidof {self.package_name}")
            if code != 0 or not output.strip():
                crashes += 1
                print(f"   ❌ App crashed after {action_name}")
            else:
                print(f"   ✅ App stable after {action_name}")
        
        self.take_screenshot("stability_test_end")
        
        success = crashes == 0
        
        self.record_test_result(
            "App Stability",
            success,
            f"Crashes detected: {crashes}/{len(actions)} actions"
        )
        
        return success
    
    def test_memory_usage(self) -> bool:
        """Test: Memory usage is reasonable"""
        print("\n🧪 Testing memory usage...")
        
        # Get memory usage
        output, code = self.run_adb_command(f"shell dumpsys meminfo {self.package_name}")
        
        if code == 0:
            # Parse memory info
            lines = output.split('\n')
            pss_total = 0
            
            for line in lines:
                if 'TOTAL PSS:' in line:
                    try:
                        pss_total = int(line.split()[2].replace(',', ''))
                        break
                    except (IndexError, ValueError):
                        continue
            
            memory_mb = pss_total / 1024 if pss_total > 0 else 0
            
            # Consider memory usage reasonable if < 200MB
            success = memory_mb < 200 and memory_mb > 0
            
            print(f"   Memory usage: {memory_mb:.1f} MB")
            
            self.record_test_result(
                "Memory Usage",
                success,
                f"PSS Total: {memory_mb:.1f} MB"
            )
        else:
            print("   ❌ Could not get memory info")
            self.record_test_result(
                "Memory Usage",
                False,
                "Could not retrieve memory information"
            )
            success = False
        
        return success
    
    def run_all_tests(self) -> Dict:
        """Run all UI automation tests"""
        print("🤖 Starting UI Automation Tests")
        print("=" * 50)
        
        # Check prerequisites
        if not self.check_device_connection():
            return {"error": "No device connected"}
        
        if not self.is_app_installed():
            print(f"❌ App {self.package_name} is not installed")
            return {"error": "App not installed"}
        
        # Run tests
        test_functions = [
            self.test_app_launch,
            self.test_login_screen,
            self.test_patient_login_flow,
            self.test_navigation_elements,
            self.test_chat_functionality,
            self.test_app_stability,
            self.test_memory_usage,
        ]
        
        for test_func in test_functions:
            try:
                test_func()
            except Exception as e:
                print(f"❌ Test {test_func.__name__} failed with exception: {e}")
                self.record_test_result(
                    test_func.__name__.replace('test_', '').replace('_', ' ').title(),
                    False,
                    f"Exception: {str(e)}"
                )
        
        # Generate report
        return self.generate_report()
    
    def generate_report(self) -> Dict:
        """Generate test report"""
        print("\nGenerating test report...")
        
        total_tests = len(self.test_results)
        passed_tests = sum(1 for result in self.test_results if result['passed'])
        failed_tests = total_tests - passed_tests
        pass_rate = (passed_tests / total_tests * 100) if total_tests > 0 else 0
        
        report = {
            "timestamp": datetime.now().isoformat(),
            "package_name": self.package_name,
            "device_id": self.device_id,
            "total_tests": total_tests,
            "passed_tests": passed_tests,
            "failed_tests": failed_tests,
            "pass_rate": pass_rate,
            "test_results": self.test_results,
            "screenshots_dir": self.screenshots_dir
        }
        
        # Save report to JSON
        report_file = os.path.join(self.test_dir, "ui_automation_report.json")
        with open(report_file, 'w', encoding='utf-8') as f:
            json.dump(report, f, indent=2, ensure_ascii=False)
        
        # Save human-readable report
        readable_report = os.path.join(self.test_dir, "ui_automation_report.txt")
        with open(readable_report, 'w', encoding='utf-8') as f:
            f.write("UI Automation Test Report\n")
            f.write("=" * 30 + "\n\n")
            f.write(f"Timestamp: {report['timestamp']}\n")
            f.write(f"Package: {report['package_name']}\n")
            f.write(f"Device: {report['device_id']}\n\n")
            f.write(f"Total Tests: {total_tests}\n")
            f.write(f"Passed: {passed_tests}\n")
            f.write(f"Failed: {failed_tests}\n")
            f.write(f"Pass Rate: {pass_rate:.1f}%\n\n")
            
            f.write("Test Results:\n")
            f.write("-" * 20 + "\n")
            for result in self.test_results:
                status = "PASS" if result['passed'] else "FAIL"
                f.write(f"{status}: {result['test_name']}\n")
                if result['details']:
                    f.write(f"   Details: {result['details']}\n")
                f.write(f"   Time: {result['timestamp']}\n\n")
        
        # Print summary
        print(f"\nUI AUTOMATION TEST SUMMARY")
        print("=" * 40)
        print(f"Total Tests: {total_tests}")
        print(f"Passed: {passed_tests}")
        print(f"Failed: {failed_tests}")
        print(f"Pass Rate: {pass_rate:.1f}%")
        print(f"\nReports saved to: {self.test_dir}")
        
        if pass_rate >= 80:
            print("Great job! Most tests passed.")
        elif pass_rate >= 60:
            print("Good progress, some issues to address.")
        else:
            print("Many tests failed, needs attention.")
        
        return report

def main():
    """Main function"""
    if len(sys.argv) > 1:
        package_name = sys.argv[1]
    else:
        package_name = "com.example.doannt118"
    
    print(f"🤖 UI Automation Tester")
    print(f"Package: {package_name}")
    print("=" * 50)
    
    tester = UIAutomationTester(package_name)
    report = tester.run_all_tests()
    
    if "error" in report:
        print(f"❌ Test execution failed: {report['error']}")
        sys.exit(1)
    
    # Exit with appropriate code
    if report['pass_rate'] >= 80:
        sys.exit(0)  # Success
    else:
        sys.exit(1)  # Some tests failed

if __name__ == "__main__":
    main()