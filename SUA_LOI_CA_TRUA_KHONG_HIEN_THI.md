# Sửa Lỗi Ca Trưa Không Hiển Thị Trong Xác Nhận Uống Thuốc

## Vấn đề
Bác sĩ kê thuốc có ca trưa nhưng trong phần xác nhận uống thuốc của bệnh nhân không hiển thị ca trưa, chỉ có ca sáng và chiều.

## Nguyên nhân
Trong `TaoBenhAnActivity`, khi lưu chi tiết đơn thuốc vào Firestore, chỉ lưu các field cơ bản (`maDuocPham`, `tenThuoc`, `soLuong`, `lieuDung`) nhưng **không lưu các field ca uống thuốc** (`uongSang`, `uongTrua`, `uongChieu`, `uongToi`).

Mặc dù trong code có đặt:
```java
chiTiet.setUongTrua(cbTrua.isChecked());
```

Nhưng khi lưu vào Firestore:
```java
Map<String, Object> chiTiet = new HashMap<>();
chiTiet.put("maDonThuoc", maDonThuoc);
chiTiet.put("tenThuoc", ct.getTenThuoc());
// ... chỉ lưu các field cơ bản
// THIẾU: không lưu uongSang, uongTrua, uongChieu, uongToi
```

## Giải pháp đã thực hiện

### 1. Sửa TaoBenhAnActivity.java - method taoDonThuoc()
```java
for (ChiTietDonThuoc ct : danhSachThuoc) {
    String maChiTiet = maDonThuoc + "_" + count[0]++;
    Map<String, Object> chiTiet = new HashMap<>();
    chiTiet.put("maChiTiet", maChiTiet);
    chiTiet.put("maDonThuoc", maDonThuoc);
    chiTiet.put("maDuocPham", ct.getMaDuocPham());
    chiTiet.put("tenThuoc", ct.getTenThuoc());
    chiTiet.put("soLuong", ct.getSoLuong());
    chiTiet.put("lieuDung", ct.getLieuDung());
    
    // ✅ THÊM: Lưu thông tin ca uống thuốc
    chiTiet.put("soNgayUong", ct.getSoNgayUong());
    chiTiet.put("soLanMoiNgay", ct.getSoLanMoiNgay());
    chiTiet.put("soVienMoiLan", ct.getSoVienMoiLan());
    chiTiet.put("uongSang", ct.isUongSang());
    chiTiet.put("uongTrua", ct.isUongTrua());
    chiTiet.put("uongChieu", ct.isUongChieu());
    chiTiet.put("uongToi", ct.isUongToi());
    chiTiet.put("cachDung", ct.getCachDung());
    
    repository.addDocument("ChiTietDonThuoc", maChiTiet, chiTiet, ...);
}
```

### 2. Thêm debug log trong DiemDanhUongThuocFragment.java
```java
private void phanLoaiThuocTheoCa(List<ChiTietDonThuoc> tatCaThuoc) {
    android.util.Log.d("DiemDanhFragment", "Phân loại " + tatCaThuoc.size() + " loại thuốc");
    
    for (ChiTietDonThuoc thuoc : tatCaThuoc) {
        android.util.Log.d("DiemDanhFragment", "Thuốc " + thuoc.getTenThuoc() + 
            ": Sáng=" + thuoc.isUongSang() + ", Trưa=" + thuoc.isUongTrua() + 
            ", Chiều=" + thuoc.isUongChieu() + ", Tối=" + thuoc.isUongToi());
        
        // Logic phân loại thuốc theo ca...
    }
    
    android.util.Log.d("DiemDanhFragment", "Kết quả phân loại: Sáng=" + thuocSang.size() + 
        ", Trưa=" + thuocTrua.size() + ", Chiều=" + thuocChieu.size());
}
```

## So sánh với KeDonThuocActivity
KeDonThuocActivity **không có vấn đề này** vì nó lưu toàn bộ object:
```java
repository.addDocument("ChiTietDonThuoc", thuoc.getMaChiTiet(), thuoc, ...);
```
Thay vì lưu từng field riêng lẻ như TaoBenhAnActivity.

## Kiểm tra dữ liệu
Để kiểm tra xem đơn thuốc đã có thông tin ca uống chưa:

1. **Xem log**: Trong Android Studio Logcat, tìm tag "DiemDanhFragment" để xem thông tin debug
2. **Kiểm tra Firestore**: Vào Firebase Console > Firestore > Collection "ChiTietDonThuoc" để xem các field `uongSang`, `uongTrua`, `uongChieu`, `uongToi`

## Kết quả
- ✅ Đơn thuốc mới tạo từ TaoBenhAnActivity sẽ lưu đầy đủ thông tin ca uống
- ✅ Ca trưa sẽ hiển thị trong xác nhận uống thuốc nếu bác sĩ đã chọn
- ✅ Tương thích với dữ liệu cũ (không có thông tin ca uống)
- ✅ Debug log giúp theo dõi quá trình phân loại thuốc

## Lưu ý
- Đơn thuốc cũ (tạo trước khi sửa) sẽ không có thông tin ca uống, hệ thống sẽ mặc định hiển thị ở ca sáng và chiều
- Đơn thuốc mới sẽ hiển thị đúng theo ca mà bác sĩ đã chọn
- Nếu vẫn không thấy ca trưa, kiểm tra log để xem dữ liệu có được lưu đúng không