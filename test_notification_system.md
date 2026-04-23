# Hướng dẫn kiểm tra hệ thống thông báo

## Các vấn đề đã sửa:

### 1. **Nút thông báo không hoạt động ở MainBacSiActivity**
- ✅ Đã thêm xử lý click cho `btnNotification`
- ✅ Đã thêm method `handleXemThongBao()` cho bác sĩ

### 2. **FCM Token không được lưu**
- ✅ Đã cập nhật `MyFirebaseMessagingService.sendRegistrationToServer()`
- ✅ Đã cập nhật `SessionManager` để lưu thông tin user cho FCM
- ✅ FCM token sẽ được lưu vào Firestore khi đăng nhập

### 3. **NotificationHelper chưa hoàn thiện**
- ✅ Đã cập nhật logic gửi push notification
- ✅ Đã thêm method tạo local notification để test

## Cách test hệ thống thông báo:

### Test 1: Kiểm tra nút thông báo
1. Mở app với tài khoản bác sĩ
2. Click vào icon thông báo ở góc phải toolbar
3. Kiểm tra xem có mở ThongBaoActivity không

### Test 2: Kiểm tra gửi thông báo (Bác sĩ)
1. Đăng nhập với tài khoản bác sĩ
2. Vào menu → Gửi thông báo (hoặc từ navigation)
3. Chọn bệnh nhân, nhập tiêu đề và nội dung
4. Gửi thông báo

### Test 3: Kiểm tra nhận thông báo (Bệnh nhân)
1. Đăng nhập với tài khoản bệnh nhân
2. Click vào icon thông báo
3. Kiểm tra xem có hiển thị thông báo từ bác sĩ không

### Test 4: Kiểm tra FCM Token
1. Kiểm tra Firestore collection `BenhNhan` và `BacSi`
2. Xem có field `fcmToken` được cập nhật không
3. Kiểm tra log để xem token có được lưu không

## Tạo dữ liệu test thông báo:

### Script Firebase Console để tạo thông báo mẫu:

```javascript
// Chạy trong Firebase Console → Firestore → Console
const testNotifications = [
  {
    maThongBao: "TB001",
    maBenhNhan: "BN001", // Thay bằng mã bệnh nhân thực tế
    maBacSi: "BS001",    // Thay bằng mã bác sĩ thực tế
    tieuDe: "Nhắc nhở uống thuốc",
    noiDung: "Đã đến giờ uống thuốc buổi sáng. Vui lòng uống theo đơn.",
    loaiThongBao: "NHAC_THUOC",
    thoiGianGui: firebase.firestore.Timestamp.now(),
    daDoc: false
  },
  {
    maThongBao: "TB002", 
    maBenhNhan: "BN001",
    maBacSi: "BS001",
    tieuDe: "Lịch hẹn khám",
    noiDung: "Bạn có lịch hẹn khám vào ngày mai lúc 9:00 AM.",
    loaiThongBao: "LICH_HEN",
    thoiGianGui: firebase.firestore.Timestamp.now(),
    daDoc: false
  }
];

// Thêm từng thông báo
testNotifications.forEach(async (tb) => {
  await firebase.firestore().collection('ThongBao').doc(tb.maThongBao).set(tb);
  console.log('Đã thêm thông báo:', tb.maThongBao);
});
```

## Kiểm tra log:

### Trong Android Studio Logcat, tìm các tag:
- `FCMService`: Kiểm tra nhận và xử lý FCM messages
- `NotificationHelper`: Kiểm tra logic gửi thông báo
- `SessionManager`: Kiểm tra lưu FCM token
- `ThongBaoActivity`: Kiểm tra load thông báo

## Các file đã được cập nhật:

1. **MainBacSiActivity.java** - Thêm xử lý click nút thông báo
2. **MyFirebaseMessagingService.java** - Cập nhật lưu FCM token
3. **SessionManager.java** - Thêm logic cập nhật FCM token
4. **NotificationHelper.java** - Hoàn thiện logic gửi thông báo

## Lưu ý:
- Hệ thống thông báo push thực tế cần Firebase Functions hoặc server backend
- Hiện tại đã có đầy đủ UI và logic cơ bản
- FCM token sẽ được lưu tự động khi đăng nhập
- Thông báo sẽ hiển thị trong app ngay lập tức