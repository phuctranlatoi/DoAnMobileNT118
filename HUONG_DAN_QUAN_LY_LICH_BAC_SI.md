# Hướng dẫn hoàn thiện Quản lý lịch làm việc & Xác nhận lịch khám

## Đã tạo:
1. ✅ Layout mới cho QuanLyLichLamViecActivity (`activity_quan_ly_lich_lam_viec_new.xml`)
2. ✅ Layout item mới (`item_lich_lam_viec_new.xml`) - hiển thị số bệnh nhân
3. ✅ Layout XacNhanLichKhamActivity (`activity_xac_nhan_lich_kham.xml`)
4. ✅ Layout item xác nhận (`item_xac_nhan_lich_kham.xml`)
5. ✅ Drawable circle_background

## Cần làm tiếp:

### 1. Cập nhật QuanLyLichLamViecActivity
- Đổi layout sang `activity_quan_ly_lich_lam_viec_new.xml`
- Bỏ form thêm/sửa/xóa (vì lịch tự động)
- Chỉ hiển thị lịch theo ngày đã chọn
- Load số lượng bệnh nhân cho mỗi khung giờ
- Hiển thị thống kê: Tổng khung giờ, Tổng bệnh nhân

### 2. Tạo LichLamViecNewAdapter
- Hiển thị ca làm việc
- Hiển thị số bệnh nhân (X/6)
- Badge trạng thái: "Còn trống" (xanh) / "Đã đầy" (đỏ)
- Click vào item → Mở XemChiTietLichKhamActivity

### 3. Tạo XacNhanLichKhamActivity
- Filter: "Chờ xác nhận" / "Đã xác nhận"
- Load danh sách lịch khám của bác sĩ
- Hiển thị thông tin bệnh nhân, ngày giờ, số thứ tự

### 4. Tạo XacNhanLichKhamAdapter
- Hiển thị thông tin lịch khám
- Nút "Xác nhận" → Cập nhật trạng thái "XAC_NHAN"
- Nút "Hủy" → Cập nhật trạng thái "HUY"
- Ẩn buttons nếu đã xác nhận

### 5. Thêm vào MainBacSiActivity
```java
// Thêm card "Xác nhận lịch khám"
cardXacNhanLichKham.setOnClickListener(v -> {
    Intent intent = new Intent(this, XacNhanLichKhamActivity.class);
    intent.putExtra("MA_TAI_KHOAN", maTaiKhoan);
    intent.putExtra("MA_BAC_SI", maBacSi);
    startActivity(intent);
});
```

### 6. Cập nhật AndroidManifest
```xml
<activity android:name=".ui.XacNhanLichKhamActivity" />
```

## Flow hoạt động:

### Quản lý lịch làm việc:
1. Bác sĩ chọn ngày trên Calendar
2. Hệ thống tự động tạo các khung giờ theo quy tắc
3. Hiển thị số bệnh nhân đã đăng ký cho mỗi khung giờ
4. Click vào khung giờ → Xem chi tiết bệnh nhân

### Xác nhận lịch khám:
1. Bác sĩ vào "Xác nhận lịch khám"
2. Xem danh sách lịch "Chờ xác nhận"
3. Click "Xác nhận" → Cập nhật trạng thái
4. Click "Hủy" → Hủy lịch khám
5. Chuyển tab "Đã xác nhận" để xem lịch đã xác nhận
