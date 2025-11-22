# Test Upload Ảnh - Hướng Dẫn Debug

## Các bước test và debug

### Bước 1: Kiểm tra nút có hoạt động không
1. Chạy app và đăng nhập
2. Vào trang Profile
3. Click vào nút camera (góc dưới phải của avatar)
4. **Kiểm tra:** Có thấy Toast "Đang mở chọn ảnh..." không?
   - ✅ Có → Nút hoạt động, chuyển sang Bước 2
   - ❌ Không → Nút không hoạt động, xem phần "Nút không click được"

### Bước 2: Kiểm tra quyền
1. Sau khi click nút, có popup xin quyền không?
   - ✅ Có → Chọn "Allow" và chuyển sang Bước 3
   - ❌ Không → Có thể đã cấp quyền rồi, chuyển sang Bước 3

### Bước 3: Kiểm tra dialog chọn ảnh
1. Có thấy dialog "Chọn ảnh đại diện" với 2 options không?
   - ✅ Có → Chọn "Chọn từ thư viện" và chuyển sang Bước 4
   - ❌ Không → Xem Logcat để tìm lỗi

### Bước 4: Kiểm tra thư viện ảnh
1. Có mở được thư viện ảnh không?
   - ✅ Có → Chọn một ảnh và chuyển sang Bước 5
   - ❌ Không → Xem phần "Lỗi mở thư viện"

### Bước 5: Kiểm tra upload
1. Sau khi chọn ảnh, có thấy Toast "Đang tải ảnh lên..." không?
   - ✅ Có → Đợi upload và xem Bước 6
   - ❌ Không → Xem Logcat để tìm lỗi

### Bước 6: Kiểm tra kết quả
1. Có thấy Toast "Cập nhật ảnh đại diện thành công!" không?
   - ✅ Có → Thành công! Ảnh đã được cập nhật
   - ❌ Không → Xem Logcat để tìm lỗi upload

## Xem Logcat để debug

### Mở Logcat trong Android Studio
1. Chạy app trên thiết bị/emulator
2. Mở tab **Logcat** ở dưới cùng
3. Trong ô filter, gõ: `ProfileActivity`
4. Click vào nút camera và xem log

### Log mong đợi khi thành công:
```
D/ProfileActivity: Edit avatar button clicked
D/ProfileActivity: checkPermissionAndPickImage called
D/ProfileActivity: Permission granted (Android 13+)
D/ProfileActivity: showImagePickerDialog called
D/ProfileActivity: Option selected: 0
D/ProfileActivity: Opening gallery
D/ProfileActivity: Ảnh được chọn: content://...
D/ProfileActivity: Bắt đầu upload ảnh: content://...
D/ProfileActivity: Upload progress: 50%
D/ProfileActivity: Upload thành công!
D/ProfileActivity: Download URL: https://firebasestorage...
D/ProfileActivity: Cập nhật Firestore - Collection: BenhNhan, maTaiKhoan: ...
D/ProfileActivity: Tìm thấy document ID: ...
D/ProfileActivity: Cập nhật Firestore thành công!
```

## Các lỗi thường gặp

### Lỗi 1: Nút không click được
**Triệu chứng:** Click vào nút camera không có phản ứng gì

**Nguyên nhân có thể:**
- Nút bị che bởi view khác
- ID không đúng
- Click listener chưa được set

**Cách khắc phục:**
1. Kiểm tra Logcat có dòng "Edit avatar button clicked" không
2. Nếu không có → Nút không nhận click
3. Thử thêm `android:elevation="8dp"` vào CardView của nút

### Lỗi 2: "btnEditAvatar is null!"
**Triệu chứng:** Trong Logcat thấy "btnEditAvatar is null!"

**Nguyên nhân:** Layout không có view với ID `btnEditAvatar`

**Cách khắc phục:**
1. Mở file `activity_profile.xml`
2. Tìm CardView có `android:id="@+id/btnEditAvatar"`
3. Nếu không có → Thêm ID vào CardView của nút camera

### Lỗi 3: Không xin quyền
**Triệu chứng:** Không có popup xin quyền

**Nguyên nhân:** 
- Đã cấp quyền trước đó
- Hoặc quyền không cần thiết (Android 13+)

**Cách khắc phục:**
- Không cần khắc phục, tiếp tục test

### Lỗi 4: "Permission denied"
**Triệu chứng:** Logcat có "Permission denied"

**Cách khắc phục:**
1. Vào Settings → Apps → [Tên app]
2. Vào Permissions
3. Bật quyền "Photos and videos" hoặc "Storage"

### Lỗi 5: "Lỗi mở thư viện"
**Triệu chứng:** Toast hiện "Lỗi mở thư viện"

**Cách khắc phục:**
1. Kiểm tra thiết bị có app Gallery/Photos không
2. Thử chạy trên thiết bị thật thay vì emulator

### Lỗi 6: "Upload failed" hoặc "Permission denied" từ Firebase
**Triệu chứng:** Logcat có "Lỗi upload ảnh: Permission denied"

**Nguyên nhân:** Firebase Storage Rules chưa đúng

**Cách khắc phục:**
1. Vào Firebase Console → Storage → Rules
2. Thay rules bằng:
```
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /{allPaths=**} {
      allow read, write: if true;  // Cho phép tất cả (chỉ dùng khi test)
    }
  }
}
```
3. Click Publish

### Lỗi 7: "Không tìm thấy thông tin người dùng"
**Triệu chứng:** Toast hiện "Không tìm thấy thông tin người dùng"

**Nguyên nhân:** Không tìm thấy document trong Firestore

**Cách khắc phục:**
1. Vào Firebase Console → Firestore
2. Kiểm tra collection `BenhNhan` có document với field `maTaiKhoan` khớp với user đang đăng nhập không
3. Nếu không có → Tạo document mới hoặc đăng ký lại user

## Test nhanh với Firebase Storage Rules mở hết

Để test nhanh, tạm thời mở hết quyền Firebase Storage:

```
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /{allPaths=**} {
      allow read, write: if true;
    }
  }
}
```

**Lưu ý:** Chỉ dùng khi test, sau khi test xong phải đổi lại rules an toàn hơn!

## Nếu vẫn không được

Hãy copy toàn bộ log từ Logcat (filter: ProfileActivity) và gửi cho tôi để debug chi tiết hơn.

### Cách copy log:
1. Mở Logcat
2. Filter: `ProfileActivity`
3. Click vào nút camera và thực hiện các bước
4. Click chuột phải vào Logcat → Copy
5. Paste vào file text và gửi
