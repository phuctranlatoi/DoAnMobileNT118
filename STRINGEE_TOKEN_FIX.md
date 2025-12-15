# 🔧 STRINGEE TOKEN FIX - GIẢI QUYẾT LỖI "INVALID SIGNATURE"

## 🚨 **Vấn đề hiện tại:**
- Lỗi: "invalid signature" khi kết nối Stringee
- Nguyên nhân: JWT token được tạo không đúng format hoặc signature không khớp

## 🎯 **GIẢI PHÁP NHANH NHẤT - Sử dụng Token từ Stringee Dashboard:**

### **Bước 1: Tạo token từ Stringee Dashboard**

1. Truy cập: https://developer.stringee.com/
2. Đăng nhập vào tài khoản Stringee
3. Vào **Project** của bạn
4. Click **Tools** → **Generate Access Token**
5. Nhập **userId**: `test_user` (hoặc bất kỳ userId nào)
6. Click **Generate**
7. **Copy token** được tạo ra

### **Bước 2: Paste token vào code**

Mở file `StringeeTokenGenerator.java` và thay đổi dòng:

```java
private static final String HARDCODED_TEST_TOKEN = null;
```

Thành:

```java
private static final String HARDCODED_TEST_TOKEN = "YOUR_TOKEN_FROM_DASHBOARD_HERE";
```

### **Bước 3: Test lại app**

- Build và chạy app
- Vào chat và thử kết nối
- Nếu kết nối thành công → Token từ Dashboard hoạt động
- Điều này xác nhận API keys đúng, chỉ có vấn đề với cách tạo token

## 🔍 **KIỂM TRA API KEYS:**

Bạn đã cung cấp:
```
SID Key: SK.0.uHNIGYBHHRcU5J0hjrSSky4nzdXvAbso
Secret Key: TnNsaE1UZWJRRXJxUDZnMWdMMTYxaUdRdEszbnpwYkY=
```

### **Xác nhận API Keys đúng:**

1. Vào Stringee Dashboard → Project Settings
2. Kiểm tra **API Key SID** có đúng là `SK.0.uHNIGYBHHRcU5J0hjrSSky4nzdXvAbso` không
3. Kiểm tra **API Key Secret** có đúng là `TnNsaE1UZWJRRXJxUDZnMWdMMTYxaUdRdEszbnpwYkY=` không

### **Lưu ý quan trọng:**
- SID Key phải bắt đầu bằng `SK.`
- Secret Key thường là base64 encoded string
- Cả hai phải thuộc cùng một Project

## 🛠️ **CẬP NHẬT ĐÃ THỰC HIỆN:**

### 1. **Thử nhiều cách tạo signature:**
```java
// Cách 1: Sử dụng secret key trực tiếp (raw string)
byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);

// Cách 2: Decode secret từ base64
byte[] decodedSecretBytes = Base64.decode(secret, Base64.DEFAULT);
```

### 2. **Thêm option hardcoded token:**
```java
// Nếu có hardcoded token, sử dụng nó để test
if (HARDCODED_TEST_TOKEN != null && !HARDCODED_TEST_TOKEN.isEmpty()) {
    return HARDCODED_TEST_TOKEN;
}
```

## 📱 **CÁCH TEST:**

### **Test 1: Với Hardcoded Token**
1. Tạo token từ Stringee Dashboard
2. Paste vào `HARDCODED_TEST_TOKEN`
3. Build và test
4. Nếu thành công → API keys đúng

### **Test 2: Kiểm tra Logs**
1. Mở Logcat trong Android Studio
2. Filter với tag: `StringeeTokenGenerator`
3. Xem logs chi tiết về quá trình tạo token
4. Tìm dòng lỗi cụ thể

## 🎯 **KẾT QUẢ MONG ĐỢI:**

**Khi thành công:**
```
🎉 Stringee connected successfully!
✅ Đã kết nối server thành công!
```

**Khi thất bại:**
```
❌ Connection error: invalid signature
```

## 📋 **CHECKLIST:**

- [ ] Kiểm tra SID Key đúng format (bắt đầu bằng `SK.`)
- [ ] Kiểm tra Secret Key đúng
- [ ] Thử với token từ Stringee Dashboard
- [ ] Kiểm tra logs để xem chi tiết lỗi
- [ ] Đảm bảo internet hoạt động

## 🔥 **HÀNH ĐỘNG TIẾP THEO:**

1. **Tạo token từ Stringee Dashboard** và test
2. **Nếu thành công**: Vấn đề là cách tạo token trong code
3. **Nếu thất bại**: Kiểm tra lại API keys hoặc project settings

---

**Hãy thử với token từ Dashboard trước để xác định vấn đề chính xác!**