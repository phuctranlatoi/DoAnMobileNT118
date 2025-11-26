# Test Firebase Storage - Checklist

## ✅ Checklist trước khi test

### 1. Firebase Console Setup
- [ ] Đã vào Firebase Console: https://console.firebase.google.com/
- [ ] Đã chọn project: **qlykhambenh**
- [ ] Đã kích hoạt Storage (menu Storage ở sidebar)
- [ ] Đã thấy bucket: `qlykhambenh.firebasestorage.app`

### 2. Storage Rules
- [ ] Đã vào tab Rules trong Storage
- [ ] Đã copy và paste rules từ file `FIX_LOI_FIREBASE_STORAGE.md`
- [ ] Đã click Publish

### 3. App Setup
- [ ] Đã rebuild project (Build → Rebuild Project)
- [ ] Đã sync gradle
- [ ] File `google-services.json` có trong folder `app/`

### 4. Device/Emulator
- [ ] Có kết nối internet
- [ ] Đã cấp quyền Camera và Storage cho app

## 🧪 Test Cases

### Test 1: Upload ảnh từ thư viện

**Bước thực hiện:**
1. Mở app và đăng nhập
2. Vào Profile (click icon profile ở bottom navigation)
3. Click vào icon camera trên avatar
4. Chọn "Chọn từ thư viện"
5. Chọn một ảnh bất kỳ

**Kết quả mong đợi:**
- ✅ Toast hiện "Đang tải ảnh lên..."
- ✅ Toast hiện "Cập nhật ảnh đại diện thành công!"
- ✅ Avatar thay đổi ngay lập tức
- ✅ Logcat hiện log upload thành công

**Nếu lỗi:**
- ❌ Xem Logcat để biết lỗi cụ thể
- ❌ Kiểm tra lại Firebase Console → Storage
- ❌ Kiểm tra lại Storage Rules

### Test 2: Upload ảnh từ camera

**Bước thực hiện:**
1. Mở app và đăng nhập
2. Vào Profile
3. Click vào icon camera trên avatar
4. Chọn "Chụp ảnh mới"
5. Chụp ảnh

**Kết quả mong đợi:**
- ✅ Camera mở ra
- ✅ Sau khi chụp, ảnh được upload
- ✅ Avatar thay đổi

### Test 3: Kiểm tra ảnh trong Firebase Console

**Bước thực hiện:**
1. Sau khi upload thành công
2. Vào Firebase Console → Storage
3. Vào folder `avatars/`

**Kết quả mong đợi:**
- ✅ Thấy file ảnh với tên dạng: `TK001_1234567890.jpg`
- ✅ Click vào file, thấy preview ảnh
- ✅ Copy URL và paste vào browser, ảnh hiển thị

### Test 4: Kiểm tra Firestore

**Bước thực hiện:**
1. Vào Firebase Console → Firestore
2. Vào collection `BenhNhan` hoặc `BacSi`
3. Tìm document của user vừa upload

**Kết quả mong đợi:**
- ✅ Field `avatarUrl` có giá trị
- ✅ URL dạng: `https://firebasestorage.googleapis.com/v0/b/qlykhambenh.firebasestorage.app/o/avatars%2F...`

### Test 5: Reload app

**Bước thực hiện:**
1. Đóng app hoàn toàn
2. Mở lại app
3. Đăng nhập
4. Vào Profile

**Kết quả mong đợi:**
- ✅ Avatar vẫn hiển thị ảnh đã upload
- ✅ Không bị mất ảnh

## 📊 Logcat Monitoring

### Mở Logcat
1. Trong Android Studio, click tab **Logcat** ở dưới
2. Trong ô filter, gõ: `ProfileActivity`
3. Chọn device/emulator đang chạy

### Log thành công
```
D/ProfileActivity: Edit avatar button clicked
D/ProfileActivity: checkPermissionAndPickImage called
D/ProfileActivity: Permission granted (Android 13+)
D/ProfileActivity: showImagePickerDialog called
D/ProfileActivity: Option selected: 0
D/ProfileActivity: Opening gallery
D/ProfileActivity: Ảnh được chọn: content://media/external/images/media/123
D/ProfileActivity: Bắt đầu upload ảnh: content://media/external/images/media/123
D/ProfileActivity: Storage bucket: qlykhambenh.firebasestorage.app
D/ProfileActivity: Upload path: avatars/TK001_1234567890.jpg
D/ProfileActivity: Upload progress: 25.5%
D/ProfileActivity: Upload progress: 51.2%
D/ProfileActivity: Upload progress: 76.8%
D/ProfileActivity: Upload progress: 100.0%
D/ProfileActivity: Upload thành công!
D/ProfileActivity: Download URL: https://firebasestorage.googleapis.com/v0/b/qlykhambenh.firebasestorage.app/o/avatars%2FTK001_1234567890.jpg?alt=media&token=...
D/ProfileActivity: Cập nhật Firestore - Collection: BenhNhan, maTaiKhoan: TK001
D/ProfileActivity: Tìm thấy document ID: abc123
D/ProfileActivity: Cập nhật Firestore thành công!
```

### Log lỗi - Storage chưa kích hoạt
```
E/ProfileActivity: Lỗi upload ảnh: com.google.firebase.storage.StorageException: Object does not exist at location.
```
**Fix:** Kích hoạt Storage trong Firebase Console

### Log lỗi - Permission denied
```
E/ProfileActivity: Lỗi upload ảnh: com.google.firebase.storage.StorageException: User does not have permission to access this object.
```
**Fix:** Cập nhật Storage Rules

### Log lỗi - Network error
```
E/ProfileActivity: Lỗi upload ảnh: java.io.IOException: Unable to resolve host
```
**Fix:** Kiểm tra kết nối internet

## 🔧 Debug Tips

### Tip 1: Clear app data
```bash
adb shell pm clear com.example.doannt118
```

### Tip 2: Check Firebase Auth
Thêm log trong `onCreate()`:
```java
FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
Log.d("ProfileActivity", "Current user: " + (user != null ? user.getUid() : "null"));
```

### Tip 3: Test với ảnh nhỏ
- Chọn ảnh có kích thước < 1MB để test nhanh
- Nếu thành công, thử với ảnh lớn hơn

### Tip 4: Check Storage quota
- Vào Firebase Console → Storage
- Kiểm tra usage (free plan: 5GB storage, 1GB/day download)

## 📝 Report Bug Template

Nếu vẫn gặp lỗi, cung cấp thông tin sau:

```
**Device/Emulator:**
- Model: [e.g., Pixel 5]
- Android version: [e.g., Android 13]

**App info:**
- Package: com.example.doannt118
- User type: [BenhNhan/BacSi]
- maTaiKhoan: [e.g., TK001]

**Steps to reproduce:**
1. [Bước 1]
2. [Bước 2]
3. [Lỗi xảy ra]

**Logcat:**
[Paste log từ Logcat]

**Screenshots:**
[Attach screenshots nếu có]

**Firebase Console:**
- Storage activated: [Yes/No]
- Storage Rules: [Paste rules]
- Bucket name: [e.g., qlykhambenh.firebasestorage.app]
```

## ✨ Success Criteria

Test được coi là thành công khi:
- ✅ Upload ảnh từ thư viện thành công
- ✅ Upload ảnh từ camera thành công
- ✅ Avatar hiển thị ngay sau khi upload
- ✅ Avatar vẫn hiển thị sau khi reload app
- ✅ Ảnh xuất hiện trong Firebase Console → Storage
- ✅ Field `avatarUrl` được cập nhật trong Firestore
- ✅ Không có lỗi trong Logcat

## 🎯 Next Steps

Sau khi test thành công:
1. Test với nhiều user khác nhau
2. Test với ảnh có kích thước khác nhau
3. Test trên nhiều thiết bị khác nhau
4. Cập nhật Storage Rules cho production (xem file `FIX_LOI_FIREBASE_STORAGE.md`)
