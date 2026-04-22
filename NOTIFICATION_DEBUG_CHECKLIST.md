# ✅ Checklist Debug Hệ thống Thông báo

## Bước 1: Tạo dữ liệu test
- [ ] Mở Firebase Console → Firestore Database → Console tab
- [ ] Chạy script `auto_create_notifications.js`
- [ ] Kiểm tra xem có tạo được 5 thông báo test không

## Bước 2: Kiểm tra trong app
- [ ] Mở app và đăng nhập với tài khoản bệnh nhân
- [ ] Click vào icon thông báo (🔔) ở toolbar
- [ ] Kiểm tra xem ThongBaoActivity có mở không
- [ ] Kiểm tra xem có hiển thị danh sách thông báo không

## Bước 3: Kiểm tra log (nếu không hiển thị)
- [ ] Mở Android Studio → Logcat
- [ ] Filter theo tag: `ThongBaoActivity`
- [ ] Click nút thông báo trong app
- [ ] Kiểm tra các log sau:

### Log cần kiểm tra:
```
ThongBaoActivity: maBenhNhan: [MÃ_BỆNH_NHÂN]
ThongBaoActivity: maBacSi: [MÃ_BÁC_SĨ]
ThongBaoActivity: userType: [benhnhan/bacsi]
ThongBaoActivity: Final userType: [benhnhan/bacsi]
ThongBaoActivity: Query for benhnhan with maBenhNhan: [MÃ]
ThongBaoActivity: Received X notifications
ThongBaoActivity: Updated adapter with X notifications
```

## Bước 4: Xử lý các lỗi thường gặp

### ❌ Lỗi: maBenhNhan = null
**Nguyên nhân**: Chưa load được thông tin user
**Giải pháp**: 
- [ ] Kiểm tra method `loadUserInfo()` trong MainBenhNhanActivity
- [ ] Đảm bảo đã đăng nhập thành công
- [ ] Kiểm tra SessionManager có lưu đúng mã không

### ❌ Lỗi: Received 0 notifications
**Nguyên nhân**: Không có dữ liệu hoặc mã không khớp
**Giải pháp**:
- [ ] Chạy lại script tạo dữ liệu test
- [ ] Kiểm tra mã trong log có khớp với mã trong Firestore không
- [ ] Kiểm tra collection ThongBao có tồn tại không

### ❌ Lỗi: Error loading notifications
**Nguyên nhân**: Lỗi Firestore hoặc quyền truy cập
**Giải pháp**:
- [ ] Kiểm tra Firestore Rules
- [ ] Kiểm tra kết nối internet
- [ ] Kiểm tra Firebase configuration

## Bước 5: Test với bác sĩ
- [ ] Đăng nhập với tài khoản bác sĩ
- [ ] Click nút thông báo
- [ ] Kiểm tra log: `Query for bacsi with maBacSi: [MÃ]`
- [ ] Kiểm tra có hiển thị thông báo không

## Bước 6: Test chi tiết thông báo
- [ ] Click vào một thông báo trong danh sách
- [ ] Kiểm tra ChiTietThongBaoActivity có mở không
- [ ] Kiểm tra hiển thị đầy đủ thông tin không

## Kết quả mong đợi ✅
- [ ] Nút thông báo hoạt động
- [ ] ThongBaoActivity mở thành công
- [ ] Hiển thị danh sách thông báo
- [ ] Click thông báo → mở chi tiết
- [ ] Log không có lỗi

## Nếu vẫn không hoạt động
1. **Gửi log đầy đủ** từ Logcat khi click nút thông báo
2. **Chụp màn hình** Firestore collection ThongBao
3. **Kiểm tra** mã bệnh nhân/bác sĩ trong app có khớp với Firestore không

## Script hữu ích

### Kiểm tra dữ liệu nhanh:
```javascript
// Chạy trong Firebase Console
firebase.firestore().collection('ThongBao').get().then(snapshot => {
  console.log('Số thông báo:', snapshot.size);
  snapshot.forEach(doc => {
    const data = doc.data();
    console.log(`${data.maThongBao}: ${data.maBenhNhan} - ${data.tieuDe}`);
  });
});
```

### Xóa dữ liệu test:
```javascript
firebase.firestore().collection('ThongBao')
  .where('maThongBao', '>=', 'TB_AUTO_')
  .get().then(snapshot => {
    snapshot.forEach(doc => doc.ref.delete());
    console.log('Đã xóa dữ liệu test');
  });
```