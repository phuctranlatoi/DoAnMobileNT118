# SỬA LỖI TÍNH HÓA ĐƠN CHÍNH XÁC

## Vấn đề trước đây
- Tính phí thuốc cố định 10,000đ/viên thay vì lấy giá thực từ database
- Không cập nhật tổng tiền sau khi tính phí thuốc thực tế
- Logic tính toán không chính xác

## Giải pháp đã áp dụng

### 1. Lấy giá thuốc thực tế từ database
**Method mới**: `taoChiTietThuocVoiGiaThucTe()`
- Truy vấn collection `DuocPham` để lấy `giaBan` thực tế
- Fallback về giá mặc định 10,000đ nếu không tìm thấy
- Xử lý bất đồng bộ cho nhiều thuốc

### 2. Cập nhật hóa đơn với phí thuốc chính xác
**Method mới**: `capNhatPhiThuocTrongHoaDon()`
- Cập nhật field `phiThuoc` với giá thực tế
- Cập nhật field `tongTien` = phiKham + phiThuoc + phiDichVu
- Sử dụng `updateDocumentFields()` để cập nhật

### 3. Flow tính toán mới
```
1. Tạo hóa đơn với phiThuoc = 0 (tạm thời)
2. Lấy giá thực tế từng thuốc từ database
3. Tạo chi tiết hóa đơn với giá chính xác
4. Tính tổng phí thuốc
5. Cập nhật hóa đơn với phí thuốc và tổng tiền đúng
6. Lưu chi tiết hóa đơn
7. Gửi thông báo với số tiền chính xác
```

## Các method đã sửa/thêm

### `taoHoaDonTuDong()`
- Tạo hóa đơn với `phiThuoc = 0` trước
- Gọi `taoChiTietHoaDonVaTinhPhiThuoc()` thay vì `taoChiTietHoaDon()`

### `taoChiTietHoaDonVaTinhPhiThuoc()` (MỚI)
- Tạo chi tiết dịch vụ khám
- Gọi `taoChiTietThuocVoiGiaThucTe()` để xử lý thuốc

### `taoChiTietThuocVoiGiaThucTe()` (MỚI)
- Truy vấn database để lấy giá thuốc thực tế
- Tạo chi tiết hóa đơn với giá chính xác
- Tính tổng phí thuốc
- Cập nhật hóa đơn với phí thuốc thực tế

### `capNhatPhiThuocTrongHoaDon()` (MỚI)
- Cập nhật `phiThuoc` và `tongTien` trong hóa đơn
- Sử dụng `updateDocumentFields()` của repository

### `guiThongBaoHoaDonMoi()` (SỬA)
- Lấy tổng tiền thực tế từ hóa đơn đã cập nhật
- Gửi thông báo với số tiền chính xác

## Cấu trúc tính toán chính xác

### Phí khám (phiKham)
```java
long phiKham = tongPhiDichVu; // Từ dịch vụ đã chọn
```

### Phí thuốc (phiThuoc)
```java
// Lấy từ database DuocPham
for (ChiTietDonThuoc thuoc : danhSachThuoc) {
    DuocPham duocPham = getDuocPhamFromDB(thuoc.getMaDuocPham());
    phiThuoc += thuoc.getSoLuong() * duocPham.getGiaBan();
}
```

### Tổng tiền
```java
double tongTien = phiKham + phiThuoc + phiDichVu;
```

## Xử lý lỗi
- Nếu không tìm thấy thuốc trong database → dùng giá mặc định 10,000đ
- Nếu lỗi truy vấn database → dùng giá mặc định và log lỗi
- Nếu lỗi cập nhật hóa đơn → vẫn tiếp tục flow và log lỗi

## Kết quả
✅ **Giá thuốc chính xác**: Lấy từ database thay vì cố định
✅ **Tổng tiền đúng**: phiKham + phiThuoc + phiDichVu
✅ **Chi tiết hóa đơn chính xác**: Mỗi item có giá đúng
✅ **Thông báo chính xác**: Hiển thị số tiền thực tế
✅ **Xử lý lỗi**: Fallback về giá mặc định khi cần

## Test case
1. Tạo bệnh án với dịch vụ khám 100,000đ
2. Kê đơn thuốc: 2 viên thuốc A (giá 15,000đ/viên)
3. Kết quả mong đợi:
   - phiKham: 100,000đ
   - phiThuoc: 30,000đ (2 × 15,000đ)
   - tongTien: 130,000đ
4. Kiểm tra hóa đơn và thông báo hiển thị đúng 130,000đ