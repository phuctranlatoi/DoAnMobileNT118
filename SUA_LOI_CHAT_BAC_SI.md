# Sửa lỗi "Lỗi tải tin nhắn" trong Chat với Bác sĩ

## 🐛 **Vấn đề:**
Khi bấm vào chat với bác sĩ sau khi thanh toán, hiển thị "Lỗi tải tin nhắn" thay vì tin nhắn chào mừng.

## 🔍 **Nguyên nhân:**

### 1. **Thiếu thông tin bệnh nhân**
- `NhanTinBacSiActivity` không có `maBenhNhan` khi được gọi từ `ThanhToanQRActivity`
- Logic load user info không được xử lý đúng

### 2. **Lỗi Firestore Query**
- Sử dụng `orderBy("thoiGianGui")` gây lỗi index trong Firestore
- Query không thể thực thi được

### 3. **Enum không khớp**
- Lưu `loaiTinNhan` là String `"BAC_SI"` 
- Nhưng model `TinNhanBacSi` sử dụng enum `LoaiTinNhan.BAC_SI`

## ✅ **Các sửa đổi đã thực hiện:**

### 1. **Sửa logic load user info trong NhanTinBacSiActivity**
```java
// Trước
if (TextUtils.isEmpty(maBenhNhan) && !isDoctorView) {
    // Tạm thời để trống, sẽ xử lý sau
}

// Sau  
if (TextUtils.isEmpty(maBenhNhan) && !isDoctorView) {
    UserInfoLoader.loadUserInfo(this, userInfo -> {
        if (userInfo != null) {
            maBenhNhan = userInfo.getMaNguoiDung();
            tenBenhNhan = userInfo.getHoTen();
            loadTinNhan(); // Load tin nhắn sau khi có thông tin
        } else {
            Toast.makeText(this, "Không tìm thấy thông tin bệnh nhân!", Toast.LENGTH_SHORT).show();
            finish();
        }
    });
}
```

### 2. **Loại bỏ orderBy trong Firestore Query**
```java
// Trước
Query query = FirebaseFirestore.getInstance()
    .collection("TinNhanBacSi")
    .whereEqualTo("maBenhNhan", maBenhNhan)
    .whereEqualTo("maBacSi", maBacSi)
    .orderBy("thoiGianGui", Query.Direction.ASCENDING);

// Sau
Query query = FirebaseFirestore.getInstance()
    .collection("TinNhanBacSi")
    .whereEqualTo("maBenhNhan", maBenhNhan)
    .whereEqualTo("maBacSi", maBacSi);

// Sort trong code thay vì database
danhSachTinNhan.sort((t1, t2) -> {
    if (t1.getThoiGianGui() == null) return -1;
    if (t2.getThoiGianGui() == null) return 1;
    return t1.getThoiGianGui().compareTo(t2.getThoiGianGui());
});
```

### 3. **Sửa enum trong ThanhToanQRActivity**
```java
// Trước
tinNhanChaoMung.put("loaiTinNhan", "BAC_SI");

// Sau
import com.example.doannt118.model.TinNhanBacSi;
tinNhanChaoMung.put("loaiTinNhan", TinNhanBacSi.LoaiTinNhan.BAC_SI);
```

### 4. **Thêm validation và logging**
```java
private void loadTinNhan() {
    if (TextUtils.isEmpty(maBenhNhan) || TextUtils.isEmpty(maBacSi)) {
        android.util.Log.d("NhanTinBacSi", "Thiếu thông tin: maBenhNhan=" + maBenhNhan + ", maBacSi=" + maBacSi);
        Toast.makeText(this, "Thiếu thông tin để tải tin nhắn", Toast.LENGTH_SHORT).show();
        return;
    }
    
    android.util.Log.d("NhanTinBacSi", "Bắt đầu load tin nhắn: maBenhNhan=" + maBenhNhan + ", maBacSi=" + maBacSi);
    // ... rest of method
}
```

### 5. **Cập nhật logic onCreate**
```java
if (maBacSi != null) {
    loadThongTinBacSi();
    // Chỉ load tin nhắn nếu đã có maBenhNhan hoặc không phải view bệnh nhân
    if (!TextUtils.isEmpty(maBenhNhan) || isDoctorView) {
        loadTinNhan();
    }
    // Nếu maBenhNhan trống, loadTinNhan() sẽ được gọi sau khi load user info
}
```

## 🎯 **Kết quả:**
- ✅ Chat với bác sĩ hoạt động bình thường
- ✅ Tin nhắn chào mừng hiển thị đúng
- ✅ Không còn lỗi "Lỗi tải tin nhắn"
- ✅ Cuộc trò chuyện xuất hiện trong danh sách tin nhắn
- ✅ Real-time messaging hoạt động

## 🔧 **Files đã sửa:**
1. `app/src/main/java/com/example/doannt118/ui/NhanTinBacSiActivity.java`
2. `app/src/main/java/com/example/doannt118/ui/ThanhToanQRActivity.java`

## 🚀 **Workflow hoàn chỉnh:**
1. Chọn bác sĩ → Chọn gói → Thanh toán QR
2. Tự động tạo tin nhắn chào mừng từ bác sĩ
3. Chuyển đến chat → Hiển thị tin nhắn chào mừng
4. Bệnh nhân có thể nhắn tin ngay lập tức
5. Cuộc trò chuyện xuất hiện trong danh sách tin nhắn

Perfect! 🎉