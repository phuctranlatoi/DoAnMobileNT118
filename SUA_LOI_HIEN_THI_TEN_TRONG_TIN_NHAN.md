# Sửa Lỗi Hiển Thị Tên Trong Tin Nhắn

## Vấn đề
- **Bác sĩ** thấy tên chính mình thay vì tên bệnh nhân trong danh sách cuộc trò chuyện
- **Bệnh nhân** có thể thấy tên không chính xác của bác sĩ
- Nguyên nhân: Sử dụng `tinNhanCuoi.getTenNguoiGui()` làm tên hiển thị, nhưng đây là tên người gửi tin nhắn cuối cùng

## Giải pháp

### 1. Sửa Logic Bác Sĩ (`DanhSachTinNhanBacSiActivity`)
- **Trước**: Sử dụng `tinNhanCuoi.getTenNguoiGui()` làm tên bệnh nhân
- **Sau**: Lấy tên bệnh nhân từ collection `BenhNhan` dựa trên `maBenhNhan`

```java
// Tạo cuộc trò chuyện với tên tạm thời
CuocTroChuyenBacSi cuocTroChuyenBacSi = new CuocTroChuyenBacSi(
    maBenhNhan,
    "Đang tải...", // Tên tạm thời
    tinNhanCuoi.getNoiDung(),
    tinNhanCuoi.getThoiGianGui(),
    laBacSiGuiCuoi
);

// Sau đó lấy tên thật từ Firestore
loadTenBenhNhanForConversations(danhSachCuocTroChuyenBacSi);
```

### 2. Sửa Logic Bệnh Nhân (`DanhSachCuocTroChuyenBenhNhanActivity`)
- **Trước**: Sử dụng `tinNhanCuoi.getTenNguoiGui()` với prefix "BS."
- **Sau**: Lấy tên bác sĩ từ collection `BacSi` dựa trên `maBacSi`

```java
// Tạo cuộc trò chuyện với tên tạm thời
CuocTroChuyenBenhNhan cuocTroChuyenBenhNhan = new CuocTroChuyenBenhNhan(
    maBacSi,
    "Đang tải...", // Tên tạm thời
    tinNhanCuoi.getNoiDung(),
    tinNhanCuoi.getThoiGianGui(),
    laBenhNhanGuiCuoi
);

// Sau đó lấy tên thật từ Firestore
loadTenBacSiForConversations(danhSachCuocTroChuyenBenhNhan);
```

### 3. Thêm Methods Lấy Tên

#### `loadTenBenhNhanForConversations()` - Cho bác sĩ
- Query collection `BenhNhan` với `whereIn("maBenhNhan", danhSachMaBenhNhan)`
- Map `maBenhNhan` -> `hoTen`
- Cập nhật `cuocTroChuyenBacSi.setTenBenhNhan(tenBenhNhan)`
- Fallback: "Bệnh nhân {maBenhNhan}" nếu không tìm thấy

#### `loadTenBacSiForConversations()` - Cho bệnh nhân
- Query collection `BacSi` với `whereIn("maBacSi", danhSachMaBacSi)`
- Map `maBacSi` -> `"BS. " + hoTen`
- Cập nhật `cuocTroChuyenBenhNhan.setTenBacSi(tenBacSi)`
- Fallback: "BS. {maBacSi}" nếu không tìm thấy

## Kết quả
- **Bác sĩ** sẽ thấy tên bệnh nhân chính xác trong danh sách cuộc trò chuyện
- **Bệnh nhân** sẽ thấy tên bác sĩ chính xác với prefix "BS."
- Tính nhất quán dữ liệu được đảm bảo
- Xử lý lỗi graceful với tên fallback

## Files Đã Sửa
1. `app/src/main/java/com/example/doannt118/ui/DanhSachTinNhanBacSiActivity.java`
2. `app/src/main/java/com/example/doannt118/ui/DanhSachCuocTroChuyenBenhNhanActivity.java`

## Test Cases
- [x] Bác sĩ thấy tên bệnh nhân đúng
- [x] Bệnh nhân thấy tên bác sĩ đúng với prefix "BS."
- [x] Xử lý trường hợp không tìm thấy tên
- [x] Performance tối ưu với whereIn query