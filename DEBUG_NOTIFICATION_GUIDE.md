# Hướng dẫn Debug Hệ thống Thông báo

## Vấn đề hiện tại
- Nút thông báo có sẵn ở cả bệnh nhân và bác sĩ
- Click vào nút → mở ThongBaoActivity
- Nhưng không hiển thị dữ liệu thông báo

## Các bước debug

### Bước 1: Kiểm tra dữ liệu trong Firestore
1. Mở Firebase Console → Firestore Database
2. Chạy script `debug_notification_data.js` để kiểm tra:
   - Collection `BenhNhan` có dữ liệu không?
   - Collection `BacSi` có dữ liệu không?
   - Collection `ThongBao` có dữ liệu không?

### Bước 2: Tạo dữ liệu test
1. Nếu không có thông báo, chạy script `create_simple_test_notifications.js`
2. **QUAN TRỌNG**: Sửa mã bệnh nhân và bác sĩ trong script cho đúng với dữ liệu thực tế

### Bước 3: Kiểm tra log trong app
1. Mở Android Studio → Logcat
2. Filter theo tag: `ThongBaoActivity`
3. Mở app → click nút thông báo
4. Xem log để kiểm tra:
   - `maBenhNhan` và `maBacSi` có được truyền đúng không?
   - `userType` có đúng không?
   - Query có được tạo đúng không?
   - Có nhận được dữ liệu từ Firestore không?

### Bước 4: Kiểm tra cách truyền tham số

#### Từ MainBenhNhanActivity:
```java
Intent intent = new Intent(this, ThongBaoActivity.class);
intent.putExtra("MA_BENH_NHAN", maBenhNhan);
startActivity(intent);
```

#### Từ MainBacSiActivity:
```java
Intent intent = new Intent(this, ThongBaoActivity.class);
intent.putExtra("MA_BAC_SI", maBacSi);
intent.putExtra("USER_TYPE", "bacsi");
startActivity(intent);
```

### Bước 5: Kiểm tra query Firestore

#### Query cho bệnh nhân:
```java
query = repo.getCollection("ThongBao")
        .whereEqualTo("maBenhNhan", maBenhNhan);
```

#### Query cho bác sĩ:
```java
query = repo.getCollection("ThongBao")
        .whereEqualTo("maBacSi", maBacSi);
```

## Các lỗi thường gặp

### 1. Mã bệnh nhân/bác sĩ null
**Triệu chứng**: Log hiển thị `maBenhNhan: null` hoặc `maBacSi: null`
**Nguyên nhân**: Chưa load được thông tin user trong MainActivity
**Giải pháp**: Kiểm tra method `loadUserInfo()` trong MainActivity

### 2. Không có dữ liệu thông báo
**Triệu chứng**: Log hiển thị `Received 0 notifications`
**Nguyên nhân**: Collection ThongBao trống hoặc mã không khớp
**Giải pháp**: Tạo dữ liệu test với mã đúng

### 3. Query không khớp
**Triệu chứng**: Có dữ liệu trong Firestore nhưng query không trả về
**Nguyên nhân**: Mã trong app khác với mã trong Firestore
**Giải pháp**: So sánh mã trong log với mã trong Firestore

## Script test nhanh

### Kiểm tra mã user hiện tại:
```javascript
// Chạy trong Firebase Console
firebase.firestore().collection('BenhNhan').get().then(snapshot => {
  snapshot.forEach(doc => {
    console.log('Bệnh nhân:', doc.data().maBenhNhan, '-', doc.data().hoTen);
  });
});

firebase.firestore().collection('BacSi').get().then(snapshot => {
  snapshot.forEach(doc => {
    console.log('Bác sĩ:', doc.data().maBacSi, '-', doc.data().hoTen);
  });
});
```

### Tạo thông báo test với mã cụ thể:
```javascript
// Thay YOUR_MA_BENH_NHAN và YOUR_MA_BAC_SI bằng mã thực tế
firebase.firestore().collection('ThongBao').doc('TB_TEST_DEBUG').set({
  maThongBao: "TB_TEST_DEBUG",
  maBenhNhan: "YOUR_MA_BENH_NHAN", // Thay đổi
  maBacSi: "YOUR_MA_BAC_SI",       // Thay đổi
  tieuDe: "Test Debug",
  noiDung: "Thông báo test để debug",
  loaiThongBao: "THONG_BAO_CHUNG",
  thoiGianGui: firebase.firestore.Timestamp.now(),
  daDoc: false
});
```

## Kết quả mong đợi
Sau khi debug xong:
1. Log hiển thị mã user đúng
2. Log hiển thị `Received X notifications` với X > 0
3. RecyclerView hiển thị danh sách thông báo
4. Click vào thông báo → mở ChiTietThongBaoActivity