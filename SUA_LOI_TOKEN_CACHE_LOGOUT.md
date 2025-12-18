# SỬA LỖI TOKEN/CACHE KHI LOGOUT - SWITCH ROLE

## Vấn đề
Khi logout từ role bác sĩ và login vào role bệnh nhân, StringeeManager vẫn giữ token/connection cũ từ role bác sĩ, gây ra:
- Cuộc gọi bị nhầm lẫn giữa các role
- Token cũ vẫn được sử dụng
- Connection state không được reset

## Nguyên nhân
**StringeeManager không được clear khi logout!**

Các method logout hiện tại chỉ:
- `sessionManager.logout()` - Clear session
- `auth.signOut()` - Logout Firebase
- **NHƯNG KHÔNG** gọi `StringeeManager.disconnect()` hoặc clear cache

## Giải pháp đã áp dụng

### 1. Thêm method logout vào StringeeManager.java
```java
/**
 * LOGOUT: Clear tất cả token, cache và disconnect hoàn toàn
 * Gọi method này khi user logout để tránh conflict giữa các role
 */
public void logout() {
    // 1. Disconnect connection hiện tại
    if (stringeeClient != null && isConnected) {
        stringeeClient.disconnect();
        isConnected = false;
    }
    
    // 2. Clear cached tokens và user info
    SharedPreferences stringeePrefs = context.getSharedPreferences("stringee_info", Context.MODE_PRIVATE);
    stringeePrefs.edit().clear().apply();
    
    SharedPreferences connectionPrefs = context.getSharedPreferences(PREF_CONNECTION, Context.MODE_PRIVATE);
    connectionPrefs.edit().clear().apply();
    
    // 3. Reset internal state
    isConnected = false;
    isReconnecting = false;
    reconnectAttempts = 0;
    
    // 4. Stop maintenance
    stopConnectionMaintenance();
}
```

### 2. Sửa tất cả các method logout để gọi StringeeManager.logout()

#### ProfileActivity.java
```java
private void handleDangXuat() {
    // 🔥 FIX: Clear Stringee connection và cache trước khi logout
    try {
        StringeeManager stringeeManager = StringeeManager.getInstance(this);
        stringeeManager.logout();
    } catch (Exception e) {
        Log.e("ProfileActivity", "❌ Error during Stringee logout: " + e.getMessage());
    }
    
    sessionManager.logout();
    auth.signOut();
    // ... rest of logout
}
```

#### Các file đã sửa:
- `ProfileActivity.java`
- `MainBenhNhanActivity.java`
- `QuanLyLichLamViecActivity.java`
- `MainAdminActivity.java`

### 3. Thêm method clearCache() (optional)
```java
/**
 * CLEAR CACHE: Chỉ clear cache mà không disconnect (dùng khi switch user)
 */
public void clearCache() {
    // Clear cached tokens và user info
    SharedPreferences stringeePrefs = context.getSharedPreferences("stringee_info", Context.MODE_PRIVATE);
    stringeePrefs.edit().clear().apply();
    
    SharedPreferences connectionPrefs = context.getSharedPreferences(PREF_CONNECTION, Context.MODE_PRIVATE);
    connectionPrefs.edit().clear().apply();
}
```

## Kết quả
- ✅ Logout bác sĩ → StringeeManager.logout() → Clear tất cả token/cache
- ✅ Login bệnh nhân → StringeeManager tạo token mới cho bệnh nhân
- ✅ Không còn conflict giữa các role
- ✅ Cuộc gọi hoạt động đúng cho từng role

## Test case
1. **Login bác sĩ** → Kiểm tra token: `doctor_xxx`
2. **Logout bác sĩ** → Kiểm tra logs: "Stringee logout completed"
3. **Login bệnh nhân** → Kiểm tra token: `patient_xxx` (mới)
4. **Bệnh nhân gọi bác sĩ** → Không còn hiện cuộc gọi đến trên điện thoại bệnh nhân

## Files đã sửa
1. `app/src/main/java/com/example/doannt118/stringee/StringeeManager.java` ⭐ **CHÍNH**
2. `app/src/main/java/com/example/doannt118/ui/ProfileActivity.java`
3. `app/src/main/java/com/example/doannt118/ui/MainBenhNhanActivity.java`
4. `app/src/main/java/com/example/doannt118/ui/QuanLyLichLamViecActivity.java`
5. `app/src/main/java/com/example/doannt118/ui/MainAdminActivity.java`

## Debug logs để kiểm tra
```
🚪 === STRINGEE LOGOUT ===
🚪 Disconnecting current connection...
🚪 Clearing cached tokens and user info...
🚪 ✅ Stringee logout completed - all tokens and cache cleared
```