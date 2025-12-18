# SỬA LỖI LOGIC CUỘC GỌI - BỆNH NHÂN GỌI BÁC SĨ

## Vấn đề
Khi bệnh nhân bấm gọi bác sĩ, cuộc gọi lại hiện trên điện thoại của chính bệnh nhân thay vì gọi đến bác sĩ.

## Nguyên nhân chính (Đã tìm ra!)
**MyApplication.java** tự động mở `IncomingCallActivity` khi nhận callback `onIncomingCall` từ StringeeManager, ngay cả khi đó là cuộc gọi đi từ chính user hiện tại!

### Luồng lỗi:
1. Bệnh nhân bấm gọi bác sĩ
2. `VoiceCallActivity` tạo cuộc gọi từ `patient_xxx` đến `doctor_xxx`
3. Stringee SDK trigger callback `onIncomingCall` (có thể do cùng client hoặc cách SDK hoạt động)
4. `MyApplication.handleIncomingVoiceCall()` được gọi
5. `MyApplication` mở `IncomingCallActivity` → Bệnh nhân thấy cuộc gọi đến!

## Giải pháp đã áp dụng

### 1. Sửa MyApplication.java - Kiểm tra cuộc gọi từ chính mình
```java
private void handleIncomingVoiceCall(StringeeCall call) {
    // 🔥 FIX: Kiểm tra xem có phải cuộc gọi từ chính mình không
    String currentUserId = getCurrentUserId();
    
    if (call.getFrom().equals(currentUserId)) {
        Log.d(TAG, "⚠️ IGNORING: This is an outgoing call from current user, not incoming!");
        return; // Bỏ qua cuộc gọi từ chính mình
    }
    
    // Chỉ xử lý cuộc gọi đến thật sự
    // ... rest of the code
}
```

### 2. Sửa VoiceCallActivity.java
```java
// TRƯỚC (SAI):
String currentCallerId = getCurrentUserId();

// SAU (ĐÚNG):
String currentCallerId = callerId; // Từ Intent
```

### 3. Sửa VideoCallActivity.java
```java
// TRƯỚC (SAI):
String callerId = getCurrentUserId();

// SAU (ĐÚNG):
// Sử dụng callerId đã được set từ Intent trong getDataFromIntent()
```

### 4. Thêm getCurrentUserId() vào MyApplication
- Sử dụng SessionManager để lấy thông tin user hiện tại
- So sánh với `call.getFrom()` để phát hiện cuộc gọi từ chính mình

### 5. Thêm debug logs
- Thêm logs trong tất cả các file để debug call logic
- Logs hiển thị rõ caller, receiver, và current user

## Kết quả
- ✅ Bệnh nhân bấm gọi bác sĩ → Chỉ hiện VoiceCallActivity (outgoing)
- ✅ Bác sĩ nhận cuộc gọi → Hiện IncomingCallActivity (incoming)
- ✅ Không còn hiện cuộc gọi đến trên điện thoại người gọi

## Files đã sửa
1. `app/src/main/java/com/example/doannt118/MyApplication.java` ⭐ **CHÍNH**
2. `app/src/main/java/com/example/doannt118/ui/VoiceCallActivity.java`
3. `app/src/main/java/com/example/doannt118/ui/VideoCallActivity.java`
4. `app/src/main/java/com/example/doannt118/ui/NhanTinBacSiActivity.java`

## Test case
1. Đăng nhập bằng tài khoản bệnh nhân
2. Vào chat với bác sĩ
3. Bấm nút gọi voice/video
4. Kiểm tra logs:
   - `MyApplication` nhận callback với `call.getFrom() = patient_xxx`
   - `getCurrentUserId() = patient_xxx`
   - `MyApplication` bỏ qua cuộc gọi (IGNORING message)
   - Chỉ hiện `VoiceCallActivity` outgoing, không hiện `IncomingCallActivity`