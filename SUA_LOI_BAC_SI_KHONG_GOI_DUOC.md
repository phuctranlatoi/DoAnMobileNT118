# SỬA LỖI BÁC SĨ KHÔNG GỌI ĐƯỢC BỆNH NHÂN

## Vấn đề
Bệnh nhân gọi bác sĩ được, nhưng bác sĩ không gọi được bệnh nhân.

## Nguyên nhân
**Logic xử lý thông tin user trong `NhanTinBacSiActivity` có vấn đề khi `isDoctorView = true`:**

1. **Thiếu maBacSi**: Khi bác sĩ mở chat, `maBacSi` có thể không được truyền đúng từ Intent
2. **Logic cũ chỉ xử lý trường hợp bệnh nhân**: Không có logic lấy `maBacSi` từ SessionManager cho bác sĩ
3. **Timing issue**: Bác sĩ có thể bấm chat trước khi `MainBacSiActivity.loadUserInfo()` hoàn thành

## Giải pháp đã áp dụng

### 1. Sửa logic xử lý thông tin user trong NhanTinBacSiActivity.java

#### TRƯỚC (chỉ xử lý bệnh nhân):
```java
// Nếu không có mã bệnh nhân và không phải view của bác sĩ, lấy từ SharedPreferences
if (TextUtils.isEmpty(maBenhNhan) && !isDoctorView) {
    // Chỉ xử lý trường hợp bệnh nhân
    // Không xử lý trường hợp bác sĩ thiếu maBacSi
}
```

#### SAU (xử lý cả bác sĩ và bệnh nhân):
```java
if (isDoctorView) {
    // Bác sĩ view: Đảm bảo có đủ thông tin bác sĩ và bệnh nhân
    if (TextUtils.isEmpty(maBacSi)) {
        // Lấy maBacSi từ SessionManager nếu không có trong Intent
        SessionManager sessionManager = new SessionManager(this);
        maBacSi = sessionManager.getMaTaiKhoan();
    }
    
    if (TextUtils.isEmpty(maBacSi) || TextUtils.isEmpty(maBenhNhan)) {
        Toast.makeText(this, "Thiếu thông tin bác sĩ hoặc bệnh nhân!", Toast.LENGTH_SHORT).show();
        finish();
        return;
    }
} else {
    // Bệnh nhân view: Logic cũ
    // ...
}
```

### 2. Thêm debug logs để kiểm tra

#### Button click logs:
```java
btnVoiceCall.setOnClickListener(v -> {
    Log.d("NhanTinBacSi", "🔘 Voice call button clicked - isDoctorView: " + isDoctorView);
    makeVoiceCall();
});
```

#### Final info logs:
```java
Log.d("NhanTinBacSi", "🔍 Final info - isDoctorView: " + isDoctorView + ", maBacSi: " + maBacSi + ", maBenhNhan: " + maBenhNhan);
```

## Luồng hoạt động đã sửa

### Bác sĩ gọi bệnh nhân:
1. **MainBacSiActivity** → **DanhSachTinNhanBacSiActivity** (truyền `maBacSi`)
2. **DanhSachTinNhanBacSiActivity** → **NhanTinBacSiActivity** (truyền `IS_DOCTOR_VIEW = true`, `maBacSi`, `maBenhNhan`)
3. **NhanTinBacSiActivity** kiểm tra:
   - `isDoctorView = true`
   - Nếu thiếu `maBacSi` → Lấy từ SessionManager
   - Đảm bảo có đủ `maBacSi` và `maBenhNhan`
4. **Bác sĩ bấm nút gọi** → `makeVoiceCall()` với `isDoctorView = true`
5. **Logic gọi**: `fromUserId = "doctor_" + maBacSi`, `toUserId = "patient_" + maBenhNhan`

### Bệnh nhân gọi bác sĩ:
1. **Luồng cũ không đổi** - vẫn hoạt động bình thường

## Kết quả
- ✅ Bác sĩ có thể gọi bệnh nhân
- ✅ Bệnh nhân có thể gọi bác sĩ  
- ✅ Logic `isDoctorView` hoạt động đúng
- ✅ Không còn thiếu thông tin `maBacSi` hoặc `maBenhNhan`

## Test case
1. **Login bác sĩ** → Vào tin nhắn → Chọn bệnh nhân → Bấm gọi
2. **Kiểm tra logs**:
   ```
   🔍 Final info - isDoctorView: true, maBacSi: BS001, maBenhNhan: BN001
   🔘 Voice call button clicked - isDoctorView: true
   🎯 Starting voice call: doctor_BS001 -> patient_BN001
   ```
3. **Kết quả**: Cuộc gọi đi từ bác sĩ đến bệnh nhân

## Files đã sửa
1. `app/src/main/java/com/example/doannt118/ui/NhanTinBacSiActivity.java` ⭐ **CHÍNH**

## Debug logs để kiểm tra
```
🔍 Doctor view - maBacSi from SessionManager: BS001
🔍 Final info - isDoctorView: true, maBacSi: BS001, maBenhNhan: BN001
🔘 Voice call button clicked - isDoctorView: true
🎯 Starting voice call: doctor_BS001 -> patient_BN001
```