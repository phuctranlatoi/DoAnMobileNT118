# Flow Đăng ký Lịch khám Mới

## Mô tả
Đã cập nhật flow đăng ký lịch khám để bệnh nhân chọn bác sĩ trước, sau đó đặt lịch từ trang chi tiết bác sĩ.

## Flow mới

### Bước 1: Từ Trang chủ Bệnh nhân
- **File**: `MainBenhNhanActivity.java`
- **Action**: Click vào card "Đăng ký lịch khám"
- **Chuyển đến**: `DanhSachBacSiActivity` (thay vì `DangKyLichKhamActivity`)

### Bước 2: Chọn Bác sĩ
- **File**: `DanhSachBacSiActivity.java` + `activity_danh_sach_bac_si.xml`
- **Chức năng**:
  - Hiển thị danh sách tất cả bác sĩ đã xác thực
  - Tìm kiếm theo tên bác sĩ, chuyên khoa
  - Header: "Chọn bác sĩ để đặt lịch"
- **Action**: Click vào bác sĩ
- **Chuyển đến**: `ChiTietBacSiActivity`

### Bước 3: Xem Chi tiết và Đặt lịch
- **File**: `ChiTietBacSiActivity.java` + `activity_chi_tiet_bac_si.xml`
- **Chức năng**:
  - Hiển thị thông tin chi tiết bác sĩ (tên, kinh nghiệm, chuyên khoa, địa chỉ, giới thiệu)
  - **CalendarView**: Chọn ngày khám (từ hôm nay trở đi)
  - **RecyclerView khung giờ**: Hiển thị các khung giờ trống
  - **Button "Đặt khám"**: Xác nhận đặt lịch

## Các thay đổi thực hiện

### 1. MainBenhNhanActivity.java
```java
// Thay đổi trong handleDangKyLichKham()
private void handleDangKyLichKham() {
    logActivity("Mở đăng ký lịch khám");
    // Mở danh sách bác sĩ để chọn (thay vì DangKyLichKhamActivity)
    Intent intent = new Intent(this, DanhSachBacSiActivity.class);
    intent.putExtra("MA_TAI_KHOAN", maTaiKhoan);
    intent.putExtra("MA_BENH_NHAN", maBenhNhan);
    startActivity(intent);
}
```

### 2. DanhSachBacSiActivity.java
- **Thêm biến**: `maBenhNhan` để nhận từ Intent
- **Cập nhật navigation**: Truyền `MA_BENH_NHAN` khi mở `ChiTietBacSiActivity`
- **Comment**: "Mở màn hình chi tiết bác sĩ để đặt lịch khám"

### 3. ChiTietBacSiActivity.java
- **Nhận thêm**: `MA_BENH_NHAN` từ Intent
- **Logic đặt lịch**: Đã có sẵn đầy đủ chức năng
  - Tự động tạo khung giờ theo quy tắc (thứ 7, CN: 8h-11h + 15h-22h; thứ 2-6: 15h-22h)
  - Kiểm tra và tạo `LichLamViec` nếu chưa có
  - Tạo `LichKham` với trạng thái "CHO"
  - Hiển thị số thứ tự sau khi đặt thành công

### 4. activity_danh_sach_bac_si.xml
- **Header title**: "Chọn bác sĩ để đặt lịch" (thay vì "Chọn bác sĩ")

## Ưu điểm của Flow mới

### Trải nghiệm người dùng
- **Trực quan hơn**: Bệnh nhân thấy được thông tin chi tiết bác sĩ trước khi đặt lịch
- **Linh hoạt hơn**: Có thể so sánh nhiều bác sĩ trước khi quyết định
- **Tin cậy hơn**: Xem được kinh nghiệm, chuyên khoa, địa chỉ của bác sĩ

### Kỹ thuật
- **Tái sử dụng code**: Sử dụng lại `DanhSachBacSiActivity` và `ChiTietBacSiActivity`
- **Logic rõ ràng**: Mỗi màn hình có một mục đích cụ thể
- **Dễ bảo trì**: Tách biệt logic chọn bác sĩ và đặt lịch

## Quy tắc tạo Khung giờ

### Thứ 7, Chủ nhật
- **Buổi sáng**: 8:00 - 11:00 (6 khung giờ 30 phút)
- **Buổi chiều**: 15:00 - 22:00 (14 khung giờ 30 phút)
- **Tổng**: 20 khung giờ/ngày

### Thứ 2 - Thứ 6
- **Chỉ buổi chiều**: 15:00 - 22:00 (14 khung giờ 30 phút)
- **Tổng**: 14 khung giờ/ngày

### Giới hạn
- **Mỗi khung giờ**: Tối đa 6 bệnh nhân
- **Trạng thái**: "CHO" (chờ xác nhận)

## Files liên quan

### Layout Files
- `activity_danh_sach_bac_si.xml` - Danh sách bác sĩ
- `activity_chi_tiet_bac_si.xml` - Chi tiết bác sĩ + đặt lịch

### Java Files
- `MainBenhNhanActivity.java` - Điểm bắt đầu
- `DanhSachBacSiActivity.java` - Chọn bác sĩ
- `ChiTietBacSiActivity.java` - Đặt lịch khám

### Model Files
- `BacSi.java` - Thông tin bác sĩ
- `LichLamViec.java` - Lịch làm việc của bác sĩ
- `LichKham.java` - Lịch khám của bệnh nhân

## Kết quả
- ✅ Flow đăng ký lịch khám mới hoạt động hoàn chỉnh
- ✅ Bệnh nhân có thể chọn bác sĩ trước khi đặt lịch
- ✅ Xem được thông tin chi tiết bác sĩ
- ✅ Chọn ngày và giờ khám linh hoạt
- ✅ Tự động tạo khung giờ theo quy tắc
- ✅ Kiểm tra số lượng và tạo số thứ tự
- ✅ Trải nghiệm người dùng tốt hơn