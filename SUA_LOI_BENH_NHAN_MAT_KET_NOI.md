# SỬA LỖI BỆNH NHÂN MẤT KẾT NỐI - STRINGEE USERID MISMATCH

## 🚨 VẤN ĐỀ PHÁT HIỆN
- **Bác sĩ**: Kết nối Stringee nhanh và ổn định ✅
- **Bệnh nhân**: Liên tục mất kết nối, lỗi "chưa kết nối được với server" ❌

## 🔍 NGUYÊN NHÂN GỐC RỄ
**MISMATCH GIỮA SESSIONMANAGER VÀ STRINGEEMANAGER:**

### SessionManager (Đúng):
```java
// Lưu thông tin đăng nhập
private static final String PREF_NAME = "UserSession";
private static final String KEY_MA_TAI_KHOAN = "maTaiKhoan";
private static final String KEY_VAI_TRO = "vaiTro";

public void createLoginSession(String maTaiKhoan, String vaiTro, String email, String hoTen) {
    editor.putString(KEY_MA_TAI_KHOAN, maTaiKhoan);
    editor.putString(KEY_VAI_TRO, vaiTro);
    // ...
}
```

### StringeeManager (SAI):
```java
// Tìm sai key trong sai SharedPreferences
SharedPreferences prefs = context.getSharedPreferences("user_info", Context.MODE_PRIVATE);
String maBenhNhan = prefs.getString("maBenhNhan", ""); // ❌ KHÔNG TỒN TẠI
String maBacSi = prefs.getString("maBacSi", "");       // ❌ KHÔNG TỒN TẠI
```

**KẾT QUẢ:**
- Bệnh nhân không có `maBenhNhan` → fallback userId → token không ổn định
- Bác sĩ có thể có `maBacSi` từ nơi khác → hoạt động tốt hơn

## ✅ GIẢI PHÁP TRIỆT ĐỂ

### 1. Sửa `getCurrentUserId()` trong StringeeManager
```java
private String getCurrentUserId() {
    // 🔥 FIX: Sử dụng SessionManager thay vì trực tiếp SharedPreferences
    try {
        SessionManager sessionManager = new SessionManager(context);
        
        String maTaiKhoan = sessionManager.getMaTaiKhoan();
        String vaiTro = sessionManager.getVaiTro();
        
        if (maTaiKhoan != null && vaiTro != null) {
            if ("BenhNhan".equalsIgnoreCase(vaiTro)) {
                return "patient_" + maTaiKhoan;
            } else if ("BacSi".equalsIgnoreCase(vaiTro)) {
                return "doctor_" + maTaiKhoan;
            } else {
                return vaiTro.toLowerCase() + "_" + maTaiKhoan;
            }
        }
    } catch (Exception e) {
        Log.e(TAG, "Error getting user info from SessionManager: " + e.getMessage());
    }
    
    // Fallback to old method
    // ...
}
```

### 2. Sửa `getCurrentUserId()` trong VoiceCallActivity và VideoCallActivity
```java
private String getCurrentUserId() {
    // 🔥 FIX: Sử dụng SessionManager
    try {
        SessionManager sessionManager = new SessionManager(this);
        
        String maTaiKhoan = sessionManager.getMaTaiKhoan();
        String vaiTro = sessionManager.getVaiTro();
        
        if (maTaiKhoan != null && vaiTro != null) {
            if ("BenhNhan".equalsIgnoreCase(vaiTro)) {
                return "patient_" + maTaiKhoan;
            } else if ("BacSi".equalsIgnoreCase(vaiTro)) {
                return "doctor_" + maTaiKhoan;
            }
        }
    } catch (Exception e) {
        Log.e(TAG, "Error getting user info from SessionManager: " + e.getMessage());
    }
    
    // Fallback...
}
```

### 3. Thêm Debug Method
```java
public void debugUserInfo() {
    SessionManager sessionManager = new SessionManager(context);
    
    Log.d(TAG, "🔍 SessionManager info:");
    Log.d(TAG, "🔍 - isLoggedIn: " + sessionManager.isLoggedIn());
    Log.d(TAG, "🔍 - maTaiKhoan: " + sessionManager.getMaTaiKhoan());
    Log.d(TAG, "🔍 - vaiTro: " + sessionManager.getVaiTro());
    Log.d(TAG, "🔍 - email: " + sessionManager.getEmail());
    Log.d(TAG, "🔍 - hoTen: " + sessionManager.getHoTen());
    
    String currentUserId = getCurrentUserId();
    Log.d(TAG, "🔍 Generated userId: " + currentUserId);
}
```

## 🔧 CÁC THAY ĐỔI CHÍNH

### StringeeManager.java
- ✅ Sử dụng SessionManager thay vì trực tiếp SharedPreferences
- ✅ Xử lý đúng `maTaiKhoan` và `vaiTro`
- ✅ Fallback logic cho trường hợp cũ
- ✅ Thêm debug method

### VoiceCallActivity.java & VideoCallActivity.java
- ✅ Sử dụng SessionManager trong `getCurrentUserId()`
- ✅ Xử lý exception properly
- ✅ Fallback logic
- ✅ Gọi debug trước khi test connection

## 🎯 KẾT QUẢ MONG ĐỢI
- ✅ Bệnh nhân có userId ổn định từ SessionManager
- ✅ Token được tạo đúng cho cả bác sĩ và bệnh nhân
- ✅ Connection ổn định cho cả 2 role
- ✅ Không còn fallback userId ngẫu nhiên
- ✅ Debug log chi tiết để troubleshoot

## 🚀 CÁCH TEST
1. **Đăng nhập bằng tài khoản bệnh nhân**
2. **Kiểm tra log debug:**
   ```
   🔍 SessionManager info:
   🔍 - maTaiKhoan: BN001
   🔍 - vaiTro: BenhNhan
   🔍 Generated userId: patient_BN001
   ```
3. **Thực hiện cuộc gọi voice/video**
4. **✅ Không còn lỗi "chưa kết nối được với server"**

## 📝 GHI CHÚ
- Vấn đề này chỉ ảnh hưởng đến bệnh nhân vì họ không có data trong SharedPreferences "user_info"
- Bác sĩ có thể hoạt động tốt hơn do có data từ nguồn khác
- SessionManager là source of truth chính thức cho thông tin user
- Build thành công và sẵn sàng test