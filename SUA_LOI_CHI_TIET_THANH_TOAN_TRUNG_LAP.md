# SỬA LỖI CHI TIẾT THANH TOÁN TRÙNG LẶP

## Vấn đề trước đây
- Chi tiết thanh toán hiển thị nhiều dòng trùng lặp cho cùng một loại thuốc
- Cùng tên thuốc "Paracetamol 500mg" nhưng có nhiều giá khác nhau
- Logic tạo ID chi tiết hóa đơn không unique

## Nguyên nhân
1. **ID không unique**: Sử dụng `System.currentTimeMillis()` có thể tạo ra cùng timestamp
2. **Race condition**: Xử lý bất đồng bộ có thể gây trùng lặp
3. **Adapter query không cần thiết**: Load lại tên thuốc từ database thay vì dùng dữ liệu có sẵn

## Giải pháp đã áp dụng

### 1. Tạo ID chi tiết hóa đơn unique
**Trước**:
```java
String maChiTiet = "CTHD_THUOC_" + System.currentTimeMillis() + "_" + thuoc.getMaDuocPham();
```

**Sau**:
```java
String maChiTiet = "CTHD_THUOC_" + maHoaDon + "_" + index + "_" + thuoc.getMaDuocPham();
```

**Lợi ích**:
- Sử dụng `index` thay vì timestamp để tránh trùng lặp
- Bao gồm `maHoaDon` để đảm bảo unique across hóa đơn
- Format: `CTHD_THUOC_HD123_0_THUOC001`

### 2. Xử lý thread-safe cho danh sách
**Thêm synchronized**:
```java
synchronized (danhSachChiTietThuoc) {
    danhSachChiTietThuoc.add(chiTiet);
}
```

**Lợi ích**:
- Tránh race condition khi nhiều thread cùng thêm vào list
- Đảm bảo tính nhất quán của dữ liệu

### 3. Sửa adapter chi tiết hóa đơn
**Trước**: Query database để lấy tên thuốc
```java
repo.getByField("DuocPham", "maDuocPham", chiTiet.getMaDuocPham(), ...)
```

**Sau**: Sử dụng dữ liệu có sẵn
```java
holder.tvTenThuoc.setText(chiTiet.getTenDichVu());
holder.tvThanhTien.setText(String.format("Thành tiền: %,.0f đ", chiTiet.getThanhTien()));
```

**Lợi ích**:
- Hiển thị ngay lập tức, không cần query
- Tránh lỗi hiển thị khi không tìm thấy thuốc
- Performance tốt hơn

### 4. Cải thiện tạo ID cho dịch vụ khám
**Trước**:
```java
String maChiTiet = "CTHD_DV_" + System.currentTimeMillis() + "_" + dv.getMaDichVu();
```

**Sau**:
```java
String maChiTiet = "CTHD_DV_" + maHoaDon + "_" + i + "_" + dv.getMaDichVu();
```

## Cấu trúc ID mới

### Dịch vụ khám
```
CTHD_DV_{maHoaDon}_{index}_{maDichVu}
Ví dụ: CTHD_DV_HD1640995200000_0_DV001
```

### Thuốc
```
CTHD_THUOC_{maHoaDon}_{index}_{maDuocPham}
Ví dụ: CTHD_THUOC_HD1640995200000_0_THUOC001
```

## Kết quả
✅ **Không còn trùng lặp**: Mỗi chi tiết hóa đơn có ID unique
✅ **Hiển thị chính xác**: Tên và giá thuốc hiển thị đúng
✅ **Performance tốt**: Không query database không cần thiết
✅ **Thread-safe**: Xử lý bất đồng bộ an toàn
✅ **Dễ debug**: ID có cấu trúc rõ ràng

## Test case
1. Tạo bệnh án với 2 loại thuốc khác nhau
2. Kiểm tra chi tiết hóa đơn chỉ hiển thị 2 dòng
3. Mỗi dòng có tên thuốc và giá chính xác
4. Không có dòng trùng lặp

## Files đã sửa
- `TaoBenhAnActivity.java`: Sửa logic tạo ID unique và thread-safe
- `ChiTietHoaDonAdapter.java`: Sử dụng dữ liệu có sẵn thay vì query database