# 🔧 STRINGEE DEBUG ENHANCED - GIẢI QUYẾT LỖI KẾT NỐI

## 🚨 **Vấn đề hiện tại:**
- Ứng dụng vẫn báo "lỗi kết nối server" với Stringee
- Cần debug chi tiết để tìm nguyên nhân chính xác

## 🔍 **Cải tiến Debug được thêm:**

### 1. **Enhanced Logging System**
- ✅ Log chi tiết từng bước tạo JWT token
- ✅ Log API keys và validation
- ✅ Log từng bước tạo signature
- ✅ Log connection attempts và responses
- ✅ Log user ID generation process

### 2. **Detailed JWT Token Generation**
```java
// Bây giờ sẽ log:
🚀 === BẮT ĐẦU TẠO JWT TOKEN ===
🔑 UserId: patient_123
🔑 SID Key: SK.0.uHNIGYBHHRcU5J0hjrSSky4nzdXvAbso
🔑 Secret Key: TnNsaE1UZWJRRXJxUDZnMWdMMTYxaUdRdEszbnpwYkY=
📋 JWT Header: {"typ":"JWT","alg":"HS256","cty":"stringee-api;v=1"}
📋 JWT Payload: {"jti":"SK.0.uHNIGYBHHRcU5J0hjrSSky4nzdXvAbso-1234567890","iss":"SK.0.uHNIGYBHHRcU5J0hjrSSky4nzdXvAbso","exp":1234571490,"userId":"patient_123"}
🔤 Encoded Header: eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiIsImN0eSI6InN0cmluZ2VlLWFwaTt2PTEifQ
🔤 Encoded Payload: eyJqdGkiOiJTSy4wLnVITklHWUJISFJjVTVKMGhqclNTa3k0bnpkWHZBYnNvLTEyMzQ1Njc4OTAiLCJpc3MiOiJTSy4wLnVITklHWUJISFJjVTVKMGhqclNTa3k0bnpkWHZBYnNvIiwiZXhwIjoxMjM0NTcxNDkwLCJ1c2VySWQiOiJwYXRpZW50XzEyMyJ9
✍️ Signature: abc123def456...
✅ JWT Token: eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiIsImN0eSI6InN0cmluZ2VlLWFwaTt2PTEifQ.eyJqdGkiOiJTSy4wLnVITklHWUJISFJjVTVKMGhqclNTa3k0bnpkWHZBYnNvLTEyMzQ1Njc4OTAiLCJpc3MiOiJTSy4wLnVITklHWUJISFJjVTVKMGhqclNTa3k0bnpkWHZBYnNvIiwiZXhwIjoxMjM0NTcxNDkwLCJ1c2VySWQiOiJwYXRpZW50XzEyMyJ9.abc123def456...
```

### 3. **Signature Creation Debug**
```java
🔐 === BẮT ĐẦU TẠO SIGNATURE ===
🔐 Data to sign: eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiIsImN0eSI6InN0cmluZ2VlLWFwaTt2PTEifQ.eyJqdGkiOiJTSy4wLnVITklHWUJISFJjVTVKMGhqclNTa3k0bnpkWHZBYnNvLTEyMzQ1Njc4OTAiLCJpc3MiOiJTSy4wLnVITklHWUJISFJjVTVKMGhqclNTa3k0bnpkWHZBYnNvIiwiZXhwIjoxMjM0NTcxNDkwLCJ1c2VySWQiOiJwYXRpZW50XzEyMyJ9
🔐 ✅ Secret decoded successfully, bytes length: 32
🔐 ✅ Mac instance created
🔐 ✅ Mac initialized with secret key
🔐 ✅ Signature bytes created, length: 32
🔐 ✅ Final signature: abc123def456...
```

### 4. **Connection Process Debug**
```java
🚀 === BẮT ĐẦU KẾT NỐI STRINGEE ===
🆔 Connecting with userId: patient_123
🌐 Configured Stringee hosts: v1.stringee.com, v2.stringee.com
🌐 Connecting to Stringee with token...
```

### 5. **Test Connection Method**
- ✅ Thêm method `testConnection()` để test với token đơn giản
- ✅ Tự động chạy test connection sau 2 giây
- ✅ Giúp xác định vấn đề là ở token hay connection

## 📱 **Cách sử dụng Debug:**

### Bước 1: Chạy ứng dụng và mở chat
- Vào màn hình chat giữa bệnh nhân và bác sĩ
- Stringee sẽ tự động thử kết nối

### Bước 2: Kiểm tra Logcat
Tìm các log với tag:
- `StringeeManager`: Connection process
- `StringeeTokenGenerator`: JWT token creation
- `NhanTinBacSi`: UI connection status

### Bước 3: Phân tích lỗi
Dựa vào logs, bạn sẽ thấy chính xác lỗi ở đâu:

**Nếu lỗi ở JWT Token:**
```
❌ Failed to create signature - signature is null or empty
❌ Invalid JWT format - should have 3 parts, got: 2
```

**Nếu lỗi ở API Keys:**
```
❌ SID Key is null or empty!
❌ Secret Key is not valid base64
❌ Invalid API keys configuration
```

**Nếu lỗi ở Connection:**
```
❌ Connection error: Authentication failed
❌ Error code: -1
❌ Network error - Kiểm tra kết nối internet
```

## 🔧 **API Keys hiện tại:**
```java
SID Key: "SK.0.uHNIGYBHHRcU5J0hjrSSky4nzdXvAbso"
Secret Key: "TnNsaE1UZWJRRXJxUDZnMWdMMTYxaUdRdEszbnpwYkY="
```

## 🎯 **Kết quả mong đợi:**

**Khi thành công:**
```
🎉 Stringee connected successfully!
✅ Đã kết nối server thành công!
```

**Khi thất bại:**
```
❌ Lỗi kết nối: [chi tiết lỗi cụ thể]
```

## 🚀 **Hướng dẫn test:**

1. **Mở ứng dụng** và vào chat
2. **Kiểm tra Logcat** để xem logs chi tiết
3. **Tìm dòng lỗi** đầu tiên xuất hiện
4. **Báo cáo lỗi** cụ thể để tôi có thể sửa

---

**Trạng thái**: 🔍 **READY FOR DETAILED DEBUGGING**  
**Mục tiêu**: Tìm nguyên nhân chính xác của lỗi kết nối Stringee  
**Phương pháp**: Comprehensive logging và step-by-step analysis