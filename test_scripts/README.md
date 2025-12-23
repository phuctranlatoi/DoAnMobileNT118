# 🧪 Automated Testing Scripts

Hệ thống kiểm thử tự động hoàn chỉnh cho ứng dụng nhắn tin bác sĩ-bệnh nhân.

## Cấu trúc Files

```
test_scripts/
├── README.md                           # Hướng dẫn này
├── run_all_tests.sh                    # Script chính chạy tất cả tests
├── performance_test.sh                 # Kiểm thử hiệu năng
├── ui_automation_test.py               # Kiểm thử UI tự động
├── KE_HOACH_KIEM_THU_HE_THONG.md      # Kế hoạch kiểm thử tổng thể
├── TEST_SCRIPTS_MANUAL.md              # Hướng dẫn kiểm thử thủ công
├── AUTOMATED_TEST_SETUP.md             # Thiết lập kiểm thử tự động
└── PERFORMANCE_TESTING_GUIDE.md        # Hướng dẫn kiểm thử hiệu năng
```

## Cách sử dụng

### 1. Chạy tất cả tests (Khuyến nghị)

```bash
# Chạy full test suite
./test_scripts/run_all_tests.sh

# Chạy với cleanup sau khi test
./test_scripts/run_all_tests.sh --clean

# Bỏ qua instrumented tests (cho CI không có emulator)
./test_scripts/run_all_tests.sh --skip-instrumented
```

### 2. Chạy từng loại test riêng biệt

#### Unit Tests
```bash
./gradlew test
```

#### Instrumented Tests (cần emulator/device)
```bash
./gradlew connectedAndroidTest
```

#### Performance Tests
```bash
./test_scripts/performance_test.sh
```

#### UI Automation Tests
```bash
python3 test_scripts/ui_automation_test.py
```

## Yêu cầu hệ thống

### Cơ bản
- Android SDK với ADB
- Java 11+
- Gradle
- Android device/emulator

### Tùy chọn (cho advanced features)
- Python 3.7+ (cho UI automation)
- matplotlib (cho performance charts)
- bc calculator (cho performance calculations)

## Các loại test được bao gồm

### 1. Unit Tests
- StringeeTokenGenerator tests
- TinNhanBacSi model tests
- Business logic validation

### 2. Instrumented Tests (UI Tests)
- LoginActivity functionality
- NhanTinBacSiActivity chat features
- MainBenhNhanActivity navigation
- Firebase integration tests

### 3. Performance Tests
- Memory usage monitoring
- CPU usage tracking
- Battery consumption analysis
- Network performance metrics

### 4. UI Automation Tests
- App launch verification
- Login flow automation
- Navigation testing
- Chat functionality testing
- Stability testing

## Test Reports

Tất cả test reports được lưu trong thư mục `test-reports/` với timestamp:

```
test-reports/
├── unit-tests-20241222_143022/
├── instrumented-tests-20241222_143022/
├── performance-20241222_143022/
├── ui-automation-20241222_143022/
└── test-summary-20241222_143022.txt
```

### Các file report chính:
- **HTML Reports**: Chi tiết kết quả tests
- **Coverage Reports**: Code coverage analysis
- **Performance Charts**: Biểu đồ hiệu năng
- **Screenshots**: Ảnh chụp màn hình UI tests
- **Summary Reports**: Tóm tắt tổng thể

## Thiết lập môi trường

### 1. Chuẩn bị Android Device/Emulator

```bash
# Kiểm tra device kết nối
adb devices

# Nếu cần tạo emulator mới
avdmanager create avd -n test_device -k "system-images;android-30;google_apis;x86_64"

# Khởi động emulator
emulator -avd test_device -no-window -no-audio
```

### 2. Cài đặt dependencies

```bash
# Cài đặt Python dependencies (tùy chọn)
pip3 install matplotlib

# Cài đặt bc calculator (macOS)
brew install bc

# Hoặc trên Ubuntu/Debian
sudo apt-get install bc
```

### 3. Cấu hình permissions

```bash
# Đảm bảo scripts có quyền execute
chmod +x test_scripts/*.sh
chmod +x test_scripts/*.py
```

## Chạy tests trong Development

### Workflow hàng ngày:
```bash
# 1. Chạy unit tests nhanh
./gradlew test

# 2. Nếu unit tests pass, chạy full suite
./test_scripts/run_all_tests.sh

# 3. Xem kết quả trong test-reports/
```

### Trước khi commit:
```bash
# Chạy full test suite với cleanup
./test_scripts/run_all_tests.sh --clean
```

### CI/CD Pipeline:
```bash
# Chạy tests không cần emulator
./test_scripts/run_all_tests.sh --skip-instrumented
```

## Performance Benchmarks

### Tiêu chuẩn PASS:
- **Memory**: < 200MB peak usage
- **CPU**: < 60% average usage
- **Battery**: < 15%/hour drain rate
- **Network**: < 2s message latency
- **UI**: > 55fps, < 5% dropped frames

### Cách đọc Performance Reports:
1. Mở `performance-*/performance_analysis.txt`
2. Kiểm tra "Overall Assessment"
3. Xem charts trong `performance_charts.png`
4. Review events trong `performance_events.csv`

## Troubleshooting

### Lỗi thường gặp:

#### "No device connected"
```bash
# Kiểm tra ADB
adb devices

# Restart ADB nếu cần
adb kill-server
adb start-server
```

#### "App not installed"
```bash
# Build và install app
./gradlew installDebug
```

#### "Permission denied"
```bash
# Fix permissions
chmod +x test_scripts/*.sh
chmod +x test_scripts/*.py
```

#### "Python not found"
```bash
# Install Python 3
brew install python3  # macOS
sudo apt install python3  # Ubuntu
```

#### Tests fail với "Resource not found"
- Kiểm tra resource IDs trong test files
- Cập nhật IDs theo layout thực tế
- Chạy `./gradlew clean build` trước khi test

## 🔄 Continuous Integration

### GitHub Actions Example:
```yaml
name: Automated Tests
on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    - name: Setup JDK
      uses: actions/setup-java@v3
      with:
        java-version: '11'
    - name: Run Tests
      run: ./test_scripts/run_all_tests.sh --skip-instrumented
```

## 📚 Tài liệu tham khảo

- 📖 [Kế hoạch kiểm thử tổng thể](KE_HOACH_KIEM_THU_HE_THONG.md)
- 📖 [Hướng dẫn kiểm thử thủ công](TEST_SCRIPTS_MANUAL.md)
- 📖 [Thiết lập kiểm thử tự động](AUTOMATED_TEST_SETUP.md)
- 📖 [Hướng dẫn kiểm thử hiệu năng](PERFORMANCE_TESTING_GUIDE.md)

## 🎯 Best Practices

### 1. Chạy tests thường xuyên
- Unit tests: Mỗi khi code
- Full suite: Trước khi commit
- Performance tests: Hàng tuần

### 2. Phân tích kết quả
- Luôn xem test reports
- Fix failed tests ngay lập tức
- Monitor performance trends

### 3. Maintain test code
- Update tests khi UI thay đổi
- Add tests cho features mới
- Remove obsolete tests

### 4. Environment consistency
- Sử dụng cùng Android version
- Consistent device specs
- Clean state trước mỗi test run

## 🆘 Hỗ trợ

Nếu gặp vấn đề:
1. Kiểm tra [Troubleshooting](#-troubleshooting)
2. Xem logs trong `test-reports/`
3. Verify environment setup
4. Check device/emulator status

---

**🎉 Happy Testing! Chúc bạn kiểm thử thành công!**