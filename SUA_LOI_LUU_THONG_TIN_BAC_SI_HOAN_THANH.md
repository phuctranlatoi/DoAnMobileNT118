# Sửa Lỗi Lưu Thông Tin Bác Sĩ - HOÀN THÀNH

## Vấn Đề
Khi admin tạo tài khoản bác sĩ mới, các trường **chuyên khoa**, **số năm kinh nghiệm**, và **giới thiệu về bác sĩ** không được lưu vào database.

## Nguyên Nhân
1. **Dialog thiếu trường**: `dialog_create_account.xml` không có các trường cần thiết
2. **Logic thiếu xử lý**: `MainAdminActivity.java` không lấy và lưu các trường mới

## Giải Pháp Thực Hiện

### 1. Cập Nhật Layout Dialog (dialog_create_account.xml)
**Thêm 4 trường mới:**
```xml
<!-- Chuyên khoa -->
<EditText
    android:id="@+id/txtChuyenKhoa"
    android:hint="Chuyên khoa (Bác sĩ)" />

<!-- Địa chỉ -->
<EditText
    android:id="@+id/txtDiaChi"
    android:hint="Địa chỉ (Bác sĩ)" />

<!-- Số năm kinh nghiệm -->
<EditText
    android:id="@+id/txtNamKinhNghiem"
    android:hint="Số năm kinh nghiệm (Bác sĩ)"
    android:inputType="number" />

<!-- Giới thiệu -->
<EditText
    android:id="@+id/txtGioiThieu"
    android:hint="Giới thiệu về bác sĩ"
    android:minLines="3" />
```

### 2. Cập Nhật MainAdminActivity.java

#### a) Thêm findViewById cho các trường mới:
```java
EditText txtChuyenKhoa = dialogView.findViewById(R.id.txtChuyenKhoa);
EditText txtDiaChi = dialogView.findViewById(R.id.txtDiaChi);
EditText txtNamKinhNghiem = dialogView.findViewById(R.id.txtNamKinhNghiem);
EditText txtGioiThieu = dialogView.findViewById(R.id.txtGioiThieu);
```

#### b) Lấy giá trị từ các trường:
```java
String chuyenKhoa = txtChuyenKhoa.getText().toString().trim();
String diaChi = txtDiaChi.getText().toString().trim();
String namKinhNghiemStr = txtNamKinhNghiem.getText().toString().trim();
String gioiThieu = txtGioiThieu.getText().toString().trim();

// Validate số năm kinh nghiệm
int namKinhNghiem = 0;
if (!namKinhNghiemStr.isEmpty()) {
    namKinhNghiem = Integer.parseInt(namKinhNghiemStr);
}
```

#### c) Cập nhật method signature:
```java
// Trước
private void createNewAccount(String tenDangNhap, String matKhau, String hoTen, String sdt, String email,
                              String bangCap, String hocVi, String chungChi, String vaiTro, AlertDialog dialog)

// Sau
private void createNewAccount(String tenDangNhap, String matKhau, String hoTen, String sdt, String email,
                              String bangCap, String hocVi, String chungChi, String chuyenKhoa, String diaChi, 
                              int namKinhNghiem, String gioiThieu, String vaiTro, AlertDialog dialog)
```

#### d) Cập nhật tạo object BacSi:
```java
// Trước
userProfile = new BacSi(maProfile, maTaiKhoan, hoTen, sdt, bangCap, hocVi, 
                       Arrays.asList(chungChi.split(",\\s*")), "Đã xác thực");

// Sau
BacSi bacSi = new BacSi(maProfile, maTaiKhoan, hoTen, sdt, bangCap, hocVi, 
                       Arrays.asList(chungChi.split(",\\s*")), "Đã xác thực");
// Set thêm các thông tin mới
bacSi.setChuyenKhoa(chuyenKhoa);
bacSi.setDiaChi(diaChi);
bacSi.setNamKinhNghiem(namKinhNghiem);
bacSi.setGioiThieu(gioiThieu);
userProfile = bacSi;
```

## Kết Quả

### Trước Khi Sửa
- ❌ Chuyên khoa: không được lưu
- ❌ Địa chỉ: không được lưu  
- ❌ Số năm kinh nghiệm: không được lưu
- ❌ Giới thiệu: không được lưu

### Sau Khi Sửa
- ✅ Chuyên khoa: được lưu vào database
- ✅ Địa chỉ: được lưu vào database
- ✅ Số năm kinh nghiệm: được lưu vào database (với validation)
- ✅ Giới thiệu: được lưu vào database

## Tính Năng Mới
1. **Validation số năm kinh nghiệm**: Kiểm tra input phải là số hợp lệ
2. **Giao diện thân thiện**: Các trường có hint rõ ràng
3. **Tự động lưu**: Tất cả thông tin được lưu ngay khi tạo tài khoản
4. **Hiển thị đầy đủ**: Thông tin sẽ hiển thị trong chi tiết bác sĩ

## Luồng Hoạt Động
1. Admin mở dialog tạo tài khoản
2. Chọn vai trò "Bác sĩ"
3. Nhập đầy đủ thông tin (bao gồm 4 trường mới)
4. Bấm "Đăng ký"
5. Hệ thống validate và lưu tất cả thông tin
6. Tài khoản bác sĩ được tạo với đầy đủ thông tin

## Files Đã Sửa
- `app/src/main/res/layout/dialog_create_account.xml`: Thêm 4 trường mới
- `app/src/main/java/com/example/doannt118/ui/MainAdminActivity.java`: Cập nhật logic xử lý

## Trạng Thái: ✅ HOÀN THÀNH
Tất cả thông tin bác sĩ giờ đây sẽ được lưu đầy đủ khi admin tạo tài khoản mới.