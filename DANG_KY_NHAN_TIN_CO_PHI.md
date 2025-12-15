# Hệ thống Đăng ký Nhắn tin có Phí với Bác sĩ

## Tổng quan
Đã cập nhật hệ thống nhắn tin để yêu cầu đăng ký gói tư vấn có phí trước khi có thể nhắn tin với bác sĩ.

## Workflow mới

### 1. Chọn bác sĩ
- Từ màn hình chính: Bấm "Chat với bác sĩ" hoặc icon "Nhắn tin" ở bottom nav
- Nếu chưa có cuộc trò chuyện nào: Hiển thị nút "Chat ngay"
- Chuyển đến màn hình chọn bác sĩ (`ChonBacSiChatActivity`)

### 2. Xem thông tin bác sĩ
- Bấm vào bác sĩ → Chuyển đến `ThongTinBacSiActivity`
- Hiển thị:
  - Thông tin cơ bản: Tên, chuyên khoa, kinh nghiệm
  - Chi tiết: Bằng cấp, nơi làm việc
  - 3 gói tư vấn với giá khác nhau

### 3. Chọn gói tư vấn
**Gói Cơ Bản (99.000đ):**
- 50 tin nhắn
- Thời hạn 7 ngày
- Chỉ nhắn tin

**Gói Nâng Cao (199.000đ):**
- 150 tin nhắn  
- Thời hạn 15 ngày
- Nhắn tin + Gọi điện thoại

**Gói Cao Cấp (299.000đ):**
- 300 tin nhắn
- Thời hạn 30 ngày
- Nhắn tin + Gọi điện + Video call

### 4. Thanh toán
- Chuyển đến `ThanhToanActivity`
- Chọn phương thức thanh toán:
  - Ví điện tử (MoMo, ZaloPay, VNPay)
  - Thẻ tín dụng/ghi nợ
  - Chuyển khoản ngân hàng
- Bấm "Thanh toán ngay"

### 5. Bắt đầu nhắn tin
- Sau khi thanh toán thành công → Chuyển đến màn hình chat
- Có thể nhắn tin trong thời hạn và số lượng tin nhắn của gói đã mua

## Models mới

### GoiNhanTin
```java
- maGoi: String
- tenGoi: String  
- moTa: String
- gia: double
- soTinNhanToiDa: int
- thoiHanNgay: int
- coGoiDienThoai: boolean
- coGoiVideo: boolean
```

### DangKyNhanTin
```java
- maDangKy: String
- maBenhNhan: String
- maBacSi: String
- maGoi: String
- giaThanhToan: double
- ngayDangKy: Timestamp
- ngayHetHan: Timestamp
- soTinNhanDaSuDung: int
- trangThaiThanhToan: String
- phuongThucThanhToan: String
- maGiaoDich: String
```

## Activities mới

### ThongTinBacSiActivity
- Hiển thị thông tin chi tiết bác sĩ
- Cho phép chọn gói tư vấn
- Xử lý đăng ký gói

### ThanhToanActivity  
- Hiển thị thông tin đơn hàng
- Chọn phương thức thanh toán
- Xử lý thanh toán (giả lập)

## Cập nhật

### ChonBacSiChatActivity
- Thay đổi từ chat trực tiếp → xem thông tin bác sĩ
- Cập nhật UI text và navigation

### Layout changes
- `item_bac_si_chat.xml`: "Chat" → "Xem thông tin"
- `activity_chon_bac_si_chat.xml`: Cập nhật header text

## Firestore Collections

### DangKyNhanTin
Lưu trữ thông tin đăng ký gói tư vấn của bệnh nhân với bác sĩ

### GoiNhanTin (tùy chọn)
Có thể tạo collection này để quản lý các gói tư vấn động thay vì hardcode

## Tính năng tương lai
- Tích hợp gateway thanh toán thực (VNPay, MoMo)
- Quản lý số lượng tin nhắn đã sử dụng
- Thông báo khi gần hết hạn/hết tin nhắn
- Gia hạn gói tư vấn
- Lịch sử thanh toán