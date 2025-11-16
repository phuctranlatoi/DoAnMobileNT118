# Hướng dẫn hoàn thiện tính năng Đặt lịch khám

## Đã tạo:
1. ✅ Layout danh sách bác sĩ (`activity_danh_sach_bac_si.xml`)
2. ✅ Layout item bác sĩ (`item_bac_si.xml`)
3. ✅ Các drawable: search_background, ic_search, ic_location, ic_verified
4. ✅ Cập nhật model BacSi với các field mới

## Cần tạo tiếp:

### 1. DanhSachBacSiActivity.java
```java
- Load danh sách bác sĩ từ Firestore
- Tìm kiếm bác sĩ theo tên/chuyên khoa
- Filter theo chuyên khoa
- Click vào bác sĩ → Mở ChiTietBacSiActivity
```

### 2. BacSiAdapter.java
```java
- Hiển thị danh sách bác sĩ
- Hiển thị avatar, tên, chuyên khoa, địa chỉ, kinh nghiệm
- Nút "Đặt lịch ngay"
```

### 3. ChiTietBacSiActivity (màn hình chi tiết bác sĩ + lịch)
Layout cần có:
- Header với avatar và thông tin bác sĩ
- Phần giới thiệu (có thể expand/collapse)
- Calendar view để chọn ngày
- Danh sách khung giờ trống
- Nút "Đặt khám"

### 4. Cập nhật MainBenhNhanActivity
```java
handleDangKyLichKham() {
    Intent intent = new Intent(this, DanhSachBacSiActivity.class);
    intent.putExtra("MA_TAI_KHOAN", maTaiKhoan);
    startActivity(intent);
}
```

## Flow hoạt động:
1. Bệnh nhân click "Đăng ký lịch khám"
2. → Màn hình danh sách bác sĩ (có search, filter)
3. → Click vào bác sĩ
4. → Màn hình chi tiết bác sĩ + lịch khám
5. → Chọn ngày → Chọn khung giờ → Đặt khám
6. → Xác nhận và lưu vào Firestore

## Các file cần thêm vào AndroidManifest:
```xml
<activity android:name=".ui.DanhSachBacSiActivity" />
<activity android:name=".ui.ChiTietBacSiActivity" />
```
