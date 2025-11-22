# Cấu hình Firebase Storage để Upload Ảnh

## Bước 1: Kiểm tra Firebase Storage đã được kích hoạt chưa

1. Vào [Firebase Console](https://console.firebase.google.com/)
2. Chọn project của bạn
3. Vào menu **Storage** ở sidebar bên trái
4. Nếu chưa kích hoạt, click **Get Started** để kích hoạt Storage

## Bước 2: Cấu hình Storage Rules

1. Trong Firebase Console, vào **Storage** → **Rules**
2. Thay thế rules hiện tại bằng code sau:

```
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    // Cho phép đọc tất cả ảnh avatar
    match /avatars/{allPaths=**} {
      allow read: if true;
      allow write: if request.auth != null;
    }
    
    // Hoặc nếu muốn cho phép tất cả (chỉ dùng khi test)
    // match /{allPaths=**} {
    //   allow read, write: if true;
    // }
  }
}
```

3. Click **Publish** để lưu rules

## Bước 3: Kiểm tra trong Logcat

Khi bạn thử upload ảnh, mở **Logcat** trong Android Studio và lọc theo tag `ProfileActivity` để xem log:

- `Bắt đầu upload ảnh: ...` - Ảnh đã được chọn
- `Upload progress: ...%` - Tiến trình upload
- `Upload thành công!` - Upload thành công
- `Download URL: ...` - Đã lấy được URL
- `Cập nhật Firestore thành công!` - Đã lưu vào database

## Bước 4: Kiểm tra quyền trong AndroidManifest.xml

Đảm bảo file `AndroidManifest.xml` có các quyền sau:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" 
    android:maxSdkVersion="32" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
<uses-permission android:name="android.permission.CAMERA" />
```

## Bước 5: Cấp quyền trên thiết bị

Khi chạy app lần đầu:
1. Click vào nút camera trên avatar
2. App sẽ yêu cầu quyền truy cập ảnh
3. Chọn **Allow** hoặc **Cho phép**

Nếu đã từ chối quyền trước đó:
1. Vào **Settings** → **Apps** → **[Tên app của bạn]**
2. Vào **Permissions**
3. Bật quyền **Photos and videos** (hoặc **Storage**)
4. Bật quyền **Camera** (nếu muốn chụp ảnh)

## Các lỗi thường gặp và cách khắc phục

### Lỗi 1: "Permission denied"
**Nguyên nhân:** Chưa cấp quyền hoặc Firebase Storage Rules chưa đúng
**Giải pháp:** 
- Kiểm tra lại Storage Rules
- Cấp quyền cho app trong Settings

### Lỗi 2: "Upload failed"
**Nguyên nhân:** Không có kết nối internet hoặc Firebase chưa được cấu hình đúng
**Giải pháp:**
- Kiểm tra kết nối internet
- Kiểm tra file `google-services.json` đã được thêm vào project chưa

### Lỗi 3: "Không tìm thấy thông tin người dùng"
**Nguyên nhân:** Dữ liệu trong Firestore chưa có hoặc maTaiKhoan không đúng
**Giải pháp:**
- Kiểm tra Firestore có collection `BenhNhan` hoặc `BacSi` chưa
- Kiểm tra document có field `maTaiKhoan` khớp với user đang đăng nhập

## Test thử

1. Build lại app: **Build** → **Rebuild Project**
2. Chạy app trên thiết bị thật (khuyến nghị) hoặc emulator
3. Đăng nhập vào app
4. Vào trang Profile
5. Click vào icon camera trên avatar
6. Chọn "Chọn từ thư viện" hoặc "Chụp ảnh mới"
7. Chọn ảnh và đợi upload
8. Kiểm tra ảnh đã thay đổi chưa

## Xem ảnh đã upload trong Firebase Console

1. Vào Firebase Console → Storage
2. Vào thư mục `avatars/`
3. Bạn sẽ thấy các file ảnh đã upload với tên dạng: `[maTaiKhoan]_[timestamp].jpg`
