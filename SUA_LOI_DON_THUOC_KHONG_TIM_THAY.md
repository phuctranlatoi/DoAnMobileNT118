# Sửa Lỗi "Không Tìm Thấy Mã Đơn Thuốc"

## Vấn đề
Khi bác sĩ vào quản lý bệnh án và bấm vào đơn thuốc của bệnh án, hệ thống báo lỗi "không tìm thấy mã đơn thuốc!" dù đơn thuốc đó được tạo cho bệnh án đó.

## Nguyên nhân
1. **Thiếu mã bệnh án**: Trong `CapNhatBenhAnActivity`, khi click vào đơn thuốc chỉ truyền `maDonThuoc` mà không truyền `maBenhAn`
2. **Key không nhất quán**: Sử dụng key khác nhau (`maDonThuoc` vs `MA_DON_THUOC`)
3. **Xử lý null không tốt**: Không xử lý trường hợp `ngayLap` có thể null
4. **Thiếu fallback**: Không có cơ chế lấy `maBenhAn` từ đơn thuốc nếu không được truyền

## Giải pháp đã thực hiện

### 1. Sửa CapNhatBenhAnActivity.java
```java
// Truyền cả maDonThuoc và maBenhAn
android.content.Intent intent = new android.content.Intent(this, ChiTietDonThuocActivity.class);
intent.putExtra("MA_DON_THUOC", donThuoc.getMaDonThuoc());
intent.putExtra("MA_BENH_AN", maBenhAn);
startActivity(intent);
```

### 2. Sửa ChiTietDonThuocActivity.java
```java
// Hỗ trợ cả 2 key để tương thích
maDonThuoc = getIntent().getStringExtra("MA_DON_THUOC");
if (maDonThuoc == null) {
    maDonThuoc = getIntent().getStringExtra("maDonThuoc");
}

maBenhAn = getIntent().getStringExtra("MA_BENH_AN");
if (maBenhAn == null) {
    maBenhAn = getIntent().getStringExtra("maBenhAn");
}
```

### 3. Cải thiện loadDonThuocInfo()
```java
// Lấy mã bệnh án từ đơn thuốc nếu chưa có
if (maBenhAn == null || maBenhAn.isEmpty()) {
    maBenhAn = donThuoc.getMaBenhAn();
}

// Xử lý trường hợp không có mã bệnh án
if (maBenhAn != null && !maBenhAn.isEmpty()) {
    loadBenhAnInfo(maBenhAn);
} else {
    // Load trực tiếp thông tin bác sĩ từ đơn thuốc
    if (donThuoc.getMaBacSi() != null) {
        loadBacSiInfo(donThuoc.getMaBacSi());
    }
    tvChanDoan.setText("Chẩn đoán: Không có thông tin");
}
```

### 4. Sửa DonThuocAdapter.java
```java
// Xử lý trường hợp ngayLap có thể null
if (donThuoc.getNgayLap() != null) {
    holder.tvNgayLap.setText("Ngày lập: " + sdf.format(donThuoc.getNgayLap()));
} else if (donThuoc.getNgayKeDon() != null) {
    holder.tvNgayLap.setText("Ngày lập: " + sdf.format(donThuoc.getNgayKeDon().toDate()));
} else {
    holder.tvNgayLap.setText("Ngày lập: Không rõ");
}
```

### 5. Cải thiện loadChiTietDonThuoc()
```java
// Thêm kiểm tra null và thông báo rõ ràng hơn
if (querySnapshot != null && !querySnapshot.isEmpty()) {
    querySnapshot.forEach(doc -> {
        ChiTietDonThuoc chiTiet = doc.toObject(ChiTietDonThuoc.class);
        if (chiTiet != null) {
            chiTietList.add(chiTiet);
        }
    });
}

if (chiTietList.isEmpty()) {
    Toast.makeText(this, "Đơn thuốc này chưa có chi tiết", Toast.LENGTH_SHORT).show();
}
```

## Kết quả
- ✅ Bác sĩ có thể xem chi tiết đơn thuốc từ quản lý bệnh án
- ✅ Hệ thống hiển thị thông tin đầy đủ về đơn thuốc
- ✅ Xử lý tốt các trường hợp dữ liệu thiếu hoặc null
- ✅ Thông báo lỗi rõ ràng và hữu ích cho người dùng
- ✅ Tương thích với cả key cũ và mới

## Lưu ý
- Đảm bảo khi tạo đơn thuốc phải lưu đầy đủ `maBenhAn` và `maBacSi`
- Kiểm tra dữ liệu trong Firestore để đảm bảo tính nhất quán
- Test kỹ các trường hợp edge case (đơn thuốc không có chi tiết, thiếu thông tin bệnh án, v.v.)