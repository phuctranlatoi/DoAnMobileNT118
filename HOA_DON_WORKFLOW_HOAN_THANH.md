# HOÀN THÀNH TÍCH HỢP HỆ THỐNG HÓA ĐƠN

## Tóm tắt
Đã tích hợp thành công hệ thống hóa đơn tự động vào flow khám bệnh. Sau khi bác sĩ tạo bệnh án và kê đơn thuốc, hệ thống sẽ tự động tạo hóa đơn và gửi thông báo cho bệnh nhân.

## Flow hoạt động

### 1. Bác sĩ tạo bệnh án (TaoBenhAnActivity)
- Chọn bệnh nhân
- Chọn dịch vụ khám (ChonDichVuKhamActivity) 
- Nhập chẩn đoán, ghi chú
- Kê đơn thuốc (tùy chọn)
- **MỚI**: Sau khi lưu bệnh án và đơn thuốc → Tự động tạo hóa đơn

### 2. Tạo hóa đơn tự động
**File đã sửa**: `TaoBenhAnActivity.java`

**Các method mới được thêm**:
- `taoHoaDonTuDong()`: Tạo hóa đơn với thông tin đầy đủ
- `tinhPhiThuoc()`: Tính phí thuốc từ đơn thuốc
- `taoChiTietHoaDon()`: Tạo chi tiết hóa đơn cho dịch vụ và thuốc
- `luuTatCaChiTietHoaDon()`: Lưu tất cả chi tiết hóa đơn
- `guiThongBaoHoaDonMoi()`: Gửi thông báo cho bệnh nhân

**Thông tin hóa đơn bao gồm**:
- Phí khám: Từ dịch vụ khám đã chọn
- Phí thuốc: Tính từ đơn thuốc (10,000đ/viên)
- Phí dịch vụ: Dành cho mở rộng sau
- Trạng thái: "CHUA_THANH_TOAN"

### 3. Bệnh nhân xem hóa đơn

**Từ MainBenhNhanActivity**:
- Click vào card "Xem hóa đơn" 
- Mở DanhSachHoaDonActivity

**DanhSachHoaDonActivity**:
- Hiển thị danh sách tất cả hóa đơn của bệnh nhân
- Tính tổng chi phí
- Click vào hóa đơn → Mở ChiTietHoaDonActivity

**ChiTietHoaDonActivity**:
- Hiển thị thông tin chi tiết hóa đơn
- Thông tin bác sĩ, chẩn đoán
- Danh sách chi tiết dịch vụ và thuốc
- Tổng tiền

## Cấu trúc dữ liệu

### HoaDon
```java
- maHoaDon: String
- maBenhAn: String  
- maBenhNhan: String
- ngayLap: Date
- tongTien: double
- phiKham: long
- phiThuoc: long
- phiDichVu: long
- trangThai: String ("CHUA_THANH_TOAN", "DA_THANH_TOAN")
- ngayThanhToan: Date
```

### ChiTietHoaDon
```java
- maChiTiet: String
- maHoaDon: String
- maDuocPham: String (cho thuốc)
- tenDichVu: String (tên dịch vụ/thuốc)
- soLuong: int
- donGia: double
```

## Thông báo
Sau khi tạo hóa đơn thành công, hệ thống sẽ:
- Gửi thông báo push cho bệnh nhân
- Nội dung: "Bạn có hóa đơn mới với tổng tiền X đ. Vui lòng kiểm tra và thanh toán."

## Các file đã có sẵn (không cần sửa)
- `DanhSachHoaDonActivity.java`: Hiển thị danh sách hóa đơn
- `ChiTietHoaDonActivity.java`: Hiển thị chi tiết hóa đơn  
- `HoaDonAdapter.java`: Adapter cho danh sách hóa đơn
- `ChiTietHoaDonAdapter.java`: Adapter cho chi tiết hóa đơn
- `HoaDon.java`: Model hóa đơn
- `ChiTietHoaDon.java`: Model chi tiết hóa đơn
- Repository methods: `getHoaDonByBenhNhan()`, `getChiTietHoaDon()`
- Layout files: Tất cả layout đã có sẵn
- AndroidManifest: Đã khai báo tất cả activity

## Kết quả
✅ **Hoàn thành**: Hệ thống hóa đơn đã hoạt động đầy đủ
✅ **Tự động**: Tạo hóa đơn tự động sau khi bác sĩ lưu bệnh án
✅ **Thông báo**: Gửi thông báo cho bệnh nhân khi có hóa đơn mới
✅ **Xem hóa đơn**: Bệnh nhân có thể xem danh sách và chi tiết hóa đơn
✅ **Tích hợp**: Tích hợp hoàn toàn với flow khám bệnh hiện tại

## Cách test
1. Đăng nhập với tài khoản bác sĩ
2. Tạo bệnh án mới (TaoBenhAnActivity)
3. Chọn dịch vụ khám và kê đơn thuốc
4. Lưu bệnh án → Hệ thống tự động tạo hóa đơn
5. Đăng nhập với tài khoản bệnh nhân tương ứng
6. Vào "Xem hóa đơn" → Thấy hóa đơn mới được tạo
7. Click vào hóa đơn → Xem chi tiết đầy đủ