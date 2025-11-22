# Sửa Lỗi Upload Ảnh Đại Diện

## Vấn đề đã phát hiện và sửa

### 1. **Thiếu trường avatarUrl trong FirestoreRepository**
**Vấn đề:** Khi cập nhật thông tin BenhNhan hoặc BacSi, trường `avatarUrl` không được lưu vào Firestore vì phương thức `convertBenhNhanToMap()` và `convertBacSiToMap()` không có trường này.

**Đã sửa:**
- Thêm `map.put("avatarUrl", b.getAvatarUrl());` vào `convertBenhNhanToMap()`
- Thêm `map.put("avatarUrl", b.getAvatarUrl());` vào `convertBacSiToMap()`

### 2. **Thiếu trường avatarUrl trong model BacSi**
**Vấn đề:** Model BacSi chưa có trường `avatarUrl` để lưu URL ảnh đại diện.

**Đã sửa:**
- Thêm field `private String avatarUrl;`
- Thêm getter `getAvatarUrl()`
- Thêm setter `setAvatarUrl(String avatarUrl)`

### 3. **Cải thiện xử lý chụp ảnh từ Camera**
**Vấn đề:** Khi chụp ảnh từ camera, app không xử lý được bitmap trả về.

**Đã sửa:**
- Cập nhật `cameraLauncher` để xử lý bitmap từ camera
- Thêm phương thức `saveBitmapAndUpload()` để lưu bitmap vào file tạm và upload

### 4. **Thêm logging chi tiết**
**Đã thêm:** Log chi tiết ở mọi bước để dễ debug:
- Log khi bắt đầu upload
- Log tiến trình upload
- Log khi upload thành công
- Log khi lấy download URL
- Log khi cập nhật Firestore

## Các file đã được cập nhật

1. ✅ `app/src/main/java/com/example/doannt118/repository/FirestoreRepository.java`
   - Thêm avatarUrl vào convertBenhNhanToMap()
   - Thêm avatarUrl vào convertBacSiToMap()

2. ✅ `app/src/main/java/com/example/doannt118/model/BacSi.java`
   - Thêm field avatarUrl
   - Thêm getter/setter

3. ✅ `app/src/main/java/com/example/doannt118/ui/ProfileActivity.java`
   - Cải thiện xử lý camera
   - Thêm phương thức saveBitmapAndUpload()
   - Thêm logging chi tiết

## Cách test lại

### Bước 1: Rebuild project
```
Build → Rebuild Project
```

### Bước 2: Chạy app
- Chạy trên thiết bị thật (khuyến nghị) hoặc emulator
- Đăng nhập vào app

### Bước 3: Test upload ảnh
1. Vào trang Profile
2. Click vào icon camera trên avatar
3. Chọn "Chọn từ thư viện"
4. Chọn một ảnh
5. Đợi thông báo "Đang tải ảnh lên..."
6. Kiểm tra thông báo "Cập nhật ảnh đại diện thành công!"
7. Ảnh avatar sẽ thay đổi ngay lập tức

### Bước 4: Test chụp ảnh
1. Click vào icon camera trên avatar
2. Chọn "Chụp ảnh mới"
3. Chụp ảnh
4. Đợi upload và kiểm tra kết quả

### Bước 5: Kiểm tra Logcat
Mở Logcat và lọc theo tag `ProfileActivity` để xem log:

```
Ảnh được chọn: content://...
Bắt đầu upload ảnh: content://...
Upload progress: 25%
Upload progress: 50%
Upload progress: 75%
Upload progress: 100%
Upload thành công!
Download URL: https://firebasestorage...
Cập nhật Firestore - Collection: BenhNhan, maTaiKhoan: ...
Tìm thấy document ID: ...
Cập nhật Firestore thành công!
```

### Bước 6: Kiểm tra Firebase Console
1. Vào Firebase Console → Storage
2. Vào thư mục `avatars/`
3. Kiểm tra file ảnh đã được upload
4. Vào Firestore → Collection BenhNhan hoặc BacSi
5. Kiểm tra document của user có field `avatarUrl` với URL đúng

## Nếu vẫn gặp lỗi

### Kiểm tra Firebase Storage Rules
Vào Firebase Console → Storage → Rules và đảm bảo có rule:
```
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /avatars/{allPaths=**} {
      allow read: if true;
      allow write: if request.auth != null;
    }
  }
}
```

### Kiểm tra quyền app
Settings → Apps → [Tên app] → Permissions → Bật:
- Photos and videos (hoặc Storage)
- Camera

### Kiểm tra kết nối internet
Đảm bảo thiết bị có kết nối internet ổn định

### Xem log lỗi chi tiết
Nếu có lỗi, Logcat sẽ hiển thị:
```
Lỗi upload ảnh: [Chi tiết lỗi]
Lỗi lấy download URL: [Chi tiết lỗi]
Lỗi cập nhật Firestore: [Chi tiết lỗi]
```

Copy log lỗi để debug tiếp.
