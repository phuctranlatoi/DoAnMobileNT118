# THÊM TRƯỜNG GIỚI THIỆU CHO ADMIN QUẢN LÝ BÁC SĨ

## Tổng quan
Đã thêm các trường thông tin bổ sung cho admin khi chỉnh sửa thông tin bác sĩ, bao gồm trường giới thiệu để admin có thể thêm/sửa thông tin giới thiệu cho bác sĩ. Thông tin này sẽ hiển thị trong phần chi tiết bác sĩ khi bệnh nhân xem.

## Các thay đổi đã thực hiện

### 1. Cập nhật Layout `dialog_edit_account.xml`
**Thêm các trường mới**:

- **etChuyenKhoa**: Nhập chuyên khoa của bác sĩ
- **etDiaChi**: Nhập địa chỉ phòng khám
- **etNamKinhNghiem**: Nhập số năm kinh nghiệm (inputType="number")
- **etGioiThieu**: Nhập giới thiệu chi tiết về bác sĩ

**Tính năng trường giới thiệu**:
- Chiều cao: 120dp
- Hỗ trợ nhiều dòng (textMultiLine)
- Tối đa 8 dòng hiển thị
- Có thanh cuộn dọc
- Placeholder hướng dẫn: "Giới thiệu về bác sĩ (kinh nghiệm, chuyên môn, thành tích...)"

### 2. Cập nhật Method `showEditAccountDialog()` trong MainAdminActivity
**Thêm xử lý các trường mới**:

```java
EditText etChuyenKhoa = dialogView.findViewById(R.id.etChuyenKhoa);
EditText etDiaChi = dialogView.findViewById(R.id.etDiaChi);
EditText etNamKinhNghiem = dialogView.findViewById(R.id.etNamKinhNghiem);
EditText etGioiThieu = dialogView.findViewById(R.id.etGioiThieu);
```

**Load dữ liệu hiện có**:
```java
etChuyenKhoa.setText(bacSi.getChuyenKhoa() != null ? bacSi.getChuyenKhoa() : "");
etDiaChi.setText(bacSi.getDiaChi() != null ? bacSi.getDiaChi() : "");
etNamKinhNghiem.setText(bacSi.getNamKinhNghiem() > 0 ? String.valueOf(bacSi.getNamKinhNghiem()) : "");
etGioiThieu.setText(bacSi.getGioiThieu() != null ? bacSi.getGioiThieu() : "");
```

**Validation và lưu dữ liệu**:
- Kiểm tra số năm kinh nghiệm hợp lệ
- Set tất cả thông tin mới vào object BacSi
- Cập nhật vào Firestore

### 3. Ẩn các trường không liên quan
**Đối với Admin và Bệnh nhân**:
```java
etChuyenKhoa.setVisibility(View.GONE);
etDiaChi.setVisibility(View.GONE);
etNamKinhNghiem.setVisibility(View.GONE);
etGioiThieu.setVisibility(View.GONE);
```

Chỉ hiển thị các trường bổ sung khi chỉnh sửa thông tin Bác sĩ.

## Luồng hoạt động

### Khi admin chỉnh sửa thông tin bác sĩ:
1. Mở MainAdminActivity → Tab "Tất cả tài khoản"
2. Bấm nút "Sửa" trên tài khoản bác sĩ
3. **MỚI**: Thấy thêm các trường:
   - Chuyên khoa
   - Địa chỉ phòng khám
   - Số năm kinh nghiệm
   - **Giới thiệu chi tiết** (trường chính)
4. Nhập/sửa thông tin và lưu
5. Thông tin được cập nhật vào Firestore

### Khi bệnh nhân xem chi tiết bác sĩ:
1. Chọn bác sĩ từ danh sách
2. Xem `ChiTietBacSiActivity`
3. **Thấy thông tin đầy đủ**:
   - Họ tên, kinh nghiệm, chuyên khoa, địa chỉ
   - **Phần giới thiệu chi tiết** do admin nhập
4. Đưa ra quyết định đặt lịch dựa trên thông tin đầy đủ

## Ví dụ thông tin admin có thể nhập

### Trường giới thiệu mẫu:
```
Bác sĩ chuyên khoa I về Nội thận với 15 năm kinh nghiệm. 
Từng công tác tại Bệnh viện Chợ Rẫy và Bệnh viện Đại học Y Dược.

Chuyên điều trị:
- Bệnh thận mạn tính
- Suy thận cấp và mạn tính  
- Lọc máu chu kỳ
- Ghép thận

Đã thực hiện hơn 500 ca ghép thận thành công và điều trị 
cho hàng nghìn bệnh nhân bệnh thận.

Giải thưởng:
- Bác sĩ xuất sắc năm 2020
- Giải thưởng nghiên cứu khoa học cấp bộ 2019
```

### Các trường khác:
- **Chuyên khoa**: "Nội thận"
- **Địa chỉ**: "123 Đường ABC, Quận 1, TP.HCM"
- **Năm kinh nghiệm**: "15"

## Lợi ích

### 1. Quản lý tập trung
- Admin có thể cập nhật thông tin bác sĩ từ một nơi
- Đảm bảo thông tin chính xác và đầy đủ
- Kiểm soát chất lượng thông tin hiển thị

### 2. Thông tin phong phú hơn
- Bệnh nhân có đủ thông tin để lựa chọn bác sĩ
- Thông tin chuyên môn chi tiết và đáng tin cậy
- Tăng tính chuyên nghiệp của hệ thống

### 3. Trải nghiệm tốt hơn
- Form chỉnh sửa đầy đủ và dễ sử dụng
- Validation đúng đắn tránh lỗi nhập liệu
- Hiển thị/ẩn trường phù hợp với từng loại tài khoản

## Quyền hạn và bảo mật

### Admin có thể:
- ✅ Xem và chỉnh sửa tất cả thông tin bác sĩ
- ✅ Thêm/sửa giới thiệu chi tiết
- ✅ Cập nhật chuyên khoa, địa chỉ, kinh nghiệm
- ✅ Duyệt/từ chối tài khoản bác sĩ

### Bác sĩ có thể:
- ✅ Tự cập nhật thông tin qua UpdateBacSiProfileActivity
- ❌ Không thể sửa thông tin của bác sĩ khác

### Bệnh nhân có thể:
- ✅ Xem thông tin chi tiết bác sĩ
- ❌ Không thể chỉnh sửa thông tin

## Files đã thay đổi
1. `app/src/main/res/layout/dialog_edit_account.xml` - Thêm các trường mới
2. `app/src/main/java/com/example/doannt118/ui/MainAdminActivity.java` - Cập nhật logic xử lý

## Files liên quan (không thay đổi)
1. `app/src/main/java/com/example/doannt118/model/BacSi.java` - Model đã có sẵn các trường
2. `app/src/main/java/com/example/doannt118/ui/ChiTietBacSiActivity.java` - Đã hiển thị thông tin

## Kết quả
Admin giờ đây có thể quản lý đầy đủ thông tin bác sĩ bao gồm:
- **Thông tin cơ bản**: Họ tên, SĐT, bằng cấp, học vị, chứng chỉ
- **Thông tin chuyên môn**: Chuyên khoa, địa chỉ, năm kinh nghiệm
- **Giới thiệu chi tiết**: Mô tả đầy đủ về bác sĩ để bệnh nhân tham khảo

Thông tin này sẽ hiển thị đầy đủ trong `ChiTietBacSiActivity` khi bệnh nhân xem để đặt lịch khám.